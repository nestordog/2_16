package com.algoTrader.starter;

import com.algoTrader.ServiceLocator;
import com.algoTrader.service.ib.IbTickService;
import com.algoTrader.util.PropertiesUtil;

public class ImportTickStarter {

    public static void main(String[] args) {

        PropertiesUtil.setProperty("simulation", "false");
        PropertiesUtil.setProperty("strategie.dataSet", "expiredOptions");
        PropertiesUtil.setProperty("ib.historicalDataServiceEnabled", "false");
        PropertiesUtil.setProperty("ib.accountServiceEnabled", "false");
        PropertiesUtil.setProperty("ib.stockOptionRetrieverServiceEnabled", "false");
        PropertiesUtil.setProperty("ib.tickServiceEnabled", "false");
        PropertiesUtil.setProperty("ib.transactionServiceEnabled", "false");

        IbTickService service = ServiceLocator.instance().getIbTickService();

        String[] isins = args[0].split(":");
        for (String isin : isins) {
            service.importTicks(isin);
        }
    }
}
