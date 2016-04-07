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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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

        if (args.length < 7) {
            System.out.println("Usage: HistoricalDataStarter " +
                    "<replaceBars> " +
                    "<endDate> " +
                    "<timePeriodLength> " +
                    "<timePeriod> " +
                    "<marketDataEventType> " +
                    "<barSize> " +
                    "<security ids>... \r\n"+
                    "where\r\n" +
                    " replaceBars = true | false \r\n" +
                    " endDate: date in format YYYY-MM-DD \r\n" +
                    " timePeriodLength: non-negative integer number \r\n" +
                    " timePeriod: SEC | DAY | WEEK | MONTH | YEAR \r\n" +
                    " marketDataEventType: TRADES | MIDPOINT | BID | ASK | BID_ASK | BEST_BID | BEST_ASK\r\n" +
                    " barSize: SEC_1 | SEC_5 | MIN_1 | MIN_2 | MIN_3 | MIN_5 | MIN_10 | MIN_15 | MIN_30 | HOUR_1 | HOUR_2 | DAY_1 | DAY_2 \r\n" +
                    " securityIds: list of security ids");
            System.exit(1);
            return;
        }

        boolean replace = Boolean.parseBoolean(args[0]);

        Date endDate = DateTimeLegacy.parseAsLocalDate(args[1]);

        int timePeriodLength = Integer.parseInt(args[2]);
        TimePeriod timePeriod = TimePeriod.valueOf(args[3]);

        MarketDataEventType marketDataEventType = MarketDataEventType.valueOf(args[4]);
        Duration barSize = Duration.valueOf(args[5]);

        List<Long> securityIds = new ArrayList<>();
        for (int i = 6; i < args.length; i++) {
            securityIds.add(Long.parseLong(args[i]));
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
