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
package ch.algotrader.vo.performance;

import java.io.Serializable;

/**
 * A ValueObject representing various performance figures related to a Simulation Run.
 */
public class PerformanceKeysVO implements Serializable {

    private static final long serialVersionUID = -6925487800320281327L;

    /**
     * The number of months
     */
    private long n;

    /**
     * The average performance per month.
     */
    private double avgM;

    /**
     * The standard deviation per month.
     */
    private double stdM;

    /**
     * The average performance per year.
     */
    private double avgY;

    /**
     * The standard deviation per year.
     */
    private double stdY;

    /**
     * The Sparp Ratio.
     */
    private double sharpeRatio;

    /**
     * Default Constructor
     */
    public PerformanceKeysVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param nIn long
     * @param avgMIn double
     * @param stdMIn double
     * @param avgYIn double
     * @param stdYIn double
     * @param sharpeRatioIn double
     */
    public PerformanceKeysVO(final long nIn, final double avgMIn, final double stdMIn, final double avgYIn, final double stdYIn, final double sharpeRatioIn) {

        this.n = nIn;
        this.avgM = avgMIn;
        this.stdM = stdMIn;
        this.avgY = avgYIn;
        this.stdY = stdYIn;
        this.sharpeRatio = sharpeRatioIn;
    }

    /**
     * Copies constructor from other PerformanceKeysVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public PerformanceKeysVO(final PerformanceKeysVO otherBean) {

        this.n = otherBean.getN();
        this.avgM = otherBean.getAvgM();
        this.stdM = otherBean.getStdM();
        this.avgY = otherBean.getAvgY();
        this.stdY = otherBean.getStdY();
        this.sharpeRatio = otherBean.getSharpeRatio();
    }

    /**
     * Return the number of months
     * @return n long
     */
    public long getN() {

        return this.n;
    }

    public void setN(final long value) {

        this.n = value;
    }

    /**
     * Return the average performance per month.
     * @return avgM double
     */
    public double getAvgM() {

        return this.avgM;
    }

    /**
     * Return the average performance per month.
     * @param value double
     */
    public void setAvgM(final double value) {

        this.avgM = value;
    }

    /**
     * Return the standard deviation per month.
     * @return stdM double
     */
    public double getStdM() {

        return this.stdM;
    }

    /**
     * The standard deviation per month.
     * @param value double
     */
    public void setStdM(final double value) {

        this.stdM = value;
    }

    /**
     * The average performance per year.
     * @return avgY double
     */
    public double getAvgY() {

        return this.avgY;
    }

    /**
     * The average performance per year.
     * @param value double
     */
    public void setAvgY(final double value) {

        this.avgY = value;
    }

    /**
     * The standard deviation per year.
     * @return stdY double
     */
    public double getStdY() {

        return this.stdY;
    }

    /**
     * The standard deviation per year.
     * @param value double
     */
    public void setStdY(final double value) {

        this.stdY = value;
    }

    public double getSharpeRatio() {

        return this.sharpeRatio;
    }

    public void setSharpeRatio(final double value) {

        this.sharpeRatio = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("PerformanceKeysVO [n=");
        builder.append(n);
        builder.append(", setN=");
        builder.append(avgM);
        builder.append(", setAvgM=");
        builder.append(stdM);
        builder.append(", avgY=");
        builder.append(avgY);
        builder.append(", stdY=");
        builder.append(stdY);
        builder.append(", sharpeRatio=");
        builder.append(sharpeRatio);
        builder.append("]");

        return builder.toString();
    }

}
