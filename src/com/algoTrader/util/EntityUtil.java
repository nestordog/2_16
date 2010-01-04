package com.algoTrader.util;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Security;

public class EntityUtil {

    public static Security[] getSecuritiesInPortfolio() {

        ServiceLocator serviceLocator = ServiceLocator.instance();
        return serviceLocator.getEntityService().getAllSecuritiesInPortfolio();
    }

    public static Security[] getDummySecurities() {

        ServiceLocator serviceLocator = ServiceLocator.instance();
        Security[] securities = serviceLocator.getEntityService().getDummySecurities();
        return securities;
    }
}
