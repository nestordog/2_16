package com.algoTrader.util;

import java.math.BigDecimal;

import org.apache.commons.math.util.MathUtils;

import com.algoTrader.enumeration.OptionType;

public class RoundUtil {

    public static BigDecimal roundTo10Cent(BigDecimal decimal) {

        double rounded = Math.round(decimal.doubleValue() * 10.0) / 10.0;
        return getBigDecimal(rounded);
    }

    public static double roundToNextN(double value, double n) {

        return MathUtils.round((value) / n, 0) * n;
    }

    public static BigDecimal roundToNextN(BigDecimal value, double n) {

        return RoundUtil.getBigDecimal(roundToNextN(value.doubleValue(), n));
    }

    public static double roundToNextN(double value, double n, int roundingMethod) {

        return MathUtils.round((value) / n, 0, roundingMethod) * n;
    }

    public static BigDecimal roundToNextN(BigDecimal value, double n, int roundingMethod) {

        return RoundUtil.getBigDecimal(roundToNextN(value.doubleValue(), n, roundingMethod));
    }

    public static BigDecimal getBigDecimal(double value) {

        if (!Double.isNaN(value)) {
            BigDecimal decimal = new BigDecimal(value);
            return decimal.setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            return null;
        }
    }

    public static BigDecimal roundStockOptionStrikeToNextN(BigDecimal spot, double n, OptionType type) {

        if (OptionType.CALL.equals(type)) {
            // increase by strikeOffset and round to upper n
            return roundToNextN(spot, n, BigDecimal.ROUND_CEILING);
        } else {
            // reduce by strikeOffset and round to lower n
            return roundToNextN(spot, n, BigDecimal.ROUND_FLOOR);
        }
    }
}
