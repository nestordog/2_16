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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.adapter.fix.fix44.GenericFix44OrderMessageHandler;
import ch.algotrader.esper.Engine;
import ch.algotrader.ordermgmt.OpenOrderRegistry;
import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.field.RefSeqNum;
import quickfix.field.SessionRejectReason;
import quickfix.field.Text;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.Reject;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class FTXFixOrderMessageHandler extends GenericFix44OrderMessageHandler {

    private static final Logger LOGGER = LogManager.getLogger(FTXFixOrderMessageHandler.class);

    public FTXFixOrderMessageHandler(final OpenOrderRegistry openOrderRegistry, final Engine serverEngine) {
        super(openOrderRegistry, serverEngine);
    }

    @Override
    protected String getStatusText(final ExecutionReport executionReport) throws FieldNotFound {
        if (executionReport.isSetText()) {
            String text = executionReport.getText().getValue();
            try {
                int code = Integer.parseInt(text);
                switch (code) {
                    case 48:
                        return "Reason Unknown";
                    case 49:
                        return "Not Logon";
                    case 50:
                        return "Destination Not Available";
                    case 51:
                        return "User Exceeding Permission Level";
                    case 52:
                        return "Order Not Found";
                    case 53:
                        return "No Shares To Sell";
                    case 54:
                        return "Can?t Short Downtick";
                    case 55:
                        return "Stock Unborrowable";
                    case 56:
                        return "Too Many Shares";
                    case 57:
                        return "Too Many Positions";
                    case 97:
                        return "Quote Not Available";
                    case 98:
                        return "Not Enough Buying Power";
                    case 99:
                        return "Rejected By Destination";
                    case 100:
                        return "Invalid Symbol";
                    case 101:
                        return "Time Limit Violation";
                    case 102:
                        return "Rejected By Admin";
                    case 103:
                        return "Exceeding Permission Level";
                    case 104:
                        return "Position Is Locked For Hedging";
                    case 105:
                        return "Position Not Hedged";
                    case 106:
                        return "Invalid Option Type";
                    case 107:
                        return "Invalid Strike Price";
                    case 108:
                        return "Incorrect To Open Or To Close";
                    case 109:
                        return "Bad Price";
                    case 110:
                        return "Order In Progress";
                    case 111:
                        return "Timed Out";
                    case 112:
                        return "No Liquidity";
                    case 113:
                        return "Duplicated Order ID";
                    default:
                        return "Error code: " + code;
                }
            } catch (NumberFormatException ex) {
                return text;
            }
        }
        return null;
    }

    @Override
    public void onMessage(final Reject reject, final SessionID sessionID) throws FieldNotFound {

        if (LOGGER.isErrorEnabled()) {
            StringBuilder buf = new StringBuilder();
            buf.append("Message rejected as invalid");
            if (reject.isSetField(RefSeqNum.FIELD)) {
                int seqNum = reject.getRefSeqNum().getValue();
                buf.append(" [seq num: ").append(seqNum).append("]");
            }
            if (reject.isSetField(Text.FIELD)) {
                buf.append(": ").append(reject.getString(Text.FIELD));
            } else if (reject.isSetField(SessionRejectReason.FIELD)) {
                SessionRejectReason reason = reject.getSessionRejectReason();
                buf.append(": reason code ").append(reason.getValue());
            }

            LOGGER.error(buf.toString());
        }
    }

}
