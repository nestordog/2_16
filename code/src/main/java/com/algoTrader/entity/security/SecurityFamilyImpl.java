package com.algoTrader.entity.security;

import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.RoundUtil;

public class SecurityFamilyImpl extends SecurityFamily {

    private static final long serialVersionUID = -2318908709333325986L;
    private static final int portfolioDigits = ConfigurationUtil.getBaseConfig().getInt("portfolioDigits");

    @Override
    public String toString() {

        return getName();
    }

    @Override
    public int getScale() {

        int digits = RoundUtil.getDigits(getTickSize());
        return Math.max(digits, portfolioDigits);
    }
}
