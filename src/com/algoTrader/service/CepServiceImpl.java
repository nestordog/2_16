package com.algoTrader.service;

import java.io.File;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.algoTrader.entity.Rule;
import com.algoTrader.entity.Security;
import com.algoTrader.util.CustomDate;
import com.algoTrader.util.EsperService;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.StatementAwareUpdateListener;
import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esper.client.time.TimerControlEvent;
import com.espertech.esperio.AdapterCoordinator;
import com.espertech.esperio.AdapterCoordinatorImpl;
import com.espertech.esperio.AdapterInputSource;
import com.espertech.esperio.InputAdapter;
import com.espertech.esperio.csv.CSVInputAdapterSpec;
import com.espertech.esperio.csv.TickCSVInputAdapter;

public class CepServiceImpl extends CepServiceBase {

    private static String[] propertyOrder = {
        "dateTime",
        "last",
        "lastDateTime",
        "volBid",
        "volAsk",
        "bid",
        "ask",
        "vol",
        "openIntrest",
        "settlement"};

    private static Map propertyTypes = new HashMap();

    static {
        propertyTypes.put("dateTime", CustomDate.class);
        propertyTypes.put("last", BigDecimal.class);
        propertyTypes.put("lastDateTime", CustomDate.class);
        propertyTypes.put("volBid", int.class);
        propertyTypes.put("volAsk", int.class);
        propertyTypes.put("bid", BigDecimal.class);
        propertyTypes.put("ask", BigDecimal.class);
        propertyTypes.put("vol", int.class);
        propertyTypes.put("openIntrest", int.class);
        propertyTypes.put("settlement", BigDecimal.class);
    }

    protected void handleRunAll() throws java.lang.Exception {

        Collection col = getRuleDao().findActiveRules();

        for (Iterator it = col.iterator(); it.hasNext();) {
            Rule rule = (Rule)it.next();
            run(rule);
        }
    }

    protected void handleRun(Rule rule) throws java.lang.Exception {

        EPServiceProvider cep = EsperService.getEPServiceInstance();
        EPAdministrator cepAdm = cep.getEPAdministrator();

        EPStatement cepStatement;
        if (rule.isPattern()) {
            cepStatement = cepAdm.createPattern(rule.getDefinition(), rule.getName());
        } else {
            cepStatement = cepAdm.createEPL(rule.getDefinition(), rule.getName());
        }

        if (rule.getSubscriber() != null) {
            Class cl = Class.forName("com.algoTrader.subscriber." + rule.getSubscriber());
            Object obj = cl.newInstance();
            cepStatement.setSubscriber(obj);
        }

        if (rule.getListeners() != null) {
            String[] listeners = rule.getListeners().split("\\s");
            for (int i = 0; i < listeners.length; i++) {
                Class cl = Class.forName("com.algoTrader.listener." + listeners[i]);
                Object obj = cl.newInstance();
                if (obj instanceof StatementAwareUpdateListener) {
                    cepStatement.addListener((StatementAwareUpdateListener)obj);
                } else {
                    cepStatement.addListener((UpdateListener)obj);
                }
            }
        }
    }

    protected void handleSendEvent(Object object) throws java.lang.Exception {

        EPServiceProvider cep = EsperService.getEPServiceInstance();
        cep.getEPRuntime().sendEvent(object);
    }

    protected void handleStop(String ruleName) throws Exception {

        EPServiceProvider cep = EsperService.getEPServiceInstance();
        EPStatement statement = cep.getEPAdministrator().getStatement(ruleName);
        statement.destroy();
    }

    protected void handleStopAll() throws Exception {

        EPServiceProvider cep = EsperService.getEPServiceInstance();
        cep.destroy();
    }

    protected void handleSimulate(long startTime, String isin) throws Exception {

        EPServiceProvider cep = EsperService.getEPServiceInstance();
        EPRuntime cepRT = cep.getEPRuntime();
        cepRT.sendEvent(new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_EXTERNAL));
        cepRT.sendEvent(new CurrentTimeEvent(startTime)); // must send time event before first schedule pattern

        runAll();

        Security security = getSecurityDao().findByISIN(isin);

        File file = new File("results/tickdata/" + isin + ".csv");

        CSVInputAdapterSpec spec = new CSVInputAdapterSpec(new AdapterInputSource(file), "Tick");
        spec.setPropertyOrder(propertyOrder);
        spec.setPropertyTypes(propertyTypes);
        spec.setTimestampColumn("dateTime");
        spec.setUsingExternalTimer(true);

        InputAdapter inputAdapter = new TickCSVInputAdapter(cep, spec, security);
        inputAdapter.start();
    }


    protected void handleSimulate(long startTime, List list) throws Exception {

        EPServiceProvider cep = EsperService.getEPServiceInstance();
        EPRuntime cepRT = cep.getEPRuntime();
        cepRT.sendEvent(new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_EXTERNAL));
        cepRT.sendEvent(new CurrentTimeEvent(startTime)); // must send time event before first schedule pattern

        runAll();

        AdapterCoordinator coordinator = new AdapterCoordinatorImpl(cep, true, true);

        for (Iterator it = list.iterator(); it.hasNext(); ) {

            String isin = (String)it.next();
            Security security = getSecurityDao().findByISIN(isin);

            File file = new File("results/tickdata/" + isin + ".csv");

            CSVInputAdapterSpec spec = new CSVInputAdapterSpec(new AdapterInputSource(file), "Tick");
            spec.setPropertyOrder(propertyOrder);
            spec.setPropertyTypes(propertyTypes);
            spec.setTimestampColumn("dateTime");
            spec.setUsingExternalTimer(true);

            InputAdapter inputAdapter = new TickCSVInputAdapter(cep, spec, security);
            coordinator.coordinate(inputAdapter);
        }

        coordinator.start();
    }
}
