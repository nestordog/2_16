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
package ch.algotrader.adapter.dc;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import ch.algotrader.adapter.fix.fix44.AbstractFix44MarketDataMessageHandler;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.util.MyLogger;
import ch.algotrader.vo.AskVO;
import ch.algotrader.vo.BidVO;
import quickfix.FieldNotFound;
import quickfix.Group;
import quickfix.SessionID;
import quickfix.field.MDEntryPx;
import quickfix.field.MDEntrySize;
import quickfix.field.MDEntryTime;
import quickfix.field.MDEntryType;
import quickfix.field.NoMDEntries;
import quickfix.field.Symbol;
import quickfix.fix44.MarketDataSnapshotFullRefresh;

/**
 * DukasCopy specific FIX market data handler.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class DCFixMarketDataMessageHandler extends AbstractFix44MarketDataMessageHandler {

    private static Logger logger = MyLogger.getLogger(DCFixMarketDataMessageHandler.class.getName());

    public void onMessage(MarketDataSnapshotFullRefresh marketData, SessionID sessionID) throws FieldNotFound {

        Symbol symbol = marketData.getSymbol();

        int count = marketData.getGroupCount(NoMDEntries.FIELD);
        for (int i = 1; i <= count; i++) {

            Group group = marketData.getGroup(i, NoMDEntries.FIELD);
            char entryType = group.getChar(MDEntryType.FIELD);
            if (entryType == MDEntryType.BID || entryType == MDEntryType.OFFER) {

                double price = group.getDouble(MDEntryPx.FIELD);
                double size = group.getDouble(MDEntrySize.FIELD);
                Date time = group.getUtcTimeOnly(MDEntryTime.FIELD);

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
                calendar = DateUtils.truncate(calendar, Calendar.DATE);
                Date date = new Date(calendar.getTimeInMillis() + time.getTime());

                String tickerId = symbol.getValue();
                switch (entryType) {
                    case MDEntryType.BID:

                        if (logger.isTraceEnabled()) {
                            logger.trace(symbol.getValue() + " BID " + size + "@" + price);
                        }

                        BidVO bidVO = new BidVO(tickerId, FeedType.DC, date, price, (int) size);
                        EngineLocator.instance().getBaseEngine().sendEvent(bidVO);
                        break;
                    case MDEntryType.OFFER:

                        if (logger.isTraceEnabled()) {
                            logger.trace(symbol.getValue() + " ASK " + size + "@" + price);
                        }

                        AskVO askVO = new AskVO(tickerId, FeedType.DC, date, price, (int) size);

                        EngineLocator.instance().getBaseEngine().sendEvent(askVO);
                        break;
                }
            }
        }
    }

}
