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
 * Defines a Indicator Series.
 */
public class IndicatorDefinitionVO extends SeriesDefinitionVO {

    private static final long serialVersionUID = -7709195978172281058L;

    /**
     * The name of this Indicator Series.
     */
    private String name;

    /**
     * Default Constructor
     */
    public IndicatorDefinitionVO() {

        super();
    }

    /**
     * Constructor with all properties
     * @param labelIn String
     * @param selectedIn boolean
     * @param colorIn Color
     * @param dashedIn boolean
     * @param nameIn String
     */
    public IndicatorDefinitionVO(final String labelIn, final boolean selectedIn, final Color colorIn, final boolean dashedIn, final String nameIn) {

        super(labelIn, selectedIn, colorIn, dashedIn);

        this.name = nameIn;
    }

    /**
     * Copies constructor from other IndicatorDefinitionVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public IndicatorDefinitionVO(final IndicatorDefinitionVO otherBean) {

        super(otherBean);

        this.name = otherBean.getName();
    }

    /**
     * The name of this Indicator Series.
     * @return name String
     */
    public String getName() {

        return this.name;
    }

    /**
     * The name of this Indicator Series.
     * @param value String
     */
    public void setName(final String value) {

        this.name = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("IndicatorDefinitionVO [name=");
        builder.append(this.name);
        builder.append(", ");
        builder.append(super.toString());
        builder.append("]");

        return builder.toString();
    }

}
