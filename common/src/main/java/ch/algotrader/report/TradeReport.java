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
package ch.algotrader.report;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import ch.algotrader.entity.Transaction;
import ch.algotrader.enumeration.TransactionType;
import ch.algotrader.util.RoundUtil;
import ch.algotrader.vo.TradePerformanceVO;

/**
 * @author <a href="mailto:mterzer@algotrader.ch">Marco Terzer</a>
 *
 * @version $Revision$ $Date$
 */
public class TradeReport extends ListReporter {

    protected static final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public TradeReport() {
        super("TradeReport", new String[] { "dateTime", "type", "quantity", "security", "currency", "strategy", "profit", "profitPct", "winning" });
    }

    public void write(Transaction transaction, TradePerformanceVO tradePerformance) {

        write(DATE_TIME_FORMAT.format(transaction.getDateTime()), //
                transaction.getType() == TransactionType.SELL ? "LONG" : "SHORT", //
                -transaction.getQuantity(), //
                transaction.getSecurity(), //
                transaction.getCurrency(), //
                transaction.getStrategy(), //
                RoundUtil.getBigDecimal(tradePerformance.getProfit()), //
                RoundUtil.getBigDecimal(tradePerformance.getProfitPct()), //
                tradePerformance.isWinning());
    }
}
