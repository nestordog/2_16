package com.algoTrader.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Subscription;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.security.Security;
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
    protected BigDecimal handleGetStrategyBenchmark() throws Exception {

        return getReportingService().getStrategyBenchmark(StrategyUtil.getStartedStrategyName());
    }

    @Override
    protected double handleGetStrategyPerformance() throws Exception {

        return getReportingService().getStrategyPerformance(StrategyUtil.getStartedStrategyName());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<TickVO> handleGetDataTicks() {

        String strategyName = StrategyUtil.getStartedStrategyName();
        List<Tick> ticks = getRuleService().getAllEventsProperty(strategyName, "GET_LAST_TICK", "tick");

        List<TickVO> tickVOs = getTickVOs(ticks);

        // get all subscribed securities
        List<TickVO> processedTickVOs = new ArrayList<TickVO>();

        // for base iterate over all subscribed securities (no alert values will be displayed)
        // for strategies iterate over all subscriptions
        if (StrategyUtil.isStartedStrategyBASE()) {
            Collection<Security> securities = getLookupService().getSubscribedSecuritiesForAutoActivateStrategiesInclFamily();
            for (Security security : securities) {

                TickVO tickVO = getTickVO(tickVOs, security);

                processedTickVOs.add(tickVO);
            }
        } else {
            for (Subscription subscription : getLookupService().getSubscriptionsByStrategy(strategyName)) {

                TickVO tickVO = getTickVO(tickVOs, subscription.getSecurity());

                processedTickVOs.add(tickVO);
            }
        }

        return processedTickVOs;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<PositionVO> handleGetDataPositions() throws Exception {

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
    protected Map<Object, Object> handleGetProperties() throws Exception {

        return new TreeMap<Object, Object>(getConfiguration().getProperties());
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
    protected void handleDeployStatement(String moduleName, String statementName) throws Exception {

        getRuleService().deployRule(StrategyUtil.getStartedStrategyName(), moduleName, statementName);
    }

    @Override
    protected void handleDeployModule(String moduleName) throws Exception {

        getRuleService().deployModule(StrategyUtil.getStartedStrategyName(), moduleName);
    }

    @Override
    protected void handleSetVariableValue(String variableName, String value) {

        getRuleService().setVariableValue(StrategyUtil.getStartedStrategyName(), variableName, value);
    }

    @Override
    protected void handleShutdown() throws Exception {

        // cancel all orders if we called from base
        if (StrategyUtil.isStartedStrategyBASE()) {
            ServiceLocator.instance().getOrderService().cancelAllOrders();
        }

        ServiceLocator.instance().shutdown();

        // need to force exit because grafefull shutdown of esper-service (and esper-jmx) does not work
        System.exit(0);
    }

    @Override
    protected void handleSubscribe(int securityid) throws Exception {

        getSubscriptionService().subscribe(StrategyUtil.getStartedStrategyName(), securityid);
    }

    @Override
    protected void handleUnsubscribe(int securityid) throws Exception {

        getSubscriptionService().unsubscribe(StrategyUtil.getStartedStrategyName(), securityid);
    }

    private List<TickVO> getTickVOs(List<Tick> ticks) {

        // create TickVOs based on the ticks (have to do this manually since we have no access to the Dao)
        List<TickVO> tickVOs = new ArrayList<TickVO>();
        for (Tick tick : ticks) {

            TickVO tickVO = new TickVO();
            tickVO.setDateTime(tick.getDateTime());
            tickVO.setLast(tick.getLast());
            tickVO.setLastDateTime(tick.getLastDateTime());
            tickVO.setVol(tick.getVol());
            tickVO.setVolBid(tick.getVolBid());
            tickVO.setVolAsk(tick.getVolAsk());
            tickVO.setBid(tick.getBid());
            tickVO.setAsk(tick.getAsk());
            tickVO.setOpenIntrest(tick.getOpenIntrest());
            tickVO.setSettlement(tick.getSettlement());
            tickVO.setSecurityId(tick.getSecurity().getId());
            tickVO.setCurrentValue(tick.getCurrentValue());

            tickVOs.add(tickVO);
        }
        return tickVOs;
    }

    private TickVO getTickVO(List<TickVO> tickVOs, final Security security) {

        // get the tickVO matching the securityId
        TickVO tickVO = CollectionUtils.find(tickVOs, new Predicate<TickVO>() {
            @Override
            public boolean evaluate(TickVO TickVO) {
                return TickVO.getSecurityId() == security.getId();
            }
        });

        // create an empty TickVO if non exists
        if (tickVO == null) {
            tickVO = new TickVO();
        }

        // set db data
        tickVO.setSecurityId(security.getId());
        tickVO.setSymbol(security.getSymbol());
        return tickVO;
    }
}
