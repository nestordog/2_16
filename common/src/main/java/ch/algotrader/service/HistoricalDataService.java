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
import ch.algotrader.enumeration.BarType;
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
     * See <a href="
     * http://www.interactivebrokers.com/php/apiUsersGuide/apiguide/api/historical_data_limitations.htm">Historical
     * Data Limitations</a> for further details.
     * @param barSize The Bar Size for which Bars should be retrieved. Example: MIN_1
     * @param barType The {@link BarType BarType} that should be retrieved.
     * @param properties Arbitrary properties that should be added to the request
     */
    public List<Bar> getHistoricalBars(int securityId, Date endDate, int timePeriodLength, TimePeriod timePeriod, Duration barSize, BarType barType, Map<String, String> properties);

    /**
     * Downloads historical Bars for the specified Security and stores them in the database,
     * existing Bars are not overwritten.
     * @param endDate The End Date up to which historical Bars should be retrieved.
     * @param timePeriodLength The length of the Time Period for which Bars should be retrieved. Example 10 x 1 Day
     * @param timePeriod The type of the Time Period for which Bars should be retrieved. Example: 1 Day.
     * See <a href="
     * http://www.interactivebrokers.com/php/apiUsersGuide/apiguide/api/historical_data_limitations.htm">Historical
     * Data Limitations</a> for further details.
     * @param barSize The Bar Size for which Bars should be retrieved. Example: MIN_1
     * @param barType The {@link BarType BarType} that should be retrieved.
     * @param properties Arbitrary properties that should be added to the request
     */
    public void updateHistoricalBars(int securityId, Date endDate, int timePeriodLength, TimePeriod timePeriod, Duration barSize, BarType barType, Map<String, String> properties);

    /**
     * Downloads historical Bars for the specified Security and stores them in the database,
     * existing Bars are replaced.
     * @param endDate The End Date up to which historical Bars should be retrieved.
     * @param timePeriodLength The length of the Time Period for which Bars should be retrieved. Example 10 x 1 Day
     * @param timePeriod The type of the Time Period for which Bars should be retrieved. Example: 1 Day.
     * See <a href="
     * http://www.interactivebrokers.com/php/apiUsersGuide/apiguide/api/historical_data_limitations.htm">Historical
     * Data Limitations</a> for further details.
     * @param barSize The Bar Size for which Bars should be retrieved. Example: MIN_1
     * @param barType The {@link BarType BarType} that should be retrieved.
     * @param properties Arbitrary properties that should be added to the request
     */
    public void replaceHistoricalBars(int securityId, Date endDate, int timePeriodLength, TimePeriod timePeriod, Duration barSize, BarType barType, Map<String, String> properties);

}
