package com.algoTrader.starter;

import com.algoTrader.ServiceLocator;
import com.algoTrader.service.ib.IbHistoricalDataService;

public class HistoricalDataStarter {

    public static void main(String[] args) {

        IbHistoricalDataService service = ServiceLocator.instance().getIbHistoricalDataService();

        service.init();
        service.requestHistoricalData(5);
    }
}
