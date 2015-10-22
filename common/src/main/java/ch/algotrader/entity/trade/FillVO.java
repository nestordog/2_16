/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.entity.trade;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import ch.algotrader.enumeration.Side;
import ch.algotrader.util.DateTimeUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class FillVO implements Serializable {

    private static final long serialVersionUID = 1619681349145226990L;

    private final String orderIntId;

    private final String extId;

    private final Date dateTime;

    private final Date extDateTime;

    private final long sequenceNumber;

    private final Side side;

    private final long quantity;

    private final BigDecimal price;

    private final BigDecimal executionCommission;

    private final BigDecimal clearingCommission;

    private final BigDecimal fee;

    public FillVO(final String orderIntId, final String extId, final Date dateTime, final Date extDateTime, final long sequenceNumber, final Side side, final long quantity,
                  final BigDecimal price, final BigDecimal executionCommission, final BigDecimal clearingCommission, final BigDecimal fee) {
        this.orderIntId = orderIntId;
        this.extId = extId;
        this.dateTime = dateTime;
        this.extDateTime = extDateTime;
        this.sequenceNumber = sequenceNumber;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.executionCommission = executionCommission;
        this.clearingCommission = clearingCommission;
        this.fee = fee;
    }

    public FillVO(final String orderIntId, final String extId, final Date dateTime, final Date extDateTime, final long sequenceNumber, final Side side, final long quantity, final BigDecimal price) {
        this.orderIntId = orderIntId;
        this.extId = extId;
        this.dateTime = dateTime;
        this.extDateTime = extDateTime;
        this.sequenceNumber = sequenceNumber;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.executionCommission = null;
        this.clearingCommission = null;
        this.fee = null;
    }

    public FillVO(final String orderIntId, final Date dateTime, final Side side, final long quantity, final BigDecimal price) {
        this.orderIntId = orderIntId;
        this.extId = null;
        this.dateTime = dateTime;
        this.extDateTime = null;
        this.sequenceNumber = 0L;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.executionCommission = null;
        this.clearingCommission = null;
        this.fee = null;
    }

    /**
     * Internal order Id
     */
    public String getOrderIntId() {
        return orderIntId;
    }

    /**
     * External Fill Id assigned by the external Broker
     */
    public String getExtId() {
        return this.extId;
    }

    /**
     * The {@code dateTime} the Fill was received by the system.
     */
    public Date getDateTime() {
        return this.dateTime;
    }

    /**
     * The external {@code dateTime} of the Fill as assigned by the external Broker.
     */
    public Date getExtDateTime() {
        return this.extDateTime;
    }

    /**
     * the sequence number of the corresponding broker specific message (e.g. fix sequence number)
     */
    public long getSequenceNumber() {
        return this.sequenceNumber;
    }

    /**
     * {@code BUY} or {@code SELL}
     */
    public Side getSide() {
        return this.side;
    }

    /**
     * The quantity of this Fill.
     * @return this.quantity long
     */
    public long getQuantity() {
        return this.quantity;
    }

    /**
     * The price on which this Fill occurred.
     * @return this.price BigDecimal
     */
    public BigDecimal getPrice() {
        return this.price;
    }

    public BigDecimal getExecutionCommission() {
        return this.executionCommission;
    }

    public BigDecimal getClearingCommission() {
        return this.clearingCommission;
    }

    public BigDecimal getFee() {
        return this.fee;
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
        buffer.append(",price=");
        buffer.append(getPrice());
        buffer.append(",extId=");
        buffer.append(getExtId());
        buffer.append(",orderIntId=");
        buffer.append(getOrderIntId());

        return buffer.toString();
    }
}
