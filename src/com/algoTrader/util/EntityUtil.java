package com.algoTrader.util;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Security;

public class EntityUtil {

    public static Security[] getAllSecurities() {

        ServiceLocator serviceLocator = ServiceLocator.instance();
        return (Security[])serviceLocator.getEntityService().getAllSecurities();
    }
}
