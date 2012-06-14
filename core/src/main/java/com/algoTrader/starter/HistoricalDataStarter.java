package com.algoTrader.starter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.algoTrader.ServiceLocator;
import com.algoTrader.enumeration.BarType;
import com.algoTrader.service.ib.IBHistoricalDataService;

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
        IBHistoricalDataService service = ServiceLocator.instance().getService("iBHistoricalDataService", IBHistoricalDataService.class);

        service.init();

        service.download1MinTicks(securityIds, barTypes, startDate, endDate);

        ServiceLocator.instance().shutdown();
    }

}
