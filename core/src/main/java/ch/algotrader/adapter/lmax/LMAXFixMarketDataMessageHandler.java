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
package ch.algotrader.adapter.lmax;

import java.util.Date;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ch.algotrader.adapter.fix.fix44.AbstractFix44MarketDataMessageHandler;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.util.MyLogger;
import ch.algotrader.vo.AskVO;
import ch.algotrader.vo.BidVO;
import quickfix.FieldNotFound;
import quickfix.Group;
import quickfix.SessionID;
import quickfix.field.MDEntryDate;
import quickfix.field.MDEntryPx;
import quickfix.field.MDEntrySize;
import quickfix.field.MDEntryTime;
import quickfix.field.MDEntryType;
import quickfix.field.MDReqID;
import quickfix.field.MDReqRejReason;
import quickfix.field.NoMDEntries;
import quickfix.field.SecurityID;
import quickfix.fix44.MarketDataRequestReject;
import quickfix.fix44.MarketDataSnapshotFullRefresh;

/**
 * LMAX specific FIX market data handler.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class LMAXFixMarketDataMessageHandler extends AbstractFix44MarketDataMessageHandler {

    private static Logger logger = MyLogger.getLogger(LMAXFixMarketDataMessageHandler.class.getName());

    public void onMessage(final MarketDataSnapshotFullRefresh marketData, final SessionID sessionID) throws FieldNotFound {

        SecurityID secId = marketData.getSecurityID();
        String lmaxId = secId.getValue();

        Date date = null;
        int count = marketData.getGroupCount(NoMDEntries.FIELD);
        for (int i = 1; i <= count; i++) {

            Group group = marketData.getGroup(i, NoMDEntries.FIELD);
            char entryType = group.getChar(MDEntryType.FIELD);

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

                        if (logger.isTraceEnabled()) {
                            logger.trace(lmaxId + " BID " + size + "@" + price);
                        }

                        BidVO bidVO = new BidVO(lmaxId, date, price, (int) size);
                        EngineLocator.instance().getBaseEngine().sendEvent(bidVO);
                        break;
                    case MDEntryType.OFFER:

                        if (logger.isTraceEnabled()) {
                            logger.trace(lmaxId + " ASK " + size + "@" + price);
                        }

                        AskVO askVO = new AskVO(lmaxId, date, price, (int) size);

                        EngineLocator.instance().getBaseEngine().sendEvent(askVO);
                        break;
                }
            }
        }
    }

    @Override
    public void onMessage(final MarketDataRequestReject reject, final SessionID sessionID) throws FieldNotFound {
        if (logger.isEnabledFor(Level.WARN) && reject.isSetField(MDReqRejReason.FIELD)) {
            MDReqID mdReqID = reject.getMDReqID();
            MDReqRejReason reason = reject.getMDReqRejReason();
            StringBuilder buf = new StringBuilder();
            buf.append("Request '").append(mdReqID.getValue()).append("' failed: ");
            switch (reason.getValue()) {
                case '0':
                    buf.append("unknown symbol");
                    break;
                case '1':
                    buf.append("duplicate request id");
                    break;
                case '4':
                    buf.append("unsupported request type");
                    break;
                case '5':
                    buf.append("unsupported market depth");
                    break;
                case '6':
                    buf.append("unsupported update type");
                    break;
                case '8':
                    buf.append("unsupported entry type");
                    break;
                default:
                    buf.append("unspecified problem");
                    break;
            }
            logger.warn(buf.toString());
        }
    }

}
