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
package ch.algotrader.entity.strategy;

import java.math.BigDecimal;
import java.util.Objects;

import ch.algotrader.ServiceLocator;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.ConfigLocator;
import ch.algotrader.util.RoundUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CashBalanceImpl extends CashBalance {

    private static final long serialVersionUID = 735304281192548146L;

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

        CommonConfig commonConfig = ConfigLocator.instance().getCommonConfig();
        double exchangeRate = ServiceLocator.instance().getLookupService().getForexRateDouble(getCurrency(), commonConfig.getPortfolioBaseCurrency());

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
            return Objects.equals(this.getStrategy(), that.getStrategy()) &&
                    Objects.equals(this.getCurrency(), that.getCurrency());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {

        int hash = 17;
        hash = hash * 37 + Objects.hashCode(getStrategy());
        hash = hash * 37 + Objects.hashCode(getCurrency());
        return hash;
    }
}
