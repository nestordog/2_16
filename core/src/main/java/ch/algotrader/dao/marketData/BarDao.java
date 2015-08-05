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
import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.enumeration.Duration;

/**
 * DAO for {@link ch.algotrader.entity.marketData.Bar} objects.
 *
 * @see ch.algotrader.entity.marketData.Bar
 */
public interface BarDao extends ReadWriteDao<Bar> {

    /**
     * Returns daily Bars created based on Ticks between {@code minDate} and {@code maxDate}
     * @param securityId
     * @param minDate
     * @param maxDate
     * @return List<Bar>
     */
    List<Bar> findDailyBarsFromTicks(long securityId, Date minDate, Date maxDate);

    /**
     * Returns all Bars of the specified Security and {@code barSize}
     * The <code>limit</code> argument allows you to specify the limit when you are paging the results.
     * @param limit
     * @param securityId
     * @param barSize
     * @return List<Bar>
     */
    List<Bar> findBarsBySecurityAndBarSize(int limit, long securityId, Duration barSize);

    /**
     * Returns all Bars of the specified Security and {@code barSize} after the specified {@code
     * minDate}
     * @param securityId
     * @param barSize
     * @param minDate
     * @return List<Bar>
     */
    List<Bar> findBarsBySecurityBarSizeAndMinDate(long securityId, Duration barSize, Date minDate);

    /**
     * Finds all Ticks for Securities that are subscribed by any Strategy between {@code minDate}
     * and {@code maxDate}
     * @param minDate
     * @param maxDate
     * @param barSize
     * @return List<Bar>
     */
    List<Bar> findSubscribedByTimePeriodAndBarSize(Date minDate, Date maxDate, Duration barSize);

    /**
     * Finds all Ticks for Securities that are subscribed by any Strategy between {@code minDate}
     * The <code>limit</code> argument allows you to specify the limit when you are paging the results.
     * @param limit
     * @param minDate
     * @param maxDate
     * @param barSize
     * @return List<Bar>
     */
    List<Bar> findSubscribedByTimePeriodAndBarSize(int limit, Date minDate, Date maxDate, Duration barSize);

    // spring-dao merge-point
}
