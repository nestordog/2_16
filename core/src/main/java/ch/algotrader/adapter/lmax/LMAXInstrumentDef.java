/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.adapter.lmax;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class LMAXInstrumentDef {

    private String name;
    private String id;
    private String symbol;
    private BigDecimal contractMultiplier;
    private BigDecimal tickSize;
    private BigDecimal tickValue;
    private Date effectiveDate;
    private Date expiryDate;
    private String quotedCurrency;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(final String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getContractMultiplier() {
        return contractMultiplier;
    }

    public void setContractMultiplier(final BigDecimal contractMultiplier) {
        this.contractMultiplier = contractMultiplier;
    }

    public BigDecimal getTickSize() {
        return tickSize;
    }

    public void setTickSize(final BigDecimal tickSize) {
        this.tickSize = tickSize;
    }

    public BigDecimal getTickValue() {
        return tickValue;
    }

    public void setTickValue(final BigDecimal tickValue) {
        this.tickValue = tickValue;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(final Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(final Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getQuotedCurrency() {
        return quotedCurrency;
    }

    public void setQuotedCurrency(final String quotedCurrency) {
        this.quotedCurrency = quotedCurrency;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[");
        sb.append("name='").append(name).append('\'');
        sb.append(", id='").append(id).append('\'');
        sb.append(", symbol='").append(symbol).append('\'');
        sb.append(", contractMultiplier=").append(contractMultiplier);
        sb.append(", tickSize=").append(tickSize);
        sb.append(", tickValue=").append(tickValue);
        sb.append(", effectiveDate=").append(effectiveDate);
        sb.append(", expiryDate=").append(expiryDate);
        sb.append(", quotedCurrency='").append(quotedCurrency).append('\'');
        sb.append(']');
        return sb.toString();
    }

}
