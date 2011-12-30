package com.algoTrader.entity;

import java.math.BigDecimal;

import com.algoTrader.ServiceLocator;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.util.RoundUtil;

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

        Currency portfolioBaseCurrency = ServiceLocator.instance().getConfiguration().getPortfolioBaseCurrency();
        double exchangeRate = ServiceLocator.instance().getLookupService().getForexRateDouble(getCurrency(), portfolioBaseCurrency);

        return getAmountDouble() * exchangeRate;
    }

    @Override
    public String toString() {

        return getCurrency() + " " + getAmount();
    }
}
