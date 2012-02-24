package com.algoTrader.entity.security;

import java.math.BigDecimal;
import java.text.ChoiceFormat;

import com.algoTrader.util.RoundUtil;

public class SecurityFamilyImpl extends SecurityFamily {

    private static final long serialVersionUID = -2318908709333325986L;

    @Override
    public String toString() {

        return getName();
    }

    @Override
    public BigDecimal getTickSize(BigDecimal price, boolean upwards) {

        return RoundUtil.getBigDecimal(getTickSize(price.doubleValue(), upwards), getScale());
    }

    @Override
    public double getTickSize(double price, boolean upwards) {

        // add or subtract a very small amount to the price to get the tickSize just above or below the trigger
        double adjustedPrice = upwards ? price * 1.00000000001 : price / 1.00000000001;
        return Double.valueOf(new ChoiceFormat(getTickSizePattern()).format(adjustedPrice));
    }


    @Override
    public BigDecimal adjustPrice(BigDecimal price, int ticks) {

        if (ticks > 0) {
            for (int i = 0; i < ticks; i++) {
                price = price.add(getTickSize(price, true));
            }
        } else if (ticks < 0) {
            for (int i = 0; i > ticks; i--) {
                price = price.subtract(getTickSize(price, false));
            }
        }
        return price;
    }

    @Override
    public int getSpreadTicks(BigDecimal bid, BigDecimal ask) {

        BigDecimal price = bid;
        int ticks = 0;
        while (price.compareTo(ask) < 0) {
            ticks++;
            adjustPrice(price, 1);
        }
        return ticks;
    }
}
