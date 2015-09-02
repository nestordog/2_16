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
package ch.algotrader.entity.trade;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import ch.algotrader.enumeration.Status;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class OrderCompletionVO implements Serializable {

    private static final long serialVersionUID = 8510334378552535810L;

    private final String orderIntId;

    private final String strategy;

    private final Date dateTime;

    private final Status status;

    private final long filledQuantity;

    private final long remainingQuantity;

    private final BigDecimal avgPrice;

    private final BigDecimal grossValue;

    private final BigDecimal netValue;

    private final BigDecimal totalCharges;

    private final int fills;

    private final double executionTime;

    public OrderCompletionVO(final String orderIntId, final String strategy, final Date dateTime, final Status status, final long filledQuantity, final long remainingQuantity, final BigDecimal avgPrice,
                             final BigDecimal grossValue, final BigDecimal netValue, final BigDecimal totalCharges, final int fills, final double executionTime) {
        this.orderIntId = orderIntId;
        this.strategy = strategy;
        this.dateTime = dateTime;
        this.status = status;
        this.filledQuantity = filledQuantity;
        this.remainingQuantity = remainingQuantity;
        this.avgPrice = avgPrice;
        this.grossValue = grossValue;
        this.netValue = netValue;
        this.totalCharges = totalCharges;
        this.fills = fills;
        this.executionTime = executionTime;
    }

    /**
     * Internal ID of the order.
     */
    public String getOrderIntId() {
        return this.orderIntId;
    }

    /**
     * Name of the corresponding strategy.
     */
    public String getStrategy() {
        return strategy;
    }

    /**
     * The dateTime the order was fully executed. This is set automatically by the {@link
     * ch.algotrader.service.OrderService OrderService}
     * @return this.dateTime Date
     */
    public Date getDateTime() {
        return this.dateTime;
    }

    /**
     * The Order {@link Status}
     * @return this.status Status
     */
    public Status getStatus() {
        return this.status;
    }

    /**
     * The quantity of the Order that has already been filled.
     * @return this.filledQuantity long
     */
    public long getFilledQuantity() {
        return this.filledQuantity;
    }

    /**
     * The remaining quantity of the Order that has not been filled yet.
     * @return this.remainingQuantity long
     */
    public long getRemainingQuantity() {
        return this.remainingQuantity;
    }

    /**
     * The volume weighted average price of all associated transactions.
     * @return this.avgPrice BigDecimal
     */
    public BigDecimal getAvgPrice() {
        return this.avgPrice;
    }

    /**
     * The total Transaction Value without Commissions of all Transactions.
     * @return this.grossValue BigDecimal
     */
    public BigDecimal getGrossValue() {
        return this.grossValue;
    }

    /**
     * The total Transaction Value incl. Commissions and Fees of all Transactions.
     * @return this.netValue BigDecimal
     */
    public BigDecimal getNetValue() {
        return this.netValue;
    }

    /**
     * The total of all Commissions and Fees
     * @return this.totalCharges BigDecimal
     */
    public BigDecimal getTotalCharges() {
        return this.totalCharges;
    }

    /**
     * The number of Fills received
     * @return this.fills int
     */
    public int getFills() {
        return this.fills;
    }

    /**
     * The total execution time in seconds.
     * @return this.executionTime double
     */
    public double getExecutionTime() {
        return this.executionTime;
    }

    @Override
    public String toString() {

        StringBuilder buffer = new StringBuilder();

        buffer.append(getStatus());
        buffer.append(",orderIntId=");
        buffer.append(this.orderIntId);
        buffer.append(",strategy=");
        buffer.append(this.strategy);
        buffer.append(",filledQuantity=");
        buffer.append(this.remainingQuantity);
        buffer.append(",remainingQuantity=");
        buffer.append(this.remainingQuantity);
        buffer.append(",avgPrice=");
        buffer.append(this.avgPrice);
        buffer.append(",grossValue=");
        buffer.append(this.grossValue);
        buffer.append(",netValue=");
        buffer.append(this.netValue);
        buffer.append(",totalCharges=");
        buffer.append(this.totalCharges);
        buffer.append(",fills=");
        buffer.append(this.fills);
        buffer.append(",executionTime=");
        buffer.append(this.executionTime);

        return buffer.toString();
    }

}
