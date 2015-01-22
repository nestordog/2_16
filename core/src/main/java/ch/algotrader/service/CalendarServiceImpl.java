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
package ch.algotrader.service;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TimeZone;
import java.util.TreeSet;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.DateUtils;

import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.exchange.ExchangeDao;
import ch.algotrader.entity.exchange.Holiday;
import ch.algotrader.entity.exchange.TradingHours;
import ch.algotrader.enumeration.WeekDay;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.util.spring.HibernateSession;

/**
 * Java (before JDK8) does not have a separate Date and Time class, therefore parameters are named accordingly.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@HibernateSession
public class CalendarServiceImpl implements CalendarService {

    private final ExchangeDao exchangeDao;
    private final EngineManager engineManager;

    public CalendarServiceImpl(final ExchangeDao exchangeDao, final EngineManager engineManager) {

        Validate.notNull(exchangeDao, "ExchangeDao is null");
        Validate.notNull(engineManager, "Engine is null");

        this.exchangeDao = exchangeDao;
        this.engineManager = engineManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getCurrentTradingDate(int exchangeId, Date dateTime) {

        Validate.notNull(dateTime, "Data time is null");

        Exchange exchange = this.exchangeDao.get(exchangeId);
        Date date = DateUtils.addDays(DateUtils.truncate(dateTime, Calendar.DATE), 2);
        NavigableSet<Date> openTimes = new TreeSet<Date>();
        while ((openTimes.floor(dateTime)) == null) {
            date = DateUtils.addDays(date, -1);
            openTimes.addAll(getOpenTimes(exchange, date));
        }
        return date;

    }

    @Override
    public Date getCurrentTradingDate(int exchangeId) {
        return getCurrentTradingDate(exchangeId, this.engineManager.getCurrentEPTime());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOpen(final int exchangeId, final Date dateTime) {

        Validate.notNull(dateTime, "Data time is null");

        Exchange exchange = this.exchangeDao.get(exchangeId);
        Date date = truncateToDayUsingTimeZone(dateTime, exchange.getTZ());
        TimeIntervals timeIntervals = getTimeIntervalsPlusMinusOneDay(exchange, date);
        return timeIntervals.contains(dateTime);

    }

    @Override
    public boolean isOpen(int exchangeId) {
        return isOpen(exchangeId, this.engineManager.getCurrentEPTime());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTradingDay(final int exchangeId, final Date date) {

        Validate.notNull(date, "Date is null");

        Exchange exchange = this.exchangeDao.get(exchangeId);
        Date dateTruncated = DateUtils.truncate(date, Calendar.DATE);
        return isTradingDay(exchange, dateTruncated);

    }

    @Override
    public boolean isTradingDay(int exchangeId) {
        return isTradingDay(exchangeId, this.engineManager.getCurrentEPTime());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getOpenTime(final int exchangeId, final Date date) {

        Validate.notNull(date, "Date is null");

        Exchange exchange = this.exchangeDao.get(exchangeId);
        Date dateTruncated = DateUtils.truncate(date, Calendar.DATE);
        TimeIntervals timeIntervals = getTimeIntervals(exchange, dateTruncated);
        return timeIntervals.isEmpty() ? null : timeIntervals.first().getFrom();

    }

    @Override
    public Date getOpenTime(int exchangeId) {
        return getOpenTime(exchangeId, this.engineManager.getCurrentEPTime());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getCloseTime(final int exchangeId, final Date date) {

        Validate.notNull(date, "Date is null");

        Exchange exchange = this.exchangeDao.get(exchangeId);
        Date dateTruncated = DateUtils.truncate(date, Calendar.DATE);
        TimeIntervals timeIntervals = getTimeIntervals(exchange, dateTruncated);
        return timeIntervals.isEmpty() ? null : timeIntervals.last().getTo();

    }

    @Override
    public Date getCloseTime(int exchangeId) {
        return getCloseTime(exchangeId, this.engineManager.getCurrentEPTime());
    }

    @Override
    public Date getNextOpenTime(int exchangeId, Date dateTime) {

        Validate.notNull(dateTime, "DateTime is null");

        Exchange exchange = this.exchangeDao.get(exchangeId);
        Date date = DateUtils.truncate(dateTime, Calendar.DATE);
        Date openTime;
        NavigableSet<Date> openTimes = new TreeSet<Date>();
        while ((openTime = openTimes.ceiling(dateTime)) == null) {
            openTimes.addAll(getOpenTimes(exchange, date));
            date = DateUtils.addDays(date, 1);
        }
        return openTime;

    }

    @Override
    public Date getNextOpenTime(int exchangeId) {
        return getNextOpenTime(exchangeId, this.engineManager.getCurrentEPTime());
    }

    @Override
    public Date getNextCloseTime(int exchangeId, Date dateTime) {

        Validate.notNull(dateTime, "DateTime is null");

        Exchange exchange = this.exchangeDao.get(exchangeId);
        Date date = DateUtils.addDays(DateUtils.truncate(dateTime, Calendar.DATE), -1);
        Date closeTime;
        NavigableSet<Date> closeTimes = new TreeSet<Date>();
        while ((closeTime = closeTimes.ceiling(dateTime)) == null) {
            closeTimes.addAll(getCloseTimes(exchange, date));
            date = DateUtils.addDays(date, 1);
        }
        return closeTime;

    }

    @Override
    public Date getNextCloseTime(int exchangeId) {
        return getNextCloseTime(exchangeId, this.engineManager.getCurrentEPTime());
    }

    /**
     * Get all open times for this date
     */
    private NavigableSet<Date> getOpenTimes(Exchange exchange, Date date) {

        NavigableSet<Date> openTimes = new TreeSet<Date>();
        for (TradingHours tradingHours : exchange.getTradingHours()) {
            TimeInterval timeInterval = getTimeInterval(date, tradingHours);
            if (timeInterval != null) {
                openTimes.add(timeInterval.getFrom());
            }
        }
        return openTimes;
    }

    /**
     * Get all close times for this date
     */
    private NavigableSet<Date> getCloseTimes(Exchange exchange, Date date) {

        NavigableSet<Date> openTimes = new TreeSet<Date>();
        for (TradingHours tradingHours : exchange.getTradingHours()) {
            TimeInterval timeInterval = getTimeInterval(date, tradingHours);
            if (timeInterval != null) {
                openTimes.add(timeInterval.getTo());
            }
        }
        return openTimes;
    }

    private boolean isTradingDay(Exchange exchange, Date date) {

        // is this date a holiday?
        Holiday holiday = getHoliday(exchange, date);
        if (holiday != null && !holiday.isPartialOpen()) {
            return false;
        }

        // check if any of the tradingHours is enabled for this date
        WeekDay weekDay = getWeekDay(date, TimeZone.getDefault());
        for (TradingHours tradingHours : exchange.getTradingHours()) {
            if (tradingHours.isEnabled(weekDay)) {
                return true;
            }
        }

        return false;
    }

    /**
     * gets the holiday for this date
     */
    private Holiday getHoliday(Exchange exchange, Date dateTime) {

        final Date date = DateUtils.truncate(dateTime, Calendar.DATE);

        return CollectionUtils.find(exchange.getHolidays(), new Predicate<Holiday>() {

            @Override
            public boolean evaluate(Holiday holiday) {
                return DateUtils.isSameDay(holiday.getDate(), date);
            }
        });
    }

    /**
     * gets the weekday of the specified date
     * @param timeZone
     */
    private WeekDay getWeekDay(Date date, TimeZone timeZone) {

        Calendar cal = new GregorianCalendar();
        cal.setTimeZone(timeZone);
        cal.setTime(date);
        return WeekDay.fromValue(cal.get(Calendar.DAY_OF_WEEK));
    }

    /**
     * Get all TimeIntervals for this date
     */
    private TimeIntervals getTimeIntervals(Exchange exchange, Date date) {

        TimeIntervals timeIntervals = new TimeIntervals();
        for (TradingHours tradingHours : exchange.getTradingHours()) {
            timeIntervals.add(getTimeInterval(date, tradingHours));
        }
        return timeIntervals;
    }

    /**
     * Get all TimeIntervals for this date, the day before and the day after
     */
    private TimeIntervals getTimeIntervalsPlusMinusOneDay(Exchange exchange, Date date) {

        TimeIntervals timeIntervals = new TimeIntervals();
        timeIntervals.addAll(getTimeIntervals(exchange, DateUtils.addDays(date, -1)));
        timeIntervals.addAll(getTimeIntervals(exchange, date));
        timeIntervals.addAll(getTimeIntervals(exchange, DateUtils.addDays(date, +1)));
        return timeIntervals;
    }

    /**
     * Get the TimeInterval for the specified week, tradingHours and weekday
     * Taking into consideration potential lateOpens and earlyCloses
     * return null if this day is a full holiday
     */
    private TimeInterval getTimeInterval(Date date, TradingHours tradingHours) {

        TimeZone timeZone = tradingHours.getExchange().getTZ();
        boolean inverse = tradingHours.getOpen().compareTo(tradingHours.getClose()) > 0;

        Date open = getDateTime(timeZone, date, tradingHours.getOpen());
        Date close = getDateTime(timeZone, date, tradingHours.getClose());

        if (!tradingHours.isEnabled(getWeekDay(open, timeZone))) {
            return null;
        }

        Holiday holiday = getHoliday(tradingHours.getExchange(), date);
        if (holiday != null) {
            if (holiday.getLateOpen() != null) {
                open = getDateTime(timeZone, date, holiday.getLateOpen());
            } else if (holiday.getEarlyClose() != null) {
                close = getDateTime(timeZone, date, holiday.getEarlyClose());
            } else {
                return null;
            }
        }

        if (inverse) {
            open = DateUtils.addDays(open, -1);
        }

        return new TimeInterval(open, close);
    }

    /**
     * takes year, month & day from date
     * and takes hour_of_day, minute, second & millisecond from time
     * and takes the specified timeZone
     */
    private Date getDateTime(TimeZone timeZone, Date date, Date time) {

        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(date);

        Calendar timeCal = Calendar.getInstance();
        timeCal.setTime(time);

        dateCal.setTimeZone(timeZone);
        dateCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        dateCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        dateCal.set(Calendar.SECOND, timeCal.get(Calendar.SECOND));
        dateCal.set(Calendar.MILLISECOND, timeCal.get(Calendar.MILLISECOND));

        return dateCal.getTime();
    }

    /**
     * truncates the given date by converting it first to the specified timezone and then
     * taking the year, month, day part and leaving all other fields at zero
     */
    private Date truncateToDayUsingTimeZone(Date date, TimeZone tz) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.setTimeZone(tz);

        Calendar truncateCal = Calendar.getInstance();
        truncateCal.set(Calendar.YEAR, cal.get(Calendar.YEAR));
        truncateCal.set(Calendar.MONTH, cal.get(Calendar.MONTH));
        truncateCal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
        truncateCal.set(Calendar.HOUR_OF_DAY, 0);
        truncateCal.set(Calendar.MINUTE, 0);
        truncateCal.set(Calendar.SECOND, 0);
        truncateCal.set(Calendar.MILLISECOND, 0);

        return truncateCal.getTime();
    }

    public class TimeInterval implements Serializable, Comparable<TimeInterval> {

        private static final long serialVersionUID = 1364116470317584523L;

        private final Date from;
        private final Date to;

        public TimeInterval(Date from, Date to) {
            this.from = from;
            this.to = to;
        }

        public Date getFrom() {
            return this.from;
        }

        public Date getTo() {
            return this.to;
        }

        public boolean contains(Date date) {

            return this.from.compareTo(date) <= 0 && this.to.compareTo(date) > 0;
        }

        @Override
        public String toString() {
            return this.from + " - " + this.to;
        }

        @Override
        public boolean equals(Object obj) {

            if (this == obj) {
                return true;
            }
            if (obj instanceof TimeInterval) {
                TimeInterval that = (TimeInterval) obj;
                return Objects.equals(this.getFrom(), that.getFrom()) && Objects.equals(this.getTo(), that.getTo());
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {

            int hash = 17;
            hash = hash * 37 + Objects.hashCode(getFrom());
            hash = hash * 37 + Objects.hashCode(getTo());
            return hash;
        }

        @Override
        public int compareTo(TimeInterval other) {
            int comp = this.getFrom().compareTo(other.getFrom());
            if (comp != 0) {
                return comp;
            } else {
                return this.getTo().compareTo(other.getTo());
            }
        }
    }

    public static class TimeIntervals extends TreeSet<TimeInterval> {

        private static final long serialVersionUID = -1313023804352151004L;

        @Override
        public boolean add(TimeInterval timeInterval) {

            // do not add null
            if (timeInterval != null) {
                return super.add(timeInterval);
            } else {
                return false;
            }
        }

        public boolean contains(Date date) {

            for (TimeInterval timeInterval : this) {
                if (timeInterval.contains(date)) {
                    return true;
                }
            }
            return false;
        }
    }

}
