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
package ch.algotrader.service.noop;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.enumeration.BarType;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.TimePeriod;
import ch.algotrader.service.HistoricalDataService;

public class NoopHistoricalDataServiceImpl implements HistoricalDataService {

    @Override
    public List<Bar> getHistoricalBars(long securityId, Date endDate, int timePeriodLength, TimePeriod timePeriod, Duration barSize, BarType barType, Map<String, String> properties) {
        return new ArrayList<>();
    }

    @Override
    public void updateHistoricalBars(long securityId, Date endDate, int timePeriodLength, TimePeriod timePeriod, Duration barSize, BarType barType, Map<String, String> properties) {
        // do nothing
    }

    @Override
    public void replaceHistoricalBars(long securityId, Date endDate, int timePeriodLength, TimePeriod timePeriod, Duration barSize, BarType barType, Map<String, String> properties) {
        // do nothing
    }

}
