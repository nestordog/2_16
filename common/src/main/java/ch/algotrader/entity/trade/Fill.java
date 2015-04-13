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

import ch.algotrader.enumeration.Side;
import ch.algotrader.util.DateTimeUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class Fill implements Serializable {

    private static final long serialVersionUID = 1619681349145226990L;

    private Date dateTime;

    private Date extDateTime;

    private String extId;

    private long sequenceNumber;

    private Side side;

    private long quantity;

    private BigDecimal price;

    private BigDecimal executionCommission;

    private BigDecimal clearingCommission;

    private BigDecimal fee;

    private Order order;

    /**
     * The {@code dateTime} the Fill was received by the system.
     * @return this.dateTime Date
     */
    public Date getDateTime() {
        return this.dateTime;
    }

    /**
     * The {@code dateTime} the Fill was received by the system.
     * @param dateTimeIn Date
     */
    public void setDateTime(Date dateTimeIn) {
        this.dateTime = dateTimeIn;
    }

    /**
     * The external {@code dateTime} of the Fill as assigned by the external Broker.
     * @return this.extDateTime Date
     */
    public Date getExtDateTime() {
        return this.extDateTime;
    }

    /**
     * The external {@code dateTime} of the Fill as assigned by the external Broker.
     * @param extDateTimeIn Date
     */
    public void setExtDateTime(Date extDateTimeIn) {
        this.extDateTime = extDateTimeIn;
    }

    /**
     * External Fill Id assigned by the external Broker
     * @return this.extId String
     */
    public String getExtId() {
        return this.extId;
    }

    /**
     * External Fill Id assigned by the external Broker
     * @param extIdIn String
     */
    public void setExtId(String extIdIn) {
        this.extId = extIdIn;
    }

    /**
     * the sequence number of the corresponding broker specific message (e.g. fix sequence number)
     * @return this.sequenceNumber long
     */
    public long getSequenceNumber() {
        return this.sequenceNumber;
    }

    /**
     * the sequence number of the corresponding broker specific message (e.g. fix sequence number)
     * @param sequenceNumberIn long
     */
    public void setSequenceNumber(long sequenceNumberIn) {
        this.sequenceNumber = sequenceNumberIn;
    }

    /**
     * {@code BUY} or {@code SELL}
     * @return this.side Side
     */
    public Side getSide() {
        return this.side;
    }

    /**
     * {@code BUY} or {@code SELL}
     * @param sideIn Side
     */
    public void setSide(Side sideIn) {
        this.side = sideIn;
    }

    /**
     * The quantity of this Fill.
     * @return this.quantity long
     */
    public long getQuantity() {
        return this.quantity;
    }

    /**
     * The quantity of this Fill.
     * @param quantityIn long
     */
    public void setQuantity(long quantityIn) {
        this.quantity = quantityIn;
    }

    /**
     * The price on which this Fill occurred.
     * @return this.price BigDecimal
     */
    public BigDecimal getPrice() {
        return this.price;
    }

    /**
     * The price on which this Fill occurred.
     * @param priceIn BigDecimal
     */
    public void setPrice(BigDecimal priceIn) {
        this.price = priceIn;
    }

    /**
     *
     * @return this.executionCommission BigDecimal
     */
    public BigDecimal getExecutionCommission() {
        return this.executionCommission;
    }

    /**
     *
     * @param executionCommissionIn BigDecimal
     */
    public void setExecutionCommission(BigDecimal executionCommissionIn) {
        this.executionCommission = executionCommissionIn;
    }

    /**
     *
     * @return this.clearingCommission BigDecimal
     */
    public BigDecimal getClearingCommission() {
        return this.clearingCommission;
    }

    /**
     *
     * @param clearingCommissionIn BigDecimal
     */
    public void setClearingCommission(BigDecimal clearingCommissionIn) {
        this.clearingCommission = clearingCommissionIn;
    }

    /**
     *
     * @return this.fee BigDecimal
     */
    public BigDecimal getFee() {
        return this.fee;
    }

    /**
     *
     * @param feeIn BigDecimal
     */
    public void setFee(BigDecimal feeIn) {
        this.fee = feeIn;
    }

    public Order getOrder() {
        return this.order;
    }

    public void setOrder(Order orderIn) {
        this.order = orderIn;
    }

    @Override
    public String toString() {

        StringBuilder buffer = new StringBuilder();
        buffer.append(getSide());
        buffer.append(",");
        buffer.append(getQuantity());
        Date extDateTime = getExtDateTime();
        if (extDateTime != null) {
            buffer.append(",");
            DateTimeUtil.formatLocalZone(extDateTime.toInstant(), buffer);
        }

        if (getOrder() != null) {
            buffer.append(",");
            buffer.append(getOrder().getSecurity());
        }

        if (getOrder() != null) {
            buffer.append(",");
            buffer.append(getOrder().getStrategy());
        }

        buffer.append(",price=");
        buffer.append(getPrice());

        if (getOrder() != null) {
            buffer.append(",");
            buffer.append(getOrder().getSecurity().getSecurityFamily().getCurrency());
        }

        buffer.append(",extId=");
        buffer.append(getExtId());

        return buffer.toString();
    }
}
