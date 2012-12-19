package com.algoTrader.service;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import net.sf.ehcache.CacheManager;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
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
import org.apache.commons.math.util.MathUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.trade.Order;
import com.algoTrader.enumeration.MarketDataType;
import com.algoTrader.esper.EsperManager;
import com.algoTrader.esper.io.CsvBarInputAdapterSpec;
import com.algoTrader.esper.io.CsvTickInputAdapterSpec;
import com.algoTrader.esper.io.GenericEventInputAdapterSpec;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.metric.MetricsUtil;
import com.algoTrader.util.spring.Configuration;
import com.algoTrader.vo.EndOfSimulationVO;
import com.algoTrader.vo.MaxDrawDownVO;
import com.algoTrader.vo.OptimizationResultVO;
import com.algoTrader.vo.PerformanceKeysVO;
import com.algoTrader.vo.PeriodPerformanceVO;
import com.algoTrader.vo.SimulationResultVO;
import com.algoTrader.vo.TradesVO;
import com.espertech.esperio.csv.CSVInputAdapterSpec;

public class SimulationServiceImpl extends SimulationServiceBase {

    private static Logger logger = MyLogger.getLogger(SimulationServiceImpl.class.getName());
    private static Logger resultLogger = MyLogger.getLogger(SimulationServiceImpl.class.getName() + ".RESULT");
    private static DecimalFormat twoDigitFormat = new DecimalFormat("#,##0.00");
    private static DateFormat monthFormat = new SimpleDateFormat(" MMM-yy ");
    private static DateFormat yearFormat = new SimpleDateFormat("   yyyy ");
    private static final NumberFormat format = NumberFormat.getInstance();

    private @Value("${simulation.roundDigits}") int roundDigits;
    private @Value("${simulation.start}") long start;
    private @Value("${dataSource.dataSetLocation}") String dataSetLocation;
    private @Value("${dataSource.feedGenericEvents}") boolean feedGenericEvents;

    {
        format.setMinimumFractionDigits(this.roundDigits);
    }

    @Override
    protected void handleInputCSV() {

        EsperManager.initCoordination(StrategyImpl.BASE);

        String baseDir = this.dataSetLocation.equals("") ? "files" + File.separator : this.dataSetLocation;
        String dataSet = getConfiguration().getDataSet();

        if (this.feedGenericEvents) {

            File dir = new File(baseDir + "genericdata" + File.separator + dataSet);
            if (dir == null || !dir.exists() || !dir.isDirectory()) {
                logger.warn("no generic events available");
            } else {
                File[] files = dir.listFiles();
                File[] sortedFiles = new File[files.length];

                // sort the files according to their order
                for (File file : files) {

                    String fileName = file.getName();
                    String baseFileName = fileName.substring(0, fileName.lastIndexOf("."));
                    int order = Integer.parseInt(baseFileName.substring(baseFileName.lastIndexOf(".") + 1));
                    sortedFiles[order] = file;
                }

                // coordinate all files
                for (File file : sortedFiles) {

                    String fileName = file.getName();
                    String baseFileName = fileName.substring(0, fileName.lastIndexOf("."));
                    String eventClassName = baseFileName.substring(0, baseFileName.lastIndexOf("."));
                    String eventTypeName = eventClassName.substring(eventClassName.lastIndexOf(".") + 1);

                    // add the eventType (in case it does not exist yet)
                    EsperManager.addEventType(StrategyImpl.BASE, eventTypeName, eventClassName);

                    GenericEventInputAdapterSpec spec = new GenericEventInputAdapterSpec(file, eventTypeName);
                    EsperManager.coordinate(StrategyImpl.BASE, spec);
                }

            }
        }

        Collection<Security> securities = getLookupService().getSubscribedSecuritiesForAutoActivateStrategiesInclFamily();
        for (Security security : securities) {

            if (security.getIsin() == null) {
                logger.warn("no data available for " + security.getSymbol());
                continue;
            }

            MarketDataType marketDataType = getConfiguration().getDataSetType();
            String fileName = security.getIsin() != null ? security.getIsin() : String.valueOf(security.getId());
            File file = new File(baseDir + marketDataType.toString().toLowerCase() + "data" + File.separator + dataSet + File.separator + fileName + ".csv");

            if (file == null || !file.exists()) {
                logger.warn("no data available for " + security.getSymbol());
                continue;
            } else {
                logger.info("data available for " + security.getSymbol());
            }

            CSVInputAdapterSpec spec;
            if (MarketDataType.TICK.equals(marketDataType)) {
                spec = new CsvTickInputAdapterSpec(file);
            } else if (MarketDataType.BAR.equals(marketDataType)) {
                spec = new CsvBarInputAdapterSpec(file, getConfiguration().getBarSize());
            } else {
                throw new SimulationServiceException("incorrect parameter for dataSetType: " + marketDataType);
            }

            EsperManager.coordinate(StrategyImpl.BASE, spec);

            logger.debug("started simulation for security " + security.getSymbol());
        }


        EsperManager.startCoordination(StrategyImpl.BASE);
    }

    @Override
    protected SimulationResultVO handleRunByUnderlyings() {

        long startTime = System.currentTimeMillis();

        // reset the db
        getResetService().resetDB();

        // init all activatable strategies
        Collection<Strategy> strategies = getLookupService().getAutoActivateStrategies();
        for (Strategy strategy : strategies) {
            EsperManager.initServiceProvider(strategy.getName());
            EsperManager.deployAllModules(strategy.getName());
        }

        // rebalance portfolio (to distribute initial CREDIT to strategies)
        getAccountService().rebalancePortfolio();

        // feed the ticks
        inputCSV();

        // log metrics in case they have been enabled
        MetricsUtil.logMetrics();
        EsperManager.logStatementMetrics();

        // close all open positions that might still exist
        for (Position position : getLookupService().getOpenTradeablePositions()) {
            getPositionService().closePosition(position.getId(), false);
        }

        // send the EndOfSimulation event
        EsperManager.sendEvent(StrategyImpl.BASE, new EndOfSimulationVO());

        // get the results
        SimulationResultVO resultVO = getSimulationResultVO(startTime);

        // destroy all service providers
        for (Strategy strategy : strategies) {
            EsperManager.destroyServiceProvider(strategy.getName());
        }

        // clear the second-level cache
        CacheManager.getInstance().clearAll();

        // run a garbage collection
        System.gc();

        return resultVO;
    }

    @Override
    protected void handleRunByActualTransactions() {

        long startTime = System.currentTimeMillis();

        // get the existingTransactions before they are deleted
        //        Collection<Transaction> transactions = ServiceLocator.instance().getLookupService().getAllTrades();

        // create orders
        List<Order> orders = new ArrayList<Order>();
        //        for (Transaction transaction : transactions) {
        //
        //            // TODO needs to be redone with the new Async Order
        //            Order order = new OrderImpl();
        //            order.setStrategy(transaction.getStrategy());
        //            order.setRequestedQuantity(Math.abs(transaction.getQuantity()));
        //            order.setTransactionType(transaction.getType());
        //            order.setStatus(Status.PREARRANGED);
        //            order.getTransactions().add(transaction);
        //            order.setSecurity(transaction.getSecurity());
        //
        //            orders.add(order);
        //        }

        getResetService().resetDB();

        EsperManager.initServiceProvider(StrategyImpl.BASE);

        // activate the necessary rules
        EsperManager.deployStatement(StrategyImpl.BASE, "base", "CREATE_PORTFOLIO_VALUE");
        EsperManager.deployStatement(StrategyImpl.BASE, "base", "CREATE_MONTHLY_PERFORMANCE");
        EsperManager.deployStatement(StrategyImpl.BASE, "base", "GET_LAST_MARKET_DATA_EVENT");
        EsperManager.deployStatement(StrategyImpl.BASE, "base", "CREATE_PERFORMANCE_KEYS");
        EsperManager.deployStatement(StrategyImpl.BASE, "base", "KEEP_MONTHLY_PERFORMANCE");
        EsperManager.deployStatement(StrategyImpl.BASE, "base", "CREATE_DRAW_DOWN");
        EsperManager.deployStatement(StrategyImpl.BASE, "base", "CREATE_MAX_DRAW_DOWN");
        EsperManager.deployStatement(StrategyImpl.BASE, "base", "PROCESS_PREARRANGED_ORDERS");

        // initialize the coordination
        EsperManager.initCoordination(StrategyImpl.BASE);

        EsperManager.coordinateTicks(StrategyImpl.BASE, new Date(this.start));

        EsperManager.coordinate(StrategyImpl.BASE, orders, "transactions[0].dateTime");

        EsperManager.startCoordination(StrategyImpl.BASE);

        SimulationResultVO resultVO = getSimulationResultVO(startTime);
        logMultiLineString(convertStatisticsToLongString(resultVO));

        EsperManager.destroyServiceProvider(StrategyImpl.BASE);
    }

    @Override
    protected void handleSimulateWithCurrentParams() throws Exception {

        SimulationResultVO resultVO = runByUnderlyings();
        logMultiLineString(convertStatisticsToLongString(resultVO));
    }

    @Override
    protected void handleSimulateBySingleParam(String parameter, String value) throws Exception {

        getConfiguration().setProperty(parameter, value);

        SimulationResultVO resultVO = runByUnderlyings();
        resultLogger.info("optimize " + parameter + "=" + value + " " + convertStatisticsToShortString(resultVO));
    }

    @Override
    protected void handleSimulateByMultiParam(String[] parameters, String[] values) throws Exception {

        StringBuffer buffer = new StringBuffer();
        buffer.append("optimize ");
        for (int i = 0; i < parameters.length; i++) {
            buffer.append(parameters[i] + "=" + values[i] + " ");
            getConfiguration().setProperty(parameters[i], values[i]);
        }

        SimulationResultVO resultVO = runByUnderlyings();
        buffer.append(convertStatisticsToShortString(resultVO));
        resultLogger.info(buffer.toString());
    }

    @Override
    protected void handleOptimizeSingleParamLinear(String parameter, double min, double max, double increment) throws Exception {

        double result = min;
        double functionValue = 0;
        for (double i = min; i <= max; i += increment) {

            getConfiguration().setProperty(parameter, format.format(i));

            SimulationResultVO resultVO = runByUnderlyings();
            resultLogger.info(parameter + "=" + format.format(i) + " " + convertStatisticsToShortString(resultVO));

            double value = resultVO.getPerformanceKeys().getSharpRatio();
            if (value > functionValue) {
                functionValue = value;
                result = i;
            }
        }
        resultLogger.info("optimal value of " + parameter + " is " + format.format(result) + " (functionValue: " + format.format(functionValue) + ")");
    }

    @Override
    @SuppressWarnings("deprecation")
    protected OptimizationResultVO handleOptimizeSingleParam(String parameter, double min, double max, double accuracy)
            throws ConvergenceException, FunctionEvaluationException {

        UnivariateRealFunction function = new UnivariateFunction(parameter);
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

    @Override
    protected void handleOptimizeMultiParamLinear(String parameters[], double[] mins, double[] maxs, double[] increments) throws Exception {

        Configuration configuration = getConfiguration();
        for (double i0 = mins[0]; i0 <= maxs[0]; i0 += increments[0]) {
            configuration.setProperty(parameters[0], format.format(i0));
            String message0 = parameters[0] + "=" + format.format(MathUtils.round(i0, this.roundDigits));

            if (parameters.length >= 2) {
                for (double i1 = mins[1]; i1 <= maxs[1]; i1 += increments[1]) {
                    configuration.setProperty(parameters[1], format.format(i1));
                    String message1 = parameters[1] + "=" + format.format(MathUtils.round(i1, this.roundDigits));

                    if (parameters.length >= 3) {
                        for (double i2 = mins[2]; i2 <= maxs[2]; i2 += increments[2]) {
                            configuration.setProperty(parameters[2], format.format(i2));
                            String message2 = parameters[2] + "=" + format.format(MathUtils.round(i2, this.roundDigits));

                            SimulationResultVO resultVO = runByUnderlyings();
                            resultLogger.info(message0 + " " + message1 + " " + message2 + " " + convertStatisticsToShortString(resultVO));
                        }
                    } else {
                        SimulationResultVO resultVO = runByUnderlyings();
                        resultLogger.info(message0 + " " + message1 + " " + convertStatisticsToShortString(resultVO));
                    }
                }
            } else {
                SimulationResultVO resultVO = runByUnderlyings();
                resultLogger.info(message0 + " " + convertStatisticsToShortString(resultVO));
            }
        }
    }

    @Override
    protected void handleOptimizeMultiParam(String[] parameters, double[] starts) throws ConvergenceException, FunctionEvaluationException {

        MultivariateRealFunction function = new MultivariateFunction(parameters);
        MultivariateRealOptimizer optimizer = new MultiDirectional();
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(0.0, 0.01));
        RealPointValuePair result = optimizer.optimize(function, GoalType.MAXIMIZE, starts);

        for (int i = 0; i < result.getPoint().length; i++) {
            resultLogger.info("optimal value for " + parameters[i] + "=" + format.format(result.getPoint()[i]));
        }
        resultLogger.info("functionValue: " + format.format(result.getValue()) + " needed iterations: " + optimizer.getEvaluations() + ")");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected SimulationResultVO handleGetSimulationResultVO(long startTime) {

        PerformanceKeysVO performanceKeys = (PerformanceKeysVO) EsperManager.getLastEvent(StrategyImpl.BASE, "INSERT_INTO_PERFORMANCE_KEYS");
        List<PeriodPerformanceVO> monthlyPerformances = EsperManager.getAllEvents(StrategyImpl.BASE, "KEEP_MONTHLY_PERFORMANCE");
        MaxDrawDownVO maxDrawDown = (MaxDrawDownVO) EsperManager.getLastEvent(StrategyImpl.BASE, "INSERT_INTO_MAX_DRAW_DOWN");
        TradesVO allTrades = (TradesVO) EsperManager.getLastEvent(StrategyImpl.BASE, "INSERT_INTO_ALL_TRADES");
        TradesVO winningTrades = (TradesVO) EsperManager.getLastEvent(StrategyImpl.BASE, "INSERT_INTO_WINNING_TRADES");
        TradesVO loosingTrades = (TradesVO) EsperManager.getLastEvent(StrategyImpl.BASE, "INSERT_INTO_LOOSING_TRADES");

        // compile yearly performance
        List<PeriodPerformanceVO> yearlyPerformances = null;
        if (monthlyPerformances.size() != 0) {
            if ((monthlyPerformances != null)) {
                yearlyPerformances = new ArrayList<PeriodPerformanceVO>();
                double currentPerformance = 1.0;
                for (PeriodPerformanceVO monthlyPerformance : monthlyPerformances) {
                    currentPerformance *= 1.0 + monthlyPerformance.getValue();
                    if (DateUtils.toCalendar(monthlyPerformance.getDate()).get(Calendar.MONTH) == 11) {
                        PeriodPerformanceVO yearlyPerformance = new PeriodPerformanceVO();
                        yearlyPerformance.setDate(monthlyPerformance.getDate());
                        yearlyPerformance.setValue(currentPerformance - 1.0);
                        yearlyPerformances.add(yearlyPerformance);
                        currentPerformance = 1.0;
                    }
                }

                PeriodPerformanceVO lastMonthlyPerformance = monthlyPerformances.get(monthlyPerformances.size() - 1);
                if (DateUtils.toCalendar(lastMonthlyPerformance.getDate()).get(Calendar.MONTH) != 11) {
                    PeriodPerformanceVO yearlyPerformance = new PeriodPerformanceVO();
                    yearlyPerformance.setDate(lastMonthlyPerformance.getDate());
                    yearlyPerformance.setValue(currentPerformance - 1.0);
                    yearlyPerformances.add(yearlyPerformance);
                }
            }
        }

        // assemble the result
        SimulationResultVO resultVO = new SimulationResultVO();
        resultVO.setMins(((double) (System.currentTimeMillis() - startTime)) / 60000);
        resultVO.setDataSet(getConfiguration().getDataSet());
        resultVO.setNetLiqValue(getPortfolioService().getNetLiqValueDouble());
        resultVO.setMonthlyPerformances(monthlyPerformances);
        resultVO.setYearlyPerformances(yearlyPerformances);
        resultVO.setPerformanceKeys(performanceKeys);
        resultVO.setMaxDrawDown(maxDrawDown);
        resultVO.setAllTrades(allTrades);
        resultVO.setWinningTrades(winningTrades);
        resultVO.setLoosingTrades(loosingTrades);

        return resultVO;
    }

    private static String convertStatisticsToShortString(SimulationResultVO resultVO) {

        StringBuffer buffer = new StringBuffer();

        PerformanceKeysVO performanceKeys = resultVO.getPerformanceKeys();
        MaxDrawDownVO maxDrawDownVO = resultVO.getMaxDrawDown();

        if (resultVO.getAllTrades().getCount() == 0) {
            return ("no trades took place!");
        }

        Collection<PeriodPerformanceVO> periodPerformanceVOs = resultVO.getMonthlyPerformances();
        double maxDrawDownM = 0d;
        double bestMonthlyPerformance = Double.NEGATIVE_INFINITY;
        if ((periodPerformanceVOs != null)) {
            for (PeriodPerformanceVO PeriodPerformanceVO : periodPerformanceVOs) {
                maxDrawDownM = Math.min(maxDrawDownM, PeriodPerformanceVO.getValue());
                bestMonthlyPerformance = Math.max(bestMonthlyPerformance, PeriodPerformanceVO.getValue());
            }
        }

        buffer.append("avgY=" + twoDigitFormat.format(performanceKeys.getAvgY() * 100.0) + "%");
        buffer.append(" sharpe=" + twoDigitFormat.format(performanceKeys.getSharpRatio()));
        buffer.append(" maxDDM=" + twoDigitFormat.format(-maxDrawDownM * 100) + "%");
        buffer.append(" bestMP=" + twoDigitFormat.format(bestMonthlyPerformance * 100) + "%");
        buffer.append(" maxDD=" + twoDigitFormat.format(maxDrawDownVO.getAmount() * 100.0) + "%");
        buffer.append(" maxDDPer=" + twoDigitFormat.format(maxDrawDownVO.getPeriod() / 86400000));
        buffer.append(" avgPPctWin=" + twoDigitFormat.format(resultVO.getWinningTrades().getAvgProfitPct() * 100.0) + "%");
        buffer.append(" avgPPctLoos=" + twoDigitFormat.format(resultVO.getLoosingTrades().getAvgProfitPct() * 100.0) + "%");
        buffer.append(" winTrdsPct=" + twoDigitFormat.format(100.0 * resultVO.getWinningTrades().getCount() / resultVO.getAllTrades().getCount()) + "%");

        return buffer.toString();
    }

    private String convertStatisticsToLongString(SimulationResultVO resultVO) {

        if (resultVO.getAllTrades().getCount() == 0) {
            return ("no trades took place!");
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append("execution time (min): " + (new DecimalFormat("0.00")).format(resultVO.getMins()) + "\r\n");
        buffer.append("dataSet: " + getConfiguration().getDataSet() + "\r\n");

        double netLiqValue = resultVO.getNetLiqValue();
        buffer.append("netLiqValue=" + twoDigitFormat.format(netLiqValue) + "\r\n");

        // monthlyPerformances
        Collection<PeriodPerformanceVO> monthlyPerformances = resultVO.getMonthlyPerformances();
        double maxDrawDownM = 0d;
        double bestMonthlyPerformance = Double.NEGATIVE_INFINITY;
        int positiveMonths = 0;
        int negativeMonths = 0;
        if ((monthlyPerformances != null)) {
            StringBuffer dateBuffer = new StringBuffer("month-year:         ");
            StringBuffer performanceBuffer = new StringBuffer("monthlyPerformance: ");
            for (PeriodPerformanceVO monthlyPerformance : monthlyPerformances) {
                maxDrawDownM = Math.min(maxDrawDownM, monthlyPerformance.getValue());
                bestMonthlyPerformance = Math.max(bestMonthlyPerformance, monthlyPerformance.getValue());
                dateBuffer.append(monthFormat.format(monthlyPerformance.getDate()));
                performanceBuffer.append(StringUtils.leftPad(twoDigitFormat.format(monthlyPerformance.getValue() * 100), 6) + "% ");
                if (monthlyPerformance.getValue() > 0) {
                    positiveMonths++;
                } else {
                    negativeMonths++;
                }
            }
            buffer.append(dateBuffer.toString() + "\r\n");
            buffer.append(performanceBuffer.toString() + "\r\n");
        }

        // yearlyPerformances
        int positiveYears = 0;
        int negativeYears = 0;
        Collection<PeriodPerformanceVO> yearlyPerformances = resultVO.getYearlyPerformances();
        if ((yearlyPerformances != null)) {
            StringBuffer dateBuffer = new StringBuffer("year:               ");
            StringBuffer performanceBuffer = new StringBuffer("yearlyPerformance:  ");
            for (PeriodPerformanceVO yearlyPerformance : yearlyPerformances) {
                dateBuffer.append(yearFormat.format(yearlyPerformance.getDate()));
                performanceBuffer.append(StringUtils.leftPad(twoDigitFormat.format(yearlyPerformance.getValue() * 100), 6) + "% ");
                if (yearlyPerformance.getValue() > 0) {
                    positiveYears++;
                } else {
                    negativeYears++;
                }
            }
            buffer.append(dateBuffer.toString() + "\r\n");
            buffer.append(performanceBuffer.toString() + "\r\n");
        }

        if ((monthlyPerformances != null)) {
            buffer.append("posMonths=" + positiveMonths + " negMonths=" + negativeMonths);
            if ((yearlyPerformances != null)) {
                buffer.append(" posYears=" + positiveYears + " negYears=" + negativeYears);
            }
            buffer.append("\r\n");
        }

        PerformanceKeysVO performanceKeys = resultVO.getPerformanceKeys();
        MaxDrawDownVO maxDrawDownVO = resultVO.getMaxDrawDown();
        if (performanceKeys != null && maxDrawDownVO != null) {
            buffer.append("avgM=" + twoDigitFormat.format(performanceKeys.getAvgM() * 100) + "%");
            buffer.append(" stdM=" + twoDigitFormat.format(performanceKeys.getStdM() * 100) + "%");
            buffer.append(" avgY=" + twoDigitFormat.format(performanceKeys.getAvgY() * 100) + "%");
            buffer.append(" stdY=" + twoDigitFormat.format(performanceKeys.getStdY() * 100) + "% ");
            buffer.append(" sharpRatio=" + twoDigitFormat.format(performanceKeys.getSharpRatio()) + "\r\n");

            buffer.append("maxMonthlyDrawDown=" + twoDigitFormat.format(-maxDrawDownM * 100) + "%");
            buffer.append(" bestMonthlyPerformance=" + twoDigitFormat.format(bestMonthlyPerformance * 100) + "%");
            buffer.append(" maxDrawDown=" + twoDigitFormat.format(maxDrawDownVO.getAmount() * 100) + "%");
            buffer.append(" maxDrawDownPeriod=" + twoDigitFormat.format(maxDrawDownVO.getPeriod() / 86400000) + "days");
            buffer.append(" colmarRatio=" + twoDigitFormat.format(performanceKeys.getAvgY() / maxDrawDownVO.getAmount()));

            buffer.append("\r\n");
        }

        buffer.append("WinningTrades:");
        buffer.append(printTrades(resultVO.getWinningTrades()));

        buffer.append("LoosingTrades:");
        buffer.append(printTrades(resultVO.getLoosingTrades()));

        buffer.append("AllTrades:");
        buffer.append(printTrades(resultVO.getAllTrades()));

        buffer.append("winningTradesPct: " + twoDigitFormat.format(100.0 * resultVO.getWinningTrades().getCount() / resultVO.getAllTrades().getCount()) + "%");

        return buffer.toString();
    }

    private static StringBuffer printTrades(TradesVO tradesVO) {

        StringBuffer buffer = new StringBuffer();
        buffer.append(" count=" + tradesVO.getCount());
        buffer.append(" totalProfit=" + twoDigitFormat.format(tradesVO.getTotalProfit()));
        buffer.append(" avgProfit=" + twoDigitFormat.format(tradesVO.getAvgProfit()));
        buffer.append(" avgProfitPct=" + twoDigitFormat.format(tradesVO.getAvgProfitPct() * 100) + "%");
        buffer.append(" avgAge=" + twoDigitFormat.format(tradesVO.getAvgAge()));
        buffer.append("\r\n");

        return buffer;
    }

    private static void logMultiLineString(String input) {

        String[] lines = input.split("\r\n");
        for (String line : lines) {
            resultLogger.info(line);
        }
    }

    private static class UnivariateFunction implements UnivariateRealFunction {

        private String param;

        public UnivariateFunction(String parameter) {
            super();
            this.param = parameter;
        }

        @Override
        public double value(double input) throws FunctionEvaluationException {

            ServiceLocator.instance().getConfiguration().setProperty(this.param, String.valueOf(input));

            SimulationResultVO resultVO = ServiceLocator.instance().getService("simulationService", SimulationService.class).runByUnderlyings();
            double result = resultVO.getPerformanceKeys().getSharpRatio();

            resultLogger.info("optimize on " + this.param + "=" + SimulationServiceImpl.format.format(input) + " "
                    + SimulationServiceImpl.convertStatisticsToShortString(resultVO));

            return result;
        }
    }

    private static class MultivariateFunction implements MultivariateRealFunction {

        private String[] params;

        public MultivariateFunction(String[] parameters) {
            super();
            this.params = parameters;
        }

        @Override
        public double value(double[] input) throws FunctionEvaluationException {

            StringBuffer buffer = new StringBuffer("optimize on ");
            for (int i = 0; i < input.length; i++) {

                String param = this.params[i];
                double value = input[i];

                ServiceLocator.instance().getConfiguration().setProperty(param, String.valueOf(value));

                buffer.append(param + "=" + SimulationServiceImpl.format.format(value) + " ");
            }

            SimulationResultVO resultVO = ServiceLocator.instance().getService("simulationService", SimulationService.class).runByUnderlyings();
            double result = resultVO.getPerformanceKeys().getSharpRatio();

            resultLogger.info(buffer.toString() + SimulationServiceImpl.convertStatisticsToShortString(resultVO));

            return result;
        }
    }
}
