/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.adapter.tt;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.adapter.fix.fix42.AbstractFix42MarketDataMessageHandler;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.esper.Engine;
import ch.algotrader.vo.marketData.AskVO;
import ch.algotrader.vo.marketData.BidVO;
import ch.algotrader.vo.marketData.TradeVO;
import quickfix.FieldNotFound;
import quickfix.Group;
import quickfix.SessionID;
import quickfix.UtcTimeStampField;
import quickfix.field.MDEntryPx;
import quickfix.field.MDEntrySize;
import quickfix.field.MDEntryType;
import quickfix.field.MDReqID;
import quickfix.field.NoMDEntries;
import quickfix.field.SendingTime;
import quickfix.fix42.MarketDataSnapshotFullRefresh;

/**
 * Trading Technologies specific FIX market data handler.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class TTFixMarketDataMessageHandler extends AbstractFix42MarketDataMessageHandler {

    private static final Logger LOGGER = LogManager.getLogger(TTFixMarketDataMessageHandler.class);

    private final Engine serverEngine;

    public TTFixMarketDataMessageHandler(final Engine serverEngine) {
        this.serverEngine = serverEngine;
    }

    public void onMessage(final MarketDataSnapshotFullRefresh marketData, final SessionID sessionID) throws FieldNotFound {

        MDReqID mdReqID = marketData.getMDReqID();
        String id = mdReqID.getValue();

        UtcTimeStampField sendingTime = marketData.getHeader().getField(new SendingTime());
        Date date = sendingTime.getValue();

        int count = marketData.getGroupCount(NoMDEntries.FIELD);
        for (int i = 0; i < count; i++) {

            Group group = marketData.getGroup(i + 1, NoMDEntries.FIELD);
            char type = group.getChar(MDEntryType.FIELD);
            if (type == MDEntryType.BID || type == MDEntryType.OFFER || type == MDEntryType.TRADE) {
                double price = group.getDouble(MDEntryPx.FIELD);
                double size = group.getDouble(MDEntrySize.FIELD);
                switch (type) {
                    case MDEntryType.BID:
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("{} BID {}@{}", id, size, price);
                        }

                        BidVO bidVO = new BidVO(id, FeedType.TT.name(), date, price, (int) size);
                        this.serverEngine.sendEvent(bidVO);
                        break;
                    case MDEntryType.OFFER:
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("{} ASK {}@{}", id, size, price);
                        }

                        AskVO askVO = new AskVO(id, FeedType.TT.name(), date, price, (int) size);
                        this.serverEngine.sendEvent(askVO);
                        break;
                    case MDEntryType.TRADE:
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("{} TRADE {}@{}", id, size, price);
                        }

                        TradeVO tradeVO = new TradeVO(id, FeedType.TT.name(), date, price, (int) size);
                        this.serverEngine.sendEvent(tradeVO);
                        break;
                }
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Skipping unexpected update " + id + ", type: " + type);
                }
            }
        }
    }

}
