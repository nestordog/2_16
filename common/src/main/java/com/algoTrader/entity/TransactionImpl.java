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
package com.algoTrader.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.entity.security.Forex;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.vo.CurrencyAmountVO;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class TransactionImpl extends Transaction {

    private static final long serialVersionUID = -1528408715199422753L;

    private static @Value("${misc.portfolioDigits}") int portfolioDigits;
    private static @Value("#{T(com.algoTrader.enumeration.Currency).fromString('${misc.portfolioBaseCurrency}')}") Currency portfolioBaseCurrency;

    private Double value = null; // cache getValueDouble because getValue get's called very often

    @Override
    public BigDecimal getGrossValue() {

        if (getSecurity() != null) {
            int scale = getSecurity().getSecurityFamily().getScale();
            return RoundUtil.getBigDecimal(getGrossValueDouble(), scale);
        } else {
            return RoundUtil.getBigDecimal(getGrossValueDouble(), portfolioDigits);
        }

    }

    @Override
    public double getGrossValueDouble() {

        if (this.value == null) {
            if (isTrade()) {
                this.value = -getQuantity() * getSecurity().getSecurityFamily().getContractSize() * getPrice().doubleValue();
            } else {
                this.value = getQuantity() * getPrice().doubleValue();
            }
        }

        return this.value;
    }

    @Override
    public BigDecimal getNetValue() {

        if (getSecurity() != null) {
            int scale = getSecurity().getSecurityFamily().getScale();
            return RoundUtil.getBigDecimal(getNetValueDouble(), scale);
        } else {
            return RoundUtil.getBigDecimal(getNetValueDouble(), portfolioDigits);
        }
    }

    @Override
    public double getNetValueDouble() {

        return getGrossValueDouble() - getTotalCommissionDouble();
    }

    @Override
    public double getTotalCommissionDouble() {

        return (getExecutionCommission() != null ? getExecutionCommission().doubleValue() : 0.0) +
                (getClearingCommission() != null ? getClearingCommission().doubleValue() : 0.0);
    }

    @Override
    public BigDecimal getTotalCommission() {

        return RoundUtil.getBigDecimal(getTotalCommissionDouble(), portfolioDigits);
    }

    @Override
    public Collection<CurrencyAmountVO> getAttributions() {

        Collection<CurrencyAmountVO> list = new ArrayList<CurrencyAmountVO>();
        if (getSecurity() instanceof Forex) {

            // gross transaction value is booked in transaction currency
            list.add(new CurrencyAmountVO(getCurrency(), getGrossValue()));

            // execution commission is booked in baseCurrency (this is IB specific!)
            if (getExecutionCommission() != null) {
                list.add(new CurrencyAmountVO(portfolioBaseCurrency, getExecutionCommission().negate()));
            }

            // clearing commission is booked in transaction currency
            if (getClearingCommission() != null) {
                list.add(new CurrencyAmountVO(getCurrency(), getClearingCommission().negate()));
            }
        } else {

            // the entire transaction (price + commission) is booked in transaction currency
            list.add(new CurrencyAmountVO(getCurrency(), getNetValue()));
        }
        return list;
    }

    @Override
    public boolean isTrade() {

        if (TransactionType.BUY.equals(getType()) ||
                TransactionType.SELL.equals(getType()) ||
                TransactionType.EXPIRATION.equals(getType())  ||
                TransactionType.TRANSFER.equals(getType())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isPerformanceRelevant() {

        // for BASE only save PortfolioValue for CREDIT and DEBIT (REBALANCE do not affect NetLiqValue and FEES, REFUND etc. are part of the performance)
        if (getStrategy().isBase()) {
            if (TransactionType.CREDIT.equals(getType()) || TransactionType.DEBIT.equals(getType())) {
                return true;
            }

            // for strategies only save PortfolioValue for REBALANCE
        } else {
            if (TransactionType.REBALANCE.equals(getType())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {

        if (isTrade()) {

            //@formatter:off
            return getType()
                + " " + getQuantity()
                + " " + getSecurity()
                + " price: " + getPrice() + " " + getCurrency()
                + " commission: " + getTotalCommission()
                + " strategy: " + getStrategy();
            //@formatter:on

        } else {

            //@formatter:off
            return getType()
                + " amount: " + (getQuantity() < 0 ? "-" : "") + getPrice()+ " " + getCurrency()
                + " strategy: " + getStrategy()
                + (getDescription() != null ? " " + getDescription() : "");
            //@formatter:on
        }
    }
}
