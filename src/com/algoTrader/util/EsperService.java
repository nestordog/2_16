package com.algoTrader.util;

import com.algoTrader.entity.TickImpl;
import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;

public class EsperService {

    private static EPServiceProvider cep;

    public static EPServiceProvider getEPServiceInstance() {

        if (cep == null) {

            Configuration cepConfig = new Configuration();

            cepConfig.addEventType("Tick", TickImpl.class);

            cepConfig.addImport(EntityUtil.class);
            cepConfig.addImport(StockOptionUtil.class);
            cepConfig.addImport(DateUtil.class);
            cepConfig.addImport("com.algoTrader.entity.*");

            cep = EPServiceProviderManager.getDefaultProvider(cepConfig);
        }
        return cep;
    }

    public static boolean hasInstance() {

        return (cep != null);
    }
}
