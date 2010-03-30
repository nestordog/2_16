package com.algoTrader.service;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.algoTrader.entity.Security;
import com.algoTrader.util.CustomDate;
import com.algoTrader.util.EsperService;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.PropertiesUtil;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esperio.AdapterCoordinator;
import com.espertech.esperio.AdapterCoordinatorImpl;
import com.espertech.esperio.AdapterInputSource;
import com.espertech.esperio.InputAdapter;
import com.espertech.esperio.csv.CSVInputAdapterSpec;
import com.espertech.esperio.csv.TickCSVInputAdapter;

public class SimulationServiceImpl extends SimulationServiceBase {

    private static Logger logger = MyLogger.getLogger(SimulationServiceImpl.class.getName());
    private static String dataSet = PropertiesUtil.getProperty("simulation.dataSet");

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

    protected void handleRun() throws Exception {

        EPServiceProvider cep = EsperService.getEPServiceInstance();

        AdapterCoordinator coordinator = new AdapterCoordinatorImpl(cep, true, true);

        List securities = getSecurityDao().findSecuritiesOnWatchlist();
        for (Iterator it = securities.iterator(); it.hasNext(); ) {

            Security security = (Security)it.next();

            if (security.getIsin() == null) {
                logger.warn("not tickdata available for " + security.getSymbol());
                continue;
            }

            File file = new File("results/tickdata/" + dataSet + "/" + security.getIsin() + ".csv");

            if (file != null && file.exists()) {

                CSVInputAdapterSpec spec = new CSVInputAdapterSpec(new AdapterInputSource(file), "Tick");
                spec.setPropertyOrder(propertyOrder);
                spec.setPropertyTypes(propertyTypes);
                spec.setTimestampColumn("dateTime");
                spec.setUsingExternalTimer(true);

                InputAdapter inputAdapter = new TickCSVInputAdapter(cep, spec, security.getId());
                coordinator.coordinate(inputAdapter);

                logger.debug("started simulation for security " + security.getIsin());
            }
        }
        coordinator.start();
    }
}
