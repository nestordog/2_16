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
package ch.algotrader.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.ConfigLocator;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.enumeration.TransactionType;
import ch.algotrader.util.ObjectUtil;
import ch.algotrader.util.RoundUtil;
import ch.algotrader.vo.CurrencyAmountVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class TransactionImpl extends Transaction {

    private static final long serialVersionUID = -1528408715199422753L;

    private Double value = null; // cache getValueDouble because getValue get's called very often

    @Override
    public BigDecimal getGrossValue() {

        if (getSecurity() != null) {
            int scale = getSecurity().getSecurityFamily().getScale();
            return RoundUtil.getBigDecimal(getGrossValueDouble(), scale);
        } else {
            CommonConfig commonConfig = ConfigLocator.instance().getCommonConfig();
            return RoundUtil.getBigDecimal(getGrossValueDouble(), commonConfig.getPortfolioDigits());
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
            CommonConfig commonConfig = ConfigLocator.instance().getCommonConfig();
            return RoundUtil.getBigDecimal(getNetValueDouble(), commonConfig.getPortfolioDigits());
        }
    }

    @Override
    public double getNetValueDouble() {

        return getGrossValueDouble() - getTotalChargesDouble();
    }

    @Override
    public BigDecimal getTotalCharges() {

        CommonConfig commonConfig = ConfigLocator.instance().getCommonConfig();
        return RoundUtil.getBigDecimal(getTotalChargesDouble(), commonConfig.getPortfolioDigits());
    }

    @Override
    public double getTotalChargesDouble() {

        double totalCharges = 0.0;

        if (getExecutionCommission() != null) {
            totalCharges += getExecutionCommission().doubleValue();
        }

        if (getClearingCommission() != null) {
            totalCharges += getClearingCommission().doubleValue();
        }

        if (getFee() != null) {
            totalCharges += getFee().doubleValue();
        }

        return totalCharges;
    }

    @Override
    public Collection<CurrencyAmountVO> getAttributions() {

        Collection<CurrencyAmountVO> list = new ArrayList<CurrencyAmountVO>();
        if (getSecurity() instanceof Forex) {

            // gross transaction value is booked in transaction currency
            list.add(new CurrencyAmountVO(getCurrency(), getGrossValue()));

            // execution commission is booked in baseCurrency (this is IB specific!)
            if (getExecutionCommission() != null) {
                CommonConfig commonConfig = ConfigLocator.instance().getCommonConfig();
                list.add(new CurrencyAmountVO(commonConfig.getPortfolioBaseCurrency(), getExecutionCommission().negate()));
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

        // for BASE only CREDIT and DEBIT are performance relevant (REBALANCE do not affect NetLiqValue and FEES, REFUND etc. are part of the performance)
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

        StringBuffer buffer = new StringBuffer();

        if (isTrade()) {

            buffer.append(getType());
            buffer.append(",");
            buffer.append(getQuantity());
            buffer.append(",");
            buffer.append(getSecurity());
            buffer.append(",price=");
            buffer.append(getPrice());
            buffer.append(",");
            buffer.append(getCurrency());
            buffer.append(",totalCharges=");
            buffer.append(getTotalCharges());
            buffer.append(",strategy=");
            buffer.append(getStrategy());

        } else {

            buffer.append(getType());
            buffer.append(",amount=");

            if (getQuantity() < 0) {
                buffer.append("-");
            }

            buffer.append(getPrice());
            buffer.append(",");
            buffer.append(getCurrency());
            buffer.append(",strategy=");
            buffer.append(getStrategy());

            if (getDescription() != null) {
                buffer.append(",");
                buffer.append(getDescription());
            }
        }

        return buffer.toString();
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj instanceof Transaction) {
            Transaction that = (Transaction) obj;
            return ObjectUtil.equalsNonZero(this.getId(), that.getId());
        } else {
            return false;
        }
    }

    /**
     * WARNING: hashCode changes when inserting a new transaction into the database, therefore uncommitted transaction should not be added to a collection
     */
    @Override
    public int hashCode() {

        int hash = 17;
        hash = hash * 37 + this.getId();
        return hash;
    }
}
