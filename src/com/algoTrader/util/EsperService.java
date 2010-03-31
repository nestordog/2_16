package com.algoTrader.util;

import com.algoTrader.enumeration.RuleName;
import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.time.CurrentTimeEvent;

public class EsperService {

    private static EPServiceProvider cep;

    public static EPServiceProvider getEPServiceInstance() {

        if (cep == null) {

            Configuration config = new Configuration();
            config.configure();

            cep = EPServiceProviderManager.getDefaultProvider(config);

            // must send time event (1.1.1990) before first schedule pattern
            cep.getEPRuntime().sendEvent(new CurrentTimeEvent(631148400000l));
        }
        return cep;
    }

    public static boolean hasInstance() {

        return (cep != null);
    }

    public static EPStatement getStatement(RuleName ruleName) {

        return getEPServiceInstance().getEPAdministrator().getStatement(ruleName.getValue());
    }

    public static long getCurrentTime() {

        return getEPServiceInstance().getEPRuntime().getCurrentTime();
    }

    public static void sendEvent(Object obj) {

        getEPServiceInstance().getEPRuntime().sendEvent(obj);
    }
}
