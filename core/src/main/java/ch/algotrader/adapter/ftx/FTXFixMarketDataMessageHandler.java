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
package ch.algotrader.adapter.ftx;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.adapter.fix.fix44.AbstractFix44MarketDataMessageHandler;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.esper.Engine;
import ch.algotrader.vo.marketData.AskVO;
import ch.algotrader.vo.marketData.BidVO;
import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.UtcTimeStampField;
import quickfix.field.SendingTime;
import quickfix.fix44.Quote;

/**
 * Fortex specific FIX market data handler.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class FTXFixMarketDataMessageHandler extends AbstractFix44MarketDataMessageHandler {

    private static final Logger LOGGER = LogManager.getLogger(FTXFixMarketDataMessageHandler.class);

    private final Engine serverEngine;

    public FTXFixMarketDataMessageHandler(final Engine serverEngine) {
        this.serverEngine = serverEngine;
    }

    public void onMessage(final Quote quote, final SessionID sessionID) throws FieldNotFound {

        UtcTimeStampField sendingTime = quote.getHeader().getField(new SendingTime());
        Date date = sendingTime.getValue();

        String id = quote.getSymbol().getValue();
        double bidPx = quote.getBidPx().getValue();
        double bidSize = quote.getBidSize().getValue();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(id + " BID " + bidSize + "@" + bidPx);
        }
        BidVO bidVO = new BidVO(id, FeedType.FTX.name(), date, bidPx, (int) bidSize);
        this.serverEngine.sendEvent(bidVO);

        final double offerPx = quote.getOfferPx().getValue();
        final double offerSize = quote.getOfferSize().getValue();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(id + " ASK " + offerSize + "@" + offerPx);
        }
        AskVO askVO = new AskVO(id, FeedType.FTX.name(), date, offerPx, (int) offerSize);
        this.serverEngine.sendEvent(askVO);
    }

}
