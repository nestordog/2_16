/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.future;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.util.DateTimePatterns;
import ch.algotrader.util.RoundUtil;

/**
 * Utility class to generate symbol, isin and ric for {@link ch.algotrader.entity.security.Future Futures}.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class FutureSymbol {

    private static final String[] monthEnc = { "F", "G", "H", "J", "K", "M", "N", "Q", "U", "V", "X", "Z" };
    private static final String[] yearEnc = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J" };

    /**
     * Generates the symbol for the specified {@link ch.algotrader.entity.security.FutureFamily}.
     *
     * Example
     *     <table>
     *     <tr><td><b>Pattern</b></td><td><b>Description</b></td><td><b>Example</b></td></tr>
     *     <tr><td>N</td><td>Name</td><td>CrudeOil</td></tr>
     *     <tr><td>CR</td><td>SymbolRoot</td><td>CL</td></tr>
     *     <tr><td>C</td><td>Currency</td><td>USD</td></tr>
     *     <tr><td>CS</td><td>ContractSize</td><td>1000</td></tr>
     *     <tr><td>M</td><td>Month 1-digit</td><td>6</td></tr>
     *     <tr><td>MM</td><td>Month 2-digit</td><td>06</td></tr>
     *     <tr><td>MMM</td><td>Month Short</td><td>JUN</td></tr>
     *     <tr><td>MMMM</td><td>Month Long</td><td>June</td></tr>
     *     <tr><td>YY</td><td>Year 2-digit</td><td>16</td></tr>
     *     <tr><td>YYYY</td><td>Year 4-digit</td><td>2016</td></tr>
     *     </table>
     */
    public static String getSymbol(SecurityFamily family, LocalDate expiration, String pattern) {

        String[] placeHolders = new String[] {
                "N",
                "SR",
                "CS",
                "C",
                "MMMM",
                "MMM",
                "MM",
                "MR",
                "YYYY",
                "YY",
                "YR"};

        String[] values = new String[] {
                family.getName(),
                family.getSymbolRoot(),
                RoundUtil.getBigDecimal(family.getContractSize(), 0).toString(),
                family.getCurrency().toString(),
                DateTimePatterns.MONTH_LONG.format(expiration).toUpperCase(),
                DateTimePatterns.MONTH_SHORT.format(expiration).toUpperCase(),
                DateTimePatterns.MONTH_2_DIGIT.format(expiration).toUpperCase(),
                monthEnc[expiration.getMonth().getValue() - 1],
                DateTimePatterns.YEAR_4_DIGIT.format(expiration),
                DateTimePatterns.YEAR_2_DIGIT.format(expiration),
                yearEnc[expiration.getYear() % 10]};

        return StringUtils.replaceEach(pattern, placeHolders, values);
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
     * Converts from future month symbol to month ordinal number from {@code 0} to {@code 11}.
     * @param month future month symbol
     * @return value from {@code 0} to {@code 11} in case of a successful symbol conversion,
     *  {@code -1} if case of a failure.
     */
    public static int convertMonth(final String month) {
        return Arrays.binarySearch(monthEnc, month);
    }

    /**
     * Converts from future year symbol to year ordinal number from  {@code 0} to {@code 9}
     * @param year future year symbol
     * @return value from {@code 0} to {@code 9} in case of a successful symbol conversion,
     *  {@code -1} if case of a failure.
     */
    public static int convertYear(final String year) {
        return Arrays.binarySearch(yearEnc, year);
    }
}
