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
import java.util.ArrayList;
import java.util.Collection;

/**
 * Contains the SABR definition of an entire Volatility Surface composed of multiple {@link
 * SABRSmileVO SABRSmileVOs}
 */
public class SABRSurfaceVO implements Serializable {

    private static final long serialVersionUID = -1056494310137669561L;

    /**
     * Contains the SABR definition of a Volatility Smile at a specific expiration defined by the
     * parameter
     * {@code years}
     */
    private Collection<SABRSmileVO> smiles;

    /**
     * Default Constructor
     */
    public SABRSurfaceVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param smilesIn Collection<SABRSmileVO>
     */
    public SABRSurfaceVO(final Collection<SABRSmileVO> smilesIn) {

        this.smiles = smilesIn;
    }

    /**
     * Copies constructor from other SABRSurfaceVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public SABRSurfaceVO(final SABRSurfaceVO otherBean) {

        this.smiles = otherBean.getSmiles();
    }

    /**
     * Contains the SABR definition of a Volatility Smile at a specific expiration defined by the
     * parameter {@code years}
     * Get the smiles Association
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the object.
     * @return this.smiles Collection<SABRSmileVO>
     */
    public Collection<SABRSmileVO> getSmiles() {

        if (this.smiles == null) {

            this.smiles = new ArrayList<>();
        }
        return this.smiles;
    }

    /**
     * Sets the smiles
     * @param value Collection<SABRSmileVO>
     */
    public void setSmiles(Collection<SABRSmileVO> value) {

        this.smiles = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("SABRSurfaceVO [smiles=");
        builder.append(this.smiles);
        builder.append("]");

        return builder.toString();
    }

}
