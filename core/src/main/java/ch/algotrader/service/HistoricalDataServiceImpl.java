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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.dao.marketData.BarDao;
import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.enumeration.BarType;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.TimePeriod;
import ch.algotrader.util.collection.CollectionUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@Transactional(propagation = Propagation.SUPPORTS)
public abstract class HistoricalDataServiceImpl implements HistoricalDataService {

    private static final Logger LOGGER = LogManager.getLogger(HistoricalDataServiceImpl.class);

    private final BarDao barDao;

    public HistoricalDataServiceImpl(final BarDao barDao) {

        Validate.notNull(barDao, "BarDao is null");

        this.barDao = barDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract List<Bar> getHistoricalBars(long securityId, Date endDate, int timePeriodLength, TimePeriod timePeriod, Duration barSize, BarType barType, Map<String,String> properties);

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateHistoricalBars(final long securityId, final Date endDate, final int timePeriodLength, final TimePeriod timePeriod, final Duration barSize, final BarType barType, Map<String,String> properties) {

        Validate.notNull(endDate, "End date is null");
        Validate.notNull(timePeriod, "Time period is null");
        Validate.notNull(barSize, "Bar size is null");
        Validate.notNull(barType, "Bar type is null");

        // get all Bars from the Market Data Provider
        List<Bar> bars = getHistoricalBars(securityId, endDate, timePeriodLength, timePeriod, barSize, barType, properties);

        // get the last Bar int the Database
        final Bar lastBar = CollectionUtil.getSingleElementOrNull(this.barDao.findBarsBySecurityAndBarSize(1, securityId, barSize));

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
        this.barDao.saveAll(bars);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("created {} new bars for security {}", bars.size(), securityId);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void replaceHistoricalBars(final long securityId, final Date endDate, final int timePeriodLength, final TimePeriod timePeriod, final Duration barSize, final BarType barType, Map<String,String> properties) {

        Validate.notNull(endDate, "End date is null");
        Validate.notNull(timePeriod, "Time period is null");
        Validate.notNull(barSize, "Bar size is null");
        Validate.notNull(barType, "Bar type is null");

        // get all Bars from the Market Data Provider
        List<Bar> newBars = getHistoricalBars(securityId, endDate, timePeriodLength, timePeriod, barSize, barType, properties);

        // remove all Bars in the database after the first newly retrieved Bar
        final Bar firstBar = CollectionUtil.getFirstElementOrNull(newBars);
        if (firstBar != null) {

            List<Bar> existingBars = this.barDao.findBarsBySecurityBarSizeAndMinDate(securityId, barSize, firstBar.getDateTime());

            if (existingBars.size() > 0) {

                // store bars according to their date
                Map<Date, Bar> dateBarMap = new HashMap<>();
                for (Bar bar : existingBars) {
                    dateBarMap.put(bar.getDateTime(), bar);
                }

                //update existing bars
                int updatedCount = 0;
                for (Iterator<Bar> it = newBars.iterator(); it.hasNext();) {

                    Bar newBar = it.next();
                    Bar existingBar = dateBarMap.remove(newBar.getDateTime());
                    boolean updated = false;
                    if (existingBar != null) {
                        if (existingBar.getOpen().compareTo(newBar.getOpen()) != 0) {
                            existingBar.setOpen(newBar.getOpen());
                            updated = true;
                        }
                        if (existingBar.getHigh().compareTo(newBar.getHigh()) != 0) {
                            existingBar.setHigh(newBar.getHigh());
                            updated = true;
                        }
                        if (existingBar.getLow().compareTo(newBar.getLow()) != 0) {
                            existingBar.setLow(newBar.getLow());
                            updated = true;
                        }
                        if (existingBar.getClose().compareTo(newBar.getClose()) != 0) {
                            existingBar.setClose(newBar.getClose());
                            updated = true;
                        }
                        if (existingBar.getVol() != newBar.getVol()) {
                            existingBar.setVol(newBar.getVol());
                            updated = true;
                        }

                        it.remove();
                    }
                    updatedCount += updated ? 1 : 0;
                }

                // remove obsolete Bars
                this.barDao.deleteAll(dateBarMap.values());

                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("updated {} bars for security {}", updatedCount, securityId);
                }
            }
        }

        // save the newly retrieved Bars that do not exist yet in the db
        this.barDao.saveAll(newBars);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("created {} new bars for security {}", newBars.size(), securityId);
        }

    }
}
