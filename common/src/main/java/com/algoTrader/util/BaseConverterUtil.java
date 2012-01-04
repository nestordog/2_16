package com.algoTrader.util;

public class BaseConverterUtil {

    private static final String baseDigits = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static String toBase36(int decimalNumber) {
        return fromDecimalToOtherBase(36, decimalNumber);
    }

    public static int fromBase36(String base36Number) {
        return fromOtherBaseToDecimal(36, base36Number);
    }

    private static String fromDecimalToOtherBase(int base, int decimalNumber) {
        String tempVal = decimalNumber == 0 ? "0" : "";
        int mod = 0;

        while (decimalNumber != 0) {
            mod = decimalNumber % base;
            tempVal = baseDigits.substring(mod, mod + 1) + tempVal;
            decimalNumber = decimalNumber / base;
        }

        return tempVal;
    }

    private static int fromOtherBaseToDecimal(int base, String number) {
        int iterator = number.length();
        int returnValue = 0;
        int multiplier = 1;

        while (iterator > 0) {
            returnValue = returnValue + (baseDigits.indexOf(number.substring(iterator - 1, iterator)) * multiplier);
            multiplier = multiplier * base;
            --iterator;
        }
        return returnValue;
    }
}
