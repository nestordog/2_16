/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.adapter.bb;

import java.util.Date;

import org.apache.log4j.Logger;

import ch.algotrader.esper.EngineLocator;
import ch.algotrader.util.MyLogger;
import ch.algotrader.vo.AskVO;
import ch.algotrader.vo.BidVO;
import ch.algotrader.vo.TradeVO;

import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.EventHandler;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.Name;
import com.bloomberglp.blpapi.Session;

/**
 * Bloomberg MessageHandler.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision: 5941 $ $Date: 2013-05-31 13:23:59 +0200 (Fr, 31 Mai 2013) $
 */
public class BBMessageHandler implements EventHandler {

    private static Logger logger = MyLogger.getLogger(BBMessageHandler.class.getName());

    private static final Name SESSION_CONNECTION_UP = Name.getName("SessionConnectionUp");
    private static final Name SESSION_STARTED = Name.getName("SessionStarted");
    private static final Name SESSION_TERMINATED = Name.getName("SessionTerminated");
    private static final Name SESSION_STARTUP_FAILURE = Name.getName("SessionStartupFailure");

    private static final Name SERVICE_OPENED = Name.getName("ServiceOpened");

    private static final Name SUBSCRIPTION_STARTED = Name.getName("SubscriptionStarted");
    private static final Name SUBSCRIPTION_FAILURE = Name.getName("SubscriptionFailure");
    private static final Name SUBSCRIPTION_TERMINATED = Name.getName("SubscriptionTerminated");

    private final Object lock = new Object();
    private boolean running;

    @Override
    public void processEvent(Event event, Session session) {

        try {
            switch (event.eventType().intValue()) {
                case Event.EventType.Constants.SESSION_STATUS:
                    synchronized (this.lock) {
                        processSessionStatus(event, session);
                    }
                    break;
                case Event.EventType.Constants.SUBSCRIPTION_STATUS:
                    synchronized (this.lock) {
                        processSubscriptionStatus(event, session);
                    }
                    break;
                case Event.EventType.Constants.SUBSCRIPTION_DATA:
                    processSubscriptionDataEvent(event, session);
                    break;
                case Event.EventType.Constants.ADMIN:
                    synchronized (this.lock) {
                        processAdminEvent(event, session);
                    }
                    break;
                default:
                    processMiscEvents(event, session);
                    break;
            }
        } catch (Exception e) {
            logger.error("problem processing event", e);
            for (Message msg : event) {
                logger.error("correlationID: " + msg.correlationID().value() + ", " + msg);
            }
        }
    }

    private void processSessionStatus(Event event, Session session) {

        for (Message msg : event) {

            if (msg.messageType() == SESSION_CONNECTION_UP) {
                logger.info("session connection up");
            } else if (msg.messageType() == SESSION_STARTED) {
                this.running = true;
                logger.info("session started");
            } else if (msg.messageType() == SESSION_TERMINATED) {
                this.running = false;
                logger.info("session terminated");
            } else if (msg.messageType() == SESSION_STARTUP_FAILURE) {
                logger.error(msg);
            } else {
                throw new IllegalStateException("unknown messageType " + msg.messageType());
            }
        }
    }

    private void processSubscriptionStatus(Event event, Session session) throws Exception {

        for (Message msg : event) {

            int cid = (int) msg.correlationID().value();

            if (msg.messageType() == SUBSCRIPTION_STARTED) {
                logger.info("subscription for tickerId " + cid + " has started");
            } else if (msg.messageType() == SUBSCRIPTION_TERMINATED) {
                logger.info("subscription for tickerId " + cid + " has terminated");
            } else if (msg.messageType() == SUBSCRIPTION_FAILURE) {
                logger.warn(msg);
            } else {
                throw new IllegalStateException("unknown messageType " + msg.messageType());
            }
        }
    }

    private void processSubscriptionDataEvent(Event event, Session session) throws Exception {

        for (Message msg : event) {

            int cid = (int) msg.correlationID().value();

            Element fields = msg.asElement();

            String marketDataEventType = fields.getElementAsString("MKTDATA_EVENT_TYPE");
            String marketDataEventSubType = fields.getElementAsString("MKTDATA_EVENT_SUBTYPE");

            if ("SUMMARY".equals(marketDataEventType)) {

                if (!"INTRADAY".equals(marketDataEventSubType)) {

                    // there might not have been a last trade
                    Date lastDateTime = null;
                    double last = 0;
                    if (fields.hasElement("LAST_PRICE") && fields.getElement("LAST_PRICE").numValues() == 1) {
                        lastDateTime = fields.getElementAsDate("TRADE_UPDATE_STAMP_RT").calendar().getTime();
                        last = fields.getElementAsFloat64("LAST_PRICE");
                    }

                    // VOLUME is null for FX and indices
                    int vol = 0;
                    if (fields.hasElement("VOLUME") && fields.getElement("VOLUME").numValues() == 1) {
                        vol = (int) fields.getElementAsInt64("VOLUME");
                    }

                    TradeVO tradeVO = new TradeVO(cid, lastDateTime, last, vol);
                    EngineLocator.instance().getBaseEngine().sendEvent(tradeVO);

                    // there are no BIDs for indices
                    if (fields.hasElement("BID") && fields.getElement("BID").numValues() == 1) {

                        double bid = fields.getElementAsFloat64("BID");

                        // BID_SIZE is null for FX
                        int volBid = 0;
                        if (fields.hasElement("BID_SIZE") && fields.getElement("BID_SIZE").numValues() == 1) {
                            volBid = fields.getElementAsInt32("BID_SIZE");
                        }

                        BidVO bidVO = new BidVO(cid, lastDateTime, bid, volBid);
                        EngineLocator.instance().getBaseEngine().sendEvent(bidVO);
                    }

                    // there are no ASKs for indices
                    if (fields.hasElement("ASK") && fields.getElement("ASK").numValues() == 1) {

                        double ask = fields.getElementAsFloat64("ASK");

                        // ASK_SIZE is null for FX
                        int volAsk = 0;
                        if (fields.hasElement("ASK_SIZE") && fields.getElement("ASK_SIZE").numValues() == 1) {
                            volAsk = fields.getElementAsInt32("ASK_SIZE");
                        }

                        AskVO askVO = new AskVO(cid, lastDateTime, ask, volAsk);
                        EngineLocator.instance().getBaseEngine().sendEvent(askVO);
                    }
                }

            } else if ("TRADE".equals(marketDataEventType)) {

                // ignore TRADES without a LAST_PRICE
                if (fields.hasElement("LAST_PRICE")) {

                    Date lastDateTime = fields.getElementAsDate("TRADE_UPDATE_STAMP_RT").calendar().getTime();
                    double last = fields.getElementAsFloat64("LAST_PRICE");

                    // ASK_SIZE is null for FX and indices
                    int vol = 0;
                    if (fields.hasElement("VOLUME") && fields.getElement("VOLUME").numValues() == 1) {
                        vol = (int) fields.getElementAsInt64("VOLUME");
                    }

                    TradeVO tradeVO = new TradeVO(cid, lastDateTime, last, vol);
                    EngineLocator.instance().getBaseEngine().sendEvent(tradeVO);
                }

            } else if ("QUOTE".equals(marketDataEventType)) {


                if ("BID".equals(marketDataEventSubType)) {

                    Date dateTime = fields.getElementAsDate("BID_UPDATE_STAMP_RT").calendar().getTime();

                    // remove existing BID if there is no value
                    double bid = 0;
                    if (fields.getElement("BID").numValues() == 1) {
                        bid = fields.getElementAsFloat64("BID");
                    }

                    // BID_SIZE is null for FX
                    int volBid = 0;
                    if (fields.hasElement("BID_SIZE") && fields.getElement("BID_SIZE").numValues() == 1) {
                        volBid = fields.getElementAsInt32("BID_SIZE");
                    }

                    BidVO bidVO = new BidVO(cid, dateTime, bid, volBid);
                    EngineLocator.instance().getBaseEngine().sendEvent(bidVO);

                } else if ("ASK".equals(marketDataEventSubType)) {

                    Date dateTime = fields.getElementAsDate("ASK_UPDATE_STAMP_RT").calendar().getTime();

                    // remove existing ASK if there is no value
                    double ask = 0;
                    if (fields.getElement("ASK").numValues() == 1) {
                        ask = fields.getElementAsFloat64("ASK");
                    }

                    // ASK_SIZE is null for FX
                    int volAsk = 0;
                    if (fields.hasElement("ASK_SIZE") && fields.getElement("ASK_SIZE").numValues() == 1) {
                        volAsk = fields.getElementAsInt32("ASK_SIZE");
                    }

                    AskVO askVO = new AskVO(cid, dateTime, ask, volAsk);
                    EngineLocator.instance().getBaseEngine().sendEvent(askVO);

                } else {
                    throw new IllegalArgumentException("unkown marketDataEventSubType " + marketDataEventSubType);
                }
            } else {
                throw new IllegalArgumentException("unkown marketDataEventType " + marketDataEventType);
            }
        }
    }

    private void processAdminEvent(Event event, Session session) {

        for (Message msg : event) {
            logger.info(msg);
        }
    }

    private void processMiscEvents(Event event, Session session) throws Exception {

        for (Message msg : event) {
            if (msg.messageType() == SERVICE_OPENED) {
                String serviceName = msg.getElementAsString("serviceName");
                logger.info("service has been opened " + serviceName);
            } else {
                throw new IllegalArgumentException("unknown msgType " + msg.messageType());
            }
        }
    }

    public boolean isRunning() {
        return this.running;
    }
}
