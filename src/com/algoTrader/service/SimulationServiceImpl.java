package com.algoTrader.service;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.UnivariateRealOptimizer;
import org.apache.commons.math.optimization.univariate.BrentOptimizer;
import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Account;
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
    private static DecimalFormat format = (new DecimalFormat("#,##0.00"));

    private static String[] tickPropertyOrder;
    private static Map<String, Object> tickPropertyTypes;

    public SimulationServiceImpl() {

        tickPropertyOrder = new String[] {
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

        tickPropertyTypes = new HashMap<String, Object>();

        tickPropertyTypes.put("dateTime", CustomDate.class);
        tickPropertyTypes.put("last", BigDecimal.class);
        tickPropertyTypes.put("lastDateTime", CustomDate.class);
        tickPropertyTypes.put("volBid", int.class);
        tickPropertyTypes.put("volAsk", int.class);
        tickPropertyTypes.put("bid", BigDecimal.class);
        tickPropertyTypes.put("ask", BigDecimal.class);
        tickPropertyTypes.put("vol", int.class);
        tickPropertyTypes.put("openIntrest", int.class);
        tickPropertyTypes.put("settlement", BigDecimal.class);
    }

    @SuppressWarnings("unchecked")
    protected void handleInit() throws Exception {

        // process all accounts
        Collection<Account> accounts = getAccountDao().loadAll();
        for (Account account : accounts) {

            // delete all transactions except the initial CREDIT
            Collection<Transaction> transactions = account.getTransactions();
            Set<Transaction> toRemoveTransactions = new HashSet<Transaction>();
            Set<Transaction> toKeepTransactions = new HashSet<Transaction>();
            for (Transaction transaction : transactions) {
                if (transaction.getType().equals(TransactionType.CREDIT)) {
                    toKeepTransactions.add(transaction);
                } else {
                    toRemoveTransactions.add(transaction);
                }
            }
            getTransactionDao().remove(toRemoveTransactions);
            account.setTransactions(toKeepTransactions);

            // delete all positions and references to them
            Collection<Position> positions = account.getPositions();
            Set<Security> toUpdateSecurities = new HashSet<Security>();
            for (Position position : positions) {
                Security security = position.getSecurity();
                security.setPosition(null);
                toUpdateSecurities.add(security);
            }
            getSecurityDao().update(toUpdateSecurities);
            getPositionDao().remove(positions);
            account.setPositions(new HashSet());

            getAccountDao().update(account);
        }

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

        // force reload the collection StockOptionsOnWatchlist, because this might have been cached
        getStockOptionDao().getStockOptionsOnWatchlist(true);
    }

    protected void handleDestroy() throws Exception {

        getRuleService().deactivateAll();
        EsperService.destroyEPServiceInstance();
    }

    @SuppressWarnings("unchecked")
    protected double handleSimulateByUnderlayings() {

        long startTime = System.currentTimeMillis();

        init();

        getRuleService().activateAll();

        // run cvs files through
        {
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

        double mins = ((double)(System.currentTimeMillis() - startTime)) / 60000;
        logger.info("execution time (min): " + (new DecimalFormat("0.00")).format(mins));

        double result = getStatistics();

        destroy();

        return result;
    }

    protected void handleSimulateByActualTransactions() {

        // get the existingTransactions before they are deleted
        Collection<Transaction> existingTransactions = Arrays.asList(ServiceLocator.instance().getLookupService().getAllTrades());

        init();

        // activate the necessary rules
        getRuleService().activate(RuleName.CREATE_PORTFOLIO_VALUE);
        getRuleService().activate(RuleName.CREATE_MONTHLY_PERFORMANCE);
        getRuleService().activate(RuleName.GET_LAST_TICK);
        getRuleService().activate(RuleName.CREATE_INTERPOLATION);
        getRuleService().activate(RuleName.CREATE_PERFORMANCE_KEYS);
        getRuleService().activate(RuleName.KEEP_MONTHLY_PERFORMANCE);
        getRuleService().activate(RuleName.CREATE_DRAW_DOWN);
        getRuleService().activate(RuleName.CREATE_MAX_DRAW_DOWN);
        getRuleService().activate(RuleName.PROCESS_PREARRANGED_ORDERS);

        // runt the cvs files through
        {
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

        getStatistics();

        destroy();
    }

    protected void handleOptimize(String parameter, double min, double max, double accuracy) throws ConvergenceException, FunctionEvaluationException {

        UnivariateRealFunction function = new SharpRatioFunction(parameter);
        UnivariateRealOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(accuracy);
        double result = optimizer.optimize(function, GoalType.MAXIMIZE, min, max);

        logger.info("optimal value for " + parameter + ": " + format.format(result));
        logger.info("needed iterations: " + optimizer.getIterationCount());
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
    private double getStatistics() {

        BigDecimal totalValue = getAccountDao().getTotalValueAllAccounts();
        logger.info("totalValue=" + format.format(totalValue));

        InterpolationVO interpolation = getInterpolation();

        if (interpolation != null) {
            StringBuffer buffer = new StringBuffer("interpolation: ");
            buffer.append("a=" + format.format(interpolation.getA()));
            buffer.append(" b=" + format.format(interpolation.getB()));
            buffer.append(" r=" + format.format(interpolation.getR()));
            logger.info(buffer.toString());
        }

        List<MonthlyPerformance> monthlyPerformances = getMonthlyPerformances();
        double maxDrawDownM = 0d;
        double bestMonthlyPerformance = Double.NEGATIVE_INFINITY;
        if (monthlyPerformances != null) {
            StringBuffer buffer = new StringBuffer("monthlyPerformance: ");
            for (MonthlyPerformance monthlyPerformance : monthlyPerformances) {
                maxDrawDownM = Math.min(maxDrawDownM, monthlyPerformance.getValue());
                bestMonthlyPerformance = Math.max(bestMonthlyPerformance, monthlyPerformance.getValue());
                buffer.append(format.format(monthlyPerformance.getValue() * 100) + "% " );
            }
            logger.info(buffer.toString());
        }

        PerformanceKeysVO performanceKeys = getPerformanceKeys();
        MaxDrawDownVO maxDrawDownVO = getMaxDrawDown();
        if (performanceKeys != null && maxDrawDownVO != null) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("n=" + performanceKeys.getN());
            buffer.append(" avgM=" + format.format(performanceKeys.getAvgM() * 100) + "%");
            buffer.append(" stdM=" + format.format(performanceKeys.getStdM() * 100) + "%");
            buffer.append(" avgY=" + format.format(performanceKeys.getAvgY() * 100) + "%");
            buffer.append(" stdY=" + format.format(performanceKeys.getStdY() * 100) + "%");
            buffer.append(" sharpRatio=" + format.format(performanceKeys.getSharpRatio()));
            logger.info(buffer.toString());

            buffer = new StringBuffer();
            buffer.append("maxDrawDownM: " + format.format(-maxDrawDownM * 100) + "%");
            buffer.append(" bestMonthlyPerformance: " + format.format(bestMonthlyPerformance * 100) + "%");
            buffer.append(" maxDrawDown: " + format.format(maxDrawDownVO.getAmount() * 100) + "%");
            buffer.append(" maxDrawDownPeriod: " + format.format(maxDrawDownVO.getPeriod() / 86400000) + "days");
            buffer.append(" colmarRatio: " + format.format(performanceKeys.getAvgY() / maxDrawDownVO.getAmount()));
            logger.info(buffer.toString());

            return performanceKeys.getSharpRatio();
        } else {
            logger.info("statistic not available because there was no performance");
            return 0;
        }
    }

    private static class SharpRatioFunction implements UnivariateRealFunction {

        private String param;

        public SharpRatioFunction(String parameter) {
            super();
            this.param = parameter;
        }

        public double value(double input) throws FunctionEvaluationException {

            PropertiesUtil.setProperty(param, String.valueOf(input));

            logger.info("optimize on " + param + " value " + format.format(input));

            double result = ServiceLocator.instance().getSimulationService().simulateByUnderlayings();

            logger.info("");
            return result;
        }
    }
}
