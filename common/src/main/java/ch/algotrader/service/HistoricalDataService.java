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
import java.util.List;
import java.util.Map;

import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.enumeration.MarketDataEventType;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.TimePeriod;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface HistoricalDataService {

    /**
     * Gets historical Bars for the specified Security.
     * @param endDate The End Date up to which historical Bars should be retrieved.
     * @param timePeriodLength The length of the Time Period for which Bars should be retrieved. Example 10 x 1 Day
     * @param timePeriod The type of the Time Period for which Bars should be retrieved. Example: 1 Day.
     * @param barSize The Bar Size for which Bars should be retrieved. Example: MIN_1
     * @param marketDataEventType The {@link MarketDataEventType MarketDataEventType} that should be retrieved.
     * @param properties Arbitrary properties that should be added to the request
     */
    public List<Bar> getHistoricalBars(long securityId, Date endDate, int timePeriodLength, TimePeriod timePeriod, Duration barSize, MarketDataEventType marketDataEventType, Map<String, String> properties);

    /**
     * Gets historical Ticks for the specified Security.
     * @param endDate The End Date up to which historical Ticks should be retrieved.
     * @param timePeriodLength The length of the Time Period for which Ticks should be retrieved. Example 10 x 1 Day
     * @param timePeriod The type of the Time Period for which Ticks should be retrieved. Example: 1 Day.
     * @param marketDataEventType The {@link MarketDataEventType MarketDataEventType} that should be retrieved.
     * @param properties Arbitrary properties that should be added to the request
     */
    public List<Tick> getHistoricalTicks(long securityId, Date endDate, int timePeriodLength, TimePeriod timePeriod, MarketDataEventType marketDataEventType, Map<String, String> properties);

    /**
     * Downloads historical Bars for the specified Security and stores them in the database,
     * @param endDate The End Date up to which historical Bars should be retrieved.
     * @param timePeriodLength The length of the Time Period for which Bars should be retrieved. Example 10 x 1 Day
     * @param timePeriod The type of the Time Period for which Bars should be retrieved. Example: 1 Day.
     * @param barSize The Bar Size for which Bars should be retrieved. Example: MIN_1
     * @param marketDataEventType The {@link MarketDataEventType MarketDataEventType} that should be retrieved.
     * @param replace should existing Bars be replaced in the database
     * @param properties Arbitrary properties that should be added to the request
     */
    public void storeHistoricalBars(long securityId, Date endDate, int timePeriodLength, TimePeriod timePeriod, Duration barSize, MarketDataEventType marketDataEventType, boolean replace, Map<String, String> properties);

}
