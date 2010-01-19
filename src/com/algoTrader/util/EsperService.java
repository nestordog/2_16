package com.algoTrader.util;

import com.algoTrader.entity.TickImpl;
import com.algoTrader.entity.TransactionImpl;
import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.ConfigurationEngineDefaults.Threading;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esper.client.time.TimerControlEvent;

public class EsperService {

    private static boolean simulation = new Boolean(PropertiesUtil.getProperty("simulation")).booleanValue();
    private static long startTime = Long.parseLong(PropertiesUtil.getProperty("simulation.startTime"));

    private static EPServiceProvider cep;

    public static EPServiceProvider getEPServiceInstance() {

        if (cep == null) {

            Configuration config = new Configuration();

            // event types
            config.addEventType("Tick", TickImpl.class);
            config.addEventType("Transaction", TransactionImpl.class);

            // User Defined Functions
            config.addImport(LookupUtil.class);
            config.addImport(StockOptionUtil.class);
            config.addImport(DateUtil.class);

            // Entities
            config.addImport("com.algoTrader.entity.*");

            // Variables
            config.addVariable("var_simulation", boolean.class, new Boolean(PropertiesUtil.getProperty("simulation")).booleanValue());
            config.addVariable("var_isin", String.class, PropertiesUtil.getProperty("simulation.isin"));

            // Settings
            //config.getEngineDefaults().getExpression().setUdfCache(false); // some Methods hit the DB!
            config.getEngineDefaults().getExecution().setPrioritized(true); // execute according to the rule priority
            //config.getEngineDefaults().getThreading().setThreadPoolInbound(true);
            //config.getEngineDefaults().getThreading().setThreadPoolOutbound(true);
            //config.getEngineDefaults().getThreading().setThreadPoolTimerExec(true);
            //config.getEngineDefaults().getThreading().setThreadPoolRouteExec(true);

            cep = EPServiceProviderManager.getDefaultProvider(config);

            if (simulation) {
                cep.getEPRuntime().sendEvent(new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_EXTERNAL));
                cep.getEPRuntime().sendEvent(new CurrentTimeEvent(startTime)); // must send time event before first schedule pattern
            }
        }
        return cep;
    }

    public static boolean hasInstance() {

        return (cep != null);
    }
}
