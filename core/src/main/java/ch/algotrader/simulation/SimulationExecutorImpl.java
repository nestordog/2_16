/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.simulation;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.espertech.esperio.csv.CSVInputAdapter;

import ch.algotrader.cache.CacheManager;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityImpl;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.enumeration.LifecyclePhase;
import ch.algotrader.enumeration.MarketDataType;
import ch.algotrader.enumeration.OperationMode;
import ch.algotrader.enumeration.TransactionType;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.esper.io.CsvBarInputAdapter;
import ch.algotrader.esper.io.CsvBarInputAdapterSpec;
import ch.algotrader.esper.io.CsvTickInputAdapter;
import ch.algotrader.esper.io.CsvTickInputAdapterSpec;
import ch.algotrader.esper.io.CvsTypeCoercer;
import ch.algotrader.esper.io.DBBarInputAdapter;
import ch.algotrader.esper.io.DBTickInputAdapter;
import ch.algotrader.esper.io.GenericEventInputAdapterSpec;
import ch.algotrader.event.EventListenerRegistry;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.event.dispatch.EventRecipient;
import ch.algotrader.report.BackTestReport;
import ch.algotrader.report.ReportManager;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.MarketDataService;
import ch.algotrader.service.PortfolioService;
import ch.algotrader.service.PositionService;
import ch.algotrader.service.ResetService;
import ch.algotrader.service.ServerLookupService;
import ch.algotrader.service.StrategyPersistenceService;
import ch.algotrader.service.TransactionService;
import ch.algotrader.service.groups.StrategyGroup;
import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.util.DateTimePatterns;
import ch.algotrader.util.RoundUtil;
import ch.algotrader.util.metric.MetricsUtil;
import ch.algotrader.vo.EndOfSimulationVO;
import ch.algotrader.vo.LifecycleEventVO;
import ch.algotrader.vo.performance.MaxDrawDownVO;
import ch.algotrader.vo.performance.OptimizationResultVO;
import ch.algotrader.vo.performance.PerformanceKeysVO;
import ch.algotrader.vo.performance.PeriodPerformanceVO;
import ch.algotrader.vo.performance.SimulationResultVO;
import ch.algotrader.vo.performance.TradesVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class SimulationExecutorImpl implements SimulationExecutor, InitializingBean, ApplicationContextAware {

    private static final Logger LOGGER = LogManager.getLogger(SimulationExecutorImpl.class);
    private static final Logger RESULT_LOGGER = LogManager.getLogger("ch.algotrader.simulation.SimulationExecutor.RESULT");
    private static final DecimalFormat twoDigitFormat = new DecimalFormat("#,##0.00");
    private static final NumberFormat format = NumberFormat.getInstance();

    private final CommonConfig commonConfig;

    private final PositionService positionService;

    private final ResetService resetService;

    private final TransactionService transactionService;

    private final StrategyPersistenceService strategyPersistenceService;

    private final PortfolioService portfolioService;

    private final LookupService lookupService;

    private final MarketDataService marketDataService;

    private final ServerLookupService serverLookupService;

    private final EventListenerRegistry eventListenerRegistry;

    private final EventDispatcher eventDispatcher;

    private final EngineManager engineManager;

    private final Engine serverEngine;

    private final CacheManager cacheManager;

    private final SimulationResultFormatter resultFormatter;

    private volatile ApplicationContext applicationContext;

    public SimulationExecutorImpl(final CommonConfig commonConfig,
                                  final PositionService positionService,
                                  final ResetService resetService,
                                  final TransactionService transactionService,
                                  final PortfolioService portfolioService,
                                  final StrategyPersistenceService strategyPersistenceService,
                                  final LookupService lookupService,
                                  final MarketDataService marketDataService,
                                  final ServerLookupService serverLookupService,
                                  final EventListenerRegistry eventListenerRegistry,
                                  final EventDispatcher eventDispatcher,
                                  final EngineManager engineManager,
                                  final Engine serverEngine,
                                  final CacheManager cacheManager) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(positionService, "PositionService is null");
        Validate.notNull(resetService, "ResetService is null");
        Validate.notNull(transactionService, "TransactionService is null");
        Validate.notNull(portfolioService, "PortfolioService is null");
        Validate.notNull(strategyPersistenceService, "StrategyPersistenceService is null");
        Validate.notNull(lookupService, "LookupService is null");
        Validate.notNull(marketDataService, "MarketDataService is null");
        Validate.notNull(serverLookupService, "ServerLookupService is null");
        Validate.notNull(eventListenerRegistry, "EventListenerRegistry is null");
        Validate.notNull(eventDispatcher, "EventDispatcher is null");
        Validate.notNull(engineManager, "EngineManager is null");
        Validate.notNull(serverEngine, "Engine is null");
        Validate.notNull(cacheManager, "CacheManager is null");

        this.commonConfig = commonConfig;
        this.positionService = positionService;
        this.resetService = resetService;
        this.transactionService = transactionService;
        this.portfolioService = portfolioService;
        this.strategyPersistenceService = strategyPersistenceService;
        this.lookupService = lookupService;
        this.marketDataService = marketDataService;
        this.serverLookupService = serverLookupService;
        this.eventListenerRegistry = eventListenerRegistry;
        this.eventDispatcher = eventDispatcher;
        this.engineManager = engineManager;
        this.serverEngine = serverEngine;
        this.cacheManager = cacheManager;
        this.resultFormatter = new SimulationResultFormatter();
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
    public SimulationResultVO runSimulation(final StrategyGroup strategyGroup) {

        long startTime = System.currentTimeMillis();

        // init strategies
        initStrategies(strategyGroup);

        if (this.eventListenerRegistry.getListeners(LifecycleEventVO.class).isEmpty()) {
            LOGGER.warn("No life cycle event listeners have been registered");
        }

        // reset the db
        this.resetService.resetSimulation();

        rebalancePortfolio(strategyGroup);

        // init coordination
        this.serverEngine.initCoordination();
        this.serverEngine.deployAllModules();

        //strategy engines
        final Collection<Engine> strategyEngines = this.engineManager.getStrategyEngines();

        // LifecycleEvent: INIT
        broadcastLocal(LifecyclePhase.INIT);

        //deploy init modules
        for (final Engine engine: strategyEngines) {
            engine.deployInitModules();
        }

        // LifecycleEvent: PREFEED
        broadcastLocal(LifecyclePhase.PREFEED);

        //deploy run modules
        for (final Engine engine: strategyEngines) {
            engine.deployRunModules();
        }

        this.marketDataService.initSubscriptions();

        // LifecycleEvent: START
        broadcastLocal(LifecyclePhase.START);

        // feed the ticks
        feedMarketData();

        // LifecycleEvent: EXIT
        broadcastLocal(LifecyclePhase.EXIT);

        // log metrics in case they have been enabled
        MetricsUtil.logMetrics();
        this.engineManager.logStatementMetrics();

        // close all open positions that might still exist
        for (Position position : this.lookupService.getOpenTradeablePositions()) {
            this.positionService.closePosition(position.getId(), false);
        }

        // send the EndOfSimulation event
        this.serverEngine.sendEvent(new EndOfSimulationVO());

        // get the results
        SimulationResultVO resultVO = getSimulationResultVO(startTime);

        // destroy all service providers
        for (final Engine engine : strategyEngines) {
            engine.destroy();
        }
        this.serverEngine.destroy();

        // clear the second-level cache
        net.sf.ehcache.CacheManager.getInstance().clearAll();

        // close all reports
        ReportManager.closeAll();

        return resultVO;

    }

    private void initStrategies(final StrategyGroup strategyGroup) {
        //add or update strategy for each group item
        for (final String strategyName : strategyGroup.getStrategyNames()) {
            final Strategy strategy = this.strategyPersistenceService.getOrCreateStrategy(strategyName);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Update strategy '{}'", strategy.getName());
            }
        }
    }

    private void broadcastLocal(LifecyclePhase phase) {
        this.eventDispatcher.broadcast(new LifecycleEventVO(OperationMode.SIMULATION, phase, new Date()), EventRecipient.ALL_LOCAL);
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
        this.serverLookupService.initSecurityStrings();
        for (Security security : securities) {
            this.cacheManager.get(SecurityImpl.class, security.getId());
        }

        this.serverEngine.startCoordination();
    }

    private void feedGenericEvents(File baseDir) {

        File genericdata = new File(baseDir, "genericdata");
        File dataDir = new File(genericdata, this.commonConfig.getDataSet());
        if (dataDir == null || !dataDir.exists() || !dataDir.isDirectory()) {
            LOGGER.warn("no generic events available");
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

                CSVInputAdapter inputAdapter = new CSVInputAdapter(null, new GenericEventInputAdapterSpec(file, eventTypeName));
                inputAdapter.setCoercer(new CvsTypeCoercer());
                this.serverEngine.coordinate(inputAdapter);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("started feeding file {}", file.getName());
                }
            }
        }
    }

    private void feedCSV(Collection<Security> securities, File baseDir) {

        MarketDataType marketDataType = this.commonConfig.getDataSetType();
        File marketDataTypeDir = new File(baseDir, marketDataType.toString().toLowerCase() + "data");
        File dataDir = new File(marketDataTypeDir, this.commonConfig.getDataSet());

        if (this.commonConfig.isFeedAllMarketDataFiles()) {

            if (dataDir == null || !dataDir.exists() || !dataDir.isDirectory()) {
                LOGGER.warn("no market data events available");
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
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn("no data available for {} in {}", security.getSymbol(), dataDir);
                    }
                    continue;
                } else {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("data available for {}", security.getSymbol());
                    }
                }

                feedFile(file);
            }
        }
    }

    private void feedFile(File file) {

        CSVInputAdapter inputAdapter;
        MarketDataType marketDataType = this.commonConfig.getDataSetType();
        if (MarketDataType.TICK.equals(marketDataType)) {
            inputAdapter = new CsvTickInputAdapter(new CsvTickInputAdapterSpec(file));
        } else if (MarketDataType.BAR.equals(marketDataType)) {
            inputAdapter = new CsvBarInputAdapter(new CsvBarInputAdapterSpec(file, this.commonConfig.getBarSize()));
        } else {
            throw new SimulationExecutorException("incorrect parameter for dataSetType: " + marketDataType);
        }
        inputAdapter.setCoercer(new CvsTypeCoercer());

        this.serverEngine.coordinate(inputAdapter);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("started feeding file {}", file.getName());
        }
    }

    private void feedDB() {

        MarketDataType marketDataType = this.commonConfig.getDataSetType();
        int feedBatchSize = this.commonConfig.getFeedBatchSize();

        if (MarketDataType.TICK.equals(marketDataType)) {

            DBTickInputAdapter inputAdapter = new DBTickInputAdapter(feedBatchSize);
            this.serverEngine.coordinate(inputAdapter);
            LOGGER.debug("started feeding ticks from db");

        } else if (MarketDataType.BAR.equals(marketDataType)) {

            DBBarInputAdapter inputAdapter = new DBBarInputAdapter(feedBatchSize, this.commonConfig.getBarSize());
            this.serverEngine.coordinate(inputAdapter);
            LOGGER.debug("started feeding bars from db");

        } else {
            throw new SimulationExecutorException("incorrect parameter for dataSetType: " + marketDataType);
        }
    }

    private void rebalancePortfolio(final StrategyGroup strategyGroup) {

        double initialBalance = this.commonConfig.getSimulationInitialBalance().doubleValue();
        double totalAllocation = 0.0;
        Set<String> strategyNames = strategyGroup.getStrategyNames();
        for (String strategyName: strategyNames) {

            Strategy strategy = this.lookupService.getStrategyByName(strategyName);
            double weight = strategyGroup.getWeight(strategyName);
            totalAllocation += weight;
            BigDecimal amount = RoundUtil.getBigDecimal(initialBalance * weight, 2);

            Transaction transaction = Transaction.Factory.newInstance();
            transaction.setUuid(UUID.randomUUID().toString());
            transaction.setDateTime(this.engineManager.getCurrentEPTime());
            transaction.setQuantity(1);
            transaction.setPrice(amount);
            transaction.setCurrency(this.commonConfig.getPortfolioBaseCurrency());
            transaction.setType(TransactionType.REBALANCE);
            transaction.setStrategy(strategy);

            this.transactionService.recordTransaction(transaction);
        }

        // check allocations add up to 1.0
        if (MathUtils.round(totalAllocation, 2) != 1.0) {
            throw new IllegalStateException("the total of all allocations is: " + totalAllocation + " where it should be 1.0");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SimulationResultVO simulateWithCurrentParams(final StrategyGroup strategyGroup) {

        SimulationResultVO resultVO = runSimulation(strategyGroup);
        reportStatisticsToFile(resultVO);
        logMultiLineString(convertStatisticsToLongString(resultVO));
        return resultVO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SimulationResultVO simulateBySingleParam(final StrategyGroup strategyGroup, final String parameter, final String value) {

        Validate.notEmpty(parameter, "Parameter is empty");
        Validate.notEmpty(value, "Value is empty");

        System.setProperty(parameter, value);

        SimulationResultVO resultVO = runSimulation(strategyGroup);
        if (RESULT_LOGGER.isInfoEnabled()) {
            RESULT_LOGGER.info("optimize {}={} {}", parameter, value, convertStatisticsToShortString(resultVO));
        }
        return resultVO;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SimulationResultVO simulateByMultiParam(final StrategyGroup strategyGroup, final String[] parameters, final String[] values) {

        Validate.notNull(parameters, "Parameter is null");
        Validate.notNull(values, "Value is null");

        StringBuilder buffer = new StringBuilder();
        buffer.append("optimize ");
        for (int i = 0; i < parameters.length; i++) {
            buffer.append(parameters[i] + "=" + values[i] + " ");
            System.setProperty(parameters[i], values[i]);
        }

        SimulationResultVO resultVO = runSimulation(strategyGroup);
        buffer.append(convertStatisticsToShortString(resultVO));
        RESULT_LOGGER.info(buffer.toString());
        return resultVO;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void optimizeSingleParamLinear(final StrategyGroup strategyGroup, final String parameter, final double min, final double max, final double increment) {

        Validate.notEmpty(parameter, "Parameter is empty");

        for (double i = min; i <= max; i += increment) {

            System.setProperty(parameter, format.format(i));

            SimulationResultVO resultVO = runSimulation(strategyGroup);
            if (RESULT_LOGGER.isInfoEnabled()) {
                RESULT_LOGGER.info("{}={} {}", parameter, format.format(i), convertStatisticsToShortString(resultVO));
            }

        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void optimizeSingleParamByValues(final StrategyGroup strategyGroup, final String parameter, final double[] values) {

        Validate.notEmpty(parameter, "Parameter is empty");
        Validate.notNull(values, "Value is null");

        for (double value : values) {

            System.setProperty(parameter, format.format(value));

            SimulationResultVO resultVO = runSimulation(strategyGroup);
            if (RESULT_LOGGER.isInfoEnabled()) {
                RESULT_LOGGER.info("{}={} {}", parameter, format.format(value), convertStatisticsToShortString(resultVO));
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OptimizationResultVO optimizeSingleParam(final StrategyGroup strategyGroup, final String parameter, final double min, final double max, final double accuracy) {

        Validate.notEmpty(parameter, "Parameter is empty");

        try {
            UnivariateRealFunction function = new UnivariateFunction(this, strategyGroup, parameter);
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
            throw new SimulationExecutorException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void optimizeMultiParamLinear(final StrategyGroup strategyGroup, final String[] parameters, final double[] mins, final double[] maxs, final double[] increments) {

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

                            SimulationResultVO resultVO = runSimulation(strategyGroup);
                            if (RESULT_LOGGER.isInfoEnabled()) {
                                RESULT_LOGGER.info("{} {} {} {}", message0, message1, message2, convertStatisticsToShortString(resultVO));
                            }
                        }
                    } else {
                        SimulationResultVO resultVO = runSimulation(strategyGroup);
                        if (RESULT_LOGGER.isInfoEnabled()) {
                            RESULT_LOGGER.info("{} {} {}", message0, message1, convertStatisticsToShortString(resultVO));
                        }
                    }
                }
            } else {
                SimulationResultVO resultVO = runSimulation(strategyGroup);
                if (RESULT_LOGGER.isInfoEnabled()) {
                    RESULT_LOGGER.info("{} {}", message0, convertStatisticsToShortString(resultVO));
                }
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void optimizeMultiParam(final StrategyGroup strategyGroup, final String[] parameters, final double[] starts) {

        Validate.notNull(parameters, "Parameter is null");
        Validate.notNull(starts, "Starts is null");

        RealPointValuePair result;
        try {
            MultivariateRealFunction function = new MultivariateFunction(this, strategyGroup, parameters);
            MultivariateRealOptimizer optimizer = new MultiDirectional();
            optimizer.setConvergenceChecker(new SimpleScalarValueChecker(0.0, 0.01));
            result = optimizer.optimize(function, GoalType.MAXIMIZE, starts);
            if (RESULT_LOGGER.isInfoEnabled()) {
                for (int i = 0; i < result.getPoint().length; i++) {
                    RESULT_LOGGER.info("optimal value for {}={}", parameters[i], format.format(result.getPoint()[i]));
                }
                RESULT_LOGGER.info("functionValue: {} needed iterations: {})", format.format(result.getValue()), optimizer.getEvaluations());
            }
        } catch (MathException ex) {
            throw new SimulationExecutorException(ex);
        }
    }

    private SimulationResultVO getSimulationResultVO(final long startTime) {

        SimulationResultVO resultVO = new SimulationResultVO();
        resultVO.setMins(((double) (System.currentTimeMillis() - startTime)) / 60000);

        Engine engine = this.serverEngine;
        if (!engine.isDeployed("INSERT_INTO_PERFORMANCE_KEYS")) {
            resultVO.setAllTrades(new TradesVO(0, 0, 0, 0));
            return resultVO;
        }

        @SuppressWarnings("unchecked")
        List<PeriodPerformanceVO> monthlyPerformances = engine.getAllEvents("KEEP_MONTHLY_PERFORMANCE");
        if (monthlyPerformances.size() == 0) {
            resultVO.setAllTrades(new TradesVO(0, 0, 0, 0));
            return resultVO;
        }

        PerformanceKeysVO performanceKeys = (PerformanceKeysVO) engine.getLastEvent("INSERT_INTO_PERFORMANCE_KEYS");
        MaxDrawDownVO maxDrawDown = (MaxDrawDownVO) engine.getLastEvent("INSERT_INTO_MAX_DRAW_DOWN");
        TradesVO allTrades = (TradesVO) engine.getLastEvent("INSERT_INTO_ALL_TRADES");
        TradesVO winningTrades = (TradesVO) engine.getLastEvent("INSERT_INTO_WINNING_TRADES");
        TradesVO losingTrades = (TradesVO) engine.getLastEvent("INSERT_INTO_LOOSING_TRADES");

        // increase last monthlyPerformance date by one month
        PeriodPerformanceVO lastMonthlyPerformance = monthlyPerformances.get(monthlyPerformances.size() - 1);
        Date lastMonthlyPerformanceDate = DateUtils.addMonths(lastMonthlyPerformance.getDate(), 1);
        lastMonthlyPerformance.setDate(lastMonthlyPerformanceDate);

        // compile yearly performance
        List<PeriodPerformanceVO> yearlyPerformances = null;
        Date lastDate = null;
        if (monthlyPerformances.size() != 0) {
            yearlyPerformances = new ArrayList<PeriodPerformanceVO>();
            double currentPerformance = 1.0;
            for (PeriodPerformanceVO monthlyPerformance : monthlyPerformances) {
                if (lastDate != null && DateUtils.toCalendar(monthlyPerformance.getDate()).get(Calendar.YEAR) != DateUtils.toCalendar(lastDate).get(Calendar.YEAR)) {
                    PeriodPerformanceVO yearlyPerformance = new PeriodPerformanceVO();
                    yearlyPerformance.setDate(lastDate);
                    yearlyPerformance.setValue(currentPerformance - 1.0);
                    yearlyPerformances.add(yearlyPerformance);
                    currentPerformance = 1.0;
                }
                currentPerformance *= 1.0 + monthlyPerformance.getValue();
                lastDate = monthlyPerformance.getDate();
            }

            if (DateUtils.toCalendar(lastMonthlyPerformance.getDate()).get(Calendar.MONTH) != 11) {
                PeriodPerformanceVO yearlyPerformance = new PeriodPerformanceVO();
                yearlyPerformance.setDate(lastMonthlyPerformance.getDate());
                yearlyPerformance.setValue(currentPerformance - 1.0);
                yearlyPerformances.add(yearlyPerformance);
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
        resultVO.setLoosingTrades(losingTrades);

        // get potential strategy specific results
        Map<String, Object> strategyResults = new HashMap<>();
        for (SimulationResultsProducer resultsProducer : this.applicationContext.getBeansOfType(SimulationResultsProducer.class).values()) {
            strategyResults.putAll(resultsProducer.getSimulationResults());
        }
        resultVO.setStrategyResults(strategyResults);

        return resultVO;
    }

    private void reportStatisticsToFile(SimulationResultVO resultVO) {

        if (!this.commonConfig.isDisableReports()) {

            try {

                File reportLocation = this.commonConfig.getReportLocation();
                File reportFile = new File(reportLocation != null ? reportLocation : new File("."), "BackTestReport.csv");
                BackTestReport backTestReport = new BackTestReport(reportFile);

                backTestReport.write("dateTime", DateTimePatterns.LOCAL_DATE_TIME.format(LocalDateTime.now()));
                backTestReport.write("executionTime", resultVO.getMins());
                backTestReport.write("dataSet", this.commonConfig.getDataSet());

                if (resultVO.getAllTrades().getCount() == 0) {
                    backTestReport.write("allTradesCount", 0);
                    backTestReport.close();
                    return;
                }

                double netLiqValue = resultVO.getNetLiqValue();
                backTestReport.write("netLiqValue", twoDigitFormat.format(netLiqValue));

                // monthlyPerformances
                Collection<PeriodPerformanceVO> monthlyPerformances = resultVO.getMonthlyPerformances();
                double maxDrawDownM = 0d;
                double bestMonthlyPerformance = Double.NEGATIVE_INFINITY;
                int positiveMonths = 0;
                int negativeMonths = 0;
                if ((monthlyPerformances != null)) {
                    for (PeriodPerformanceVO monthlyPerformance : monthlyPerformances) {
                        maxDrawDownM = Math.min(maxDrawDownM, monthlyPerformance.getValue());
                        bestMonthlyPerformance = Math.max(bestMonthlyPerformance, monthlyPerformance.getValue());
                        if (monthlyPerformance.getValue() > 0) {
                            positiveMonths++;
                        } else {
                            negativeMonths++;
                        }
                    }
                }

                // yearlyPerformances
                int positiveYears = 0;
                int negativeYears = 0;
                Collection<PeriodPerformanceVO> yearlyPerformances = resultVO.getYearlyPerformances();
                if ((yearlyPerformances != null)) {
                    for (PeriodPerformanceVO yearlyPerformance : yearlyPerformances) {
                        if (yearlyPerformance.getValue() > 0) {
                            positiveYears++;
                        } else {
                            negativeYears++;
                        }
                    }
                }

                if ((monthlyPerformances != null)) {
                    backTestReport.write("posMonths", positiveMonths);
                    backTestReport.write("negMonths", negativeMonths);
                    if ((yearlyPerformances != null)) {
                        backTestReport.write("posYears", positiveYears);
                        backTestReport.write("negYears", negativeYears);
                    }
                }

                PerformanceKeysVO performanceKeys = resultVO.getPerformanceKeys();
                MaxDrawDownVO maxDrawDownVO = resultVO.getMaxDrawDown();
                if (performanceKeys != null && maxDrawDownVO != null) {
                    backTestReport.write("avgM", performanceKeys.getAvgM());
                    backTestReport.write("stdM", performanceKeys.getStdM());
                    backTestReport.write("avgY", performanceKeys.getAvgY());
                    backTestReport.write("stdY", performanceKeys.getStdY());
                    backTestReport.write("sharpeRatio", performanceKeys.getSharpeRatio());

                    backTestReport.write("maxMonthlyDrawDown", -maxDrawDownM);
                    backTestReport.write("bestMonthlyPerformance", bestMonthlyPerformance);
                    backTestReport.write("maxDrawDown", maxDrawDownVO.getAmount());
                    backTestReport.write("maxDrawDownPeriod", maxDrawDownVO.getPeriod() / 86400000);
                    backTestReport.write("colmarRatio", performanceKeys.getAvgY() / maxDrawDownVO.getAmount());
                }

                reportTrades(backTestReport, "winningTrades", resultVO.getWinningTrades(), resultVO.getAllTrades().getCount());
                reportTrades(backTestReport, "losingTrades", resultVO.getLoosingTrades(), resultVO.getAllTrades().getCount());
                reportTrades(backTestReport, "allTrades", resultVO.getAllTrades(), resultVO.getAllTrades().getCount());

                backTestReport.write("returns");
                if ((monthlyPerformances != null)) {
                    for (PeriodPerformanceVO monthlyPerformance : monthlyPerformances) {
                        backTestReport.write(DateTimePatterns.LOCAL_DATE.format(DateTimeLegacy.toLocalDate(monthlyPerformance.getDate())), monthlyPerformance.getValue());
                    }
                }

                backTestReport.close();

                // make sure BackTestReport.xlsx exists
                File excelReportFile = new File(reportLocation != null ? reportLocation : new File("."), "BackTestReport.xlsm");
                if (!excelReportFile.exists()) {
                    InputStream is = getClass().getResourceAsStream("/BackTestReport.xlsm");
                    FileUtils.copyInputStreamToFile(is, excelReportFile);
                }

                if (this.commonConfig.isOpenBackTestReport()) {
                    if (Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().open(excelReportFile);
                        } catch (IOException e) {
                            // no application registered to .xlsm files
                            RESULT_LOGGER.info("BackTestReport available at: " + excelReportFile);
                        }
                    } else {
                        RESULT_LOGGER.info("BackTestReport available at: " + excelReportFile);
                    }
                }

            } catch (IOException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
    }

    private void reportTrades(BackTestReport backTestReport, String type, TradesVO tradesVO, long totalTrades) throws IOException {

        backTestReport.write(type + "Count", tradesVO.getCount());
        if (tradesVO.getCount() != totalTrades) {
            backTestReport.write(type + "Pct", (double) tradesVO.getCount() / totalTrades);
        }
        backTestReport.write(type + "TotalProfit", tradesVO.getTotalProfit());
        backTestReport.write(type + "AvgProfit", tradesVO.getAvgProfit());
        backTestReport.write(type + "AvgProfitPct", tradesVO.getAvgProfitPct());
    }

    private String convertStatisticsToShortString(SimulationResultVO resultVO) {

        StringBuilder buffer = new StringBuilder();
        try {
            this.resultFormatter.formatShort(buffer, resultVO);
            return buffer.toString();
        } catch (IOException ex) {
            throw new SimulationExecutorException(ex);
        }
    }

    private String convertStatisticsToLongString(SimulationResultVO resultVO) {

        StringBuilder buffer = new StringBuilder();
        try {
            this.resultFormatter.formatLong(buffer, resultVO, this.commonConfig);
            return buffer.toString();
        } catch (IOException ex) {
            throw new SimulationExecutorException(ex);
        }
    }

    private void logMultiLineString(String input) {

        if (RESULT_LOGGER.isInfoEnabled()) {
            String[] lines = input.split("\r\n");
            for (String line : lines) {
                RESULT_LOGGER.info(line);
            }
        }
    }

    private class UnivariateFunction implements UnivariateRealFunction {

        private final SimulationExecutorImpl simulationExecutor;
        private final StrategyGroup strategyGroup;
        private final String param;

        public UnivariateFunction(final SimulationExecutorImpl simulationExecutor, final StrategyGroup strategyGroup, final String parameter) {
            super();
            this.simulationExecutor = simulationExecutor;
            this.strategyGroup = strategyGroup;
            this.param = parameter;
        }

        @Override
        public double value(double input) throws FunctionEvaluationException {

            System.setProperty(this.param, String.valueOf(input));

            SimulationResultVO resultVO = this.simulationExecutor.runSimulation(this.strategyGroup);
            double result = resultVO.getPerformanceKeys().getSharpeRatio();

            if (RESULT_LOGGER.isInfoEnabled()) {
                RESULT_LOGGER.info("optimize on {}={} {}", this.param, SimulationExecutorImpl.format.format(input), SimulationExecutorImpl.this.convertStatisticsToShortString(resultVO));
            }
            return result;
        }
    }

    private class MultivariateFunction implements MultivariateRealFunction {

        private final SimulationExecutorImpl simulationExecutor;
        private final StrategyGroup strategyGroup;
        private final String[] params;

        public MultivariateFunction(final SimulationExecutorImpl simulationExecutor, final StrategyGroup strategyGroup, final String[] parameters) {
            super();
            this.simulationExecutor = simulationExecutor;
            this.strategyGroup = strategyGroup;
            this.params = parameters;
        }

        @Override
        public double value(double[] input) throws FunctionEvaluationException {

            StringBuilder buffer = new StringBuilder("optimize on ");
            for (int i = 0; i < input.length; i++) {

                String param = this.params[i];
                double value = input[i];

                System.setProperty(param, String.valueOf(value));

                buffer.append(param + "=" + SimulationExecutorImpl.format.format(value) + " ");
            }

            SimulationResultVO resultVO = this.simulationExecutor.runSimulation(this.strategyGroup);
            double result = resultVO.getPerformanceKeys().getSharpeRatio();

            if (RESULT_LOGGER.isInfoEnabled()) {
                RESULT_LOGGER.info("{}{}", buffer.toString(), SimulationExecutorImpl.this.convertStatisticsToShortString(resultVO));
            }
            return result;
        }
    }
}