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
package ch.algotrader.vo;

import java.io.Serializable;

/**
 * A ValueObject representing performance figures related to one individual trade (round trip).
 */
public class TradePerformanceVO implements Serializable {

    private static final long serialVersionUID = -5023632083826173592L;

    /**
     * The monetary profit of the trade (round trip).
     */
    private final double profit;

    /**
     * The percent profit of the trade (round trip).
     */
    private final double profitPct;

    /**
     * True if this was a winning trade (round trip)
     */
    private final boolean winning;

    public TradePerformanceVO(final double profitIn, final double profitPctIn, final boolean winningIn) {
        this.profit = profitIn;
        this.profitPct = profitPctIn;
        this.winning = winningIn;
    }

    /**
     * The monetary profit of the trade (round trip).
     * @return profit double
     */
    public double getProfit() {

        return this.profit;
    }

    /**
     * The percent profit of the trade (round trip).
     * @return profitPct double
     */
    public double getProfitPct() {

        return this.profitPct;
    }

    /**
     * True if this was a winning trade (round trip)
     * @return winning boolean
     */
    public boolean isWinning() {

        return this.winning;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{profit=");
        builder.append(this.profit);
        builder.append(", profitPct=");
        builder.append(this.profitPct);
        builder.append(", winning=");
        builder.append(this.winning);
        builder.append("}");

        return builder.toString();
    }

}
