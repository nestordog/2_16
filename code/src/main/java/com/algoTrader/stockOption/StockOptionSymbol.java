package com.algoTrader.stockOption;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang.StringUtils;

import com.algoTrader.entity.security.StockOptionFamily;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.util.BaseConverterUtil;

public class StockOptionSymbol {

    private static final String[] monthCallEnc = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L" };
    private static final String[] monthPutEnc = { "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X" };
    private static final String[] yearEnc = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J" };

    public static String getSymbol(StockOptionFamily family, Date expiration, OptionType type, BigDecimal strike) {

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(expiration);

        //@formatter:off
        String symbol = family.getName() + " " +
            new SimpleDateFormat("MMM").format(cal.getTime()).toUpperCase() + "/" +
            (cal.get(Calendar.YEAR) + "-").substring(2) +
            type.toString().substring(0, 1) + " " +
            strike.intValue() + " " +
            family.getContractSize();
        //@formatter:on

        return symbol;
    }

    public static String getIsin(StockOptionFamily family, Date expiration, OptionType type, BigDecimal strike) {

        int week = 1;

        String month;
        Calendar cal = new GregorianCalendar();
        cal.setTime(expiration);
        if (OptionType.CALL.equals(type)) {
            month = monthCallEnc[cal.get(Calendar.MONTH)];
        } else {
            month = monthPutEnc[cal.get(Calendar.MONTH)];
        }

        int yearIndex = cal.get(Calendar.YEAR) % 10;
        String year = yearEnc[yearIndex];

        String strike36 = BaseConverterUtil.toBase36(strike.intValue());
        String strikeVal = StringUtils.leftPad(strike36, 5, "0");

        StringBuffer buffer = new StringBuffer();
        buffer.append(week);
        buffer.append(family.getName());
        buffer.append(month);
        buffer.append(year);
        buffer.append(strikeVal);

        return buffer.toString();
    }

    public static int getWeek(String symbol) {

        return Integer.parseInt(symbol.substring(0, 1));
    }

    public static String getUnderlaying(String symbol) {

        return symbol.substring(2, 5);
    }

    public static OptionType getOptionType(String symbol) {

        String month = symbol.substring(5, 6);
        int callIndex = Arrays.binarySearch(monthCallEnc, month);

        return callIndex > 0 ? OptionType.CALL : OptionType.PUT;
    }

    public static int getMonth(String symbol) {

        String month = symbol.substring(5, 6);
        int callIndex = Arrays.binarySearch(monthCallEnc, month);
        int putIndex = Arrays.binarySearch(monthPutEnc, month);

        return Math.max(callIndex, putIndex) + 1;
    }

    public static int getYear(String symbol) {

        String year = symbol.substring(6, 7);
        return Arrays.binarySearch(yearEnc, year) + 2010;
    }

    public static int getStrike(String symbol) {

        String strike = symbol.substring(7, 12);

        return BaseConverterUtil.fromBase36(strike);
    }
}
