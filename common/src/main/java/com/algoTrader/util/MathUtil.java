package com.algoTrader.util;

public class MathUtil {

    public static Double nullSafeAbs(Double a) {
        if (a == null) {
            return null;
        } else {
            return java.lang.Math.abs(a);
        }
    }

    public static Integer nullSafeAbs(Integer a) {
        if (a == null) {
            return null;
        } else {
            return java.lang.Math.abs(a);
        }
    }

    public static Long nullSafeAbs(Long a) {
        if (a == null) {
            return null;
        } else {
            return java.lang.Math.abs(a);
        }
    }

    public static Float nullSafeAbs(Float a) {
        if (a == null) {
            return null;
        } else {
            return java.lang.Math.abs(a);
        }
    }
}
