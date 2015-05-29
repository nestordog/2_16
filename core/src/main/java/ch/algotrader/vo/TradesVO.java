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
 * A ValueObject representing a collection of Trades related to a Simulation Run. The collection can
 * represent either all winning, all loosing or all trades.
 */
public class TradesVO implements Serializable {

    private static final long serialVersionUID = -5647751466173867494L;

    /**
     * The number of trades (round trips).
     */
    private long count;

    /**
     * The total monetary profit.
     */
    private double totalProfit;

    /**
     * The average monetary profit per trade (round trip).
     */
    private double avgProfit;

    /**
     * The average profit in percent per trade (round trip).
     */
    private double avgProfitPct;

    /**
     * Default Constructor
     */
    public TradesVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param countIn long
     * @param totalProfitIn double
     * @param avgProfitIn double
     * @param avgProfitPctIn double
     */
    public TradesVO(final long countIn, final double totalProfitIn, final double avgProfitIn, final double avgProfitPctIn) {

        this.count = countIn;
        this.totalProfit = totalProfitIn;
        this.avgProfit = avgProfitIn;
        this.avgProfitPct = avgProfitPctIn;
    }

    /**
     * Copies constructor from other TradesVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public TradesVO(final TradesVO otherBean) {

        this.count = otherBean.getCount();
        this.totalProfit = otherBean.getTotalProfit();
        this.avgProfit = otherBean.getAvgProfit();
        this.avgProfitPct = otherBean.getAvgProfitPct();
    }

    /**
     * The number of trades (round trips).
     * @return count long
     */
    public long getCount() {

        return this.count;
    }

    /**
     * The number of trades (round trips).
     * @param value long
     */
    public void setCount(final long value) {

        this.count = value;
    }

    /**
     * The total monetary profit.
     * @return totalProfit double
     */
    public double getTotalProfit() {

        return this.totalProfit;
    }

    /**
     * The total monetary profit.
     * @param value double
     */
    public void setTotalProfit(final double value) {

        this.totalProfit = value;
    }

    /**
     * The average monetary profit per trade (round trip).
     * @return avgProfit double
     */
    public double getAvgProfit() {

        return this.avgProfit;
    }

    /**
     * The average monetary profit per trade (round trip).
     * @param value double
     */
    public void setAvgProfit(final double value) {

        this.avgProfit = value;
    }

    /**
     * The average profit in percent per trade (round trip).
     * @return avgProfitPct double
     */
    public double getAvgProfitPct() {

        return this.avgProfitPct;
    }

    /**
     * The average profit in percent per trade (round trip).
     * @param value double
     */
    public void setAvgProfitPct(final double value) {

        this.avgProfitPct = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("TradesVO [count=");
        builder.append(this.count);
        builder.append(", totalProfit=");
        builder.append(this.totalProfit);
        builder.append(", avgProfit=");
        builder.append(this.avgProfit);
        builder.append(", avgProfitPct=");
        builder.append(this.avgProfitPct);
        builder.append("]");

        return builder.toString();
    }

}
