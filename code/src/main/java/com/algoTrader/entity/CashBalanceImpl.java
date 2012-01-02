package com.algoTrader.entity;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.ServiceLocator;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.util.RoundUtil;

public class CashBalanceImpl extends CashBalance {

    private static final long serialVersionUID = 735304281192548146L;

    private static @Value("#{T(com.algoTrader.enumeration.Currency).fromString('${portfolioBaseCurrency}')}") Currency portfolioBaseCurrency;

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

        double exchangeRate = ServiceLocator.instance().getLookupService().getForexRateDouble(getCurrency(), this.portfolioBaseCurrency);

        return getAmountDouble() * exchangeRate;
    }

    @Override
    public String toString() {

        return getCurrency() + " " + getAmount();
    }
}
