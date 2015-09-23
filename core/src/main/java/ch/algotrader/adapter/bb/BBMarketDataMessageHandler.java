/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.adapter.bb;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.Session;

import ch.algotrader.adapter.ExternalSessionStateHolder;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.esper.Engine;
import ch.algotrader.vo.marketData.AskVO;
import ch.algotrader.vo.marketData.BidVO;
import ch.algotrader.vo.marketData.TradeVO;

/**
 * Bloomberg MessageHandler for MarketData events.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class BBMarketDataMessageHandler extends BBMessageHandler {

    private static final Logger LOGGER = LogManager.getLogger(BBMarketDataMessageHandler.class);

    private final Engine serverEngine;

    public BBMarketDataMessageHandler(final Engine serverEngine, final ExternalSessionStateHolder sessionStateHolder) {
        super(sessionStateHolder);
        this.serverEngine = serverEngine;
    }

    @Override
    protected void processSubscriptionStatus(Event event, Session session) {

        for (Message msg : event) {

            String cid = (String) msg.correlationID().object();

            if (msg.messageType() == BBConstants.SUBSCRIPTION_STARTED) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("subscription for tickerId {} has started", cid);
                }
            } else if (msg.messageType() == BBConstants.SUBSCRIPTION_TERMINATED) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("subscription for tickerId {} has terminated", cid);
                }
            } else if (msg.messageType() == BBConstants.SUBSCRIPTION_FAILURE) {
                LOGGER.warn(msg);
            } else {
                throw new IllegalStateException("unknown messageType " + msg.messageType());
            }
        }
    }

    @Override
    protected void processSubscriptionDataEvent(Event event, Session session) {

        for (Message msg : event) {

            String cid = (String) msg.correlationID().object();

            Element fields = msg.asElement();

            String marketDataEventType = fields.getElementAsString("MKTDATA_EVENT_TYPE");
            String marketDataEventSubType = fields.getElementAsString("MKTDATA_EVENT_SUBTYPE");

            Calendar calendar = fields.getElementAsDate("TRADE_UPDATE_STAMP_RT").calendar();
			calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
			if ("SUMMARY".equals(marketDataEventType)) {

                if (!"INTRADAY".equals(marketDataEventSubType)) {

                    // there might not have been a last trade
                    Date lastDateTime = null;
                    double last = 0;
                    if (fields.hasElement("LAST_PRICE") && fields.getElement("LAST_PRICE").numValues() == 1) {
                        lastDateTime = calendar.getTime();
                        last = fields.getElementAsFloat64("LAST_PRICE");
                    }

                    // VOLUME is null for FX and indices
                    int vol = 0;
                    if (fields.hasElement("VOLUME") && fields.getElement("VOLUME").numValues() == 1) {
                        vol = (int) fields.getElementAsInt64("VOLUME");
                    }

                    TradeVO tradeVO = new TradeVO(cid, FeedType.BB.name(), lastDateTime, last, vol);
                    this.serverEngine.sendEvent(tradeVO);

                    // there are no BIDs for indices
                    if (fields.hasElement("BID") && fields.getElement("BID").numValues() == 1) {

                        double bid = fields.getElementAsFloat64("BID");

                        // BID_SIZE is null for FX
                        int volBid = 0;
                        if (fields.hasElement("BID_SIZE") && fields.getElement("BID_SIZE").numValues() == 1) {
                            volBid = fields.getElementAsInt32("BID_SIZE");
                        }

                        BidVO bidVO = new BidVO(cid, FeedType.BB.name(), lastDateTime, bid, volBid);
                        this.serverEngine.sendEvent(bidVO);
                    }

                    // there are no ASKs for indices
                    if (fields.hasElement("ASK") && fields.getElement("ASK").numValues() == 1) {

                        double ask = fields.getElementAsFloat64("ASK");

                        // ASK_SIZE is null for FX
                        int volAsk = 0;
                        if (fields.hasElement("ASK_SIZE") && fields.getElement("ASK_SIZE").numValues() == 1) {
                            volAsk = fields.getElementAsInt32("ASK_SIZE");
                        }

                        AskVO askVO = new AskVO(cid, FeedType.BB.name(), lastDateTime, ask, volAsk);
                        this.serverEngine.sendEvent(askVO);
                    }
                }

            } else if ("TRADE".equals(marketDataEventType)) {

                // ignore TRADES without a LAST_PRICE
                if (fields.hasElement("LAST_PRICE")) {

                    Date lastDateTime = calendar.getTime();
                    double last = fields.getElementAsFloat64("LAST_PRICE");

                    // ASK_SIZE is null for FX and indices
                    int vol = 0;
                    if (fields.hasElement("VOLUME") && fields.getElement("VOLUME").numValues() == 1) {
                        vol = (int) fields.getElementAsInt64("VOLUME");
                    }

                    TradeVO tradeVO = new TradeVO(cid, "BB", lastDateTime, last, vol);
                    this.serverEngine.sendEvent(tradeVO);
                }

            } else if ("QUOTE".equals(marketDataEventType)) {


                if ("BID".equals(marketDataEventSubType)) {

                    Calendar bidCalendar = fields.getElementAsDate("BID_UPDATE_STAMP_RT").calendar();
    				bidCalendar.setTimeZone(TimeZone.getTimeZone("UTC"));                    
					Date dateTime = bidCalendar.getTime();

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

                    BidVO bidVO = new BidVO(cid, "BB", dateTime, bid, volBid);
                    this.serverEngine.sendEvent(bidVO);

                } else if ("ASK".equals(marketDataEventSubType)) {

                    Calendar askCalendar = fields.getElementAsDate("ASK_UPDATE_STAMP_RT").calendar();
    				askCalendar.setTimeZone(TimeZone.getTimeZone("UTC"));                    
					Date dateTime = askCalendar.getTime();

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

                    AskVO askVO = new AskVO(cid, "BB", dateTime, ask, volAsk);
                    this.serverEngine.sendEvent(askVO);

                } else {
                    throw new IllegalArgumentException("unkown marketDataEventSubType " + marketDataEventSubType);
                }
            } else {
                throw new IllegalArgumentException("unkown marketDataEventType " + marketDataEventType);
            }
        }
    }
}
