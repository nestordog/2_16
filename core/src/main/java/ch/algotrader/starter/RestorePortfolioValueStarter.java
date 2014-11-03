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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.PortfolioService;

/**
 * Starter Class to restores all PortfolioValues of a specified Strategy
 * <p>
 * Usage: {@code RestorePortfolioValueStarter strategyName fromDate toDate}
 * <p>
 * Example: {@code RestorePortfolioValueStarter MOV 01.01.2012_00:00:00 31.12.2012_00:00:00}
 */
public class RestorePortfolioValueStarter {

    private static DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy_HH:mm:ss");

    public static void main(String[] args) throws ParseException {

        ServiceLocator.instance().init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);
        PortfolioService portfolioService = ServiceLocator.instance().getService("portfolioService", PortfolioService.class);
        LookupService lookupService = ServiceLocator.instance().getLookupService();

        Strategy strategy = lookupService.getStrategyByName(args[0]);

        Date fromDate = formatter.parse(args[1]);
        Date toDate = formatter.parse(args[2]);

        portfolioService.restorePortfolioValues(strategy, fromDate, toDate);

        ServiceLocator.instance().shutdown();
    }
}
