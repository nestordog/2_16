package com.algoTrader.util;

import java.math.BigDecimal;

public class RoundUtil {

    public static BigDecimal roundTo5Cent(BigDecimal decimal) {

        double rounded = Math.round(decimal.doubleValue() * 20.0) / 20.0;
        return getBigDecimal(rounded);
    }

    public static BigDecimal roundTo10Cent(BigDecimal decimal) {

        double rounded = Math.round(decimal.doubleValue() * 10.0) / 10.0;
        return getBigDecimal(rounded);
    }

    public static BigDecimal getBigDecimal(double value) {

        if (!Double.isNaN(value)) {
            BigDecimal decimal = new BigDecimal(value);
            return decimal.setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            return null;
        }
    }
}
