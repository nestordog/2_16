package com.algoTrader.service;

import java.io.File;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.apache.log4j.Logger;

import com.algoTrader.entity.Interpolation;
import com.algoTrader.entity.Rule;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.Transaction;
import com.algoTrader.enumeration.RuleName;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.CustomDate;
import com.algoTrader.util.EsperService;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.PropertiesUtil;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
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

    private static Map<String, Object> propertyTypes = new HashMap<String, Object>();

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

    @SuppressWarnings("unchecked")
    protected void handleRun() throws Exception {

        EPServiceProvider cep = EsperService.getEPServiceInstance();

        AdapterCoordinator coordinator = new AdapterCoordinatorImpl(cep, true, true);

        List<Security> securities = getSecurityDao().findSecuritiesOnWatchlist();
        for (Security security : securities) {

            if (security.getIsin() == null) {
                logger.warn("no tickdata available for " + security.getSymbol());
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


    protected Interpolation handleGetInterpolation() throws Exception {

        EPStatement statement = EsperService.getStatement(RuleName.CREATE_INTERPOLATION);

        return (Interpolation)statement.iterator().next().getUnderlying();
    }

    @SuppressWarnings("unchecked")
    protected void handleInit() throws Exception {

        // delete all dummySecurities
        getSecurityDao().remove(getSecurityDao().findDummySecurities());

        // delete all transactions except the initial CREDIT
        Collection<Transaction> transactions = getTransactionDao().loadAll();
        CollectionUtils.filter(transactions, new Predicate(){
            public boolean evaluate(Object obj) {
                return ((Transaction)obj).getType().equals(TransactionType.CREDIT) ? false : true;
            }});
        getTransactionDao().remove(transactions);

        // delete al positions
        getPositionDao().remove(getPositionDao().loadAll());

        // remove all the targets from preparedRules
        Collection<Rule> rules = getRuleDao().findPreparedRules();
        CollectionUtils.transform(rules, new Transformer() {
            public Object transform(Object arg) {
                ((Rule)arg).setTarget(null);
                return arg;
            }});
        getRuleDao().update(rules);
    }
}
