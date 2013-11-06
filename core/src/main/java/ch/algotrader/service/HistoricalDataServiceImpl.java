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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;

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
public abstract class HistoricalDataServiceImpl extends HistoricalDataServiceBase {

    @Override
    protected void handleUpdateHistoricalBars(int securityId, Date endDate, int timePeriodLength, TimePeriod timePeriod, Duration barSize, BarType barType) throws Exception {

        // get all Bars from the Market Data Provider
        List<Bar> bars = getHistoricalBars(securityId, endDate, timePeriodLength, timePeriod, barSize, barType);

        // get the last Bar int the Database
        final Bar lastBar = CollectionUtil.getSingleElementOrNull(getBarDao().findBarsBySecurityAndBarSize(1, 1, securityId, barSize));

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
        getBarDao().create(bars);
    }

    @Override
    protected void handleReplaceHistoricalBars(int securityId, Date endDate, int timePeriodLength, TimePeriod timePeriod, Duration barSize, BarType barType) throws Exception {

        // get all Bars from the Market Data Provider
        List<Bar> newBars = getHistoricalBars(securityId, endDate, timePeriodLength, timePeriod, barSize, barType);

        // remove all Bars in the database after the first newly retrieved Bar
        final Bar firstBar = CollectionUtil.getFirstElementOrNull(newBars);
        if (firstBar != null) {

            List<Bar> existingBars = getBarDao().findBarsBySecurityBarSizeAndMinDate(securityId, barSize, firstBar.getDateTime());

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
                getBarDao().remove(dateBarMap.values());
            }
        }

        // save the newly retrieved Bars that do not exist yet in the db
        getBarDao().create(newBars);
    }
}
