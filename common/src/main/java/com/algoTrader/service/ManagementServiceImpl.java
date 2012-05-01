package com.algoTrader.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Property;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.Subscription;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.security.Security;
import com.algoTrader.esper.EsperManager;
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

        return format.format(new Date(EsperManager.getCurrentTime(StrategyUtil.getStartedStrategyName())));
    }

    @Override
    protected String handleGetStrategyName() throws Exception {

        return StrategyUtil.getStartedStrategyName();
    }

    @Override
    protected BigDecimal handleGetStrategyCashBalance() throws Exception {

        String strategyName = StrategyUtil.getStartedStrategyName();
        if (strategyName.equals(StrategyImpl.BASE)) {
            return getPortfolioService().getCashBalance();
        } else {
            return getPortfolioService().getCashBalance(StrategyUtil.getStartedStrategyName());
        }
    }

    @Override
    protected BigDecimal handleGetStrategySecuritiesCurrentValue() throws Exception {

        String strategyName = StrategyUtil.getStartedStrategyName();
        if (strategyName.equals(StrategyImpl.BASE)) {
            return getPortfolioService().getSecuritiesCurrentValue();
        } else {
            return getPortfolioService().getSecuritiesCurrentValue(strategyName);
        }
    }

    @Override
    protected BigDecimal handleGetStrategyMaintenanceMargin() throws Exception {

        String strategyName = StrategyUtil.getStartedStrategyName();
        if (strategyName.equals(StrategyImpl.BASE)) {
            return getPortfolioService().getMaintenanceMargin();
        } else {
            return getPortfolioService().getMaintenanceMargin(strategyName);
        }
    }

    @Override
    protected BigDecimal handleGetStrategyNetLiqValue() throws Exception {

        String strategyName = StrategyUtil.getStartedStrategyName();
        if (strategyName.equals(StrategyImpl.BASE)) {
            return getPortfolioService().getNetLiqValue();
        } else {
            return getPortfolioService().getNetLiqValue(strategyName);
        }
    }

    @Override
    protected BigDecimal handleGetStrategyAvailableFunds() throws Exception {

        String strategyName = StrategyUtil.getStartedStrategyName();
        if (strategyName.equals(StrategyImpl.BASE)) {
            return getPortfolioService().getAvailableFunds();
        } else {
            return getPortfolioService().getAvailableFunds(strategyName);
        }
    }

    @Override
    protected double handleGetStrategyAllocation() throws Exception {

        return StrategyUtil.getStartedStrategy().getAllocation();
    }

    @Override
    protected double handleGetStrategyLeverage() throws Exception {

        String strategyName = StrategyUtil.getStartedStrategyName();
        if (strategyName.equals(StrategyImpl.BASE)) {
            return getPortfolioService().getLeverage();
        } else {
            return getPortfolioService().getLeverage(strategyName);
        }
    }

    @Override
    protected BigDecimal handleGetStrategyBenchmark() throws Exception {

        return StrategyUtil.getStartedStrategy().getBenchmark();
    }

    @Override
    protected double handleGetStrategyPerformance() throws Exception {

        String strategyName = StrategyUtil.getStartedStrategyName();
        if (strategyName.equals(StrategyImpl.BASE)) {
            return getPortfolioService().getPerformance();
        } else {
            return getPortfolioService().getPerformance(strategyName);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<TickVO> handleGetDataTicks() {

        String strategyName = StrategyUtil.getStartedStrategyName();
        List<Tick> ticks = EsperManager.getAllEventsProperty(strategyName, "GET_LAST_TICK", "tick");

        List<TickVO> tickVOs = getTickVOs(ticks);

        // get all subscribed securities
        List<TickVO> processedTickVOs = new ArrayList<TickVO>();

        if (StrategyUtil.isStartedStrategyBASE()) {

            // for base iterate over all subscribed securities
            // TODO eliminate LazyLoading with new Finder: Subscription.findForAutoActivateStrategiesInclProperties
            Collection<Security> securities = getLookupService().getSubscribedSecuritiesForAutoActivateStrategiesInclFamily();
            for (Security security : securities) {

                TickVO tickVO = getTickVO(tickVOs, security);

                processedTickVOs.add(tickVO);

                // add properties from all subscriptions
                Map<String, Property> properties = new HashMap<String, Property>();
                for (Subscription subscription : security.getSubscriptionsInitialized()) {
                    properties.putAll(subscription.getPropertiesInitialized());
                }
                if (!properties.isEmpty()) {
                    tickVO.setProperties(properties);
                }
            }
        } else {

            // for strategies iterate over all subscriptions
            // TODO eliminate LazyLoading with new Finder: Subscription.findByStrategyInclProperties
            for (Subscription subscription : getLookupService().getSubscriptionsByStrategy(strategyName)) {

                TickVO tickVO = getTickVO(tickVOs, subscription.getSecurity());

                // add properties from this strategies subscription
                Map<String, Property> properties = subscription.getPropertiesInitialized();
                if (!properties.isEmpty()) {
                    tickVO.setProperties(properties);
                }

                processedTickVOs.add(tickVO);
            }
        }

        return processedTickVOs;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<PositionVO> handleGetDataPositions() throws Exception {

        return getLookupService().getOpenPositionsVO(StrategyUtil.getStartedStrategyName());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<BalanceVO> handleGetDataBalances() throws Exception {

        String strategyName = StrategyUtil.getStartedStrategyName();
        if (strategyName.equals(StrategyImpl.BASE)) {
            return getPortfolioService().getBalances();
        } else {
            return new ArrayList<BalanceVO>();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<TransactionVO> handleGetDataTransactions() throws Exception {

        return getLookupService().getTransactionsVO(StrategyUtil.getStartedStrategyName());
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
    protected List<Object> handleGetAllEvents(String statementName) throws Exception {

        String strategyName = StrategyUtil.getStartedStrategyName();
        return EsperManager.getAllEvents(strategyName, statementName);
    }

    @Override
    protected void handleDeployStatement(String moduleName, String statementName) throws Exception {

        EsperManager.deployStatement(StrategyUtil.getStartedStrategyName(), moduleName, statementName);
    }

    @Override
    protected void handleDeployModule(String moduleName) throws Exception {

        EsperManager.deployModule(StrategyUtil.getStartedStrategyName(), moduleName);
    }

    @Override
    protected void handleSetVariableValue(String variableName, String value) {

        EsperManager.setVariableValue(StrategyUtil.getStartedStrategyName(), variableName, value);
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
        tickVO.setName(security.toString());
        return tickVO;
    }
}
