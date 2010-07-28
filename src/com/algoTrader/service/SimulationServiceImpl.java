package com.algoTrader.service;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.apache.log4j.Logger;

import com.algoTrader.entity.MonthlyPerformance;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.Rule;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.Transaction;
import com.algoTrader.enumeration.RuleName;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.CustomDate;
import com.algoTrader.util.EsperService;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.PropertiesUtil;
import com.algoTrader.util.csv.CsvTickInputAdapter;
import com.algoTrader.util.csv.TransactionInputAdapter;
import com.algoTrader.vo.InterpolationVO;
import com.algoTrader.vo.MaxDrawDownVO;
import com.algoTrader.vo.PerformanceKeysVO;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esperio.AdapterCoordinator;
import com.espertech.esperio.AdapterCoordinatorImpl;
import com.espertech.esperio.AdapterInputSource;
import com.espertech.esperio.InputAdapter;
import com.espertech.esperio.csv.CSVInputAdapterSpec;

public class SimulationServiceImpl extends SimulationServiceBase {

    private static Logger logger = MyLogger.getLogger(SimulationServiceImpl.class.getName());
    private static String dataSet = PropertiesUtil.getProperty("simulation.dataSet");

    private static String[] tickPropertyOrder = {
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

    private static Map<String, Object> tickPropertyTypes = new HashMap<String, Object>();

    public SimulationServiceImpl() {

        SimulationServiceImpl.tickPropertyTypes.put("dateTime", CustomDate.class);
        SimulationServiceImpl.tickPropertyTypes.put("last", BigDecimal.class);
        SimulationServiceImpl.tickPropertyTypes.put("lastDateTime", CustomDate.class);
        SimulationServiceImpl.tickPropertyTypes.put("volBid", int.class);
        SimulationServiceImpl.tickPropertyTypes.put("volAsk", int.class);
        SimulationServiceImpl.tickPropertyTypes.put("bid", BigDecimal.class);
        SimulationServiceImpl.tickPropertyTypes.put("ask", BigDecimal.class);
        SimulationServiceImpl.tickPropertyTypes.put("vol", int.class);
        SimulationServiceImpl.tickPropertyTypes.put("openIntrest", int.class);
        SimulationServiceImpl.tickPropertyTypes.put("settlement", BigDecimal.class);
    }

    @SuppressWarnings("unchecked")
    protected void handleSimulateByUnderlayings() {

        AdapterCoordinator coordinator = new AdapterCoordinatorImpl(EsperService.getEPServiceInstance(), true, true);

        List<Security> securities = getSecurityDao().findSecuritiesOnWatchlist();
        for (Security security : securities) {

            if (security.getIsin() == null) {
                logger.warn("no tickdata available for " + security.getSymbol());
                continue;
            }

            File file = new File("results/tickdata/" + dataSet + "/" + security.getIsin() + ".csv");

            if (file == null || !file.exists()) {
                logger.warn("no tickdata available for " + security.getSymbol());
                continue;
            }

            CSVInputAdapterSpec spec = new CSVInputAdapterSpec(new AdapterInputSource(file), "Tick");
            spec.setPropertyOrder(tickPropertyOrder);
            spec.setPropertyTypes(tickPropertyTypes);
            spec.setTimestampColumn("dateTime");
            spec.setUsingExternalTimer(true);

            InputAdapter inputAdapter = new CsvTickInputAdapter(EsperService.getEPServiceInstance(), spec, security.getId());
            coordinator.coordinate(inputAdapter);

            logger.debug("started simulation for security " + security.getIsin());
        }
        coordinator.start();
    }

    @SuppressWarnings("unchecked")
    protected void handleSimulateByActualTransactions(Collection existingTransactions) {

        AdapterCoordinator coordinator = new AdapterCoordinatorImpl(EsperService.getEPServiceInstance(), true, true);

        File[] files = (new File("results/tickdata/" + dataSet)).listFiles();
        for (File file : files) {

            String isin = file.getName().split("\\.")[0];

            Security security = getSecurityDao().findByISIN(isin);

            CSVInputAdapterSpec spec = new CSVInputAdapterSpec(new AdapterInputSource(file), "Tick");
            spec.setPropertyOrder(tickPropertyOrder);
            spec.setPropertyTypes(tickPropertyTypes);
            spec.setTimestampColumn("dateTime");
            spec.setUsingExternalTimer(true);

            InputAdapter inputAdapter = new CsvTickInputAdapter(EsperService.getEPServiceInstance(), spec, security.getId());
            coordinator.coordinate(inputAdapter);

            logger.debug("started simulation for security " + security.getIsin());
        }

        InputAdapter inputAdapter = new TransactionInputAdapter(existingTransactions);
        coordinator.coordinate(inputAdapter);

        logger.debug("started simulation for transactions");

        coordinator.start();
    }

    protected InterpolationVO handleGetInterpolation() throws Exception {

        EPStatement statement = EsperService.getStatement(RuleName.CREATE_INTERPOLATION);

        if (statement == null) return null;

        if (!statement.iterator().hasNext()) return null;

        return (InterpolationVO)statement.iterator().next().getUnderlying();
    }

    protected PerformanceKeysVO handleGetPerformanceKeys() throws Exception {

        EPStatement statement = EsperService.getStatement(RuleName.CREATE_PERFORMANCE_KEYS);

        if (statement == null) return null;

        if (!statement.iterator().hasNext()) return null;

        PerformanceKeysVO performanceKeys = (PerformanceKeysVO)statement.iterator().next().getUnderlying();

        if (performanceKeys.getStdY() == 0.0) return null;

        return performanceKeys;
    }

    protected List<MonthlyPerformance> handleGetMonthlyPerformances() throws Exception {

        EPStatement statement = EsperService.getStatement(RuleName.KEEP_MONTHLY_PERFORMANCE);

        if (statement == null) return null;

        if (!statement.iterator().hasNext()) return null;

        List<MonthlyPerformance> list = new ArrayList<MonthlyPerformance>();
        for (Iterator<EventBean> it = statement.iterator(); it.hasNext(); ) {
            list.add((MonthlyPerformance)it.next().getUnderlying());
        }
        return list;
    }

    protected MaxDrawDownVO handleGetMaxDrawDown() throws Exception {

        EPStatement statement = EsperService.getStatement(RuleName.CREATE_MAX_DRAW_DOWN);

        if (statement == null) return null;

        if (!statement.iterator().hasNext()) return null;

        return (MaxDrawDownVO)statement.iterator().next().getUnderlying();
    }

    @SuppressWarnings("unchecked")
    protected void handleInit() throws Exception {

        // delete all transactions except the initial CREDIT
        Collection<Transaction> transactions = getTransactionDao().loadAll();
        CollectionUtils.filter(transactions, new Predicate(){
            public boolean evaluate(Object obj) {
                return ((Transaction)obj).getType().equals(TransactionType.CREDIT) ? false : true;
            }});

        // remove all existing transaction from the db
        getTransactionDao().remove(transactions);


        // remove all position associations from securities (especially from the non dummy ones)
        Collection<Position> positions = getPositionDao().loadAll();
        List<Security> securities = new ArrayList<Security>();
        for (Position position : positions) {
            Security security = position.getSecurity();
            security.setPosition(null);
            securities.add(security);
        }
        getSecurityDao().update(securities);

        // delete al positions
        getPositionDao().remove(positions);

        // remove all the targets from preparedRules
        Collection<Rule> rules = getRuleDao().findPreparedRules();
        CollectionUtils.transform(rules, new Transformer() {
            public Object transform(Object arg) {
                ((Rule)arg).setTarget(null);
                return arg;
            }});
        getRuleDao().update(rules);

        // delete all dummySecurities
        getSecurityDao().remove(getSecurityDao().findDummySecurities());
    }
}
