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
package ch.algotrader.entity.strategy;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;

import ch.algotrader.ServiceLocator;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.util.ObjectUtil;
import ch.algotrader.util.RoundUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CashBalanceImpl extends CashBalance {

    private static final long serialVersionUID = 735304281192548146L;

    private static @Value("#{T(ch.algotrader.enumeration.Currency).fromString('${misc.portfolioBaseCurrency}')}") Currency portfolioBaseCurrency;

    @Override
    public double getAmountDouble() {

        return getAmount().doubleValue();
    }

    @Override
    public BigDecimal getAmountBase() {

        return RoundUtil.getBigDecimal(getAmountBaseDouble());
    }

    @Override
    public double getAmountBaseDouble() {

        double exchangeRate = ServiceLocator.instance().getLookupService().getForexRateDouble(getCurrency(), portfolioBaseCurrency);

        return getAmountDouble() * exchangeRate;
    }

    @Override
    public String toString() {

        return getStrategy() + "," + getCurrency() + "," + getAmount();
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj instanceof CashBalance) {
            CashBalance that = (CashBalance) obj;
            return ObjectUtil.equalsNonNull(this.getStrategy(), that.getStrategy()) &&
                    ObjectUtil.equalsNonNull(this.getCurrency(), that.getCurrency());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {

        int hash = 17;
        hash = hash * 37 + ObjectUtil.hashCode(getStrategy());
        hash = hash * 37 + ObjectUtil.hashCode(getCurrency());
        return hash;
    }
}
