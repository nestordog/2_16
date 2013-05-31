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
package ch.algotrader.starter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import ch.algotrader.ServiceLocator;
import ch.algotrader.enumeration.BarType;
import ch.algotrader.service.ib.IBNativeHistoricalDataService;

/**
 * Starter Class to download historical tick data
 * <p>
 * Usage: {@code HistoricalDataStarter fromDate toDate barType(s) securityId(s)}
 * <p>
 * Examle: {@code 20120115 20120216 BID:ASK 29:103850:104586:104587:104588}
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class HistoricalDataStarter {

    private static SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd  HH:mm:ss");

    public static void main(String[] args) throws ParseException {

        Date startDate = format.parse(args[0] + "  24:00:00");
        Date endDate = format.parse(args[1] + "  24:00:00");

        String[] barTypesString = args[2].split(":");
        Set<BarType> barTypes = new HashSet<BarType>();
        for (String element : barTypesString) {
            barTypes.add(BarType.fromString(element));
        }

        String[] securityIdStrings = args[3].split(":");
        int[] securityIds = new int[securityIdStrings.length];
        for (int i = 0; i < securityIdStrings.length; i++) {
            securityIds[i] = Integer.valueOf(securityIdStrings[i]);
        }

        ServiceLocator.instance().init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);
        IBNativeHistoricalDataService service = ServiceLocator.instance().getService("iBHistoricalDataService", IBNativeHistoricalDataService.class);

        service.init();

        service.download1MinTicks(securityIds, barTypes, startDate, endDate);

        ServiceLocator.instance().shutdown();
    }
}
