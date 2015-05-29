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
    private double profit;

    /**
     * The percent profit of the trade (round trip).
     */
    private double profitPct;

    /**
     * True if this was a winning trade (round trip)
     */
    private boolean winning;

    /**
     * Default Constructor
     */
    public TradePerformanceVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param profitIn double
     * @param profitPctIn double
     * @param winningIn boolean
     */
    public TradePerformanceVO(final double profitIn, final double profitPctIn, final boolean winningIn) {

        this.profit = profitIn;
        this.profitPct = profitPctIn;
        this.winning = winningIn;
    }

    /**
     * Copies constructor from other TradePerformanceVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public TradePerformanceVO(final TradePerformanceVO otherBean) {

        this.profit = otherBean.getProfit();
        this.profitPct = otherBean.getProfitPct();
        this.winning = otherBean.isWinning();
    }

    /**
     * The monetary profit of the trade (round trip).
     * @return profit double
     */
    public double getProfit() {

        return this.profit;
    }

    /**
     * The monetary profit of the trade (round trip).
     * @param value double
     */
    public void setProfit(final double value) {

        this.profit = value;
    }

    /**
     * The percent profit of the trade (round trip).
     * @return profitPct double
     */
    public double getProfitPct() {

        return this.profitPct;
    }

    /**
     * The percent profit of the trade (round trip).
     * @param value double
     */
    public void setProfitPct(final double value) {

        this.profitPct = value;
    }

    /**
     * True if this was a winning trade (round trip)
     * @return winning boolean
     */
    public boolean isWinning() {

        return this.winning;
    }

    /**
     * True if this was a winning trade (round trip)
     * Duplicates isBoolean method, for use as Jaxb2 compatible object
     * @return winning boolean
     */
    @Deprecated
    public boolean getWinning() {

        return this.winning;
    }

    /**
     * True if this was a winning trade (round trip)
     * @param value boolean
     */
    public void setWinning(final boolean value) {

        this.winning = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("TradePerformanceVO [profit=");
        builder.append(this.profit);
        builder.append(", profitPct=");
        builder.append(this.profitPct);
        builder.append(", winning=");
        builder.append(this.winning);
        builder.append("]");

        return builder.toString();
    }

}
