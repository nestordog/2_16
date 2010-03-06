package com.algoTrader.util;

import java.math.BigDecimal;

import org.apache.commons.math.util.MathUtils;

public class RoundUtil {

    public static BigDecimal roundTo5Cent(BigDecimal decimal) {

        double rounded = Math.round(decimal.doubleValue() * 20.0) / 20.0;
        return getBigDecimal(rounded);
    }

    public static BigDecimal getBigDecimal(double value) {

        BigDecimal decimal = new BigDecimal(value);
        return decimal.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal roundTo50(BigDecimal input) {

        double rounded = MathUtils.round(input.doubleValue()/ 50.0, 0, BigDecimal.ROUND_FLOOR) * 50.0;
        return getBigDecimal(rounded);
    }
}
