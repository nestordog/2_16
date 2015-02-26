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
package ch.algotrader.starter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import ch.algotrader.ServiceLocator;
import ch.algotrader.enumeration.BarType;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.TimePeriod;
import ch.algotrader.service.HistoricalDataService;

/**
 * Starter Class to download historical bars and update/replace bars in the database
 * <p>
 * Usage: {@code HistoricalDataStarter updateBars endDate timePeriodLength timePeriod barType barSize securityId(s)}
 * <p>
 * Examle: {@code HistoricalDataStarter true 20120101 4 WEEK TRADES DAY_1 10:11:12}
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision: 6388 $ $Date: 2013-11-01 11:01:37 +0100 (Fr, 01 Nov 2013) $
 */
public class HistoricalDataStarter {

    private static SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

    public static void main(String[] args) throws Exception {

        boolean update = Boolean.parseBoolean(args[0]);

        Date endDate = format.parse(args[1]);

        int timePeriodLength = Integer.parseInt(args[2]);
        TimePeriod timePeriod = TimePeriod.valueOf(args[3]);

        BarType barType = BarType.valueOf(args[4]);
        Duration barSize = Duration.valueOf(args[5]);

        String[] securityIdStrings = args[6].split(":");
        int[] securityIds = new int[securityIdStrings.length];
        for (int i = 0; i < securityIdStrings.length; i++) {
            securityIds[i] = Integer.valueOf(securityIdStrings[i]);
        }

        ServiceLocator.instance().init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);

        ServiceLocator.instance().getLifecycleManager().initServices();

        HistoricalDataService service = ServiceLocator.instance().getService("historicalDataService", HistoricalDataService.class);
        for (int securityId : securityIds) {
            if (update) {
                service.updateHistoricalBars(securityId, endDate, timePeriodLength, timePeriod, barSize, barType, new HashMap<String, String>());
            } else {
                service.replaceHistoricalBars(securityId, endDate, timePeriodLength, timePeriod, barSize, barType, new HashMap<String, String>());
            }
        }

        ServiceLocator.instance().shutdown();
    }
}
