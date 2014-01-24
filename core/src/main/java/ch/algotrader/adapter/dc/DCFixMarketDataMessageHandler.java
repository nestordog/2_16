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
package ch.algotrader.adapter.dc;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

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
import ch.algotrader.adapter.fix.fix44.Fix44MarketDataMessageHandler;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.util.MyLogger;
import ch.algotrader.vo.AskVO;
import ch.algotrader.vo.BidVO;

/**
 * DukasCopy specific FIX market data handler.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class DCFixMarketDataMessageHandler extends Fix44MarketDataMessageHandler {

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

                int tickerId = DCUtil.getTickerId(symbol.getValue());
                switch (entryType) {
                    case MDEntryType.BID:

                        if (logger.isTraceEnabled()) {
                            logger.trace(symbol.getValue() + " BID " + size + "@" + price);
                        }

                        BidVO bidVO = new BidVO((int) tickerId, date, price, (int) size);
                        EngineLocator.instance().getBaseEngine().sendEvent(bidVO);
                        break;
                    case MDEntryType.OFFER:

                        if (logger.isTraceEnabled()) {
                            logger.trace(symbol.getValue() + " ASK " + size + "@" + price);
                        }

                        AskVO askVO = new AskVO((int) tickerId, date, price, (int) size);

                        EngineLocator.instance().getBaseEngine().sendEvent(askVO);
                        break;
                }
            }
        }
    }

}
