package com.algoTrader.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.combination.Combination;
import com.algoTrader.entity.combination.CombinationTick;
import com.algoTrader.entity.combination.CombinationTickDaoImpl;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.marketData.TickDaoImpl;
import com.algoTrader.entity.security.Security;
import com.algoTrader.util.StrategyUtil;
import com.algoTrader.vo.BalanceVO;
import com.algoTrader.vo.CombinationTickVO;
import com.algoTrader.vo.DiagramVO;
import com.algoTrader.vo.PositionVO;
import com.algoTrader.vo.TickVO;
import com.algoTrader.vo.TransactionVO;

public class ManagementServiceImpl extends ManagementServiceBase {

    private static final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss");

    private List<DiagramVO> diagrams;

    public List<DiagramVO> getDiagrams() {
        return this.diagrams;
    }

    public void setDiagrams(List<DiagramVO> diagrams) {
        this.diagrams = diagrams;
    }

    @Override
    protected String handleGetCurrentTime() throws Exception {

        return format.format(new Date(getRuleService().getCurrentTime(StrategyUtil.getStartedStrategyName())));
    }

    @Override
    protected String handleGetStrategyName() throws Exception {

        return StrategyUtil.getStartedStrategyName();
    }

    @Override
    protected BigDecimal handleGetStrategyCashBalance() throws Exception {

        return getReportingService().getStrategyCashBalance(StrategyUtil.getStartedStrategyName());
    }

    @Override
    protected BigDecimal handleGetStrategySecuritiesCurrentValue() throws Exception {

        return getReportingService().getStrategySecuritiesCurrentValue(StrategyUtil.getStartedStrategyName());
    }

    @Override
    protected BigDecimal handleGetStrategyMaintenanceMargin() throws Exception {

        return getReportingService().getStrategyMaintenanceMargin(StrategyUtil.getStartedStrategyName());
    }

    @Override
    protected BigDecimal handleGetStrategyNetLiqValue() throws Exception {

        return getReportingService().getStrategyNetLiqValue(StrategyUtil.getStartedStrategyName());
    }

    @Override
    protected BigDecimal handleGetStrategyAvailableFunds() throws Exception {

        return getReportingService().getStrategyAvailableFunds(StrategyUtil.getStartedStrategyName());
    }

    @Override
    protected double handleGetStrategyAllocation() throws Exception {

        return getReportingService().getStrategyAllocation(StrategyUtil.getStartedStrategyName());
    }

    @Override
    protected double handleGetStrategyLeverage() throws Exception {

        return getReportingService().getStrategyLeverage(StrategyUtil.getStartedStrategyName());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<TickVO> handleGetDataTicks() {

        String strategyName = StrategyUtil.getStartedStrategyName();
        List<Tick> ticks = getRuleService().getAllEventsProperty(strategyName, "GET_LAST_TICK", "tick");

        // get a list of all securities in ticks
        Collection<Security> tickSecurities = CollectionUtils.collect(ticks, new Transformer<Tick, Security>() {
            @Override
            public Security transform(Tick tick) {
                return tick.getSecurity();
            }});

        // get all securities on the watchlist
        final List<Security> securitiesOnWatchList;
        if (StrategyUtil.isStartedStrategyBASE()) {
            securitiesOnWatchList = getLookupService().getSecuritiesOnWatchlist();
        } else {
            securitiesOnWatchList = getLookupService().getSecuritiesOnWatchlist(strategyName);
        }

        // filter out ticks from securities that are not on the watchList (anymore)
        CollectionUtils.filter(ticks, new Predicate<Tick>() {
            @Override
            public boolean evaluate(Tick tick) {
                return securitiesOnWatchList.contains(tick.getSecurity());
            }
        });

        // we don't have access to the "real" TickDao in client services, but since we just use the conversion methods
        // we just instanciate a new Dao
        List<TickVO> tickVOs = (List<TickVO>) (new TickDaoImpl()).toTickVOCollection(ticks);

        // create "empty" TickVOs for all securites not contained in ticks
        for (Security security : CollectionUtils.subtract(securitiesOnWatchList, tickSecurities)) {
            TickVO tickVO = new TickVO();
            tickVO.setSecurityId(security.getId());
            tickVO.setSymbol(security.getSymbol());

            tickVOs.add(tickVO);
        }

        // sort by id
        Collections.sort(tickVOs, new Comparator<TickVO>() {
            @Override
            public int compare(TickVO tick1, TickVO tick2) {
                return tick1.getSecurityId() - tick2.getSecurityId();
            }
        });

        return tickVOs;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<CombinationTickVO> handleGetDataCombinationTicks() throws Exception {

        String strategyName = StrategyUtil.getStartedStrategyName();
        List<CombinationTick> combinationTicks = getRuleService().getAllEventsProperty(strategyName, "GET_LAST_COMBINATION_TICK", "tick");

        // get a list of all combinations in combinationTicks
        Collection<Combination> tickCombinations = CollectionUtils.collect(combinationTicks, new Transformer<CombinationTick, Combination>() {
            @Override
            public Combination transform(CombinationTick combinationTick) {
                return combinationTick.getCombination();
            }
        });

        final Collection<Combination> actualCombinations;
        if (StrategyUtil.isStartedStrategyBASE()) {
            actualCombinations = new ArrayList<Combination>();
        } else {
            actualCombinations = getLookupService().getCombinationsByStrategy(strategyName);
        }

        // filter out combinationTicks from combinations that do not exist anymore
        CollectionUtils.filter(combinationTicks, new Predicate<CombinationTick>() {
            @Override
            public boolean evaluate(CombinationTick copmbinationTick) {
                return actualCombinations.contains(copmbinationTick.getCombination());
            }
        });

        // we don't have access to the "real" CombinationTickDao in client services, but since we just use the conversion methods
        // we just instanciate a new Dao
        List<CombinationTickVO> combinationTickVOs = (List<CombinationTickVO>) (new CombinationTickDaoImpl()).toCombinationTickVOCollection(combinationTicks);

        // create "empty" TickVOs for all securites not contained in ticks
        for (Combination combination : CollectionUtils.subtract(actualCombinations, tickCombinations)) {
            CombinationTickVO combinationTickVO = new CombinationTickVO();
            combinationTickVO.setId(combination.getId());
            combinationTickVO.setType(combination.getType());
            if (combination.getMaster() != null) {
                combinationTickVO.setMaster(combination.getMaster().getSymbol());
                combinationTickVO.setMasterQuantity(combination.getMasterQuantity());
            }
            combinationTickVOs.add(combinationTickVO);
        }

        // sort by id
        Collections.sort(combinationTickVOs, new Comparator<CombinationTickVO>() {
            @Override
            public int compare(CombinationTickVO tick1, CombinationTickVO tick2) {
                return tick1.getId() - tick2.getId();
            }
        });

        return combinationTickVOs;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<PositionVO> handleGetDataOpenPositions() throws Exception {

        return getReportingService().getDataOpenPositions(StrategyUtil.getStartedStrategyName());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<BalanceVO> handleGetDataBalances() throws Exception {

        return getReportingService().getDataBalances(StrategyUtil.getStartedStrategyName());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<TransactionVO> handleGetDataTransactions() throws Exception {

        return getReportingService().getDataTransactions(StrategyUtil.getStartedStrategyName());
    }

    @Override
    protected List<DiagramVO> handleGetIndicatorDiagrams(boolean param) throws Exception {

        return getDiagrams();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Object> handleGetAllEvents(String ruleName) throws Exception {

        String strategyName = StrategyUtil.getStartedStrategyName();
        return getRuleService().getAllEvents(strategyName, ruleName);
    }

    @Override
    protected void handleActivate(String moduleName, String ruleName) throws Exception {

        getRuleService().deployRule(StrategyUtil.getStartedStrategyName(), moduleName, ruleName);
    }

    @Override
    protected void handleDeactivate(String ruleName) throws Exception {

        getRuleService().undeployRule(StrategyUtil.getStartedStrategyName(), ruleName);
    }

    @Override
    protected void handleSetVariableValue(String variableName, String value) {

        getRuleService().setVariableValue(StrategyUtil.getStartedStrategyName(), variableName, value);
    }

    @Override
    protected void handleShutdown() throws Exception {

        // cancel all orders if we called from base
        if (StrategyUtil.isStartedStrategyBASE()) {
            ServiceLocator.commonInstance().getOrderService().cancelAllOrders();
        }

        ServiceLocator.commonInstance().shutdown();

        // need to force exit because grafefull shutdown of esper-service (and esper-jmx) does not work
        System.exit(0);
    }

    @Override
    protected void handlePutOnWatchlist(int securityid) throws Exception {

        getWatchListService().putOnWatchlist(StrategyUtil.getStartedStrategyName(), securityid);
    }

    @Override
    protected void handleRemoveFromWatchlist(int securityid) throws Exception {

        getWatchListService().removeFromWatchlist(StrategyUtil.getStartedStrategyName(), securityid);
    }
}
