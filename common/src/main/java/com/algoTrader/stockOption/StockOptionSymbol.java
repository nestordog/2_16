package com.algoTrader.stockOption;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang.StringUtils;

import com.algoTrader.entity.security.SecurityFamily;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.util.BaseConverterUtil;

public class StockOptionSymbol {

    private static final String[] monthCallEnc = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L" };
    private static final String[] monthPutEnc = { "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X" };
    private static final String[] yearEnc = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J" };
    private static SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
    private static SimpleDateFormat dayFormat = new SimpleDateFormat("dd");

    public static String getSymbol(SecurityFamily family, Date expiration, OptionType type, BigDecimal strike) {

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(expiration);

        StringBuffer buffer = new StringBuffer();
        buffer.append(family.getName());
        buffer.append(" ");
        buffer.append(monthFormat.format(cal.getTime()).toUpperCase());
        buffer.append("/");
        buffer.append((cal.get(Calendar.YEAR) + "-").substring(2));
        buffer.append(type.toString().substring(0, 1));
        buffer.append(" ");
        buffer.append(strike);
        buffer.append(" ");
        buffer.append(family.getContractSize());

        return buffer.toString();
    }

    public static String getIsin(SecurityFamily family, Date expiration, OptionType type, BigDecimal strike) {

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

            String strike36 = BaseConverterUtil.toBase36(strike.multiply(new BigDecimal(10)).intValue());
            String strikeVal = strike.scale() + StringUtils.leftPad(strike36, 4, "0");

        StringBuffer buffer = new StringBuffer();
        buffer.append(week);
        buffer.append(family.getName());
        buffer.append(month);
        buffer.append(year);
        buffer.append(strikeVal);

        return buffer.toString();
    }

    public static String getRic(SecurityFamily family, Date expiration, OptionType type, BigDecimal strike) {

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(expiration);

        StringBuffer buffer = new StringBuffer();
        buffer.append(family.getRicRoot());
        if (OptionType.CALL.equals(type)) {
            buffer.append(monthCallEnc[cal.get(Calendar.MONTH)]);
        } else {
            buffer.append(monthPutEnc[cal.get(Calendar.MONTH)]);
        }
        buffer.append(dayFormat.format(cal.getTime()));
        buffer.append((cal.get(Calendar.YEAR) + "").substring(2));
        buffer.append(StringUtils.leftPad(String.valueOf((int) (strike.doubleValue() * 100)), 5, "0"));
        buffer.append(".U");

        return buffer.toString();
    }

    public static int getWeek(String symbol) {

        return Integer.parseInt(symbol.substring(0, 1));
    }

    public static String getUnderlying(String symbol) {

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
