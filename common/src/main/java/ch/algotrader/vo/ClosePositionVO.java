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

import ch.algotrader.enumeration.Direction;

/**
 * A ValueObject representing a closing of a {@link ch.algotrader.entity.Position Position}
 */
public class ClosePositionVO extends PositionMutationVO {

    private static final long serialVersionUID = 4175765086214246002L;

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
     */
    public ClosePositionVO(final int idIn, final int securityIdIn, final String strategyIn, final long quantityIn, final Direction directionIn) {

        super(idIn, securityIdIn, strategyIn, quantityIn, directionIn);
    }

    /**
     * Copies constructor from other ClosePositionVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public ClosePositionVO(final ClosePositionVO otherBean) {

        super(otherBean);
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("ClosePositionVO [");
        builder.append(super.toString());
        builder.append("]");

        return builder.toString();
    }

}
