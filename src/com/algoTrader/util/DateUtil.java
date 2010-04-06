package com.algoTrader.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtil {

    public static Date toDate(long time) {

        return new Date(time);
    }

    public static Date getCurrentEPTime() {

        if (EsperService.hasInstance()) {
            return new Date(EsperService.getCurrentTime());
        } else {
            return new Date();
        }
    }

    public static Date getNextThirdFriday(Date input) {

        Calendar cal = new GregorianCalendar();
        cal.setTime(input);
        cal.set(Calendar.WEEK_OF_MONTH, 3);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        cal.set(Calendar.HOUR_OF_DAY, 13);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        if (cal.getTimeInMillis() < input.getTime()) {
            cal.add(Calendar.MONTH, 1);
            cal.set(Calendar.WEEK_OF_MONTH, 3);
            cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        }

        return cal.getTime();
    }
}
