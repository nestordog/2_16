package com.algoTrader.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.marketData.TickDaoImpl;
import com.algoTrader.util.StrategyUtil;
import com.algoTrader.vo.BalanceVO;
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

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected List<TickVO> handleGetDataLastTicks() {

        String strategyName = StrategyUtil.getStartedStrategyName();
        List ticks = getRuleService().getAllEventsProperty(strategyName, "GET_LAST_TICK", "tick");

        // we don't have access to the "real" TickDao in client services, but since we just use the conversion methods
        // we just instanciate a new Dao
        (new TickDaoImpl()).toTickVOCollection(ticks);
        return ticks;
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
    protected void handleShutdown() throws Exception {

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
