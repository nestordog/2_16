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
package ch.algotrader.future;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;

import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.util.DateTimePatterns;

/**
 * Utility class to generate symbol, isin and ric for {@link ch.algotrader.entity.security.Future Futures}.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class FutureSymbol {

    private static final String[] monthEnc = { "F", "G", "H", "J", "K", "M", "N", "Q", "U", "V", "X", "Z" };
    private static final String[] yearEnc = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J" };

    /**
     * Generates the symbole for the specified {@link ch.algotrader.entity.security.FutureFamily}.
     */
    public static String getSymbol(SecurityFamily family, LocalDate expiration) {

        StringBuilder buffer = new StringBuilder();
        buffer.append(family.getSymbolRoot());
        buffer.append(" ");
        buffer.append(DateTimePatterns.MONTH_LONG.format(expiration).toUpperCase());
        buffer.append("/");
        final String s = DateTimePatterns.YEAR_4_DIGIT.format(expiration);
        buffer.append(s.substring(s.length() - 2, s.length()));
        return buffer.toString();
    }

    /**
     * Generates the ISIN for the specified {@link ch.algotrader.entity.security.FutureFamily}.
     */
    public static String getIsin(SecurityFamily family, LocalDate expiration) {

        int week = 0;
        Month month = expiration.getMonth();
        int year = expiration.getYear();                    ;

        StringBuilder buffer = new StringBuilder();
        buffer.append(week);
        buffer.append("F");
        buffer.append(family.getIsinRoot() != null ? family.getIsinRoot() : family.getSymbolRoot());
        buffer.append(monthEnc[month.getValue() - 1]);
        buffer.append(yearEnc[year % 10]);
        buffer.append("00000");

        return buffer.toString();
    }

    /**
     * Generates the RIC for the specified {@link ch.algotrader.entity.security.FutureFamily}.
     */
    public static String getRic(SecurityFamily family, LocalDate expiration) {

        Month month = expiration.getMonth();
        int year = expiration.getYear();                    ;

        StringBuilder buffer = new StringBuilder();
        buffer.append(family.getRicRoot() != null ? family.getRicRoot() : family.getSymbolRoot());
        buffer.append(monthEnc[month.getValue() - 1]);
        buffer.append(String.valueOf(year).substring(3));
        buffer.append(":VE");

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
     * Gets the month number based on the specified {@code symbol}
     */
    public static int getMonth(String symbol) {

        String month = symbol.substring(5, 6);
        int index = Arrays.binarySearch(monthEnc, month);

        return index;
    }

    /**
     * Gets the year number based on the specified {@code symbol}
     */
    public static int getYear(String symbol) {

        String year = symbol.substring(6, 7);
        return Arrays.binarySearch(yearEnc, year) + 2010;
    }
}
