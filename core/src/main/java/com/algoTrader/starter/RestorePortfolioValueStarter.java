package com.algoTrader.starter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.strategy.Strategy;
import com.algoTrader.service.LookupService;
import com.algoTrader.service.PortfolioPersistenceService;

public class RestorePortfolioValueStarter {

    private static DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

    public static void main(String[] args) throws ParseException {

        ServiceLocator.instance().init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);
        PortfolioPersistenceService accountService = ServiceLocator.instance().getService("portfolioPersistenceService", PortfolioPersistenceService.class);
        LookupService lookupService = ServiceLocator.instance().getLookupService();

        Strategy strategy = lookupService.getStrategyByName(args[0]);

        Date fromDate = formatter.parse(args[1]);
        Date toDate = formatter.parse(args[2]);

        accountService.restorePortfolioValues(strategy, fromDate, toDate);

        ServiceLocator.instance().shutdown();
    }
}
