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

import java.math.BigDecimal;

import ch.algotrader.enumeration.Direction;

/**
 * A ValueObject representing a closing of a {@link ch.algotrader.entity.Position Position}
 */
public class ClosePositionVO extends PositionMutationVO {

    private static final long serialVersionUID = 4175765086214246002L;

    /**
     * The last {@code exitValue} defined on the Position before it was closed.
     */
    private BigDecimal exitValue;

    /**
     * Default Constructor
     */
    public ClosePositionVO() {

        super();
    }

    /**
     * Constructor with all properties
     * @param idIn int
     * @param securityIdIn int
     * @param strategyIn String
     * @param quantityIn long
     * @param directionIn Direction
     * @param exitValueIn BigDecimal
     */
    public ClosePositionVO(final int idIn, final int securityIdIn, final String strategyIn, final long quantityIn, final Direction directionIn, final BigDecimal exitValueIn) {

        super(idIn, securityIdIn, strategyIn, quantityIn, directionIn);

        this.exitValue = exitValueIn;
    }

    /**
     * Copies constructor from other ClosePositionVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public ClosePositionVO(final ClosePositionVO otherBean) {

        super(otherBean);

        this.exitValue = otherBean.getExitValue();
    }

    /**
     * The last {@code exitValue} defined on the Position before it was closed.
     * @return exitValue BigDecimal
     */
    public BigDecimal getExitValue() {

        return this.exitValue;
    }

    /**
     * The last {@code exitValue} defined on the Position before it was closed.
     * @param value BigDecimal
     */
    public void setExitValue(final BigDecimal value) {

        this.exitValue = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("ClosePositionVO [exitValue=");
        builder.append(this.exitValue);
        builder.append(", ");
        builder.append(super.toString());
        builder.append("]");

        return builder.toString();
    }

}
