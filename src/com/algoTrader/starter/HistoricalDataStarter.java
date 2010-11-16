package com.algoTrader.starter;

import com.algoTrader.ServiceLocator;
import com.algoTrader.service.ib.IbHistoricalDataService;
import com.algoTrader.util.PropertiesUtil;

public class HistoricalDataStarter {

    public static void main(String[] args) {

        PropertiesUtil.setProperty("strategie.dataSet", "expiredOptions");
        PropertiesUtil.setProperty("ib.historicalDataServiceEnabled", "true");
        PropertiesUtil.setProperty("ib.accountServiceEnabled", "false");
        PropertiesUtil.setProperty("ib.stockOptionRetrieverServiceEnabled", "false");
        PropertiesUtil.setProperty("ib.tickServiceEnabled", "false");
        PropertiesUtil.setProperty("ib.transactionServiceEnabled", "false");

        String startDate = args[0];

        String endDate = args[1];

        String[] whatToShow = args[2].split(":");

        String[] securityIdStrings = args[3].split(":");
        int[] securityIds = new int[securityIdStrings.length];
        for (int i = 0; i < securityIdStrings.length; i++) {
            securityIds[i] = Integer.valueOf(securityIdStrings[i]);
        }

        IbHistoricalDataService service = ServiceLocator.instance().getIbHistoricalDataService();

        service.requestHistoricalData(securityIds, whatToShow, startDate, endDate);
    }
}
