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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.lang.Validate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.entity.marketData.BarDao;
import ch.algotrader.enumeration.BarType;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.TimePeriod;
import ch.algotrader.util.collection.CollectionUtil;
import ch.algotrader.util.spring.HibernateSession;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@HibernateSession
public abstract class HistoricalDataServiceImpl implements HistoricalDataService {

    private final BarDao barDao;

    public HistoricalDataServiceImpl(final BarDao barDao) {

        Validate.notNull(barDao, "BarDao is null");

        this.barDao = barDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract List<Bar> getHistoricalBars(int securityId, Date endDate, int timePeriodLength, TimePeriod timePeriod, Duration barSize, BarType barType);

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateHistoricalBars(final int securityId, final Date endDate, final int timePeriodLength, final TimePeriod timePeriod, final Duration barSize, final BarType barType) {

        Validate.notNull(endDate, "End date is null");
        Validate.notNull(timePeriod, "Time period is null");
        Validate.notNull(barSize, "Bar size is null");
        Validate.notNull(barType, "Bar type is null");

        try {
            // get all Bars from the Market Data Provider
            List<Bar> bars = getHistoricalBars(securityId, endDate, timePeriodLength, timePeriod, barSize, barType);

            // get the last Bar int the Database
            final Bar lastBar = CollectionUtil.getSingleElementOrNull(this.barDao.findBarsBySecurityAndBarSize(1, 1, securityId, barSize));

            // remove all Bars prior to the lastBar
            if (lastBar != null) {

                CollectionUtils.filter(bars, new Predicate<Bar>() {
                    @Override
                    public boolean evaluate(Bar bar) {
                        return bar.getDateTime().compareTo(lastBar.getDateTime()) > 0;
                    }
                });
            }

            // save the Bars
            this.barDao.create(bars);
        } catch (Exception ex) {
            throw new HistoricalDataServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void replaceHistoricalBars(final int securityId, final Date endDate, final int timePeriodLength, final TimePeriod timePeriod, final Duration barSize, final BarType barType) {

        Validate.notNull(endDate, "End date is null");
        Validate.notNull(timePeriod, "Time period is null");
        Validate.notNull(barSize, "Bar size is null");
        Validate.notNull(barType, "Bar type is null");

        try {
            // get all Bars from the Market Data Provider
            List<Bar> newBars = getHistoricalBars(securityId, endDate, timePeriodLength, timePeriod, barSize, barType);

            // remove all Bars in the database after the first newly retrieved Bar
            final Bar firstBar = CollectionUtil.getFirstElementOrNull(newBars);
            if (firstBar != null) {

                List<Bar> existingBars = this.barDao.findBarsBySecurityBarSizeAndMinDate(securityId, barSize, firstBar.getDateTime());

                if (existingBars.size() > 0) {

                    // store bars according to their date
                    Map<Date, Bar> dateBarMap = new HashMap<Date, Bar>();
                    for (Bar bar : existingBars) {
                        dateBarMap.put(bar.getDateTime(), bar);
                    }

                    //update existing bars
                    for (Iterator<Bar> it = newBars.iterator(); it.hasNext();) {

                        Bar newBar = it.next();
                        Bar existingBar = dateBarMap.remove(newBar.getDateTime());
                        if (existingBar != null) {
                            existingBar.setOpen(newBar.getOpen());
                            existingBar.setHigh(newBar.getHigh());
                            existingBar.setLow(newBar.getLow());
                            existingBar.setClose(newBar.getClose());
                            existingBar.setVol(newBar.getVol());
                            it.remove();
                        }
                    }

                    // remove obsolete Bars
                    this.barDao.remove(dateBarMap.values());
                }
            }

            // save the newly retrieved Bars that do not exist yet in the db
            this.barDao.create(newBars);
        } catch (Exception ex) {
            throw new HistoricalDataServiceException(ex.getMessage(), ex);
        }
    }
}
