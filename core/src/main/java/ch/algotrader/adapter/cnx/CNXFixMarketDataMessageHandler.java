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
package ch.algotrader.adapter.cnx;

import java.util.Date;

import org.apache.log4j.Logger;

import ch.algotrader.adapter.fix.fix44.AbstractFix44MarketDataMessageHandler;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.esper.Engine;
import ch.algotrader.vo.AskVO;
import ch.algotrader.vo.BidVO;
import quickfix.FieldNotFound;
import quickfix.Group;
import quickfix.SessionID;
import quickfix.UtcTimeStampField;
import quickfix.field.MDEntryPx;
import quickfix.field.MDEntrySize;
import quickfix.field.MDEntryType;
import quickfix.field.MDReqID;
import quickfix.field.MDUpdateAction;
import quickfix.field.NoMDEntries;
import quickfix.field.SendingTime;
import quickfix.fix44.MarketDataIncrementalRefresh;

/**
 * Currenex specific FIX market data handler.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class CNXFixMarketDataMessageHandler extends AbstractFix44MarketDataMessageHandler {

    private static Logger LOGGER = Logger.getLogger(CNXFixMarketDataMessageHandler.class.getName());

    private final Engine serverEngine;

    public CNXFixMarketDataMessageHandler(final Engine serverEngine) {
        this.serverEngine = serverEngine;
    }

    public void onMessage(final MarketDataIncrementalRefresh marketData, final SessionID sessionID) throws FieldNotFound {

        MDReqID mdReqID = marketData.getMDReqID();
        String id = mdReqID.getValue();
        UtcTimeStampField sendingTime = marketData.getHeader().getField(new SendingTime());
        Date date = sendingTime.getValue();

        int count = marketData.getGroupCount(NoMDEntries.FIELD);
        for (int i = 1; i <= count; i++) {

            Group group = marketData.getGroup(i, NoMDEntries.FIELD);
            char updateAction = group.getChar(MDUpdateAction.FIELD);
            if (updateAction != MDUpdateAction.NEW && updateAction != MDUpdateAction.CHANGE) {

                continue;
            }

            char entryType = group.getChar(MDEntryType.FIELD);
            if (entryType == MDEntryType.BID || entryType == MDEntryType.OFFER) {

                double price = group.getDouble(MDEntryPx.FIELD);
                double size = group.getDouble(MDEntrySize.FIELD);

                switch (entryType) {
                    case MDEntryType.BID:

                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace(id + " BID " + size + "@" + price);
                        }

                        BidVO bidVO = new BidVO(id, FeedType.CNX, date, price, (int) size);
                        this.serverEngine.sendEvent(bidVO);
                        break;
                    case MDEntryType.OFFER:

                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace(id + " ASK " + size + "@" + price);
                        }

                        AskVO askVO = new AskVO(id, FeedType.CNX, date, price, (int) size);

                        this.serverEngine.sendEvent(askVO);
                        break;
                }
            }
        }
    }

}
