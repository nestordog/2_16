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
package ch.algorader.entity.strategy;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;

import ch.algorader.util.RoundUtil;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.strategy.CashBalance;
import com.algoTrader.enumeration.Currency;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CashBalanceImpl extends CashBalance {

    private static final long serialVersionUID = 735304281192548146L;

    private static @Value("#{T(ch.algorader.enumeration.Currency).fromString('${misc.portfolioBaseCurrency}')}") Currency portfolioBaseCurrency;

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
}
