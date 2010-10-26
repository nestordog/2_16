package com.algoTrader.util;

import java.math.BigDecimal;

import org.apache.commons.math.util.MathUtils;

import com.algoTrader.enumeration.OptionType;

public class RoundUtil {

    public static BigDecimal roundTo5Cent(BigDecimal decimal) {

        double rounded = Math.round(decimal.doubleValue() * 20.0) / 20.0;
        return getBigDecimal(rounded);
    }

    public static BigDecimal roundTo10Cent(BigDecimal decimal) {

        double rounded = Math.round(decimal.doubleValue() * 10.0) / 10.0;
        return getBigDecimal(rounded);
    }

    public static BigDecimal roundToNextN(BigDecimal spot, double n, OptionType type) {

        if (OptionType.CALL.equals(type)) {

            // increase by strikeOffset and round to upper n
            return RoundUtil.getBigDecimal(MathUtils.round((spot.doubleValue()) / n, 0, BigDecimal.ROUND_CEILING) * n);
        } else {
            // reduce by strikeOffset and round to lower n
            return RoundUtil.getBigDecimal(MathUtils.round((spot.doubleValue()) / n, 0, BigDecimal.ROUND_FLOOR) * n);
        }
    }

    public static BigDecimal getBigDecimal(double value) {

        if (!Double.isNaN(value)) {
            BigDecimal decimal = new BigDecimal(value);
            return decimal.setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            return null;
        }
    }

    public static double round(double value, int scale) {

        double factor = Math.pow(10, scale);
        return (Math.round(value * factor)) / factor;
    }
}
