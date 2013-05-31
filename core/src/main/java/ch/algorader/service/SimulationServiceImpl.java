/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algorader.service;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import ch.algorader.entity.strategy.StrategyImpl;
import ch.algorader.esper.EsperManager;
import ch.algorader.esper.io.CsvBarInputAdapterSpec;
import ch.algorader.esper.io.CsvTickInputAdapterSpec;
import ch.algorader.esper.io.GenericEventInputAdapterSpec;
import ch.algorader.util.MyLogger;
import ch.algorader.util.metric.MetricsUtil;
import ch.algorader.util.spring.Configuration;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.strategy.Strategy;
import com.algoTrader.enumeration.MarketDataType;
import com.algoTrader.service.SimulationService;
import com.algoTrader.service.SimulationServiceBase;
import com.algoTrader.service.SimulationServiceException;
import com.algoTrader.service.StrategyService;
import com.algoTrader.vo.EndOfSimulationVO;
import com.algoTrader.vo.MaxDrawDownVO;
import com.algoTrader.vo.OptimizationResultVO;
import com.algoTrader.vo.PerformanceKeysVO;
import com.algoTrader.vo.PeriodPerformanceVO;
import com.algoTrader.vo.SimulationResultVO;
import com.algoTrader.vo.TradesVO;
import com.espertech.esperio.csv.CSVInputAdapterSpec;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class SimulationServiceImpl extends SimulationServiceBase implements InitializingBean {

    private static Logger logger = MyLogger.getLogger(SimulationServiceImpl.class.getName());
    private static Logger resultLogger = MyLogger.getLogger(SimulationServiceImpl.class.getName() + ".RESULT");
    private static DecimalFormat twoDigitFormat = new DecimalFormat("#,##0.00");
    private static DecimalFormat fourDigitFormat = new DecimalFormat("#,##0.00");
    private static DateFormat monthFormat = new SimpleDateFormat(" MMM-yy ");
    private static DateFormat yearFormat = new SimpleDateFormat("   yyyy ");
    private static final NumberFormat format = NumberFormat.getInstance();

    private @Value("${simulation.roundDigits}") int roundDigits;
    private @Value("${dataSource.dataSetLocation}") String dataSetLocation;
    private @Value("${dataSource.feedGenericEvents}") boolean feedGenericEvents;

    @Override
    public void afterPropertiesSet() throws Exception {

        format.setGroupingUsed(false);
        format.setMinimumFractionDigits(this.roundDigits);
        format.setMaximumFractionDigits(this.roundDigits);
    }

    @Override
    protected SimulationResultVO handleRunSimulation() {

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
        getPortfolioPersistenceService().rebalancePortfolio();

        // init all StrategyServices in the classpath
        for (StrategyService strategyService : ServiceLocator.instance().getServices(StrategyService.class)) {
            strategyService.initSimulation();
        }

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

    /**
     * Starts the Market Data Feed. MarketData for all Securities that are subscribed by Strategies
     * marked as {@code autoActivate} are fed into the System. Depending on the VM-Argument {@code dataSetType},
     * either {@code TICKs} or {@code BARs} are fed. If Generic Events are enabled via the VM-Argument
     * {@code feedGenericEvents} they are processed a long Market Data Events. All Events are fed to
     * the system in the correct order defined by their dateTime value.
     */
    private void inputCSV() {

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
    protected void handleSimulateWithCurrentParams() throws Exception {

        SimulationResultVO resultVO = runSimulation();
        logMultiLineString(convertStatisticsToLongString(resultVO));
    }

    @Override
    protected void handleSimulateBySingleParam(String parameter, String value) throws Exception {

        getConfiguration().setProperty(parameter, value);

        SimulationResultVO resultVO = runSimulation();
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

        SimulationResultVO resultVO = runSimulation();
        buffer.append(convertStatisticsToShortString(resultVO));
        resultLogger.info(buffer.toString());
    }

    @Override
    protected void handleOptimizeSingleParamLinear(String parameter, double min, double max, double increment) throws Exception {

        for (double i = min; i <= max; i += increment) {

            getConfiguration().setProperty(parameter, format.format(i));

            SimulationResultVO resultVO = runSimulation();
            resultLogger.info(parameter + "=" + format.format(i) + " " + convertStatisticsToShortString(resultVO));

        }
    }

    @Override
    protected void handleOptimizeSingleParamByValues(String parameter, double[] values) throws Exception {

        for (double value : values) {

            getConfiguration().setProperty(parameter, format.format(value));

            SimulationResultVO resultVO = runSimulation();
            resultLogger.info(parameter + "=" + format.format(value) + " " + convertStatisticsToShortString(resultVO));
        }
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

                            SimulationResultVO resultVO = runSimulation();
                            resultLogger.info(message0 + " " + message1 + " " + message2 + " " + convertStatisticsToShortString(resultVO));
                        }
                    } else {
                        SimulationResultVO resultVO = runSimulation();
                        resultLogger.info(message0 + " " + message1 + " " + convertStatisticsToShortString(resultVO));
                    }
                }
            } else {
                SimulationResultVO resultVO = runSimulation();
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

    @SuppressWarnings("unchecked")
    private SimulationResultVO getSimulationResultVO(final long startTime) {

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

        // get potential strategy specific results
        Map<String, Object> strategyResults = new HashMap<String, Object>();
        for (StrategyService strategyService : ServiceLocator.instance().getServices(StrategyService.class)) {
            strategyResults.putAll(strategyService.getSimulationResults());
        }
        resultVO.setStrategyResults(strategyResults);

        return resultVO;
    }

    @SuppressWarnings("unchecked")
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
        buffer.append(" stdY=" + twoDigitFormat.format(performanceKeys.getStdY() * 100) + "%");
        buffer.append(" sharpe=" + twoDigitFormat.format(performanceKeys.getSharpeRatio()));
        buffer.append(" maxDDM=" + twoDigitFormat.format(-maxDrawDownM * 100) + "%");
        buffer.append(" bestMP=" + twoDigitFormat.format(bestMonthlyPerformance * 100) + "%");
        buffer.append(" maxDD=" + twoDigitFormat.format(maxDrawDownVO.getAmount() * 100.0) + "%");
        buffer.append(" maxDDPer=" + twoDigitFormat.format(maxDrawDownVO.getPeriod() / 86400000));
        buffer.append(" winTrds=" + resultVO.getWinningTrades().getCount());
        buffer.append(" winTrdsPct=" + twoDigitFormat.format(100.0 * resultVO.getWinningTrades().getCount() / resultVO.getAllTrades().getCount()) + "%");
        buffer.append(" avgPPctWin=" + twoDigitFormat.format(resultVO.getWinningTrades().getAvgProfitPct() * 100.0) + "%");
        buffer.append(" loosTrds=" + resultVO.getLoosingTrades().getCount());
        buffer.append(" loosTrdsPct=" + twoDigitFormat.format(100.0 * resultVO.getLoosingTrades().getCount() / resultVO.getAllTrades().getCount()) + "%");
        buffer.append(" avgPPctLoos=" + twoDigitFormat.format(resultVO.getLoosingTrades().getAvgProfitPct() * 100.0) + "%");
        buffer.append(" totalTrds=" + resultVO.getAllTrades().getCount());

        for (Map.Entry<String, Object> entry : ((Map<String, Object>) resultVO.getStrategyResults()).entrySet()) {
            buffer.append(" " + entry.getKey() + "=" + entry.getValue());
        }

        return buffer.toString();
    }

    @SuppressWarnings("unchecked")
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
            buffer.append(" sharpeRatio=" + twoDigitFormat.format(performanceKeys.getSharpeRatio()) + "\r\n");

            buffer.append("maxMonthlyDrawDown=" + twoDigitFormat.format(-maxDrawDownM * 100) + "%");
            buffer.append(" bestMonthlyPerformance=" + twoDigitFormat.format(bestMonthlyPerformance * 100) + "%");
            buffer.append(" maxDrawDown=" + twoDigitFormat.format(maxDrawDownVO.getAmount() * 100) + "%");
            buffer.append(" maxDrawDownPeriod=" + twoDigitFormat.format(maxDrawDownVO.getPeriod() / 86400000) + "days");
            buffer.append(" colmarRatio=" + twoDigitFormat.format(performanceKeys.getAvgY() / maxDrawDownVO.getAmount()));

            buffer.append("\r\n");
        }

        buffer.append("WinningTrades:");
        buffer.append(printTrades(resultVO.getWinningTrades(), resultVO.getAllTrades().getCount()));

        buffer.append("LoosingTrades:");
        buffer.append(printTrades(resultVO.getLoosingTrades(), resultVO.getAllTrades().getCount()));

        buffer.append("AllTrades:");
        buffer.append(printTrades(resultVO.getAllTrades(), resultVO.getAllTrades().getCount()));

        for (Map.Entry<String, Object> entry : ((Map<String, Object>) resultVO.getStrategyResults()).entrySet()) {
            buffer.append(entry.getKey() + "=" + entry.getValue() + " ");
        }

        return buffer.toString();
    }

    private static StringBuffer printTrades(TradesVO tradesVO, long totalTrades) {

        StringBuffer buffer = new StringBuffer();
        buffer.append(" count=" + tradesVO.getCount());
        if (tradesVO.getCount() != totalTrades) {
            buffer.append("(" + twoDigitFormat.format(100.0 * tradesVO.getCount() / totalTrades) + "%)");
        }
        buffer.append(" totalProfit=" + twoDigitFormat.format(tradesVO.getTotalProfit()));
        buffer.append(" avgProfit=" + twoDigitFormat.format(tradesVO.getAvgProfit()));
        buffer.append(" avgProfitPct=" + twoDigitFormat.format(tradesVO.getAvgProfitPct() * 100) + "%");
        buffer.append(" avgAge=" + fourDigitFormat.format(tradesVO.getAvgAge()));
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

            SimulationResultVO resultVO = ServiceLocator.instance().getService("simulationService", SimulationService.class).runSimulation();
            double result = resultVO.getPerformanceKeys().getSharpeRatio();

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

            SimulationResultVO resultVO = ServiceLocator.instance().getService("simulationService", SimulationService.class).runSimulation();
            double result = resultVO.getPerformanceKeys().getSharpeRatio();

            resultLogger.info(buffer.toString() + SimulationServiceImpl.convertStatisticsToShortString(resultVO));

            return result;
        }
    }
}
