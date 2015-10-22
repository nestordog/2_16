/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.service.noop;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.enumeration.MarketDataEventType;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.TimePeriod;
import ch.algotrader.service.HistoricalDataService;

public class NoopHistoricalDataServiceImpl implements HistoricalDataService {

    @Override
    public List<Bar> getHistoricalBars(long securityId, Date endDate, int timePeriodLength, TimePeriod timePeriod, Duration barSize, MarketDataEventType marketDataEventType, Map<String, String> properties) {
        return new ArrayList<>();
    }

    @Override
    public List<Tick> getHistoricalTicks(long securityId, Date endDate, int timePeriodLength, TimePeriod timePeriod, MarketDataEventType marketDataEventType, Map<String, String> properties) {
        return new ArrayList<>();
    }

    @Override
    public void storeHistoricalBars(long securityId, Date endDate, int timePeriodLength, TimePeriod timePeriod, Duration barSize, MarketDataEventType marketDataEventType, boolean replace, Map<String, String> properties) {
        // do nothing
    }

}
