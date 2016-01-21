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
package ch.algotrader.report;

import java.io.File;
import java.io.IOException;

import ch.algotrader.entity.Transaction;
import ch.algotrader.enumeration.TransactionType;
import ch.algotrader.util.RoundUtil;
import ch.algotrader.vo.TradePerformanceVO;

/**
 * @author <a href="mailto:mterzer@algotrader.ch">Marco Terzer</a>
 */
public class TradeReport extends ListReporter {

    public static TradeReport create() throws IOException {
        return new TradeReport(Report.generateFile("TradeReport"), new String[] { "dateTime", "type", "quantity", "security", "currency", "strategy", "profit", "profitPct", "winning" });
    }

    protected TradeReport(File file, String[] header) throws IOException {
        super(file, header);
    }

    public void write(Transaction transaction, TradePerformanceVO tradePerformance) throws IOException {

        writeAndFlush(formatDateTime(transaction.getDateTime()), //
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
