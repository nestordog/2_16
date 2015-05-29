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

import ch.algotrader.enumeration.Color;

/**
 * Defines a Bar Series.
 */
public class BarDefinitionVO extends SeriesDefinitionVO {

    private static final long serialVersionUID = 1018594211005720755L;

    /**
     * The Security Id this Bar Series is related to.
     */
    private long securityId;

    /**
     * Default Constructor
     */
    public BarDefinitionVO() {

        super();
    }

    /**
     * Constructor with all properties
     * @param labelIn String
     * @param selectedIn boolean
     * @param colorIn Color
     * @param dashedIn boolean
     * @param securityIdIn int
     */
    public BarDefinitionVO(final String labelIn, final boolean selectedIn, final Color colorIn, final boolean dashedIn, final int securityIdIn) {

        super(labelIn, selectedIn, colorIn, dashedIn);

        this.securityId = securityIdIn;
    }

    /**
     * Copies constructor from other BarDefinitionVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public BarDefinitionVO(final BarDefinitionVO otherBean) {

        super(otherBean);

        this.securityId = otherBean.getSecurityId();
    }

    /**
     * The Security Id this Bar Series is related to.
     * @return securityId int
     */
    public long getSecurityId() {

        return this.securityId;
    }

    /**
     * The Security Id this Bar Series is related to.
     * @param value int
     */
    public void setSecurityId(final int value) {

        this.securityId = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("BarDefinitionVO [securityId=");
        builder.append(this.securityId);
        builder.append(", ");
        builder.append(super.toString());
        builder.append("]");

        return builder.toString();
    }

}
