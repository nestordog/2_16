package com.algoTrader.service;

import java.io.File;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.algoTrader.entity.Property;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.Subscription;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.security.Component;
import com.algoTrader.entity.security.FutureDao;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.StockOptionDao;
import com.algoTrader.entity.strategy.CashBalance;
import com.algoTrader.entity.trade.Order;
import com.algoTrader.enumeration.MarketDataType;
import com.algoTrader.esper.io.CsvBarInputAdapterSpec;
import com.algoTrader.esper.io.CsvTickInputAdapterSpec;
import com.algoTrader.util.MyLogger;
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
    private @Value("${statement.simulateStockOptions}") boolean simulateStockOptions;
    private @Value("${statement.simulateFuturesByUnderlying}") boolean simulateFuturesByUnderlying;
    private @Value("${statement.simulateFuturesByGenericFutures}") boolean simulateFuturesByGenericFutures;

    public SimulationServiceImpl() {
        format.setMinimumFractionDigits(this.roundDigits);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void handleResetDB() throws Exception {

        // process all strategies
        Collection<Strategy> strategies = getStrategyDao().loadAll();
        for (Strategy strategy : strategies) {

            // delete all transactions except the initial CREDIT
            Collection<Transaction> transactions = strategy.getTransactions();
            Set<Transaction> toRemoveTransactions = new HashSet<Transaction>();
            Set<Transaction> toKeepTransactions = new HashSet<Transaction>();
            BigDecimal initialAmount = new BigDecimal(0);
            for (Transaction transaction : transactions) {
                if (transaction.getId() == 1) {
                    toKeepTransactions.add(transaction);
                    initialAmount = transaction.getPrice();
                } else {
                    toRemoveTransactions.add(transaction);
                }
            }
            getTransactionDao().remove(toRemoveTransactions);
            strategy.setTransactions(toKeepTransactions);

            // delete all cashBalances except the initial CREDIT
            Collection<CashBalance> cashBalances = strategy.getCashBalances();
            Set<CashBalance> toRemoveCashBalance = new HashSet<CashBalance>();
            Set<CashBalance> toKeepCashBalances = new HashSet<CashBalance>();
            for (CashBalance cashBalance : cashBalances) {
                if (cashBalance.getId() == 1) {
                    toKeepCashBalances.add(cashBalance);
                    cashBalance.setAmount(initialAmount);
                } else {
                    toRemoveCashBalance.add(cashBalance);
                }
            }
            getCashBalanceDao().remove(toRemoveCashBalance);
            strategy.setCashBalances(toKeepCashBalances);

            // delete all positions and references to them
            Collection<Position> positions = strategy.getPositions();
            getPositionDao().remove(positions);
            strategy.getPositions().removeAll(positions);
        }

        // delete all non-presistent subscriptions and references to them
        List<Subscription> nonPersistentSubscriptions = getSubscriptionDao().findNonPersistent();
        getSubscriptionDao().remove(nonPersistentSubscriptions);
        for (Subscription subscription : nonPersistentSubscriptions) {
            subscription.getSecurity().getSubscriptions().remove(subscription);
            subscription.getStrategy().getSubscriptions().remove(subscription);
        }

        // delete all non-persistent combinations
        getCombinationDao().remove(getCombinationDao().findNonPersistent());

        // delete all non-persistent components and references to them
        List<Component> nonPersistentComponents = getComponentDao().findNonPersistent();
        getComponentDao().remove(nonPersistentComponents);
        for (Component component : nonPersistentComponents) {
            component.getParentSecurity().getComponents().remove(component);
        }

        // delete all non-persistent properties
        List<Property> nonPersistentProperties = getPropertyDao().findNonPersistent();
        getPropertyDao().remove(nonPersistentProperties);
        for (Property property : nonPersistentProperties) {
            property.getPropertyHolder().getProperties().remove(property);
        }

        // delete all StockOptions if they are beeing simulated
        if (this.simulateStockOptions) {
            getSecurityDao().remove((Collection<Security>) getStockOptionDao().loadAll(StockOptionDao.TRANSFORM_NONE));
        }

        // delete all Futures if they are beeing simulated
        if (this.simulateFuturesByUnderlying || this.simulateFuturesByGenericFutures) {
            getSecurityDao().remove((Collection<Security>) getFutureDao().loadAll(FutureDao.TRANSFORM_NONE));
        }
    }

    @Override
    protected void handleInputCSV() {

        getEventService().initCoordination(StrategyImpl.BASE);

        List<Security> securities = getSecurityDao().findSubscribedForAutoActivateStrategiesInclFamily();
        for (Security security : securities) {

            if (security.getIsin() == null) {
                logger.warn("no data available for " + security.getSymbol());
                continue;
            }
            MarketDataType marketDataType = getConfiguration().getDataSetType();
            String dataSet = getConfiguration().getDataSet();

            File file = new File("results/" + marketDataType.toString().toLowerCase() + "data/" + dataSet + "/" + security.getIsin() + ".csv");

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
                spec = new CsvBarInputAdapterSpec(file);
            } else {
                throw new SimulationServiceException("incorrect parameter for dataSetType: " + marketDataType);
            }

            getEventService().coordinate(StrategyImpl.BASE, spec);

            logger.debug("started simulation for security " + security.getSymbol());
        }

        getEventService().startCoordination(StrategyImpl.BASE);
    }

    @Override
    protected SimulationResultVO handleRunByUnderlyings() {

        long startTime = System.currentTimeMillis();

        // must call resetDB through ServiceLocator in order to get a transaction
        ServiceLocator.instance().getService("simulationService", SimulationService.class).resetDB();
        ServiceLocator.instance().getService("accountService", AccountService.class).rebalancePortfolio();

        // init all activatable strategies
        List<Strategy> strategies = getStrategyDao().findAutoActivateStrategies();
        for (Strategy strategy : strategies) {
            getEventService().initServiceProvider(strategy.getName());
            getEventService().deployAllModules(strategy.getName());
        }

        // feed the ticks
        inputCSV();

        // close all open positions that might still exist
        for (Position position : getPositionDao().loadAll()) {
            getPositionService().closePosition(position.getId(), false);
        }

        // send the EndOfSimulation event
        getEventService().sendEvent(StrategyImpl.BASE, new EndOfSimulationVO());

        // get the results
        SimulationResultVO resultVO = getSimulationResultVO(startTime);

        // destroy all service providers
        for (Strategy strategy : strategies) {
            getEventService().destroyServiceProvider(strategy.getName());
        }

        // run a garbage collection
        System.gc();

        return resultVO;
    }

    @Override
    protected void handleRunByActualTransactions() {

        long startTime = System.currentTimeMillis();

        // get the existingTransactions before they are deleted
        Collection<Transaction> transactions = ServiceLocator.instance().getLookupService().getAllTrades();

        // create orders
        List<Order> orders = new ArrayList<Order>();
        for (Transaction transaction : transactions) {

            // TODO needs to be redone with the new Async Order
//            Order order = new OrderImpl();
//            order.setStrategy(transaction.getStrategy());
//            order.setRequestedQuantity(Math.abs(transaction.getQuantity()));
//            order.setTransactionType(transaction.getType());
//            order.setStatus(Status.PREARRANGED);
//            order.getTransactions().add(transaction);
//            order.setSecurity(transaction.getSecurity());
            //
            //            orders.add(order);
        }

        resetDB();

        getEventService().initServiceProvider(StrategyImpl.BASE);

        // activate the necessary rules
        getEventService().deployStatement(StrategyImpl.BASE, "base", "CREATE_PORTFOLIO_VALUE");
        getEventService().deployStatement(StrategyImpl.BASE, "base", "CREATE_MONTHLY_PERFORMANCE");
        getEventService().deployStatement(StrategyImpl.BASE, "base", "GET_LAST_TICK");
        getEventService().deployStatement(StrategyImpl.BASE, "base", "CREATE_PERFORMANCE_KEYS");
        getEventService().deployStatement(StrategyImpl.BASE, "base", "KEEP_MONTHLY_PERFORMANCE");
        getEventService().deployStatement(StrategyImpl.BASE, "base", "CREATE_DRAW_DOWN");
        getEventService().deployStatement(StrategyImpl.BASE, "base", "CREATE_MAX_DRAW_DOWN");
        getEventService().deployStatement(StrategyImpl.BASE, "base", "PROCESS_PREARRANGED_ORDERS");

        // initialize the coordination
        getEventService().initCoordination(StrategyImpl.BASE);

        getEventService().coordinateTicks(StrategyImpl.BASE, new Date(this.start));

        getEventService().coordinate(StrategyImpl.BASE, orders, "transactions[0].dateTime");

        getEventService().startCoordination(StrategyImpl.BASE);

        SimulationResultVO resultVO = getSimulationResultVO(startTime);
        logMultiLineString(convertStatisticsToLongString(resultVO));

        getEventService().destroyServiceProvider(StrategyImpl.BASE);
    }

    @Override
    protected void handleSimulateWithCurrentParams() throws Exception {

        SimulationResultVO resultVO = ServiceLocator.instance().getService("simulationService", SimulationService.class).runByUnderlyings();
        logMultiLineString(convertStatisticsToLongString(resultVO));
    }

    @Override
    protected void handleSimulateBySingleParam(String strategyName, String parameter, String value) throws Exception {

        getConfiguration().setProperty(strategyName, parameter, value);

        SimulationResultVO resultVO = ServiceLocator.instance().getService("simulationService", SimulationService.class).runByUnderlyings();
        resultLogger.info("optimize " + parameter + "=" + value + " " + convertStatisticsToShortString(resultVO));
    }

    @Override
    protected void handleSimulateByMultiParam(String strategyName, String[] parameters, String[] values) throws Exception {

        StringBuffer buffer = new StringBuffer();
        buffer.append("optimize ");
        for (int i = 0; i < parameters.length; i++) {
            buffer.append(parameters[i] + "=" + values[i] + " ");
            getConfiguration().setProperty(strategyName, parameters[i], values[i]);
            getConfiguration().setProperty(parameters[i], values[i]);
        }

        SimulationResultVO resultVO = ServiceLocator.instance().getService("simulationService", SimulationService.class).runByUnderlyings();
        buffer.append(convertStatisticsToShortString(resultVO));
        resultLogger.info(buffer.toString());
    }

    @Override
    protected void handleOptimizeSingleParamLinear(String strategyName, String parameter, double min, double max, double increment) throws Exception {

        double result = min;
        double functionValue = 0;
        for (double i = min; i <= max; i += increment) {

            getConfiguration().setProperty(strategyName, parameter, format.format(i));

            SimulationResultVO resultVO = ServiceLocator.instance().getService("simulationService", SimulationService.class).runByUnderlyings();
            resultLogger.info(parameter + "=" + format.format(i) + " " + convertStatisticsToShortString(resultVO));

            double value = resultVO.getPerformanceKeysVO().getSharpRatio();
            if (value > functionValue) {
                functionValue = value;
                result = i;
            }
        }
        resultLogger.info("optimal value of " + parameter + " is " + format.format(result) + " (functionValue: " + format.format(functionValue) + ")");
    }

    @Override
    @SuppressWarnings("deprecation")
    protected OptimizationResultVO handleOptimizeSingleParam(String strategyName, String parameter, double min, double max, double accuracy)
            throws ConvergenceException, FunctionEvaluationException {

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

    @Override
    protected void handleOptimizeMultiParamLinear(String strategyName, String parameters[], double[] mins, double[] maxs, double[] increments) throws Exception {

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

                            SimulationResultVO resultVO = ServiceLocator.instance().getService("simulationService", SimulationService.class).runByUnderlyings();
                            resultLogger.info(message0 + " " + message1 + " " + message2 + " " + convertStatisticsToShortString(resultVO));
                        }
                    } else {
                        SimulationResultVO resultVO = ServiceLocator.instance().getService("simulationService", SimulationService.class).runByUnderlyings();
                        resultLogger.info(message0 + " " + message1 + " " + convertStatisticsToShortString(resultVO));
                    }
                }
            } else {
                SimulationResultVO resultVO = ServiceLocator.instance().getService("simulationService", SimulationService.class).runByUnderlyings();
                resultLogger.info(message0 + " " + convertStatisticsToShortString(resultVO));
            }
        }
    }

    @Override
    protected void handleOptimizeMultiParam(String strategyName, String[] parameters, double[] starts) throws ConvergenceException, FunctionEvaluationException {

        MultivariateRealFunction function = new MultivariateFunction(strategyName, parameters);
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

        PerformanceKeysVO performanceKeys = (PerformanceKeysVO) getEventService().getLastEvent(StrategyImpl.BASE, "CREATE_PERFORMANCE_KEYS");
        List<PeriodPerformanceVO> monthlyPerformances = getEventService().getAllEvents(StrategyImpl.BASE, "KEEP_MONTHLY_PERFORMANCE");
        MaxDrawDownVO maxDrawDown = (MaxDrawDownVO) getEventService().getLastEvent(StrategyImpl.BASE, "CREATE_MAX_DRAW_DOWN");
        TradesVO allTrades = (TradesVO) getEventService().getLastEvent(StrategyImpl.BASE, "ALL_TRADES");
        TradesVO winningTrades = (TradesVO) getEventService().getLastEvent(StrategyImpl.BASE, "WINNING_TRADES");
        TradesVO loosingTrades = (TradesVO) getEventService().getLastEvent(StrategyImpl.BASE, "LOOSING_TRADES");

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
        resultVO.setNetLiqValue(getStrategyDao().getPortfolioNetLiqValueDouble());
        resultVO.setMonthlyPerformanceVOs(monthlyPerformances);
        resultVO.setYearlyPerformanceVOs(yearlyPerformances);
        resultVO.setPerformanceKeysVO(performanceKeys);
        resultVO.setMaxDrawDownVO(maxDrawDown);
        resultVO.setAllTrades(allTrades);
        resultVO.setWinningTrades(winningTrades);
        resultVO.setLoosingTrades(loosingTrades);

        return resultVO;
    }

    @SuppressWarnings("unchecked")
    private static String convertStatisticsToShortString(SimulationResultVO resultVO) {

        StringBuffer buffer = new StringBuffer();

        PerformanceKeysVO performanceKeys = resultVO.getPerformanceKeysVO();
        MaxDrawDownVO maxDrawDownVO = resultVO.getMaxDrawDownVO();

        if (resultVO.getAllTrades().getCount() == 0) {
            return ("no trades took place!");
        }

        List<PeriodPerformanceVO> PeriodPerformanceVOs = resultVO.getMonthlyPerformanceVOs();
        double maxDrawDownM = 0d;
        double bestMonthlyPerformance = Double.NEGATIVE_INFINITY;
        if ((PeriodPerformanceVOs != null)) {
            for (PeriodPerformanceVO PeriodPerformanceVO : PeriodPerformanceVOs) {
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
        List<PeriodPerformanceVO> monthlyPerformances = resultVO.getMonthlyPerformanceVOs();
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
        List<PeriodPerformanceVO> yearlyPerformances = resultVO.getYearlyPerformanceVOs();
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

        PerformanceKeysVO performanceKeys = resultVO.getPerformanceKeysVO();
        MaxDrawDownVO maxDrawDownVO = resultVO.getMaxDrawDownVO();
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
        private String strategyName;

        public UnivariateFunction(String strategyName, String parameter) {
            super();
            this.param = parameter;
            this.strategyName = strategyName;
        }

        @Override
        public double value(double input) throws FunctionEvaluationException {

            ServiceLocator.instance().getConfiguration().setProperty(this.strategyName, this.param, String.valueOf(input));

            SimulationResultVO resultVO = ServiceLocator.instance().getService("simulationService", SimulationService.class).runByUnderlyings();
            double result = resultVO.getPerformanceKeysVO().getSharpRatio();

            resultLogger.info("optimize on " + this.param + "=" + SimulationServiceImpl.format.format(input) + " "
                    + SimulationServiceImpl.convertStatisticsToShortString(resultVO));

            return result;
        }
    }

    private static class MultivariateFunction implements MultivariateRealFunction {

        private String[] params;
        private String strategyName;

        public MultivariateFunction(String strategyName, String[] parameters) {
            super();
            this.params = parameters;
            this.strategyName = strategyName;
        }

        @Override
        public double value(double[] input) throws FunctionEvaluationException {

            StringBuffer buffer = new StringBuffer("optimize on ");
            for (int i = 0; i < input.length; i++) {

                String param = this.params[i];
                double value = input[i];

                ServiceLocator.instance().getConfiguration().setProperty(this.strategyName, param, String.valueOf(value));

                buffer.append(param + "=" + SimulationServiceImpl.format.format(value) + " ");
            }

            SimulationResultVO resultVO = ServiceLocator.instance().getService("simulationService", SimulationService.class).runByUnderlyings();
            double result = resultVO.getPerformanceKeysVO().getSharpRatio();

            resultLogger.info(buffer.toString() + SimulationServiceImpl.convertStatisticsToShortString(resultVO));

            return result;
        }
    }
}
