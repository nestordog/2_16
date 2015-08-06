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
package ch.algotrader.dao.marketData;

import java.util.Date;
import java.util.List;

import ch.algotrader.dao.ReadWriteDao;
import ch.algotrader.entity.marketData.Tick;

/**
 * DAO for {@link ch.algotrader.entity.marketData.Tick} objects.
 *
 * @see ch.algotrader.entity.marketData.Tick
 */
public interface TickDao extends ReadWriteDao<Tick> {

    /**
     * Finds all Ticks of the specified Security
     * @param securityId
     * @return List<Tick>
     */
    List<Tick> findBySecurity(long securityId);

    /**
     * Finds the first Tick of the defined Security that is before the maxDate (but not earlier than
     * one minute before that the maxDate).
     * @param securityId
     * @param maxDate
     * @return Tick
     */
    Tick findBySecurityAndMaxDate(long securityId, Date maxDate);

    /**
     * Finds all Ticks of the defined Security that are after the {@code minDate} and before {@code
     * minDate} + {@code intervalDays}
     * @param securityId
     * @param minDate
     * @param intervalDays
     * @return List<Tick>
     */
    List<Tick> findTicksBySecurityAndMinDate(long securityId, Date minDate, int intervalDays);

    /**
     * <p>
     * Does the same thing as {@link #findTicksBySecurityAndMinDate(long, Date, int)} with an
     * additional argument called <code>limit</code>. The <code>limit</code>
     * argument allows you to specify the limit when you are paging the results.
     * </p>
     * @param limit
     * @param securityId
     * @param minDate
     * @param intervalDays
     * @return List<Tick>
     */
    List<Tick> findTicksBySecurityAndMinDate(int limit, long securityId, Date minDate, int intervalDays);

    /**
     * Finds all Ticks of the defined Security that are before the {@code maxDate} and after {@code
     * minDate} - {@code intervalDays}
     * @param securityId
     * @param maxDate
     * @param intervalDays
     * @return List<Tick>
     */
    List<Tick> findTicksBySecurityAndMaxDate(long securityId, Date maxDate, int intervalDays);

    /**
     * <p>
     * Does the same thing as {@link #findTicksBySecurityAndMaxDate(long, Date, int)} with an
     * additional argument called <code>limit</code>. The <code>limit</code>
     * argument allows you to specify the limit when you are paging the results.
     * </p>
     * @param limit
     * @param securityId
     * @param maxDate
     * @param intervalDays
     * @return List<Tick>
     */
    List<Tick> findTicksBySecurityAndMaxDate(int limit, long securityId, Date maxDate, int intervalDays);

    /**
     * Finds one Tick-Id per day of the defined Security that is just before the specified {@code
     * time}.
     * @param securityId
     * @param time
     * @return List<Long>
     */
    List<Long> findDailyTickIdsBeforeTime(long securityId, Date time);

    /**
     * Finds one Tick-Id per day of the defined Security that is just after the specified {@code
     * time}.
     * @param securityId
     * @param time
     * @return List<Long>
     */
    List<Long> findDailyTickIdsAfterTime(long securityId, Date time);

    /**
     * Finds one Tick-Id per hour of the defined Security that is just before the specified number
     * of {@code minutes} and after the specified {@code minDate}.
     * @param securityId
     * @param minutes
     * @param minDate
     * @return List<Long>
     */
    List<Long> findHourlyTickIdsBeforeMinutesByMinDate(long securityId, int minutes, Date minDate);

    /**
     * Finds one Tick-Id per hour of the defined Security that is just after the specified number of
     * {@code minutes} and after the specified {@code minDate}.
     * @param securityId
     * @param minutes
     * @param minDate
     * @return List<Long>
     */
    List<Long> findHourlyTickIdsAfterMinutesByMinDate(long securityId, int minutes, Date minDate);

    /**
     * Returns all Ticks of the specified Ids.
     * @param ids
     * @return List<Tick>
     */
    List<Tick> findByIdsInclSecurityAndUnderlying(List<Long> ids);

    /**
     * Finds all Ticks for Securities that are subscribed by any Strategy between {@code minDate}
     * and {@code maxDate}
     * @param minDate
     * @param maxDate
     * @return List<Tick>
     */
    List<Tick> findSubscribedByTimePeriod(Date minDate, Date maxDate);

    /**
     * <p>
     * Does the same thing as {@link #findSubscribedByTimePeriod(Date, Date)} with an
     * additional argument <code>limit</code>. The <code>limit</code>
     * argument allows you to specify the limit when you are paging the results.
     * </p>
     * @param limit
     * @param minDate
     * @param maxDate
     * @return List<Tick>
     */
    List<Tick> findSubscribedByTimePeriod(int limit, Date minDate, Date maxDate);

    /**
     * Returns the {@code tickerId} from the TickWindow, that belongs to the defined Security. The
     * {@code tickerId} is assigned by external Brokers to corresponding {@link
     * ch.algotrader.entity.Subscription Subscription}
     * @param securityId
     * @return String
     */
    String findTickerIdBySecurity(long securityId);

}
