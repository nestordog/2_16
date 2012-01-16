package com.algoTrader.entity.security;

import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.util.RoundUtil;

public class SecurityFamilyImpl extends SecurityFamily {

    private static final long serialVersionUID = -2318908709333325986L;

    private static @Value("${misc.portfolioDigits}") int portfolioDigits;

    @Override
    public String toString() {

        return getName();
    }

    @Override
    public int getScale() {

        int digits = RoundUtil.getDigits(getTickSize());
        return Math.max(digits, this.portfolioDigits);
    }
}
