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
package ch.algotrader.starter;

import java.util.Date;
import java.util.HashMap;

import ch.algotrader.ServiceLocator;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.MarketDataEventType;
import ch.algotrader.enumeration.TimePeriod;
import ch.algotrader.service.HistoricalDataService;
import ch.algotrader.util.DateTimeLegacy;

/**
 * Starter Class to download historical bars and replace/update bars in the database
 * <p>
 * Usage: {@code HistoricalDataStarter replaceBars endDate timePeriodLength timePeriod marketDataEventType barSize securityId(s)}
 * <p>
 * Example: {@code HistoricalDataStarter true 2015-10-01 4 WEEK TRADES DAY_1 10:11:12}
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision: 6388 $ $Date: 2013-11-01 11:01:37 +0100 (Fr, 01 Nov 2013) $
 */
public class HistoricalDataStarter {

    public static void main(String[] args) throws Exception {

        boolean replace = Boolean.parseBoolean(args[0]);

        Date endDate = DateTimeLegacy.parseAsLocalDate(args[1]);

        int timePeriodLength = Integer.parseInt(args[2]);
        TimePeriod timePeriod = TimePeriod.valueOf(args[3]);

        MarketDataEventType marketDataEventType = MarketDataEventType.valueOf(args[4]);
        Duration barSize = Duration.valueOf(args[5]);

        String[] securityIdStrings = args[6].split(":");
        long[] securityIds = new long[securityIdStrings.length];
        for (int i = 0; i < securityIdStrings.length; i++) {
            securityIds[i] = Long.parseLong(securityIdStrings[i]);
        }

        ServiceLocator serviceLocator = ServiceLocator.instance();
        serviceLocator.init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);
        try {
            serviceLocator.runServices();

            HistoricalDataService service = serviceLocator.getService("historicalDataService", HistoricalDataService.class);
            for (long securityId : securityIds) {
                service.storeHistoricalBars(securityId, endDate, timePeriodLength, timePeriod, barSize, marketDataEventType, replace, new HashMap<>());
            }
        } finally {
            serviceLocator.shutdown();
        }
    }
}
