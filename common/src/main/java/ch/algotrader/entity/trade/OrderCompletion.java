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
public class OrderCompletion implements Serializable {

    private static final long serialVersionUID = 8510334378552535810L;

    private Date dateTime;

    private Status status;

    private long filledQuantity;

    private long remainingQuantity;

    private BigDecimal avgPrice;

    private BigDecimal grossValue;

    private BigDecimal netValue;

    private BigDecimal totalCharges;

    private int fills;

    private double executionTime;

    private Order order;

    /**
     * The dateTime the order was fully executed. This is set automatically by the {@link
     * ch.algotrader.service.OrderService OrderService}
     * @return this.dateTime Date
     */
    public Date getDateTime() {
        return this.dateTime;
    }

    /**
     * The dateTime the order was fully executed. This is set automatically by the {@link
     * ch.algotrader.service.OrderService OrderService}
     * @param dateTimeIn Date
     */
    public void setDateTime(Date dateTimeIn) {
        this.dateTime = dateTimeIn;
    }

    /**
     * The Order {@link Status}
     * @return this.status Status
     */
    public Status getStatus() {
        return this.status;
    }

    /**
     * The Order {@link Status}
     * @param statusIn Status
     */
    public void setStatus(Status statusIn) {
        this.status = statusIn;
    }

    /**
     * The quantity of the Order that has already been filled.
     * @return this.filledQuantity long
     */
    public long getFilledQuantity() {
        return this.filledQuantity;
    }

    /**
     * The quantity of the Order that has already been filled.
     * @param filledQuantityIn long
     */
    public void setFilledQuantity(long filledQuantityIn) {
        this.filledQuantity = filledQuantityIn;
    }

    /**
     * The remaining quantity of the Order that has not been filled yet.
     * @return this.remainingQuantity long
     */
    public long getRemainingQuantity() {
        return this.remainingQuantity;
    }

    /**
     * The remaining quantity of the Order that has not been filled yet.
     * @param remainingQuantityIn long
     */
    public void setRemainingQuantity(long remainingQuantityIn) {
        this.remainingQuantity = remainingQuantityIn;
    }

    /**
     * The volume weighted average price of all associated transactions.
     * @return this.avgPrice BigDecimal
     */
    public BigDecimal getAvgPrice() {
        return this.avgPrice;
    }

    /**
     * The volume weighted average price of all associated transactions.
     * @param avgPriceIn BigDecimal
     */
    public void setAvgPrice(BigDecimal avgPriceIn) {
        this.avgPrice = avgPriceIn;
    }

    /**
     * The total Transaction Value without Commissions of all Transactions.
     * @return this.grossValue BigDecimal
     */
    public BigDecimal getGrossValue() {
        return this.grossValue;
    }

    /**
     * The total Transaction Value without Commissions of all Transactions.
     * @param grossValueIn BigDecimal
     */
    public void setGrossValue(BigDecimal grossValueIn) {
        this.grossValue = grossValueIn;
    }

    /**
     * The total Transaction Value incl. Commissions and Fees of all Transactions.
     * @return this.netValue BigDecimal
     */
    public BigDecimal getNetValue() {
        return this.netValue;
    }

    /**
     * The total Transaction Value incl. Commissions and Fees of all Transactions.
     * @param netValueIn BigDecimal
     */
    public void setNetValue(BigDecimal netValueIn) {
        this.netValue = netValueIn;
    }

    /**
     * The total of all Commissions and Fees
     * @return this.totalCharges BigDecimal
     */
    public BigDecimal getTotalCharges() {
        return this.totalCharges;
    }

    /**
     * The total of all Commissions and Fees
     * @param totalChargesIn BigDecimal
     */
    public void setTotalCharges(BigDecimal totalChargesIn) {
        this.totalCharges = totalChargesIn;
    }

    /**
     * The number of Fills received
     * @return this.fills int
     */
    public int getFills() {
        return this.fills;
    }

    /**
     * The number of Fills received
     * @param fillsIn int
     */
    public void setFills(int fillsIn) {
        this.fills = fillsIn;
    }

    /**
     * The total execution time in seconds.
     * @return this.executionTime double
     */
    public double getExecutionTime() {
        return this.executionTime;
    }

    /**
     * The total execution time in seconds.
     * @param executionTimeIn double
     */
    public void setExecutionTime(double executionTimeIn) {
        this.executionTime = executionTimeIn;
    }

    /**
     * Base Class for all Order Types
     * @return this.order Order
     */
    public Order getOrder() {
        return this.order;
    }

    /**
     * Base Class for all Order Types
     * @param orderIn Order
     */
    public void setOrder(Order orderIn) {
        this.order = orderIn;
    }

    @Override
    public String toString() {

        StringBuilder buffer = new StringBuilder();

        buffer.append(getStatus());

        if (getOrder() != null) {
            buffer.append(",");
            buffer.append(getOrder().getDescription());
        }

        buffer.append(",filledQuantity=");
        buffer.append(getFilledQuantity());
        buffer.append(",remainingQuantity=");
        buffer.append(getRemainingQuantity());
        buffer.append(",avgPrice=");
        buffer.append(getAvgPrice());
        buffer.append(",grossValue=");
        buffer.append(getGrossValue());
        buffer.append(",netValue=");
        buffer.append(getNetValue());
        buffer.append(",totalCharges=");
        buffer.append(getTotalCharges());
        buffer.append(",fills=");
        buffer.append(getFills());
        buffer.append(",executionTime=");
        buffer.append(getExecutionTime());

        return buffer.toString();
    }

    public static final class Factory {
        /**
         * Constructs a new instance of {@link OrderCompletion}.
         *
         * @return new OrderCompletionImpl()
         */
        public static OrderCompletion newInstance() {
            return new OrderCompletion();
        }


        /**
         * Constructs a new instance of {@link OrderCompletion}, taking all possible properties
         * (except the identifier(s))as arguments.
         *
         * @param dateTime          Date
         * @param status            Status
         * @param filledQuantity    long
         * @param remainingQuantity long
         * @param avgPrice          BigDecimal
         * @param grossValue        BigDecimal
         * @param netValue          BigDecimal
         * @param totalCharges      BigDecimal
         * @param fills             int
         * @param executionTime     double
         * @param order             Order
         * @return newInstance OrderCompletion
         */
        public static OrderCompletion newInstance(Date dateTime, Status status, long filledQuantity, long remainingQuantity, BigDecimal avgPrice, BigDecimal grossValue, BigDecimal netValue, BigDecimal totalCharges, int fills, double executionTime, Order order) {
            final OrderCompletion entity = new OrderCompletion();
            entity.setDateTime(dateTime);
            entity.setStatus(status);
            entity.setFilledQuantity(filledQuantity);
            entity.setRemainingQuantity(remainingQuantity);
            entity.setAvgPrice(avgPrice);
            entity.setGrossValue(grossValue);
            entity.setNetValue(netValue);
            entity.setTotalCharges(totalCharges);
            entity.setFills(fills);
            entity.setExecutionTime(executionTime);
            entity.setOrder(order);
            return entity;
        }
    }

}
