package com.algoTrader.entity.security;

import com.algoTrader.ServiceLocator;
import com.algoTrader.util.RoundUtil;

public class SecurityFamilyImpl extends SecurityFamily {

    private static final long serialVersionUID = -2318908709333325986L;

    @Override
    public String toString() {

        return getName();
    }

    @Override
    public int getScale() {

        int digits = RoundUtil.getDigits(getTickSize());
        int portfolioDigits = ServiceLocator.instance().getConfiguration().getPortfolioDigits();
        return Math.max(digits, portfolioDigits);
    }
}
