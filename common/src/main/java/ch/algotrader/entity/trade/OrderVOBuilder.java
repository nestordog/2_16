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

import java.util.Date;

import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.TIF;

/**
 * Factory for OrderVOs
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public abstract class OrderVOBuilder<O> {

    private String intId;

    private Side side;

    private long quantity;

    private TIF tif;

    private Date tifDateTime;

    private long exchangeId;

    private long securityId;

    private long accountId;

    private long strategyId;

    protected String getIntId() {
        return this.intId;
    }

    protected Side getSide() {
        return this.side;
    }

    protected long getQuantity() {
        return this.quantity;
    }

    protected TIF getTif() {
        return this.tif;
    }

    protected Date getTifDateTime() {
        return this.tifDateTime;
    }

    protected long getExchangeId() {
        return this.exchangeId;
    }

    protected long getSecurityId() {
        return this.securityId;
    }

    protected long getAccountId() {
        return this.accountId;
    }

    protected long getStrategyId() {
        return this.strategyId;
    }

    public OrderVOBuilder<O> setIntId(final String intId) {
        this.intId = intId;
        return this;
    }

    public OrderVOBuilder<O> setSide(final Side side) {
        this.side = side;
        return this;
    }

    public OrderVOBuilder<O> setQuantity(final long quantity) {
        this.quantity = quantity;
        return this;
    }

    public OrderVOBuilder<O> setTif(final TIF tif) {
        this.tif = tif;
        return this;
    }

    public OrderVOBuilder<O> setTifDateTime(final Date tifDateTime) {
        this.tifDateTime = tifDateTime;
        return this;
    }

    public OrderVOBuilder<O> setExchangeId(final long exchangeId) {
        this.exchangeId = exchangeId;
        return this;
    }

    public OrderVOBuilder<O> setSecurityId(final long securityId) {
        this.securityId = securityId;
        return this;
    }

    public OrderVOBuilder<O> setAccountId(final long accountId) {
        this.accountId = accountId;
        return this;
    }

    public OrderVOBuilder<O> setStrategyId(final long strategyId) {
        this.strategyId = strategyId;
        return this;
    }

    abstract public O build();

}
