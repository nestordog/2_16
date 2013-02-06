package com.algoTrader.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang.time.DateUtils;

import com.algoTrader.entity.security.ExpirableFamilyI;
import com.algoTrader.entity.security.SecurityFamily;
import com.algoTrader.enumeration.Duration;
import com.algoTrader.enumeration.ExpirationType;
import com.algoTrader.esper.EsperManager;

public class DateUtil {

    public static Date getCurrentEPTime() {

        String strategyName = StrategyUtil.getStartedStrategyName();
        if (EsperManager.isInitialized(strategyName) && !EsperManager.isInternalClock(strategyName)) {
            return new Date(EsperManager.getCurrentTime(strategyName));
        } else {
            return new Date();
        }
    }

    public static Date toDate(long time) {

        return new Date(time);
    }

    public static int toHour(long time) {

        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(time);
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    public static int toDayOfWeek(long time) {

        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(time);
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    public static Date getLastDayOfMonth(Date date) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        int lastDate = cal.getActualMaximum(Calendar.DATE);
        cal.set(Calendar.DATE, lastDate);

        return cal.getTime();
    }

    public static long getDuration(String durationString) {

        return Duration.valueOf(durationString).getValue();
    }

    /**
     *
     * @param first
     * @param second
     * @return the value 0 if first is equal to second; a value less than 0 if
     *         first is before second; and a value greater than 0 if first is
     *         after second.
     */
    public static int compareTime(Date first, Date second) {

        Calendar firstCal = new GregorianCalendar();
        firstCal.setTime(first);
        firstCal.set(0, 0, 0);

        Calendar secondCal = new GregorianCalendar();
        secondCal.setTime(second);
        secondCal.set(0, 0, 0);

        return firstCal.compareTo(secondCal);
    }

    public static int compareTime(long firstMillis, long secondMills) {

        return compareTime(new Date(firstMillis), new Date(secondMills));
    }

    /**
     *
     * @param time
     * @return the value 0 if currentTime is equal to time; a value less than 0
     *         if currenTime is before time; and a value greater than 0 if
     *         currenTime is after time.
     */
    public static int compareToTime(Date time) {

        return compareTime(getCurrentEPTime(), time);
    }

    public static int compareToTime(long millis) {

        return compareToTime(new Date(millis));
    }

    public static boolean isEqualTime(Date time) {

        return compareToTime(time) == 0;
    }

    public static boolean isEqualTime(long millis) {

        return isEqualTime(new Date(millis));
    }

    public static boolean isAfterTime(Date time) {

        return compareToTime(time) > 0;
    }

    public static boolean isAfterTime(long millis) {

        return isAfterTime(new Date(millis));
    }

    public static boolean isBeforeTime(Date time) {

        return compareToTime(time) < 0;
    }

    public static boolean isBeforeTime(long millis) {

        return isBeforeTime(new Date(millis));
    }

    public static boolean isMultipleOff(Date date, long millis) {

        return date.getTime() % millis == 0;
    }

    private static Date getNext3rdFriday(Date input) {

        Calendar cal = new GregorianCalendar();
        cal.setTime(input);
        cal.setFirstDayOfWeek(Calendar.SUNDAY);
        cal.setMinimalDaysInFirstWeek(2);
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

    private static Date getNext3rdFriday(Date input, int months) {

        // get the next third friday
        Date nextThirdFriday = getNext3rdFriday(input);

        Calendar cal = new GregorianCalendar();
        cal.setFirstDayOfWeek(Calendar.SUNDAY);
        cal.setMinimalDaysInFirstWeek(2);
        cal.setTime(nextThirdFriday);

        // add months and set to friday 3rd week
        cal.add(Calendar.MONTH, (months - 1));
        cal.set(Calendar.WEEK_OF_MONTH, 3);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);

        return cal.getTime();
    }

    private static Date getNext3rdMonday3Months(Date input) {

        Calendar cal = new GregorianCalendar();
        cal.setTime(input);
        cal.setFirstDayOfWeek(Calendar.SUNDAY);
        cal.setMinimalDaysInFirstWeek(2);

        // round to 3-month cycle
        int month = (cal.get(Calendar.MONTH) + 1) / 3 * 3 - 1;

        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.WEEK_OF_MONTH, 3);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        cal.set(Calendar.HOUR_OF_DAY, 13);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        if (cal.getTimeInMillis() < input.getTime()) {
            cal.add(Calendar.MONTH, 3);
            cal.set(Calendar.WEEK_OF_MONTH, 3);
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        }

        return cal.getTime();
    }

    private static Date getNext3rdFriday3Months(Date input) {

        Calendar cal = new GregorianCalendar();
        cal.setTime(input);
        cal.setFirstDayOfWeek(Calendar.SUNDAY);
        cal.setMinimalDaysInFirstWeek(2);

        // round to 3-month cycle
        int month = (cal.get(Calendar.MONTH) + 1) / 3 * 3 - 1;

        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.WEEK_OF_MONTH, 3);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        cal.set(Calendar.HOUR_OF_DAY, 13);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        if (cal.getTimeInMillis() < input.getTime()) {
            cal.add(Calendar.MONTH, 3);
            cal.set(Calendar.WEEK_OF_MONTH, 3);
            cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        }

        return cal.getTime();
    }

    private static Date get30DaysPriorNext3rdFriday(Date input) {

        return get30DaysPriorNext3rdFriday(input, 1);
    }

    private static Date get30DaysPriorNext3rdFriday(Date input, int months) {

        // add 30 days to the input
        Calendar cal1 = new GregorianCalendar();
        cal1.setTime(input);
        cal1.add(Calendar.DAY_OF_YEAR, 30);

        // get the next third friday
        Date nextThirdFriday = getNext3rdFriday(cal1.getTime());

        // set 1st day of week and minimumDaysInFirstWeek
        Calendar cal2 = new GregorianCalendar();
        cal2.setTime(nextThirdFriday);
        cal2.setFirstDayOfWeek(Calendar.SUNDAY);
        cal2.setMinimalDaysInFirstWeek(2);

        // add months and set to friday 3rd week
        cal2.add(Calendar.MONTH, (months - 1));
        cal2.set(Calendar.WEEK_OF_MONTH, 3);
        cal2.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);

        // subtract 30 days again
        cal2.add(Calendar.DAY_OF_YEAR, -30);

        return cal2.getTime();
    }

    public static Date getExpirationDate(ExpirationType type, Date input) {

        if (ExpirationType.NEXT_3_RD_FRIDAY.equals(type)) {
            return getNext3rdFriday(input);
        } else if (ExpirationType.NEXT_3_RD_MONDAY_3_MONTHS.equals(type)) {
            return getNext3rdMonday3Months(input);
        } else if (ExpirationType.NEXT_3_RD_FRIDAY_3_MONTHS.equals(type)) {
            return getNext3rdFriday3Months(input);
        } else if (ExpirationType.THIRTY_DAYS_BEFORE_NEXT_3_RD_FRIDAY.equals(type)) {
            return get30DaysPriorNext3rdFriday(input);
        } else {
            throw new IllegalArgumentException("unknown expiration type " + type);
        }
    }

    public static Date getExpirationDateNMonths(ExpirationType type, Date input, int months) {

        if (ExpirationType.NEXT_3_RD_FRIDAY.equals(type)) {
            return getNext3rdFriday(input, months);
        } else if (ExpirationType.NEXT_3_RD_MONDAY_3_MONTHS.equals(type)) {
            throw new UnsupportedOperationException();
        } else if (ExpirationType.NEXT_3_RD_FRIDAY_3_MONTHS.equals(type)) {
            throw new UnsupportedOperationException();
        } else if (ExpirationType.THIRTY_DAYS_BEFORE_NEXT_3_RD_FRIDAY.equals(type)) {
            return get30DaysPriorNext3rdFriday(input, months);
        } else {
            throw new IllegalArgumentException("unknown expiration type " + type);
        }
    }

    public static Date getExpirationDate(SecurityFamily securityFamily, Date input) {

        if (securityFamily instanceof ExpirableFamilyI) {
            ExpirableFamilyI expirableFamily = (ExpirableFamilyI) securityFamily;
            return getExpirationDate(expirableFamily.getExpirationType(), input);
        } else {
            throw new IllegalArgumentException("securityFamily must be a ExpirableFamily");
        }
    }

    public static Date getExpirationDateNMonths(SecurityFamily securityFamily, Date input, int months) {

        if (securityFamily instanceof ExpirableFamilyI) {
            ExpirableFamilyI expirableFamily = (ExpirableFamilyI) securityFamily;
            return getExpirationDateNMonths(expirableFamily.getExpirationType(), input, months);
        } else {
            throw new IllegalArgumentException("securityFamily must be a ExpirableFamily");
        }
    }

    public static boolean isMarketOpen(SecurityFamily securityFamily, Date currentDateTime) {

        // market session starting today
        Date todayOpen = setTime(currentDateTime, securityFamily.getMarketOpen());
        Date todayClose = setTime(currentDateTime, securityFamily.getMarketClose());

        // close is on the next day
        if (securityFamily.getMarketOpen().compareTo(securityFamily.getMarketClose()) > 0) {
            todayClose = DateUtils.addDays(todayClose, 1);
        }

        // during todays session
        if (isTradingDay(securityFamily, todayOpen) && currentDateTime.compareTo(todayOpen) >= 0 && currentDateTime.compareTo(todayClose) <= 0) {
            return true;
        }

        // market session starting yesterday
        Date yesterdayOpen = DateUtils.addDays(todayOpen, -1);
        Date yesterdayClose = DateUtils.addDays(todayClose, -1);

        // during yesterdays session
        if (isTradingDay(securityFamily, yesterdayOpen) && currentDateTime.compareTo(yesterdayOpen) >= 0 && currentDateTime.compareTo(yesterdayClose) <= 0) {
            return true;
        }

        return false;
    }

    private static Date setTime(Date date, Date time) {

        Calendar timeCal = Calendar.getInstance();
        timeCal.setTime(time);

        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(date);
        dateCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        dateCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        dateCal.set(Calendar.SECOND, timeCal.get(Calendar.SECOND));
        dateCal.set(Calendar.MILLISECOND, timeCal.get(Calendar.MILLISECOND));

        return dateCal.getTime();
    }

    private static boolean isTradingDay(SecurityFamily securityFamily, Date currentDateTime) {

        return toDayOfWeek(currentDateTime.getTime()) >= securityFamily.getMarketOpenDay().getValue() && toDayOfWeek(currentDateTime.getTime()) <= securityFamily.getMarketOpenDay().getValue() + 4;
    }
}
