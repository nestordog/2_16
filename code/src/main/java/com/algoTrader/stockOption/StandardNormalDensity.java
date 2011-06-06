package com.algoTrader.stockOption;

public class StandardNormalDensity {

    public static double n(double input) {

        return 1.0 / Math.sqrt(2.0 * Math.PI) * Math.exp(-(input * input) / 2.0);
    }
}
