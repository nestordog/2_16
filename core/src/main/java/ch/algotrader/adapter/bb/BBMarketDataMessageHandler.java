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
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.Session;

/**
 * Bloomberg MessageHandler for MarketData events.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class BBMarketDataMessageHandler extends BBMessageHandler {

    private static Logger logger = MyLogger.getLogger(BBMarketDataMessageHandler.class.getName());

    @Override
    protected void processSubscriptionStatus(Event event, Session session) {

        for (Message msg : event) {

            int cid = (int) msg.correlationID().value();

            if (msg.messageType() == BBConstants.SUBSCRIPTION_STARTED) {
                logger.info("subscription for tickerId " + cid + " has started");
            } else if (msg.messageType() == BBConstants.SUBSCRIPTION_TERMINATED) {
                logger.info("subscription for tickerId " + cid + " has terminated");
            } else if (msg.messageType() == BBConstants.SUBSCRIPTION_FAILURE) {
                logger.warn(msg);
            } else {
                throw new IllegalStateException("unknown messageType " + msg.messageType());
            }
        }
    }

    @Override
    protected void processSubscriptionDataEvent(Event event, Session session) {

        for (Message msg : event) {

            String cid = Long.toString(msg.correlationID().value());

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
}
