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
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setProfit = false;

    /**
     * The percent profit of the trade (round trip).
     */
    private double profitPct;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setProfitPct = false;

    /**
     * True if this was a winning trade (round trip)
     */
    private boolean winning;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setWinning = false;

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
        this.setProfit = true;
        this.profitPct = profitPctIn;
        this.setProfitPct = true;
        this.winning = winningIn;
        this.setWinning = true;
    }

    /**
     * Copies constructor from other TradePerformanceVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public TradePerformanceVO(final TradePerformanceVO otherBean) {

        this.profit = otherBean.getProfit();
        this.setProfit = true;
        this.profitPct = otherBean.getProfitPct();
        this.setProfitPct = true;
        this.winning = otherBean.isWinning();
        this.setWinning = true;
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
        this.setProfit = true;
    }

    /**
     * Return true if the primitive attribute profit is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetProfit() {

        return this.setProfit;
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
        this.setProfitPct = true;
    }

    /**
     * Return true if the primitive attribute profitPct is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetProfitPct() {

        return this.setProfitPct;
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
        this.setWinning = true;
    }

    /**
     * Return true if the primitive attribute winning is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetWinning() {

        return this.setWinning;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("TradePerformanceVO [profit=");
        builder.append(this.profit);
        builder.append(", setProfit=");
        builder.append(this.setProfit);
        builder.append(", profitPct=");
        builder.append(this.profitPct);
        builder.append(", setProfitPct=");
        builder.append(this.setProfitPct);
        builder.append(", winning=");
        builder.append(this.winning);
        builder.append(", setWinning=");
        builder.append(this.setWinning);
        builder.append("]");

        return builder.toString();
    }

}
