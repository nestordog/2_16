package com.algoTrader.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.marketData.TickDaoImpl;
import com.algoTrader.util.StrategyUtil;
import com.algoTrader.vo.BalanceVO;
import com.algoTrader.vo.DiagramVO;
import com.algoTrader.vo.PositionVO;
import com.algoTrader.vo.TickVO;
import com.algoTrader.vo.TransactionVO;

public class ManagementServiceImpl extends ManagementServiceBase {

    private static final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss");

    protected String handleGetCurrentTime() throws Exception {

        return format.format(new Date(getRuleService().getCurrentTime(StrategyUtil.getStartedStrategyName())));
    }

    protected String handleGetStrategyName() throws Exception {

        return StrategyUtil.getStartedStrategyName();
    }

    protected BigDecimal handleGetStrategyCashBalance() throws Exception {

        return getReportingService().getStrategyCashBalance(StrategyUtil.getStartedStrategyName());
    }

    protected BigDecimal handleGetStrategySecuritiesCurrentValue() throws Exception {

        return getReportingService().getStrategySecuritiesCurrentValue(StrategyUtil.getStartedStrategyName());
    }

    protected BigDecimal handleGetStrategyMaintenanceMargin() throws Exception {

        return getReportingService().getStrategyMaintenanceMargin(StrategyUtil.getStartedStrategyName());
    }

    protected BigDecimal handleGetStrategyNetLiqValue() throws Exception {

        return getReportingService().getStrategyNetLiqValue(StrategyUtil.getStartedStrategyName());
    }

    protected BigDecimal handleGetStrategyAvailableFunds() throws Exception {

        return getReportingService().getStrategyAvailableFunds(StrategyUtil.getStartedStrategyName());
    }

    protected double handleGetStrategyAllocation() throws Exception {

        return getReportingService().getStrategyAllocation(StrategyUtil.getStartedStrategyName());
    }

    protected double handleGetStrategyLeverage() throws Exception {

        return getReportingService().getStrategyLeverage(StrategyUtil.getStartedStrategyName());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected List<TickVO> handleGetDataLastTicks() {

        List ticks = getRuleService().getAllEventsProperty(StrategyUtil.getStartedStrategyName(), "GET_LAST_TICK", "tick");

        // we don't have access to the "real" TickDao in client services, but since we just use the conversion methods
        // we just instanciate a new Dao
        (new TickDaoImpl()).toTickVOCollection(ticks);
        return ticks;
    }

    @SuppressWarnings("unchecked")
    protected List<PositionVO> handleGetDataOpenPositions() throws Exception {

        return getReportingService().getDataOpenPositions(StrategyUtil.getStartedStrategyName());
    }

    @SuppressWarnings("unchecked")
    protected List<BalanceVO> handleGetDataBalances() throws Exception {

        return getReportingService().getDataBalances(StrategyUtil.getStartedStrategyName());
    }

    @SuppressWarnings("unchecked")
    protected List<TransactionVO> handleGetDataTransactions() throws Exception {

        return getReportingService().getDataTransactions(StrategyUtil.getStartedStrategyName());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<DiagramVO> handleGetIndicatorDiagrams(boolean param) throws Exception {

        return super.getDiagrams();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Object> handleGetAllEvents(String ruleName) throws Exception {

        String strategyName = StrategyUtil.getStartedStrategyName();
        return getRuleService().getAllEvents(strategyName, ruleName);
    }

    protected void handleActivate(String moduleName, String ruleName) throws Exception {

        getRuleService().deployRule(StrategyUtil.getStartedStrategyName(), moduleName, ruleName);
    }

    protected void handleDeactivate(String ruleName) throws Exception {

        getRuleService().undeployRule(StrategyUtil.getStartedStrategyName(), ruleName);
    }

    protected void handleRegisterStrategy() throws Exception {

        String strategyName = StrategyUtil.getStartedStrategyName();

        if (!StrategyImpl.BASE.equals(strategyName) && !getStrategyService().isStrategyRegistered(strategyName)) {
            getStrategyService().registerStrategy(strategyName);
        }
    }

    protected void handleShutdown() throws Exception {

        ServiceLocator.commonInstance().shutdown();

        // need to force exit because grafefull shutdown of esper-service (and esper-jmx) does not work
        System.exit(0);
    }
}
