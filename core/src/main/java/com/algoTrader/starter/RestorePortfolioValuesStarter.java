package com.algoTrader.starter;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.algoTrader.ServiceLocator;
import com.algoTrader.service.sim.SimAccountService;

public class RestorePortfolioValuesStarter {

    private static SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy");

    public static void main(String[] args) throws Exception {

        ServiceLocator.instance().init(ServiceLocator.SIMULATION_BEAN_REFERENCE_LOCATION);

        Date fromDate = format.parse(args[0]);
        Date toDate = format.parse(args[1]);

        ServiceLocator.instance().getService("accountService", SimAccountService.class).restorePortfolioValues(fromDate, toDate);

        ServiceLocator.instance().shutdown();
    }
}
