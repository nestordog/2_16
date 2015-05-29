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

import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;

/**
 * A ValueObject representing an {@link ch.algotrader.entity.trade.OrderStatus OrderStatus}. Used
 * for Client display.
 */
public class OrderStatusVO implements Serializable {

    private static final long serialVersionUID = -9002550670469080729L;

    /**
     * {@code BUY} or {@code SELL}
     */
    private Side side;

    /**
     * The requested number of contracts
     */
    private long quantity;

    /**
     * The Order Type (e.g. Market, Limit, etc.)
     */
    private String type;

    /**
     * The Symbol of the associated Security
     */
    private String name;

    /**
     * The name of the Strategy
     */
    private String strategy;

    /**
     * The name of the Account
     */
    private String account;

    /**
     * The time-in-force
     */
    private String tif;

    /**
     * The Internal Order Id.
     */
    private String intId;

    /**
     * The External Order Id.
     */
    private String extId;

    /**
     * The Order {@link Status}
     */
    private Status status;

    /**
     * The quantity of the Order that has already been filled.
     */
    private long filledQuantity;

    /**
     * The remaining quantity of the Order that has not been filled yet.
     */
    private long remainingQuantity;

    /**
     * The extended description of the Order comprised of Order Type specific properties (i.e.
     * {@code limit} for {@link ch.algotrader.entity.trade.LimitOrder LimitOrders})
     */
    private String description;

    /**
     * Default Constructor
     */
    public OrderStatusVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param sideIn Side
     * @param quantityIn long
     * @param typeIn String
     * @param nameIn String
     * @param strategyIn String
     * @param accountIn String
     * @param tifIn String
     * @param intIdIn String
     * @param extIdIn String
     * @param statusIn Status
     * @param filledQuantityIn long
     * @param remainingQuantityIn long
     * @param descriptionIn String
     */
    public OrderStatusVO(final Side sideIn, final long quantityIn, final String typeIn, final String nameIn, final String strategyIn, final String accountIn, final String tifIn, final String intIdIn,
            final String extIdIn, final Status statusIn, final long filledQuantityIn, final long remainingQuantityIn, final String descriptionIn) {

        this.side = sideIn;
        this.quantity = quantityIn;
        this.type = typeIn;
        this.name = nameIn;
        this.strategy = strategyIn;
        this.account = accountIn;
        this.tif = tifIn;
        this.intId = intIdIn;
        this.extId = extIdIn;
        this.status = statusIn;
        this.filledQuantity = filledQuantityIn;
        this.remainingQuantity = remainingQuantityIn;
        this.description = descriptionIn;
    }

    /**
     * Copies constructor from other OrderStatusVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public OrderStatusVO(final OrderStatusVO otherBean) {

        this.side = otherBean.getSide();
        this.quantity = otherBean.getQuantity();
        this.type = otherBean.getType();
        this.name = otherBean.getName();
        this.strategy = otherBean.getStrategy();
        this.account = otherBean.getAccount();
        this.tif = otherBean.getTif();
        this.intId = otherBean.getIntId();
        this.extId = otherBean.getExtId();
        this.status = otherBean.getStatus();
        this.filledQuantity = otherBean.getFilledQuantity();
        this.remainingQuantity = otherBean.getRemainingQuantity();
        this.description = otherBean.getDescription();
    }

    /**
     * {@code BUY} or {@code SELL}
     * @return side Side
     */
    public Side getSide() {

        return this.side;
    }

    /**
     * {@code BUY} or {@code SELL}
     * @param value Side
     */
    public void setSide(final Side value) {

        this.side = value;
    }

    /**
     * The requested number of contracts
     * @return quantity long
     */
    public long getQuantity() {

        return this.quantity;
    }

    /**
     * The requested number of contracts
     * @param value long
     */
    public void setQuantity(final long value) {

        this.quantity = value;
    }

    /**
     * The Order Type (e.g. Market, Limit, etc.)
     * @return type String
     */
    public String getType() {

        return this.type;
    }

    /**
     * The Order Type (e.g. Market, Limit, etc.)
     * @param value String
     */
    public void setType(final String value) {

        this.type = value;
    }

    /**
     * The Symbol of the associated Security
     * @return name String
     */
    public String getName() {

        return this.name;
    }

    /**
     * The Symbol of the associated Security
     * @param value String
     */
    public void setName(final String value) {

        this.name = value;
    }

    /**
     * The name of the Strategy
     * @return strategy String
     */
    public String getStrategy() {

        return this.strategy;
    }

    /**
     * The name of the Strategy
     * @param value String
     */
    public void setStrategy(final String value) {

        this.strategy = value;
    }

    /**
     * The name of the Account
     * @return account String
     */
    public String getAccount() {

        return this.account;
    }

    /**
     * The name of the Account
     * @param value String
     */
    public void setAccount(final String value) {

        this.account = value;
    }

    /**
     * The time-in-force
     * @return tif String
     */
    public String getTif() {

        return this.tif;
    }

    /**
     * The time-in-force
     * @param value String
     */
    public void setTif(final String value) {

        this.tif = value;
    }

    /**
     * The Internal Order Id.
     * @return intId String
     */
    public String getIntId() {

        return this.intId;
    }

    /**
     * The Internal Order Id.
     * @param value String
     */
    public void setIntId(final String value) {

        this.intId = value;
    }

    /**
     * The External Order Id.
     * @return extId String
     */
    public String getExtId() {

        return this.extId;
    }

    /**
     * The External Order Id.
     * @param value String
     */
    public void setExtId(final String value) {

        this.extId = value;
    }

    /**
     * The Order {@link Status}
     * @return status Status
     */
    public Status getStatus() {

        return this.status;
    }

    /**
     * The Order {@link Status}
     * @param value Status
     */
    public void setStatus(final Status value) {

        this.status = value;
    }

    /**
     * The quantity of the Order that has already been filled.
     * @return filledQuantity long
     */
    public long getFilledQuantity() {

        return this.filledQuantity;
    }

    /**
     * The quantity of the Order that has already been filled.
     * @param value long
     */
    public void setFilledQuantity(final long value) {

        this.filledQuantity = value;
    }

    /**
     * The remaining quantity of the Order that has not been filled yet.
     * @return remainingQuantity long
     */
    public long getRemainingQuantity() {

        return this.remainingQuantity;
    }

    /**
     * The remaining quantity of the Order that has not been filled yet.
     * @param value long
     */
    public void setRemainingQuantity(final long value) {

        this.remainingQuantity = value;
    }

    /**
     * The extended description of the Order comprised of Order Type specific properties (i.e.
     * {@code limit} for {@link ch.algotrader.entity.trade.LimitOrder LimitOrders})
     * @return description String
     */
    public String getDescription() {

        return this.description;
    }

    /**
     * The extended description of the Order comprised of Order Type specific properties (i.e.
     * {@code limit} for {@link ch.algotrader.entity.trade.LimitOrder LimitOrders})
     * @param value String
     */
    public void setDescription(final String value) {

        this.description = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("OrderStatusVO [side=");
        builder.append(this.side);
        builder.append(", quantity=");
        builder.append(this.quantity);
        builder.append(", type=");
        builder.append(this.type);
        builder.append(", name=");
        builder.append(this.name);
        builder.append(", strategy=");
        builder.append(this.strategy);
        builder.append(", account=");
        builder.append(this.account);
        builder.append(", tif=");
        builder.append(this.tif);
        builder.append(", intId=");
        builder.append(this.intId);
        builder.append(", extId=");
        builder.append(this.extId);
        builder.append(", status=");
        builder.append(this.status);
        builder.append(", filledQuantity=");
        builder.append(this.filledQuantity);
        builder.append(", remainingQuantity=");
        builder.append(this.remainingQuantity);
        builder.append(", description=");
        builder.append(this.description);
        builder.append("]");

        return builder.toString();
    }

}
