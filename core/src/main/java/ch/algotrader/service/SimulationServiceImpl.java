/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.service;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathException;
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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.espertech.esperio.CoordinatedAdapter;
import com.espertech.esperio.csv.CSVInputAdapter;

import ch.algotrader.cache.CacheManager;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.enumeration.LifecyclePhase;
import ch.algotrader.enumeration.MarketDataType;
import ch.algotrader.enumeration.OperationMode;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.esper.io.CsvBarInputAdapter;
import ch.algotrader.esper.io.CsvBarInputAdapterSpec;
import ch.algotrader.esper.io.CsvTickInputAdapter;
import ch.algotrader.esper.io.CsvTickInputAdapterSpec;
import ch.algotrader.esper.io.DBBarInputAdapter;
import ch.algotrader.esper.io.DBTickInputAdapter;
import ch.algotrader.esper.io.GenericEventInputAdapterSpec;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.report.ReportManager;
import ch.algotrader.util.metric.MetricsUtil;
import ch.algotrader.vo.EndOfSimulationVO;
import ch.algotrader.vo.LifecycleEventVO;
import ch.algotrader.vo.MaxDrawDownVO;
import ch.algotrader.vo.OptimizationResultVO;
import ch.algotrader.vo.PerformanceKeysVO;
import ch.algotrader.vo.PeriodPerformanceVO;
import ch.algotrader.vo.SimulationResultVO;
import ch.algotrader.vo.TradesVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class SimulationServiceImpl implements SimulationService, InitializingBean, ApplicationContextAware {

    private static Logger logger = Logger.getLogger(SimulationServiceImpl.class.getName());
    private static Logger resultLogger = Logger.getLogger(SimulationServiceImpl.class.getName() + ".RESULT");
    private static DecimalFormat twoDigitFormat = new DecimalFormat("#,##0.00");
    private static DateFormat monthFormat = new SimpleDateFormat(" MMM-yy ");
    private static DateFormat yearFormat = new SimpleDateFormat("   yyyy ");
    private static final NumberFormat format = NumberFormat.getInstance();

    private final CommonConfig commonConfig;

    private final PositionService positionService;

    private final ResetService resetService;

    private final TransactionService transactionService;

    private final PortfolioService portfolioService;

    private final LookupService lookupService;

    private final EventDispatcher eventDispatcher;

    private final EngineManager engineManager;

    private final Engine serverEngine;

    private final CacheManager cacheManager;

    private volatile ApplicationContext applicationContext;

    public SimulationServiceImpl(final CommonConfig commonConfig,
            final PositionService positionService,
            final ResetService resetService,
            final TransactionService transactionService,
            final PortfolioService portfolioService,
            final LookupService lookupService,
            final EventDispatcher eventDispatcher,
            final EngineManager engineManager,
            final Engine serverEngine,
            final CacheManager cacheManager) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(positionService, "PositionService is null");
        Validate.notNull(resetService, "ResetService is null");
        Validate.notNull(transactionService, "TransactionService is null");
        Validate.notNull(portfolioService, "PortfolioService is null");
        Validate.notNull(lookupService, "LookupService is null");
        Validate.notNull(eventDispatcher, "EventDispatcher is null");
        Validate.notNull(engineManager, "EngineManager is null");
        Validate.notNull(serverEngine, "Engine is null");
        Validate.notNull(cacheManager, "CacheManager is null");

        this.commonConfig = commonConfig;
        this.positionService = positionService;
        this.resetService = resetService;
        this.transactionService = transactionService;
        this.portfolioService = portfolioService;
        this.lookupService = lookupService;
        this.eventDispatcher = eventDispatcher;
        this.engineManager = engineManager;
        this.serverEngine = serverEngine;
        this.cacheManager = cacheManager;
    }

    @Override
    public void afterPropertiesSet() {

        int portfolioDigits = this.commonConfig.getPortfolioDigits();
        format.setGroupingUsed(false);
        format.setMinimumFractionDigits(portfolioDigits);
        format.setMaximumFractionDigits(portfolioDigits);
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {

        this.applicationContext = applicationContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SimulationResultVO runSimulation() {

        long startTime = System.currentTimeMillis();

        // reset the db
        this.resetService.resetDB();

        // rebalance portfolio (to distribute initial CREDIT to strategies)
        this.transactionService.rebalancePortfolio();

        // init coordination
        this.serverEngine.initCoordination();

        // init modules of all activatable strategies
        Collection<Strategy> strategies = this.lookupService.getAutoActivateStrategies();
        for (Strategy strategy : strategies) {
            this.engineManager.getEngine(strategy.getName()).deployAllModules();
        }

        this.eventDispatcher.sendAllLocal(new LifecycleEventVO(OperationMode.SIMULATION, LifecyclePhase.INIT, new Date()));

        this.eventDispatcher.sendAllLocal(new LifecycleEventVO(OperationMode.SIMULATION, LifecyclePhase.PREFEED, new Date()));

        // feed the ticks
        feedMarketData();

        this.eventDispatcher.sendAllLocal(new LifecycleEventVO(OperationMode.SIMULATION, LifecyclePhase.EXIT, new Date()));

        // log metrics in case they have been enabled
        MetricsUtil.logMetrics();
        this.engineManager.logStatementMetrics();

        // close all open positions that might still exist
        for (Position position : this.lookupService.getOpenTradeablePositions()) {
            this.positionService.closePosition(position.getId(), false);
        }

        // send the EndOfSimulation event
        this.serverEngine.sendEvent(new EndOfSimulationVO());

        this.eventDispatcher.sendAllLocal(new LifecycleEventVO(OperationMode.SIMULATION, LifecyclePhase.GET_RESULTS, new Date()));

        // get the results
        SimulationResultVO resultVO = getSimulationResultVO(startTime);

        // destroy all service providers
        for (Strategy strategy : strategies) {
            this.engineManager.destroyEngine(strategy.getName());
        }

        // clear the second-level cache
        net.sf.ehcache.CacheManager.getInstance().clearAll();

        // close all reports
        try {
            ReportManager.closeAll();
        } catch (IOException ex) {
            throw new SimulationServiceException(ex);
        }

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
    private void feedMarketData() {

        final Collection<Security> securities = this.lookupService.getSubscribedSecuritiesForAutoActivateStrategies();

        final CommonConfig commonConfig = this.commonConfig;
        final File baseDir = commonConfig.getDataSetLocation();

        if (commonConfig.isFeedGenericEvents()) {
            feedGenericEvents(baseDir);
        }

        if (commonConfig.isFeedCSV()) {
            feedCSV(securities, baseDir);
        }

        if (commonConfig.isFeedDB()) {
            feedDB();
        }

        // initialize all securityStrings for subscribed securities
        this.lookupService.initSecurityStrings();
        for (Security security : securities) {
            this.cacheManager.put(security);
        }

        this.serverEngine.startCoordination();
    }

    private void feedGenericEvents(File baseDir) {

        File genericdata = new File(baseDir, "genericdata");
        File dataDir = new File(genericdata, this.commonConfig.getDataSet());
        if (dataDir == null || !dataDir.exists() || !dataDir.isDirectory()) {
            logger.warn("no generic events available");
        } else {
            File[] files = dataDir.listFiles();
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
                this.serverEngine.addEventType(eventTypeName, eventClassName);

                CoordinatedAdapter inputAdapter = new CSVInputAdapter(null, new GenericEventInputAdapterSpec(file, eventTypeName));
                this.serverEngine.coordinate(inputAdapter);

                logger.debug("started feeding file " + file.getName());
            }
        }
    }

    private void feedCSV(Collection<Security> securities, File baseDir) {

        MarketDataType marketDataType = this.commonConfig.getDataSetType();
        File marketDataTypeDir = new File(baseDir, marketDataType.toString().toLowerCase() + "data");
        File dataDir = new File(marketDataTypeDir, this.commonConfig.getDataSet());

        if (this.commonConfig.isFeedAllMarketDataFiles()) {

            if (dataDir == null || !dataDir.exists() || !dataDir.isDirectory()) {
                logger.warn("no market data events available");
            } else {

                // coordinate all files
                for (File file : dataDir.listFiles()) {
                    feedFile(file);
                }
            }

        } else {

            for (Security security : securities) {

                // try to find the security by isin, symbol, bbgid, ric conid or id
                File file = null;

                if (security.getSymbol() != null) {
                    file = new File(dataDir, security.getSymbol() + ".csv");
                }

                if ((file == null || !file.exists()) && security.getIsin() != null) {
                    file = new File(dataDir, security.getIsin() + ".csv");
                }

                if ((file == null || !file.exists()) && security.getBbgid() != null) {
                    file = new File(dataDir, security.getBbgid() + ".csv");
                }

                if ((file == null || !file.exists()) && security.getRic() != null) {
                    file = new File(dataDir, security.getRic() + ".csv");
                }

                if ((file == null || !file.exists()) && security.getConid() != null) {
                    file = new File(dataDir, security.getConid() + ".csv");
                }

                if (file == null || !file.exists()) {
                    file = new File(dataDir, security.getId() + ".csv");
                }

                if (file == null || !file.exists()) {
                    logger.warn("no data available for " + security.getSymbol() + " in " + dataDir);
                    continue;
                } else {
                    logger.info("data available for " + security.getSymbol());
                }

                feedFile(file);
            }
        }
    }

    private void feedFile(File file) {

        CoordinatedAdapter inputAdapter;
        MarketDataType marketDataType = this.commonConfig.getDataSetType();
        if (MarketDataType.TICK.equals(marketDataType)) {
            inputAdapter = new CsvTickInputAdapter(new CsvTickInputAdapterSpec(file));
        } else if (MarketDataType.BAR.equals(marketDataType)) {
            inputAdapter = new CsvBarInputAdapter(new CsvBarInputAdapterSpec(file, this.commonConfig.getBarSize()));
        } else {
            throw new SimulationServiceException("incorrect parameter for dataSetType: " + marketDataType);
        }

        this.serverEngine.coordinate(inputAdapter);

        logger.debug("started feeding file " + file.getName());
    }

    private void feedDB() {

        MarketDataType marketDataType = this.commonConfig.getDataSetType();
        int feedBatchSize = this.commonConfig.getFeedBatchSize();

        if (MarketDataType.TICK.equals(marketDataType)) {

            DBTickInputAdapter inputAdapter = new DBTickInputAdapter(feedBatchSize);
            this.serverEngine.coordinate(inputAdapter);
            logger.debug("started feeding ticks from db");

        } else if (MarketDataType.BAR.equals(marketDataType)) {

            DBBarInputAdapter inputAdapter = new DBBarInputAdapter(feedBatchSize, this.commonConfig.getBarSize());
            this.serverEngine.coordinate(inputAdapter);
            logger.debug("started feeding bars from db");

        } else {
            throw new SimulationServiceException("incorrect parameter for dataSetType: " + marketDataType);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void simulateWithCurrentParams() {

        SimulationResultVO resultVO = runSimulation();
        logMultiLineString(convertStatisticsToLongString(resultVO));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void simulateBySingleParam(final String parameter, final String value) {

        Validate.notEmpty(parameter, "Parameter is empty");
        Validate.notEmpty(value, "Value is empty");

        System.setProperty(parameter, value);

        SimulationResultVO resultVO = runSimulation();
        resultLogger.info("optimize " + parameter + "=" + value + " " + convertStatisticsToShortString(resultVO));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void simulateByMultiParam(final String[] parameters, final String[] values) {

        Validate.notNull(parameters, "Parameter is null");
        Validate.notNull(values, "Value is null");

        StringBuffer buffer = new StringBuffer();
        buffer.append("optimize ");
        for (int i = 0; i < parameters.length; i++) {
            buffer.append(parameters[i] + "=" + values[i] + " ");
            System.setProperty(parameters[i], values[i]);
        }

        SimulationResultVO resultVO = runSimulation();
        buffer.append(convertStatisticsToShortString(resultVO));
        resultLogger.info(buffer.toString());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void optimizeSingleParamLinear(final String parameter, final double min, final double max, final double increment) {

        Validate.notEmpty(parameter, "Parameter is empty");

        for (double i = min; i <= max; i += increment) {

            System.setProperty(parameter, format.format(i));

            SimulationResultVO resultVO = runSimulation();
            resultLogger.info(parameter + "=" + format.format(i) + " " + convertStatisticsToShortString(resultVO));

        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void optimizeSingleParamByValues(final String parameter, final double[] values) {

        Validate.notEmpty(parameter, "Parameter is empty");
        Validate.notNull(values, "Value is null");

        for (double value : values) {

            System.setProperty(parameter, format.format(value));

            SimulationResultVO resultVO = runSimulation();
            resultLogger.info(parameter + "=" + format.format(value) + " " + convertStatisticsToShortString(resultVO));
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OptimizationResultVO optimizeSingleParam(final String parameter, final double min, final double max, final double accuracy) {

        Validate.notEmpty(parameter, "Parameter is empty");

        try {
            UnivariateRealFunction function = new UnivariateFunction(this, parameter);
            UnivariateRealOptimizer optimizer = new BrentOptimizer();
            optimizer.setAbsoluteAccuracy(accuracy);
            optimizer.optimize(function, GoalType.MAXIMIZE, min, max);
            OptimizationResultVO optimizationResult = new OptimizationResultVO();
            optimizationResult.setParameter(parameter);
            optimizationResult.setResult(optimizer.getResult());
            optimizationResult.setFunctionValue(optimizer.getFunctionValue());
            optimizationResult.setIterations(optimizer.getIterationCount());

            return optimizationResult;
        } catch (MathException ex) {
            throw new SimulationServiceException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void optimizeMultiParamLinear(final String[] parameters, final double[] mins, final double[] maxs, final double[] increments) {

        Validate.notNull(parameters, "Parameter is null");
        Validate.notNull(mins, "Mins is null");
        Validate.notNull(maxs, "Maxs is null");
        Validate.notNull(increments, "Increments is null");

        int roundDigits = this.commonConfig.getPortfolioDigits();
        for (double i0 = mins[0]; i0 <= maxs[0]; i0 += increments[0]) {
            System.setProperty(parameters[0], format.format(i0));
            String message0 = parameters[0] + "=" + format.format(MathUtils.round(i0, roundDigits));

            if (parameters.length >= 2) {
                for (double i1 = mins[1]; i1 <= maxs[1]; i1 += increments[1]) {
                    System.setProperty(parameters[1], format.format(i1));
                    String message1 = parameters[1] + "=" + format.format(MathUtils.round(i1, roundDigits));

                    if (parameters.length >= 3) {
                        for (double i2 = mins[2]; i2 <= maxs[2]; i2 += increments[2]) {
                            System.setProperty(parameters[2], format.format(i2));
                            String message2 = parameters[2] + "=" + format.format(MathUtils.round(i2, roundDigits));

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

    /**
     * {@inheritDoc}
     */
    @Override
    public void optimizeMultiParam(final String[] parameters, final double[] starts) {

        Validate.notNull(parameters, "Parameter is null");
        Validate.notNull(starts, "Starts is null");

        RealPointValuePair result;
        try {
            MultivariateRealFunction function = new MultivariateFunction(this, parameters);
            MultivariateRealOptimizer optimizer = new MultiDirectional();
            optimizer.setConvergenceChecker(new SimpleScalarValueChecker(0.0, 0.01));
            result = optimizer.optimize(function, GoalType.MAXIMIZE, starts);
            for (int i = 0; i < result.getPoint().length; i++) {
                resultLogger.info("optimal value for " + parameters[i] + "=" + format.format(result.getPoint()[i]));
            }
            resultLogger.info("functionValue: " + format.format(result.getValue()) + " needed iterations: " + optimizer.getEvaluations() + ")");
        } catch (MathException ex) {
            throw new SimulationServiceException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private SimulationResultVO getSimulationResultVO(final long startTime) {

        SimulationResultVO resultVO = new SimulationResultVO();
        resultVO.setMins(((double) (System.currentTimeMillis() - startTime)) / 60000);

        Engine engine = this.serverEngine;
        if (!engine.isDeployed("INSERT_INTO_PERFORMANCE_KEYS")) {
            resultVO.setAllTrades(new TradesVO(0, 0, 0, 0));
            return resultVO;
        }

        PerformanceKeysVO performanceKeys = (PerformanceKeysVO) engine.getLastEvent("INSERT_INTO_PERFORMANCE_KEYS");
        List<PeriodPerformanceVO> monthlyPerformances = engine.getAllEvents("KEEP_MONTHLY_PERFORMANCE");
        MaxDrawDownVO maxDrawDown = (MaxDrawDownVO) engine.getLastEvent("INSERT_INTO_MAX_DRAW_DOWN");
        TradesVO allTrades = (TradesVO) engine.getLastEvent("INSERT_INTO_ALL_TRADES");
        TradesVO winningTrades = (TradesVO) engine.getLastEvent("INSERT_INTO_WINNING_TRADES");
        TradesVO loosingTrades = (TradesVO) engine.getLastEvent("INSERT_INTO_LOOSING_TRADES");

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
        resultVO.setDataSet(this.commonConfig.getDataSet());
        resultVO.setNetLiqValue(this.portfolioService.getNetLiqValueDouble());
        resultVO.setMonthlyPerformances(monthlyPerformances);
        resultVO.setYearlyPerformances(yearlyPerformances);
        resultVO.setPerformanceKeys(performanceKeys);
        resultVO.setMaxDrawDown(maxDrawDown);
        resultVO.setAllTrades(allTrades);
        resultVO.setWinningTrades(winningTrades);
        resultVO.setLoosingTrades(loosingTrades);

        // get potential strategy specific results
        Map<String, Object> strategyResults = new HashMap<String, Object>();
        for (StrategyService strategyService : this.applicationContext.getBeansOfType(StrategyService.class).values()) {
            strategyResults.putAll(strategyService.getSimulationResults());
        }
        resultVO.setStrategyResults(strategyResults);

        return resultVO;
    }

    @SuppressWarnings("unchecked")
    private String convertStatisticsToShortString(SimulationResultVO resultVO) {

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

        StringBuffer buffer = new StringBuffer();
        buffer.append("execution time (min): " + (new DecimalFormat("0.00")).format(resultVO.getMins()) + "\r\n");

        if (resultVO.getAllTrades().getCount() == 0) {
            buffer.append("no trades took place! \r\n");
            return buffer.toString();
        }

        buffer.append("dataSet: " + this.commonConfig.getDataSet() + "\r\n");

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

    private StringBuffer printTrades(TradesVO tradesVO, long totalTrades) {

        StringBuffer buffer = new StringBuffer();
        buffer.append(" count=" + tradesVO.getCount());
        if (tradesVO.getCount() != totalTrades) {
            buffer.append("(" + twoDigitFormat.format(100.0 * tradesVO.getCount() / totalTrades) + "%)");
        }
        buffer.append(" totalProfit=" + twoDigitFormat.format(tradesVO.getTotalProfit()));
        buffer.append(" avgProfit=" + twoDigitFormat.format(tradesVO.getAvgProfit()));
        buffer.append(" avgProfitPct=" + twoDigitFormat.format(tradesVO.getAvgProfitPct() * 100) + "%");
        buffer.append("\r\n");

        return buffer;
    }

    private void logMultiLineString(String input) {

        String[] lines = input.split("\r\n");
        for (String line : lines) {
            resultLogger.info(line);
        }
    }

    private class UnivariateFunction implements UnivariateRealFunction {

        private final SimulationService simulationService;
        private final String param;

        public UnivariateFunction(final SimulationService simulationService, final String parameter) {
            super();
            this.simulationService = simulationService;
            this.param = parameter;
        }

        @Override
        public double value(double input) throws FunctionEvaluationException {

            System.setProperty(this.param, String.valueOf(input));

            SimulationResultVO resultVO = this.simulationService.runSimulation();
            double result = resultVO.getPerformanceKeys().getSharpeRatio();

            resultLogger.info("optimize on " + this.param + "=" + SimulationServiceImpl.format.format(input) + " " + SimulationServiceImpl.this.convertStatisticsToShortString(resultVO));

            return result;
        }
    }

    private class MultivariateFunction implements MultivariateRealFunction {

        private final SimulationService simulationService;
        private final String[] params;

        public MultivariateFunction(final SimulationService simulationService, final String[] parameters) {
            super();
            this.simulationService = simulationService;
            this.params = parameters;
        }

        @Override
        public double value(double[] input) throws FunctionEvaluationException {

            StringBuffer buffer = new StringBuffer("optimize on ");
            for (int i = 0; i < input.length; i++) {

                String param = this.params[i];
                double value = input[i];

                System.setProperty(param, String.valueOf(value));

                buffer.append(param + "=" + SimulationServiceImpl.format.format(value) + " ");
            }

            SimulationResultVO resultVO = this.simulationService.runSimulation();
            double result = resultVO.getPerformanceKeys().getSharpeRatio();

            resultLogger.info(buffer.toString() + SimulationServiceImpl.this.convertStatisticsToShortString(resultVO));

            return result;
        }
    }
}
