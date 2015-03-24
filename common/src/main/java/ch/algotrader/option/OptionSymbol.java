/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.option;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang.StringUtils;

import ch.algotrader.entity.security.OptionFamily;
import ch.algotrader.enumeration.OptionType;
import ch.algotrader.util.BaseConverterUtil;

/**
 * Utility class to generate symbol, isin and ric for {@link ch.algotrader.entity.security.Option Options}.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class OptionSymbol {

    private static final String[] monthCallEnc = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L" };
    private static final String[] monthPutEnc = { "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X" };
    private static final String[] yearEnc = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J" };
    private static final SimpleDateFormat dayMonthYearFormat = new SimpleDateFormat("dd/MMM/yy");
    private static final SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMM/yy");
    private static final SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
    private static final SimpleDateFormat weekFormat = new SimpleDateFormat("W");

    /**
     * Generates the symbole for the specified {@link ch.algotrader.entity.security.OptionFamily}.
     */
    public static String getSymbol(OptionFamily family, Date expiration, OptionType type, BigDecimal strike, boolean includeDay) {

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(expiration);

        String week = "";
        if (family.isWeekly()) {
            week = weekFormat.format(cal.getTime());
        }

        StringBuilder buffer = new StringBuilder();
        buffer.append(family.getSymbolRoot());
        buffer.append(week);
        buffer.append(" ");
        buffer.append(includeDay ? dayMonthYearFormat.format(cal.getTime()) : monthYearFormat.format(cal.getTime()).toUpperCase());
        buffer.append("-");
        buffer.append(type.toString().substring(0, 1));
        buffer.append(" ");
        buffer.append(strike);

        return buffer.toString();
    }

    /**
     * Generates the ISIN for the specified {@link ch.algotrader.entity.security.OptionFamily}.
     */
    public static String getIsin(OptionFamily family, Date expiration, OptionType type, BigDecimal strike) {

        Calendar cal = new GregorianCalendar();
        cal.setTime(expiration);

        String week = "";
        if (family.isWeekly()) {
            week = weekFormat.format(cal.getTime());
        }

        String month;
        if (OptionType.CALL.equals(type)) {
            month = monthCallEnc[cal.get(Calendar.MONTH)];
        } else {
            month = monthPutEnc[cal.get(Calendar.MONTH)];
        }

        int yearIndex = cal.get(Calendar.YEAR) % 10;
        String year = yearEnc[yearIndex];

            String strike36 = BaseConverterUtil.toBase36(strike.multiply(new BigDecimal(10)).intValue());
            String strikeVal = strike.scale() + StringUtils.leftPad(strike36, 4, "0");

        StringBuilder buffer = new StringBuilder();
        buffer.append("1O");
        buffer.append(family.getIsinRoot() != null ? family.getIsinRoot() : family.getSymbolRoot());
        buffer.append(week);
        buffer.append(month);
        buffer.append(year);
        buffer.append(strikeVal);

        return buffer.toString();
    }

    /**
     * Generates the RIC for the specified {@link ch.algotrader.entity.security.OptionFamily}.
     */
    public static String getRic(OptionFamily family, Date expiration, OptionType type, BigDecimal strike) {

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(expiration);

        StringBuilder buffer = new StringBuilder();
        buffer.append(family.getRicRoot() != null ? family.getRicRoot() : family.getSymbolRoot());
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

    /**
     * Gets the week number based on the specified {@code symbol}
     */
    public static int getWeek(String symbol) {

        return Integer.parseInt(symbol.substring(0, 1));
    }

    /**
     * Gets the underlying symbole based on the specified {@code symbol}
     */
    public static String getUnderlying(String symbol) {

        return symbol.substring(2, 5);
    }

    /**
     * Gets the {@link OptionType} based on the specified {@code symbol}
     */
    public static OptionType getOptionType(String symbol) {

        String month = symbol.substring(5, 6);
        int callIndex = Arrays.binarySearch(monthCallEnc, month);

        return callIndex > 0 ? OptionType.CALL : OptionType.PUT;
    }

    /**
     * Gets the month number based on the specified {@code symbol}
     */
    public static int getMonth(String symbol) {

        String month = symbol.substring(5, 6);
        int callIndex = Arrays.binarySearch(monthCallEnc, month);
        int putIndex = Arrays.binarySearch(monthPutEnc, month);

        return Math.max(callIndex, putIndex) + 1;
    }

    /**
     * Gets the year number based on the specified {@code symbol}
     */
    public static int getYear(String symbol) {

        String year = symbol.substring(6, 7);
        return Arrays.binarySearch(yearEnc, year) + 2010;
    }

    /**
     * Gets the strike based on the specified {@code symbol}
     */
    public static int getStrike(String symbol) {

        String strike = symbol.substring(7, 12);

        return BaseConverterUtil.fromBase36(strike);
    }
}
