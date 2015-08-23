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

import ch.algotrader.enumeration.Direction;

/**
 * A ValueObject representing a mutation of a {@link ch.algotrader.entity.Position Position}
 */
public class PositionMutationVO implements Serializable {

    private static final long serialVersionUID = 8785576161302312640L;

    /**
     * The Id of the Position.
     */
    private final long id;

    /**
     * The Id of the Security.
     */
    private final long securityId;

    /**
     * The name of the Strategy
     */
    private final String strategy;

    /**
     * The current quantity of the Position
     */
    private final long quantity;

    /**
     * The direction of the Position
     */
    private final Direction direction;

    public PositionMutationVO(final long id, final long securityId, final String strategy, final long quantity, final Direction direction) {

        this.id = id;
        this.securityId = securityId;
        this.strategy = strategy;
        this.quantity = quantity;
        this.direction = direction;
    }

    /**
     * Copies constructor from other PositionMutationVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public PositionMutationVO(final PositionMutationVO otherBean) {

        this.id = otherBean.getId();
        this.securityId = otherBean.getSecurityId();
        this.strategy = otherBean.getStrategy();
        this.quantity = otherBean.getQuantity();
        this.direction = otherBean.getDirection();
    }

    /**
     * The Id of the Position.
     * @return id int
     */
    public long getId() {

        return this.id;
    }

    /**
     * The Id of the Security.
     * @return securityId int
     */
    public long getSecurityId() {

        return this.securityId;
    }

    /**
     * The name of the Strategy
     * @return strategy String
     */
    public String getStrategy() {

        return this.strategy;
    }

    /**
     * The current quantity of the Position
     * @return quantity long
     */
    public long getQuantity() {

        return this.quantity;
    }

    /**
     * The direction of the Position
     * @return direction Direction
     */
    public Direction getDirection() {

        return this.direction;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{id=");
        builder.append(this.id);
        builder.append(", securityId=");
        builder.append(this.securityId);
        builder.append(", strategy=");
        builder.append(this.strategy);
        builder.append(", quantity=");
        builder.append(this.quantity);
        builder.append(", direction=");
        builder.append(this.direction);
        builder.append("}");

        return builder.toString();
    }

}
