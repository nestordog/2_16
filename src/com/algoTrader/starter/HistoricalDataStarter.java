package com.algoTrader.starter;

import com.algoTrader.ServiceLocator;
import com.algoTrader.service.ib.IbHistoricalDataService;

public class HistoricalDataStarter {

    public static void main(String[] args) {

        IbHistoricalDataService service = ServiceLocator.instance().getIbHistoricalDataService();

        service.init();
        service.requestHistoricalData(new int[] { 10711, 10712 }, new String[] { "BID", "ASK", "TRADES" }, "20100719", "20101013");
    }
}
