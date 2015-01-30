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
 * A ValueObject representing various performance figures related to a Simulation Run.
 */
public class PerformanceKeysVO implements Serializable {

    private static final long serialVersionUID = -6925487800320281327L;

    /**
     * The number of months
     */
    private long n;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setN = false;

    /**
     * The average performance per month.
     */
    private double avgM;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setAvgM = false;

    /**
     * The standard deviation per month.
     */
    private double stdM;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setStdM = false;

    /**
     * The average performance per year.
     */
    private double avgY;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setAvgY = false;

    /**
     * The standard deviation per year.
     */
    private double stdY;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setStdY = false;

    /**
     * The Sparp Ratio.
     */
    private double sharpeRatio;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setSharpeRatio = false;

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
        this.setN = true;
        this.avgM = avgMIn;
        this.setAvgM = true;
        this.stdM = stdMIn;
        this.setStdM = true;
        this.avgY = avgYIn;
        this.setAvgY = true;
        this.stdY = stdYIn;
        this.setStdY = true;
        this.sharpeRatio = sharpeRatioIn;
        this.setSharpeRatio = true;
    }

    /**
     * Copies constructor from other PerformanceKeysVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public PerformanceKeysVO(final PerformanceKeysVO otherBean) {

        this.n = otherBean.getN();
        this.setN = true;
        this.avgM = otherBean.getAvgM();
        this.setAvgM = true;
        this.stdM = otherBean.getStdM();
        this.setStdM = true;
        this.avgY = otherBean.getAvgY();
        this.setAvgY = true;
        this.stdY = otherBean.getStdY();
        this.setStdY = true;
        this.sharpeRatio = otherBean.getSharpeRatio();
        this.setSharpeRatio = true;
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
        this.setN = true;
    }

    /**
     * Return true if the primitive attribute n is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetN() {

        return this.setN;
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
        this.setAvgM = true;
    }

    /**
     * Return true if the primitive attribute avgM is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetAvgM() {

        return this.setAvgM;
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
        this.setStdM = true;
    }

    /**
     * Return true if the primitive attribute stdM is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetStdM() {

        return this.setStdM;
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
        this.setAvgY = true;
    }

    /**
     * Return true if the primitive attribute avgY is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetAvgY() {

        return this.setAvgY;
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
        this.setStdY = true;
    }

    /**
     * Return true if the primitive attribute stdY is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetStdY() {

        return this.setStdY;
    }

    public double getSharpeRatio() {

        return this.sharpeRatio;
    }

    public void setSharpeRatio(final double value) {

        this.sharpeRatio = value;
        this.setSharpeRatio = true;
    }

    /**
     * Return true if the primitive attribute sharpeRatio is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetSharpeRatio() {

        return this.setSharpeRatio;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("PerformanceKeysVO [n=");
        builder.append(n);
        builder.append(", setN=");
        builder.append(setN);
        builder.append(", avgM=");
        builder.append(avgM);
        builder.append(", setAvgM=");
        builder.append(setAvgM);
        builder.append(", stdM=");
        builder.append(stdM);
        builder.append(", setStdM=");
        builder.append(setStdM);
        builder.append(", avgY=");
        builder.append(avgY);
        builder.append(", setAvgY=");
        builder.append(setAvgY);
        builder.append(", stdY=");
        builder.append(stdY);
        builder.append(", setStdY=");
        builder.append(setStdY);
        builder.append(", sharpeRatio=");
        builder.append(sharpeRatio);
        builder.append(", setSharpeRatio=");
        builder.append(setSharpeRatio);
        builder.append("]");

        return builder.toString();
    }

}
