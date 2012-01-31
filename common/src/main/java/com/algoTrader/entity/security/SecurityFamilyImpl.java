package com.algoTrader.entity.security;

import java.text.ChoiceFormat;

public class SecurityFamilyImpl extends SecurityFamily {

    private static final long serialVersionUID = -2318908709333325986L;

    @Override
    public String toString() {

        return getName();
    }

    @Override
    public double getTickSize(double price, boolean upwards) {

        // add or subtract a very small amount to the price to get the tickSize just above or below the trigger
        double adjustedPrice = upwards ? price * 1.00000000001 : price / 1.00000000001;
        return Double.valueOf(new ChoiceFormat(getTickSizePattern()).format(adjustedPrice));
    }
}
