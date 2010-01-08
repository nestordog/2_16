package com.algoTrader.service;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.algoTrader.entity.Security;
import com.algoTrader.util.CustomDate;
import com.algoTrader.util.EsperService;
import com.algoTrader.util.MyLogger;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esper.client.time.TimerControlEvent;
import com.espertech.esperio.AdapterCoordinator;
import com.espertech.esperio.AdapterCoordinatorImpl;
import com.espertech.esperio.AdapterInputSource;
import com.espertech.esperio.InputAdapter;
import com.espertech.esperio.csv.CSVInputAdapterSpec;
import com.espertech.esperio.csv.TickCSVInputAdapter;

public class SimulationServiceImpl extends SimulationServiceBase {

    private static Logger logger = MyLogger.getLogger(SimulationServiceImpl.class.getName());

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

    public SimulationServiceImpl() {

        SimulationServiceImpl.propertyTypes.put("dateTime", CustomDate.class);
        SimulationServiceImpl.propertyTypes.put("last", BigDecimal.class);
        SimulationServiceImpl.propertyTypes.put("lastDateTime", CustomDate.class);
        SimulationServiceImpl.propertyTypes.put("volBid", int.class);
        SimulationServiceImpl.propertyTypes.put("volAsk", int.class);
        SimulationServiceImpl.propertyTypes.put("bid", BigDecimal.class);
        SimulationServiceImpl.propertyTypes.put("ask", BigDecimal.class);
        SimulationServiceImpl.propertyTypes.put("vol", int.class);
        SimulationServiceImpl.propertyTypes.put("openIntrest", int.class);
        SimulationServiceImpl.propertyTypes.put("settlement", BigDecimal.class);
    }

    protected void handleSimulate(long startTime, String isin) throws Exception {


        Security security = getSecurityDao().findByISIN(isin);
        simulate(startTime, security);
    }

    protected void handleSimulate(long startTime, Security security) throws Exception {

        EPServiceProvider cep = EsperService.getEPServiceInstance();
        EPRuntime cepRT = cep.getEPRuntime();
        cepRT.sendEvent(new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_EXTERNAL));
        cepRT.sendEvent(new CurrentTimeEvent(startTime)); // must send time event before first schedule pattern

        File file = new File("results/tickdata/" + security.getIsin() + ".csv");

        CSVInputAdapterSpec spec = new CSVInputAdapterSpec(new AdapterInputSource(file), "Tick");
        spec.setPropertyOrder(propertyOrder);
        spec.setPropertyTypes(propertyTypes);
        spec.setTimestampColumn("dateTime");
        spec.setUsingExternalTimer(true);

        InputAdapter inputAdapter = new TickCSVInputAdapter(cep, spec, security);
        inputAdapter.start();

        logger.debug("started simulation for security " + security.getIsin());
    }

    protected void handleSimulate(long startTime, List isins) throws Exception {

        List securities = new ArrayList();
        for (Iterator it = isins.iterator(); it.hasNext(); ) {
            String isin = (String)it.next();
            Security security = getSecurityDao().findByISIN(isin);
            securities.add(security);
        }
        simulateSecurites(startTime, securities);
    }

    protected void handleSimulateWatchlist(long startTime) throws Exception {

        List securities = getSecurityDao().findOnWatchlist();
        simulateSecurites(startTime, securities);
    }

    private void simulateSecurites(long startTime, List securities) {

        EPServiceProvider cep = EsperService.getEPServiceInstance();
        EPRuntime cepRT = cep.getEPRuntime();
        cepRT.sendEvent(new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_EXTERNAL));
        cepRT.sendEvent(new CurrentTimeEvent(startTime)); // must send time event before first schedule pattern

        AdapterCoordinator coordinator = new AdapterCoordinatorImpl(cep, true, true);

        for (Iterator it = securities.iterator(); it.hasNext(); ) {

            Security security = (Security)it.next();

            File file = new File("results/tickdata/" + security.getIsin() + ".csv");

            CSVInputAdapterSpec spec = new CSVInputAdapterSpec(new AdapterInputSource(file), "Tick");
            spec.setPropertyOrder(propertyOrder);
            spec.setPropertyTypes(propertyTypes);
            spec.setTimestampColumn("dateTime");
            spec.setUsingExternalTimer(true);

            InputAdapter inputAdapter = new TickCSVInputAdapter(cep, spec, security);
            coordinator.coordinate(inputAdapter);

            logger.debug("started simulation for security " + security.getIsin());
        }
        coordinator.start();
    }

}
