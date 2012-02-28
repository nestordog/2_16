package com.algoTrader.starter;

import com.algoTrader.ServiceLocator;
import com.algoTrader.service.ib.IBHistoricalDataService;

public class HistoricalDataStarter {

    public static void main(String[] args) {

        String startDate = args[0];

        String endDate = args[1];

        String[] whatToShow = args[2].split(":");

        String[] securityIdStrings = args[3].split(":");
        int[] securityIds = new int[securityIdStrings.length];
        for (int i = 0; i < securityIdStrings.length; i++) {
            securityIds[i] = Integer.valueOf(securityIdStrings[i]);
        }

        ServiceLocator.instance().init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);
        IBHistoricalDataService service = ServiceLocator.instance().getService("iBHistoricalDataService", IBHistoricalDataService.class);

        service.init();

        service.downloadHistoricalData(securityIds, whatToShow, startDate, endDate);

        ServiceLocator.instance().shutdown();
    }

    public static void main2(String[] args) {

        String[] whatToShow = args[0].split(":");

        String[] startDate = new String[args.length - 1];
        String[] endDate = new String[args.length - 1];
        int[] securityIds = new int[args.length - 1];
        for (int i = 0; i < args.length - 1; i++) {
            String[] batch = args[i + 1].split(":");
            startDate[i] = batch[0];
            endDate[i] = batch[1];
            securityIds[i] = Integer.valueOf(batch[2]);
        }

        ServiceLocator.instance().init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);
        IBHistoricalDataService service = ServiceLocator.instance().getService("iBHistoricalDataService", IBHistoricalDataService.class);

        service.init();

        service.downloadHistoricalData(securityIds, whatToShow, startDate, endDate);

        ServiceLocator.instance().shutdown();
    }
}
