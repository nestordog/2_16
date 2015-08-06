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
package ch.algotrader.adapter.fxcm;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.adapter.fix.fix44.AbstractFix44MarketDataMessageHandler;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.esper.Engine;
import ch.algotrader.vo.marketData.AskVO;
import ch.algotrader.vo.marketData.BidVO;
import quickfix.FieldNotFound;
import quickfix.Group;
import quickfix.SessionID;
import quickfix.field.ContractMultiplier;
import quickfix.field.MDEntryDate;
import quickfix.field.MDEntryPx;
import quickfix.field.MDEntrySize;
import quickfix.field.MDEntryTime;
import quickfix.field.MDEntryType;
import quickfix.field.NoMDEntries;
import quickfix.field.Symbol;
import quickfix.fix44.MarketDataSnapshotFullRefresh;

/**
 * FXCM specific FIX market data handler.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class FXCMFixMarketDataMessageHandler extends AbstractFix44MarketDataMessageHandler {

    private static final Logger LOGGER = LogManager.getLogger(FXCMFixMarketDataMessageHandler.class);

    private final Engine serverEngine;

    public FXCMFixMarketDataMessageHandler(final Engine serverEngine) {
        this.serverEngine = serverEngine;
    }

    public void onMessage(MarketDataSnapshotFullRefresh marketData, SessionID sessionID) throws FieldNotFound {

        Symbol symbol = marketData.getSymbol();
        double contractMultiplier = 1.0d;
        if (marketData.isSetField(ContractMultiplier.FIELD)) {

            contractMultiplier = marketData.getContractMultiplier().getValue();
        }

        int count = marketData.getGroupCount(NoMDEntries.FIELD);
        for (int i = 1; i <= count; i++) {

            Group group = marketData.getGroup(i, NoMDEntries.FIELD);
            char entryType = group.getChar(MDEntryType.FIELD);

            Date date = null;
            if (group.isSetField(MDEntryDate.FIELD) && group.isSetField(MDEntryTime.FIELD)) {
                Date dateOnly = group.getUtcDateOnly(MDEntryDate.FIELD);
                Date timeTOnly = group.getUtcTimeOnly(MDEntryTime.FIELD);
                date = new Date(dateOnly.getTime() + timeTOnly.getTime());
            }

            if (entryType == MDEntryType.BID || entryType == MDEntryType.OFFER) {

                double price = group.getDouble(MDEntryPx.FIELD);
                double size = group.getDouble(MDEntrySize.FIELD);

                switch (entryType) {
                    case MDEntryType.BID:

                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("{} BID {}@{}", symbol.getValue(), size, price);
                        }

                        BidVO bidVO = new BidVO(symbol.getValue(), FeedType.FXCM, date != null ? date : new Date(), price, (int) (size * contractMultiplier));
                        this.serverEngine.sendEvent(bidVO);
                        break;
                    case MDEntryType.OFFER:

                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("{} ASK {}@{}", symbol.getValue(), size, price);
                        }

                        AskVO askVO = new AskVO(symbol.getValue(), FeedType.FXCM, date != null ? date : new Date(), price, (int) (size * contractMultiplier));

                        this.serverEngine.sendEvent(askVO);
                        break;
                }
            }
        }
    }

}
