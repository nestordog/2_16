package com.algoTrader.util;

import java.math.BigDecimal;

public class RoundUtil {

    public static BigDecimal roundTo5Cent(BigDecimal decimal) {

        double rounded = Math.round(decimal.doubleValue() * 20.0) / 20.0;
        return new BigDecimal(rounded).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
