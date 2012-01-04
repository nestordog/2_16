package com.algoTrader.util;

import java.math.BigDecimal;

public class CustomBigDecimal extends BigDecimal {

    private static final long serialVersionUID = -547978502941785518L;

    public CustomBigDecimal(String value) {
        super(!"".equals(value) ? value : "0");
    }
}
