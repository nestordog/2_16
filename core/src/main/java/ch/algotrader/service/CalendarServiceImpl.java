/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.service;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;

import ch.algotrader.entity.security.Exchange;
import ch.algotrader.entity.security.Holiday;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CalendarServiceImpl extends CalendarServiceBase {

    private Map<Exchange, Map<Date, Holiday>> holidays = new HashMap<Exchange, Map<Date, Holiday>>();

    @Override
    protected boolean handleIsExchangeOpen(int exchangeId, Date date) throws Exception {

        Exchange exchange = getExchangeDao().get(exchangeId);

        // are we on a trading day?
        if (!isTradingDay(exchange, date)) {
            return false;
        }

        // exchange session starting today
        Date todayOpen = getOpenTime(exchange, date);
        Date todayClose = getCloseTime(exchange, date);

        // is close on the next day?
        if (exchange.getOpen().compareTo(exchange.getClose()) >= 0) {
            todayClose = DateUtils.addDays(todayClose, 1);
        }

        // are we during todays session?
        if (date.compareTo(todayOpen) >= 0 && date.compareTo(todayClose) <= 0) {
            return true;
        }

        // exchange session starting yesterday
        Date yesterdayOpen = DateUtils.addDays(todayOpen, -1);
        Date yesterdayClose = DateUtils.addDays(todayClose, -1);

        // are we during yesterdays session
        if (isTradingDay(exchange, yesterdayOpen) && date.compareTo(yesterdayOpen) >= 0 && date.compareTo(yesterdayClose) <= 0) {
            return true;
        }

        return false;
    }

    @Override
    protected boolean handleIsTradingDay(int exchangeId, Date date) throws Exception {

        Exchange exchange = getExchangeDao().get(exchangeId);
        return isTradingDay(exchange, date);
    }

    private boolean isTradingDay(Exchange exchange, Date date) {

        Holiday holiday = getHoliday(exchange, date);
        if (holiday != null && !holiday.isPartialOpen()) {
            return false;
        } else {
            int dayOfWeek = toDayOfWeek(date.getTime());
            int openDay = exchange.getOpenDay().getValue();
            return dayOfWeek >= openDay && dayOfWeek <= openDay + 4;
        }
    }

    @Override
    protected Date handleGetOpenTime(int exchangeId, Date date) throws Exception {

        Exchange exchange = getExchangeDao().get(exchangeId);
        return getOpenTime(exchange, date);
    }

    private Date getOpenTime(Exchange exchange, Date date) {

        if (!isTradingDay(exchange, date)) {
            return null;
        }

        Holiday holiday = getHoliday(exchange, date);
        if (holiday != null && holiday.getLateOpen() != null) {
            return setTime(date, holiday.getLateOpen());
        } else {
            return setTime(date, exchange.getOpen());
        }
    }

    @Override
    protected Date handleGetCloseTime(int exchangeId, Date date) throws Exception {

        Exchange exchange = getExchangeDao().get(exchangeId);
        return getCloseTime(exchange, date);
    }

    private Date getCloseTime(Exchange exchange, Date date) {

        if (!isTradingDay(exchange, date)) {
            return null;
        }

        Holiday holiday = getHoliday(exchange, date);
        if (holiday != null && holiday.getEarlyClose() != null) {
            return setTime(date, holiday.getEarlyClose());
        } else {
            return setTime(date, exchange.getClose());
        }
    }

    private Holiday getHoliday(Exchange exchange, Date date) {

        // load all holidays for this exchange
        if (!this.holidays.containsKey(exchange)) {
            Map<Date, Holiday> map = new HashMap<Date, Holiday>();
            for (Holiday holiday : exchange.getHolidays()) {
                map.put(holiday.getDate(), holiday);
            }
            this.holidays.put(exchange, map);
        }

        return this.holidays.get(exchange).get(DateUtils.truncate(date, Calendar.DATE));
    }

    private int toDayOfWeek(long time) {

        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(time);
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    private Date setTime(Date date, Date time) {

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

}
