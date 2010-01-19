package com.algoTrader.util;

import java.math.BigDecimal;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Security;

public class LookupUtil {

    public static Security[] getSecuritiesInPortfolio() {

        ServiceLocator serviceLocator = ServiceLocator.instance();
        return serviceLocator.getLookupService().getAllSecuritiesInPortfolio();
    }

    public static Security[] getDummySecurities() {

        ServiceLocator serviceLocator = ServiceLocator.instance();
        return serviceLocator.getLookupService().getDummySecurities();
    }

    public static BigDecimal[] getStrikesOnWatchlist() {

        ServiceLocator serviceLocator = ServiceLocator.instance();
        return serviceLocator.getLookupService().getStrikesOnWatchlist();
    }
}
