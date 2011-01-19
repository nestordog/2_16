package com.algoTrader.service;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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
import com.algoTrader.vo.SimulationResultVO;
import com.espertech.esperio.csv.CSVInputAdapterSpec;

public class SimulationServiceImpl extends SimulationServiceBase {

    private static Logger logger = MyLogger.getLogger(SimulationServiceImpl.class.getName());
    private static String dataSet = ConfigurationUtil.getBaseConfig().getString("dataSource.dataSet");
    private static DecimalFormat twoDigitFormat = new DecimalFormat("#,##0.00");
    private static DecimalFormat threeDigitFormat = new DecimalFormat("#,##0.000");
    private static DateFormat dateFormat = new SimpleDateFormat(" MMM-yy ");

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
        getSecurityDao().remove(getStockOptionDao().loadAll());
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
    protected SimulationResultVO handleRunByUnderlayings() {

        long startTime = System.currentTimeMillis();

        // must call resetDB through ServiceLocator in order to get a transaction
        ServiceLocator.serverInstance().getSimulationService().resetDB();

        // init all activatable strategies
        List<Strategy> strategies = getStrategyDao().findAutoActivateStrategies();
        for (Strategy strategy : strategies) {
            getRuleService().initServiceProvider(strategy.getName());
            getRuleService().activateAll(strategy.getName());
        }

        // feed the ticks
        inputCSV();

        // get the results
        SimulationResultVO resultVO = getSimulationResultVO(startTime);

        // destroy all service providers
        for (Strategy strategy : strategies) {
            getRuleService().destroyServiceProvider(strategy.getName());
        }

        // reset all configuration variables
        ConfigurationUtil.resetConfig();

        return resultVO;
    }

    @SuppressWarnings("unchecked")
    protected void handleRunByActualTransactions() {

        long startTime = System.currentTimeMillis();

        // get the existingTransactions before they are deleted
        Collection<Transaction> existingTransactions = Arrays.asList(ServiceLocator.serverInstance().getLookupService().getAllTrades());

        // create orders
        for (Transaction transaction : existingTransactions) {
            Order order = new OrderImpl();
            order.setStrategy(transaction.getStrategy());
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

        SimulationResultVO resultVO = getSimulationResultVO(startTime);
        logMultiLineString(convertStatisticsToLongString(resultVO));

        getRuleService().destroyServiceProvider(StrategyImpl.BASE);
    }

    protected void handleSimulateWithCurrentParams() throws Exception {

        SimulationResultVO resultVO = ServiceLocator.serverInstance().getSimulationService().runByUnderlayings();
        logMultiLineString(convertStatisticsToLongString(resultVO));
    }

    protected void handleSimulateBySingleParam(String strategyName, String parameter, double value) throws Exception {

        ConfigurationUtil.getStrategyConfig(strategyName).setProperty(parameter, String.valueOf(value));

        SimulationResultVO resultVO = ServiceLocator.serverInstance().getSimulationService().runByUnderlayings();
        logger.info("optimize on " + parameter + " value=" + threeDigitFormat.format(value) + " " + convertStatisticsToShortString(resultVO));
    }

    protected void handleSimulateByMultiParam(String strategyName, String[] parameters, double[] values) throws Exception {

        StringBuffer buffer = new StringBuffer();
        buffer.append("optimize on ");
        for (int i = 0; i < parameters.length; i++) {
            buffer.append(parameters[i] + " value=" + threeDigitFormat.format(values[i]) + " ");
            ConfigurationUtil.getStrategyConfig(strategyName).setProperty(parameters[i], String.valueOf(values[i]));
        }

        SimulationResultVO resultVO = ServiceLocator.serverInstance().getSimulationService().runByUnderlayings();
        buffer.append(convertStatisticsToShortString(resultVO));
        logger.info(buffer.toString());
    }

    protected void handleOptimizeSingleParamLinear(String strategyName, String parameter, double min, double max, double increment) throws Exception {

        double result = min;
        double functionValue = 0;
        for (double i = min; i <= max; i += increment ) {

            ConfigurationUtil.getStrategyConfig(strategyName).setProperty(parameter, String.valueOf(i));

            SimulationResultVO resultVO = ServiceLocator.serverInstance().getSimulationService().runByUnderlayings();
            logger.info("optimize on " + parameter + " value=" + threeDigitFormat.format(i) + " " + convertStatisticsToShortString(resultVO));

            double value = resultVO.getPerformanceKeysVO().getSharpRatio();
            if (value > functionValue) {
                functionValue = value;
                result = i;
            }
        }
        logger.info("optimal value of " + parameter + " is " + threeDigitFormat.format(result) + "(functionValue: " + threeDigitFormat.format(functionValue) + ")");
    }

    protected OptimizationResultVO handleOptimizeSingleParam(String strategyName, String parameter, double min, double max, double accuracy) throws ConvergenceException, FunctionEvaluationException {

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

    protected void handleOptimizeMultiParam(String strategyName, String[] parameters, double[] starts) throws ConvergenceException, FunctionEvaluationException {

        MultivariateRealFunction function = new MultivariateFunction(strategyName, parameters);
        MultivariateRealOptimizer optimizer = new MultiDirectional();
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(0.0, 0.01));
        RealPointValuePair result = optimizer.optimize(function, GoalType.MAXIMIZE, starts);

        for (int i = 0; i < result.getPoint().length; i++) {
            logger.info("optimal value for " + parameters[i] + ": " + twoDigitFormat.format(result.getPoint()[i]));
        }
        logger.info("functionValue: " + twoDigitFormat.format(result.getValue()) + " needed iterations: " + optimizer.getEvaluations() + ")");
    }

    @SuppressWarnings("unchecked")
    protected SimulationResultVO handleGetSimulationResultVO(long startTime) {

        PerformanceKeysVO performanceKeys = (PerformanceKeysVO) getRuleService().getLastEvent(StrategyImpl.BASE, "CREATE_PERFORMANCE_KEYS");
        List<MonthlyPerformanceVO> monthlyPerformances = getRuleService().getAllEvents(StrategyImpl.BASE, "KEEP_MONTHLY_PERFORMANCE");
        MaxDrawDownVO maxDrawDown = (MaxDrawDownVO) getRuleService().getLastEvent(StrategyImpl.BASE, "CREATE_MAX_DRAW_DOWN");

        // assemble the result
        SimulationResultVO resultVO = new SimulationResultVO();
        resultVO.setMins(((double) (System.currentTimeMillis() - startTime)) / 60000);
        resultVO.setDataSet(dataSet);
        resultVO.setNetLiqValue(getStrategyDao().getPortfolioNetLiqValueDouble());
        resultVO.setMonthlyPerformanceVOs(monthlyPerformances);
        resultVO.setPerformanceKeysVO(performanceKeys);
        resultVO.setMaxDrawDownVO(maxDrawDown);
        return resultVO;
    }

    private static String convertStatisticsToShortString(SimulationResultVO resultVO) {

        StringBuffer buffer = new StringBuffer();

        double netLiqValue = resultVO.getNetLiqValue();
        buffer.append("netLiqValue=" + twoDigitFormat.format(netLiqValue));

        double sharpRatio = resultVO.getPerformanceKeysVO().getSharpRatio();
        buffer.append(" sharpRatio=" + threeDigitFormat.format(sharpRatio));

        return buffer.toString();
    }

    @SuppressWarnings("unchecked")
    private static String convertStatisticsToLongString(SimulationResultVO resultVO) {

        StringBuffer buffer = new StringBuffer();
        buffer.append("execution time (min): " + (new DecimalFormat("0.00")).format(resultVO.getMins()) + "\r\n");
        buffer.append("dataSet: " + dataSet + "\r\n");

        double netLiqValue = resultVO.getNetLiqValue();
        buffer.append("netLiqValue=" + twoDigitFormat.format(netLiqValue) + "\r\n");

        List<MonthlyPerformanceVO> monthlyPerformanceVOs = resultVO.getMonthlyPerformanceVOs();
        double maxDrawDownM = 0d;
        double bestMonthlyPerformance = Double.NEGATIVE_INFINITY;
        if ((monthlyPerformanceVOs != null)) {
            StringBuffer dateBuffer= new StringBuffer("month-year:         ");
            StringBuffer performanceBuffer = new StringBuffer("MonthlyPerformance: ");
            for (MonthlyPerformanceVO MonthlyPerformanceVO : monthlyPerformanceVOs) {
                maxDrawDownM = Math.min(maxDrawDownM, MonthlyPerformanceVO.getValue());
                bestMonthlyPerformance = Math.max(bestMonthlyPerformance, MonthlyPerformanceVO.getValue());
                dateBuffer.append(dateFormat.format(MonthlyPerformanceVO.getDate()));
                performanceBuffer.append(StringUtils.leftPad(twoDigitFormat.format(MonthlyPerformanceVO.getValue() * 100), 6) + "% ");
            }
            buffer.append(dateBuffer.toString() + "\r\n");
            buffer.append(performanceBuffer.toString() + "\r\n");
        }

        PerformanceKeysVO performanceKeys = resultVO.getPerformanceKeysVO();
        MaxDrawDownVO maxDrawDownVO = resultVO.getMaxDrawDownVO();
        if (performanceKeys != null && maxDrawDownVO != null) {
            buffer.append("n=" + performanceKeys.getN());
            buffer.append(" avgM=" + twoDigitFormat.format(performanceKeys.getAvgM() * 100) + "%");
            buffer.append(" stdM=" + twoDigitFormat.format(performanceKeys.getStdM() * 100) + "%");
            buffer.append(" avgY=" + twoDigitFormat.format(performanceKeys.getAvgY() * 100) + "%");
            buffer.append(" stdY=" + twoDigitFormat.format(performanceKeys.getStdY() * 100) + "% ");
            buffer.append(" sharpRatio=" + threeDigitFormat.format(performanceKeys.getSharpRatio()) + "\r\n");

            buffer.append("maxDrawDownM=" + twoDigitFormat.format(-maxDrawDownM * 100) + "%");
            buffer.append(" bestMonthlyPerformance=" + twoDigitFormat.format(bestMonthlyPerformance * 100) + "%");
            buffer.append(" maxDrawDown=" + twoDigitFormat.format(maxDrawDownVO.getAmount() * 100) + "%");
            buffer.append(" maxDrawDownPeriod=" + twoDigitFormat.format(maxDrawDownVO.getPeriod() / 86400000) + "days");
            buffer.append(" colmarRatio=" + twoDigitFormat.format(performanceKeys.getAvgY() / maxDrawDownVO.getAmount()));
        }

        return buffer.toString();
    }

    private static void logMultiLineString(String input) {

        String[] lines = input.split("\r\n");
        for (String line : lines) {
            logger.info(line);
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

            SimulationResultVO resultVO = ServiceLocator.serverInstance().getSimulationService().runByUnderlayings();
            double result = resultVO.getPerformanceKeysVO().getSharpRatio();

            logger.info("optimize on " + this.param + " value " + threeDigitFormat.format(input) + " " + SimulationServiceImpl.convertStatisticsToShortString(resultVO));

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

            SimulationResultVO resultVO = ServiceLocator.serverInstance().getSimulationService().runByUnderlayings();
            double result = resultVO.getPerformanceKeysVO().getSharpRatio();

            logger.info(buffer.toString() + SimulationServiceImpl.convertStatisticsToShortString(resultVO));

            return result;
        }
    }
}
