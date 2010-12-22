package com.algoTrader.service;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.MultivariateRealOptimizer;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.SimpleScalarValueChecker;
import org.apache.commons.math.optimization.UnivariateRealOptimizer;
import org.apache.commons.math.optimization.direct.MultiDirectional;
import org.apache.commons.math.optimization.univariate.BrentOptimizer;
import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Order;
import com.algoTrader.entity.OrderImpl;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.Transaction;
import com.algoTrader.enumeration.OrderStatus;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.io.CsvTickInputAdapterSpec;
import com.algoTrader.vo.MaxDrawDownVO;
import com.algoTrader.vo.MonthlyPerformanceVO;
import com.algoTrader.vo.OptimizationResultVO;
import com.algoTrader.vo.PerformanceKeysVO;
import com.espertech.esperio.csv.CSVInputAdapterSpec;

public class SimulationServiceImpl extends SimulationServiceBase {

    private static Logger logger = MyLogger.getLogger(SimulationServiceImpl.class.getName());
    private static String dataSet = ConfigurationUtil.getBaseConfig().getString("dataSource.dataSet");
    private static DecimalFormat twoDigitFormat = new DecimalFormat("#,##0.00");
    private static DecimalFormat threeDigitFormat = new DecimalFormat("#,##0.000");
    private static DateFormat dateFormat = new SimpleDateFormat(" MMM-yy ");
    private static boolean compressed = false;

    @SuppressWarnings("unchecked")
    protected void handleResetDB() throws Exception {

        // process all strategies
        Collection<Strategy> strategies = getStrategyDao().loadAll();
        for (Strategy strategy : strategies) {

            // delete all transactions except the initial CREDIT
            Collection<Transaction> transactions = strategy.getTransactions();
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
            strategy.setTransactions(toKeepTransactions);

            // delete all positions and references to them
            Collection<Position> positions = strategy.getPositions();
            getPositionDao().remove(positions);
            strategy.setPositions(new HashSet<Position>());

            getStrategyDao().update(strategy);
        }

        // delete all StockOptions
        // getSecurityDao().remove(getStockOptionDao().loadAll());
    }

    @SuppressWarnings("unchecked")
    protected void handleInputCSV() {

        getRuleService().initCoordination(StrategyImpl.BASE);

        List<Security> securities = getSecurityDao().findSecuritiesOnActiveWatchlist();
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

            CSVInputAdapterSpec spec = new CsvTickInputAdapterSpec(file);

            getRuleService().coordinate(StrategyImpl.BASE, spec);

            logger.debug("started simulation for security " + security.getSymbol());
        }

        getRuleService().startCoordination(StrategyImpl.BASE);
    }

    @SuppressWarnings("unchecked")
    protected double handleSimulateByUnderlayings() {

        long startTime = System.currentTimeMillis();

        // must call resetDB through ServiceLocator in order to get a transaction
        ServiceLocator.serverInstance().getSimulationService().resetDB();

        // init all activatable strategies
        List<Strategy> strategies = getStrategyDao().findAutoActivateStrategies();
        for (Strategy strategy : strategies) {
            getRuleService().initServiceProvider(strategy.getName());
            getRuleService().activateAll(strategy.getName());
        }

        inputCSV();

        // print execution times
        double mins = ((double)(System.currentTimeMillis() - startTime)) / 60000;
        if(!compressed) logger.info("execution time (min): " + (new DecimalFormat("0.00")).format(mins));
        if(!compressed) logger.info("dataSet: " + dataSet);

        double result = getStatistics();

        // destory all service providers
        for (Strategy strategy : strategies) {
            getRuleService().destroyServiceProvider(strategy.getName());
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    protected void handleSimulateByActualTransactions() {

        // get the existingTransactions before they are deleted
        Collection<Transaction> existingTransactions = Arrays.asList(ServiceLocator.serverInstance().getLookupService().getAllTrades());

        // create orders
        for (Transaction transaction : existingTransactions) {
            Order order = new OrderImpl();
            order.setRequestedQuantity(Math.abs(transaction.getQuantity()));
            order.setTransactionType(transaction.getType());
            order.setStatus(OrderStatus.PREARRANGED);
            order.getTransactions().add(transaction);

            Security security = transaction.getSecurity();

            order.setSecurity(security);
            transaction.setSecurity(security);
        }

        resetDB();

        getRuleService().initServiceProvider(StrategyImpl.BASE);

        // activate the necessary rules
        getRuleService().activate(StrategyImpl.BASE, "CREATE_PORTFOLIO_VALUE");
        getRuleService().activate(StrategyImpl.BASE, "CREATE_MONTHLY_PERFORMANCE");
        getRuleService().activate(StrategyImpl.BASE, "GET_LAST_TICK");
        getRuleService().activate(StrategyImpl.BASE, "CREATE_PERFORMANCE_KEYS");
        getRuleService().activate(StrategyImpl.BASE, "KEEP_MONTHLY_PERFORMANCE");
        getRuleService().activate(StrategyImpl.BASE, "CREATE_DRAW_DOWN");
        getRuleService().activate(StrategyImpl.BASE, "CREATE_MAX_DRAW_DOWN");
        getRuleService().activate(StrategyImpl.BASE, "PROCESS_PREARRANGED_ORDERS");

        // runt the cvs files through
        {

            getRuleService().initCoordination(StrategyImpl.BASE);

            File[] files = (new File("results/tickdata/" + dataSet)).listFiles();
            for (File file : files) {

                String isin = file.getName().split("\\.")[0];

                Security security = getSecurityDao().findByIsin(isin);

                CSVInputAdapterSpec spec = new CsvTickInputAdapterSpec(file);

                getRuleService().coordinate(StrategyImpl.BASE, spec);

                logger.debug("started simulation for security " + security.getSymbol());
            }

            getRuleService().coordinate(StrategyImpl.BASE, existingTransactions, "transaction.dateTime");

            logger.debug("started simulation for transactions");

            getRuleService().startCoordination(StrategyImpl.BASE);
        }

        getStatistics();

        getRuleService().destroyServiceProvider(StrategyImpl.BASE);
    }

    protected void handleOptimizeLinear(String strategyName, String parameter, double min, double max, double increment) throws Exception {

        double result = min;
        double functionValue = 0;
        for (double i = min; i <= max; i += increment ) {

            logger.info("optimize on " + parameter + " value " + threeDigitFormat.format(i));
            ConfigurationUtil.getStrategyConfig(strategyName).setProperty(parameter, String.valueOf(i));

            double value = ServiceLocator.serverInstance().getSimulationService().simulateByUnderlayings();
            if (value > functionValue) {
                functionValue = value;
                result = i;
            }
            if(!compressed) logger.info("");
        }
        logger.info("optimal value of " + parameter + " is " + threeDigitFormat.format(result) + "(functionValue: " + threeDigitFormat.format(functionValue) + ")");
    }

    protected void handleOptimizeSingles(String strategyName, String[] parameter, double[] min, double[] max, double[] accuracies) {

        List<OptimizationResultVO> optimizationResults = new ArrayList<OptimizationResultVO>();
        for (int i = 0; i < parameter.length; i++) {

            OptimizationResultVO optimizationResult = optimizeSingle(strategyName, parameter[i], min[i], max[i], accuracies[i]);
            optimizationResults.add(optimizationResult);
        }

        for (OptimizationResultVO optimizationResult : optimizationResults) {

            logger.info("optimal value for " + optimizationResult.getParameter()
                    + ": " + threeDigitFormat.format(optimizationResult.getResult()) +
                    " (sharpRatio: " + threeDigitFormat.format(optimizationResult.getFunctionValue()) +
                    " needed iterations: " + optimizationResult.getIterations() + ")");
        }

    }

    protected OptimizationResultVO handleOptimizeSingle(String strategyName, String parameter, double min, double max, double accuracy) throws ConvergenceException, FunctionEvaluationException {

        UnivariateRealFunction function = new UnivariateFunction(strategyName, parameter);
        UnivariateRealOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(accuracy);
        optimizer.optimize(function, GoalType.MAXIMIZE, min, max);

        OptimizationResultVO optimizationResult = new OptimizationResultVO();
        optimizationResult.setParameter(parameter);
        optimizationResult.setResult(optimizer.getResult());
        optimizationResult.setFunctionValue(optimizer.getFunctionValue());
        optimizationResult.setIterations(optimizer.getIterationCount());

        return optimizationResult;
    }

    protected void handleOptimizeMulti(String strategyName, String[] parameters, double[] starts) throws ConvergenceException, FunctionEvaluationException {

        MultivariateRealFunction function = new MultivariateFunction(strategyName, parameters);
        MultivariateRealOptimizer optimizer = new MultiDirectional();
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(0.0, 0.01));
        RealPointValuePair result = optimizer.optimize(function, GoalType.MAXIMIZE, starts);

        for (int i = 0; i < result.getPoint().length; i++) {
            logger.info("optimal value for " + parameters[i] + ": " + twoDigitFormat.format(result.getPoint()[i]));
        }
        logger.info("functionValue: " + twoDigitFormat.format(result.getValue()) + " needed iterations: " + optimizer.getEvaluations() + ")");
    }

    protected PerformanceKeysVO handleGetPerformanceKeys() throws Exception {


        PerformanceKeysVO performanceKeys = (PerformanceKeysVO) getRuleService().getLastEvent(StrategyImpl.BASE, "CREATE_PERFORMANCE_KEYS");

        if (performanceKeys == null || performanceKeys.getStdY() == 0.0)
            return null;

        return performanceKeys;
    }

    @SuppressWarnings("unchecked")
    protected List<MonthlyPerformanceVO> handleGetMonthlyPerformances() throws Exception {

        return getRuleService().getAllEvents(StrategyImpl.BASE, "KEEP_MONTHLY_PERFORMANCE");
    }

    protected MaxDrawDownVO handleGetMaxDrawDown() throws Exception {

        return (MaxDrawDownVO) getRuleService().getLastEvent(StrategyImpl.BASE, "CREATE_MAX_DRAW_DOWN");
    }

    @SuppressWarnings("unchecked")
    private double getStatistics() {

        double netLiqValue = getStrategyDao().getPortfolioNetLiqValueDouble();
        logger.info("netLiqValue: " + twoDigitFormat.format(netLiqValue));

        List<MonthlyPerformanceVO> MonthlyPerformanceVOs = getMonthlyPerformances();
        double maxDrawDownM = 0d;
        double bestMonthlyPerformanceVO = Double.NEGATIVE_INFINITY;
        if ((MonthlyPerformanceVOs != null) && !compressed) {
            StringBuffer dateBuffer= new StringBuffer("month-year:         ");
            StringBuffer performanceBuffer = new StringBuffer("MonthlyPerformance: ");
            for (MonthlyPerformanceVO MonthlyPerformanceVO : MonthlyPerformanceVOs) {
                maxDrawDownM = Math.min(maxDrawDownM, MonthlyPerformanceVO.getValue());
                bestMonthlyPerformanceVO = Math.max(bestMonthlyPerformanceVO, MonthlyPerformanceVO.getValue());
                dateBuffer.append(dateFormat.format(MonthlyPerformanceVO.getDate()));
                performanceBuffer.append(StringUtils.leftPad(twoDigitFormat.format(MonthlyPerformanceVO.getValue() * 100), 6) + "% ");
            }
            logger.info(dateBuffer.toString());
            logger.info(performanceBuffer.toString());
        }

        PerformanceKeysVO performanceKeys = getPerformanceKeys();
        MaxDrawDownVO maxDrawDownVO = getMaxDrawDown();
        if (performanceKeys != null && maxDrawDownVO != null) {
            StringBuffer buffer = new StringBuffer();
            if(!compressed) buffer.append("n=" + performanceKeys.getN());
            if(!compressed) buffer.append(" avgM=" + twoDigitFormat.format(performanceKeys.getAvgM() * 100) + "%");
            if(!compressed) buffer.append(" stdM=" + twoDigitFormat.format(performanceKeys.getStdM() * 100) + "%");
            if(!compressed) buffer.append(" avgY=" + twoDigitFormat.format(performanceKeys.getAvgY() * 100) + "%");
            if(!compressed) buffer.append(" stdY=" + twoDigitFormat.format(performanceKeys.getStdY() * 100) + "% ");
            buffer.append("sharpRatio=" + threeDigitFormat.format(performanceKeys.getSharpRatio()));
            logger.info(buffer.toString());

            buffer = new StringBuffer();
            if(!compressed) buffer.append("maxDrawDownM=" + twoDigitFormat.format(-maxDrawDownM * 100) + "%");
            if(!compressed) buffer.append(" bestMonthlyPerformance=" + twoDigitFormat.format(bestMonthlyPerformanceVO * 100) + "%");
            if(!compressed) buffer.append(" maxDrawDown=" + twoDigitFormat.format(maxDrawDownVO.getAmount() * 100) + "%");
            if(!compressed) buffer.append(" maxDrawDownPeriod=" + twoDigitFormat.format(maxDrawDownVO.getPeriod() / 86400000) + "days");
            if(!compressed) buffer.append(" colmarRatio=" + twoDigitFormat.format(performanceKeys.getAvgY() / maxDrawDownVO.getAmount()));
            logger.info(buffer.toString());

            return performanceKeys.getSharpRatio();
        } else {
            logger.info("statistic not available because there was no performance");
            return Double.NaN;
        }
    }

    private static class UnivariateFunction implements UnivariateRealFunction {

        private String param;
        private String strategyName;

        public UnivariateFunction(String strategyName, String parameter) {
            super();
            this.param = parameter;
            this.strategyName = strategyName;
        }

        public double value(double input) throws FunctionEvaluationException {

            ConfigurationUtil.getStrategyConfig(this.strategyName).setProperty(this.param, String.valueOf(input));

            logger.info("optimize on " + this.param + " value " + threeDigitFormat.format(input));

            double result = ServiceLocator.serverInstance().getSimulationService().simulateByUnderlayings();

            if(!compressed) logger.info("");

            return result;
        }
    }

    private static class MultivariateFunction implements MultivariateRealFunction {

        private String[] params;
        private String strategyName;

        public MultivariateFunction(String strategyName,String[] parameters) {
            super();
            this.params = parameters;
            this.strategyName = strategyName;
        }

        public double value(double[] input) throws FunctionEvaluationException {


            StringBuffer buffer = new StringBuffer("optimize on ");
            for (int i =0; i < input.length; i++) {

                String param = this.params[i];
                double value = input[i];

                ConfigurationUtil.getStrategyConfig(this.strategyName).setProperty(param, String.valueOf(value));

                buffer.append(param + ": " + threeDigitFormat.format(value) + " ");
            }
            logger.info(buffer.toString());

            double result = ServiceLocator.serverInstance().getSimulationService().simulateByUnderlayings();

            logger.info("");
            return result;
        }
    }
}
