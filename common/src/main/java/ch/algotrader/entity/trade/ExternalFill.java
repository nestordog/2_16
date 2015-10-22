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

import ch.algotrader.entity.Account;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Side;
import ch.algotrader.util.DateTimeUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class ExternalFill implements Serializable {

    private static final long serialVersionUID = -5533585290466610539L;

    private Security security;
    private Currency currency;
    private Date dateTime;
    private Date extDateTime;
    private String extOrderId;
    private String extId;
    private long sequenceNumber;
    private Side side;
    private long quantity;
    private BigDecimal price;
    private BigDecimal executionCommission;
    private BigDecimal clearingCommission;
    private BigDecimal fee;
    private Strategy strategy;
    private Account account;

    public Security getSecurity() {
        return this.security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public Currency getCurrency() {
        return this.currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Date getDateTime() {
        return this.dateTime;
    }

    public void setDateTime(Date dateTimeIn) {
        this.dateTime = dateTimeIn;
    }

    public Date getExtDateTime() {
        return this.extDateTime;
    }

    public void setExtDateTime(Date extDateTimeIn) {
        this.extDateTime = extDateTimeIn;
    }

    public String getExtId() {
        return this.extId;
    }

    public void setExtId(String extIdIn) {
        this.extId = extIdIn;
    }

    public String getExtOrderId() {
        return this.extOrderId;
    }

    public void setExtOrderId(String extOrderId) {
        this.extOrderId = extOrderId;
    }

    public long getSequenceNumber() {
        return this.sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumberIn) {
        this.sequenceNumber = sequenceNumberIn;
    }

    public Side getSide() {
        return this.side;
    }

    public void setSide(Side sideIn) {
        this.side = sideIn;
    }

    public long getQuantity() {
        return this.quantity;
    }

    public void setQuantity(long quantityIn) {
        this.quantity = quantityIn;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public void setPrice(BigDecimal priceIn) {
        this.price = priceIn;
    }

    public BigDecimal getExecutionCommission() {
        return this.executionCommission;
    }

    public void setExecutionCommission(BigDecimal executionCommissionIn) {
        this.executionCommission = executionCommissionIn;
    }

    public BigDecimal getClearingCommission() {
        return this.clearingCommission;
    }

    public void setClearingCommission(BigDecimal clearingCommissionIn) {
        this.clearingCommission = clearingCommissionIn;
    }

    public BigDecimal getFee() {
        return this.fee;
    }

    public void setFee(BigDecimal feeIn) {
        this.fee = feeIn;
    }

    public Strategy getStrategy() {
        return this.strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public Account getAccount() {
        return this.account;
    }

    public void setAccount(Account account) {
        this.account = account;
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

        if (getSecurity() != null) {
            buffer.append(",");
            buffer.append(getSecurity());
        }

        if (getStrategy() != null) {
            buffer.append(",");
            buffer.append(getStrategy());
        }

        buffer.append(",price=");
        buffer.append(getPrice());

        if (getCurrency() != null) {
            buffer.append(",");
            buffer.append(getCurrency());
        } else if (getSecurity() != null) {
            buffer.append(",");
            buffer.append(getSecurity().getSecurityFamily().getCurrency());
        }

        buffer.append(",extId=");
        buffer.append(getExtId());

        return buffer.toString();
    }

}
