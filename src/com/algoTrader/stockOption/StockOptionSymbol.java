package com.algoTrader.stockOption;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.commons.lang.StringUtils;

import com.algoTrader.entity.StockOption;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.util.BaseConverterUtil;

public class StockOptionSymbol {

    private static final String[] monthCallEnc = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L" };
    private static final String[] monthPutEnc = { "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X" };
    private static final String[] yearEnc = { "A", "B", "C", "D", "E" };

    public static String getSymbol(StockOption stockOption) {

        int week = 1;

        char type = 'O';

        String underlaying = stockOption.getUnderlaying().getSymbol();

        String month;
        Calendar cal = new GregorianCalendar();
        cal.setTime(stockOption.getExpiration());
        if (OptionType.CALL.equals(stockOption.getType())) {
            month = monthCallEnc[cal.get(Calendar.MONTH)];
        } else {
            month = monthPutEnc[cal.get(Calendar.MONTH)];
        }

        String year = yearEnc[cal.get(Calendar.YEAR) - 2010];

        String strike36 = BaseConverterUtil.toBase36(stockOption.getStrike().intValue());
        String strike = StringUtils.leftPad(strike36, 5, "0");

        StringBuffer buffer = new StringBuffer();
        buffer.append(week);
        buffer.append(type);
        buffer.append(underlaying);
        buffer.append(month);
        buffer.append(year);
        buffer.append(strike);

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
