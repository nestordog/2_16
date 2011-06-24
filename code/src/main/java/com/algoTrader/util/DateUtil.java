package com.algoTrader.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.algoTrader.ServiceLocator;
import com.algoTrader.enumeration.ExpirationType;
import com.algoTrader.service.RuleService;

public class DateUtil {

    public static Date getCurrentEPTime() {

        String strategyName = StrategyUtil.getStartedStrategyName();
        RuleService ruleService = ServiceLocator.commonInstance().getRuleService();
        if (ruleService.isInitialized(strategyName) && !ruleService.isInternalClock(strategyName)) {
            return new Date(ruleService.getCurrentTime(strategyName));
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

        return get30DaysPriorNext3rdFridayNMonths(input, 1);
    }

    private static Date get30DaysPriorNext3rdFridayNMonths(Date input, int months) {

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
            throw new UnsupportedOperationException();
        } else if (ExpirationType.NEXT_3_RD_FRIDAY_3_MONTHS.equals(type)) {
            throw new UnsupportedOperationException();
        } else if (ExpirationType.THIRTY_DAYS_BEFORE_NEXT_3_RD_FRIDAY.equals(type)) {
            return get30DaysPriorNext3rdFridayNMonths(input, months);
        } else {
            throw new IllegalArgumentException("unknown expiration type " + type);
        }
    }

}
