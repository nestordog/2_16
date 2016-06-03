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
package ch.algotrader.option;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.apache.commons.lang.StringUtils;

import ch.algotrader.entity.security.OptionFamily;
import ch.algotrader.enumeration.OptionType;
import ch.algotrader.util.BaseConverterUtil;
import ch.algotrader.util.DateTimePatterns;
import ch.algotrader.util.RoundUtil;

/**
 * Utility class to generate symbol, isin and ric for {@link ch.algotrader.entity.security.Option Options}.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class OptionSymbol {

    private static final String[] monthCallEnc = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L" };
    private static final String[] monthPutEnc = { "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X" };
    private static final String[] yearEnc = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J" };

    /**
     * Generates the symbole for the specified {@link ch.algotrader.entity.security.OptionFamily}.
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
     *     <tr><td>W</td><td>Week of Month</td><td>3</td></tr>
     *     <tr><td>T</td><td>Type Short</td><td>C</td></tr>
     *     <tr><td>TT</td><td>Type Long</td><td>CALL</td></tr>
     *     <tr><td>S</td><td>Strike</td><td>500</td></tr>
     *     </table>
     */
    public static String getSymbol(OptionFamily family, LocalDate expiration, OptionType type, BigDecimal strike, String pattern) {

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
                "YR",
                "W",
                "TT",
                "T",
                "S"};

        String[] values = new String[] {
                family.getName(),
                family.getSymbolRoot(),
                RoundUtil.getBigDecimal(family.getContractSize(), 0).toString(),
                family.getCurrency().toString(),
                DateTimePatterns.MONTH_LONG.format(expiration).toUpperCase(),
                DateTimePatterns.MONTH_SHORT.format(expiration).toUpperCase(),
                DateTimePatterns.MONTH_2_DIGIT.format(expiration).toUpperCase(),
                OptionType.CALL.equals(type) ? monthCallEnc[expiration.getMonth().getValue() - 1] : monthPutEnc[expiration.getMonthValue() - 1],
                DateTimePatterns.YEAR_4_DIGIT.format(expiration),
                DateTimePatterns.YEAR_2_DIGIT.format(expiration),
                yearEnc[expiration.getYear() % 10],
                DateTimePatterns.WEEK_OF_MONTH.format(expiration),
                type.toString(),
                type.toString().substring(0, 1),
                strike.toString()};

        return StringUtils.replaceEach(pattern, placeHolders, values);
    }

    /**
     * Generates the ISIN for the specified {@link ch.algotrader.entity.security.OptionFamily}.
     */
    public static String getIsin(OptionFamily family, LocalDate expiration, OptionType type, BigDecimal strike) {

        String week = family.isWeekly() ? DateTimePatterns.WEEK_OF_MONTH.format(expiration) : "";

        String month;
        if (OptionType.CALL.equals(type)) {
            month = monthCallEnc[expiration.getMonthValue() - 1];
        } else {
            month = monthPutEnc[expiration.getMonthValue() - 1];
        }

        int yearIndex = expiration.getYear() % 10;
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
    public static String getRic(OptionFamily family, LocalDate expiration, OptionType type, BigDecimal strike) {

        StringBuilder buffer = new StringBuilder();
        buffer.append(family.getRicRoot() != null ? family.getRicRoot() : family.getSymbolRoot());
        if (OptionType.CALL.equals(type)) {
            buffer.append(monthCallEnc[expiration.getMonthValue() - 1]);
        } else {
            buffer.append(monthPutEnc[expiration.getMonthValue() - 1]);
        }
        buffer.append(DateTimePatterns.DAY_OF_MONTH.format(expiration));
        final String s = DateTimePatterns.YEAR_4_DIGIT.format(expiration);
        buffer.append(s.substring(s.length() - 2, s.length()));
        buffer.append(StringUtils.leftPad(String.valueOf((int) (strike.doubleValue() * 100)), 5, "0"));
        buffer.append(".U");

        return buffer.toString();
    }
}
