package com.algoTrader.util;

import java.math.BigDecimal;

public class BigDecimalUtil {

    public static BigDecimal getBigDecimal(double value) {

        BigDecimal decimal = new BigDecimal(value);
        return decimal.setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
