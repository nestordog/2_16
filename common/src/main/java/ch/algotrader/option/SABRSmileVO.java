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
package ch.algotrader.option;

import java.io.Serializable;

/**
 * Contains the SABR definition of a Volatility Smile at a specific expiration defined by the
 * parameter {@code years}
 */
public class SABRSmileVO implements Serializable {

    private static final long serialVersionUID = -8638571149531828705L;

    /**
     * The time-to-expiration represented in {@code years}
     */
    private double years;

    /**
     * The SABR alpha
     */
    private double alpha;

    /**
     * The SABR rho
     */
    private double rho;

    /**
     * The SABR volVol
     */
    private double volVol;

    /**
     * The at-the-money Volatility
     */
    private double atmVol;

    /**
     * Default Constructor
     */
    public SABRSmileVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param yearsIn double
     * @param alphaIn double
     * @param rhoIn double
     * @param volVolIn double
     * @param atmVolIn double
     */
    public SABRSmileVO(final double yearsIn, final double alphaIn, final double rhoIn, final double volVolIn, final double atmVolIn) {

        this.years = yearsIn;
        this.alpha = alphaIn;
        this.rho = rhoIn;
        this.volVol = volVolIn;
        this.atmVol = atmVolIn;
    }

    /**
     * Copies constructor from other SABRSmileVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public SABRSmileVO(final SABRSmileVO otherBean) {

        this.years = otherBean.getYears();
        this.alpha = otherBean.getAlpha();
        this.rho = otherBean.getRho();
        this.volVol = otherBean.getVolVol();
        this.atmVol = otherBean.getAtmVol();
    }

    /**
     * The time-to-expiration represented in {@code years}
     * @return years double
     */
    public double getYears() {

        return this.years;
    }

    /**
     * The time-to-expiration represented in {@code years}
     * @param value double
     */
    public void setYears(final double value) {

        this.years = value;
    }

    /**
     * The SABR alpha
     * @return alpha double
     */
    public double getAlpha() {

        return this.alpha;
    }

    /**
     * The SABR alpha
     * @param value double
     */
    public void setAlpha(final double value) {

        this.alpha = value;
    }

    /**
     * The SABR rho
     * @return rho double
     */
    public double getRho() {

        return this.rho;
    }

    /**
     * The SABR rho
     * @param value double
     */
    public void setRho(final double value) {

        this.rho = value;
    }

    /**
     * The SABR volVol
     * @return volVol double
     */
    public double getVolVol() {

        return this.volVol;
    }

    /**
     * The SABR volVol
     * @param value double
     */
    public void setVolVol(final double value) {

        this.volVol = value;
    }

    /**
     * The at-the-money Volatility
     * @return atmVol double
     */
    public double getAtmVol() {

        return this.atmVol;
    }

    /**
     * The at-the-money Volatility
     * @param value double
     */
    public void setAtmVol(final double value) {

        this.atmVol = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("SABRSmileVO [years=");
        builder.append(this.years);
        builder.append(", alpha=");
        builder.append(this.alpha);
        builder.append(", rho=");
        builder.append(this.rho);
        builder.append(", volVol=");
        builder.append(this.volVol);
        builder.append(", atmVol=");
        builder.append(this.atmVol);
        builder.append("]");

        return builder.toString();
    }

}
