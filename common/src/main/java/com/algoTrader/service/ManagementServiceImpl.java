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
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.Subscription;
import com.algoTrader.entity.marketData.Bar;
import com.algoTrader.entity.marketData.MarketDataEvent;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.strategy.OrderPreference;
import com.algoTrader.entity.trade.LimitOrder;
import com.algoTrader.entity.trade.MarketOrder;
import com.algoTrader.entity.trade.Order;
import com.algoTrader.entity.trade.SlicingOrder;
import com.algoTrader.entity.trade.StopLimitOrder;
import com.algoTrader.entity.trade.StopOrder;
import com.algoTrader.entity.trade.TickwiseIncrementalOrder;
import com.algoTrader.entity.trade.VariableIncrementalOrder;
import com.algoTrader.enumeration.MarketChannel;
import com.algoTrader.enumeration.Side;
import com.algoTrader.esper.EsperManager;
import com.algoTrader.util.BeanUtil;
import com.algoTrader.util.StrategyUtil;
import com.algoTrader.vo.BalanceVO;
import com.algoTrader.vo.BarVO;
import com.algoTrader.vo.MarketDataEventVO;
import com.algoTrader.vo.OrderStatusVO;
import com.algoTrader.vo.PositionVO;
import com.algoTrader.vo.TickVO;
import com.algoTrader.vo.TransactionVO;

public class ManagementServiceImpl extends ManagementServiceBase {

    private static final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss");

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

        String strategyName = StrategyUtil.getStartedStrategyName();
        return getLookupService().getStrategyByName(strategyName).getAllocation();
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
    protected List<MarketDataEventVO> handleGetMarketDataEvents() {

        String strategyName = StrategyUtil.getStartedStrategyName();
        List<MarketDataEvent> marketDataEvents = EsperManager.getAllEventsProperty(strategyName, "CURRENT_MARKET_DATA_EVENT", "marketDataEvent");

        List<MarketDataEventVO> marketDataEventVOs = getMarketDataEventVOs(marketDataEvents);

        // get all subscribed securities

        if (StrategyUtil.isStartedStrategyBASE()) {

            // for base iterate over all subscribed securities
            Map<Integer, MarketDataEventVO> processedMarketDataEventVOs = new TreeMap<Integer, MarketDataEventVO>();
            for (Subscription subscription : getLookupService().getSubscriptionsForAutoActivateStrategiesInclComponents()) {

                Security security = subscription.getSecurity();

                // try to get the processedMarketDataEvent
                MarketDataEventVO marketDataEventVO = processedMarketDataEventVOs.get(security.getId());
                if (marketDataEventVO == null) {
                    marketDataEventVO = getMarketDataEventVO(marketDataEventVOs, security);
                    processedMarketDataEventVOs.put(security.getId(), marketDataEventVO);
                }
            }
            return new ArrayList<MarketDataEventVO>(processedMarketDataEventVOs.values());

        } else {

            // for strategies iterate over all subscriptions
            List<MarketDataEventVO> processedMarketDataEventVOs = new ArrayList<MarketDataEventVO>();
            for (Subscription subscription : getLookupService().getSubscriptionsByStrategyInclComponents(strategyName)) {

                MarketDataEventVO marketDataEventVO = getMarketDataEventVO(marketDataEventVOs, subscription.getSecurity());

                // add properties from this strategies subscription
                Map<String, Property> properties = subscription.getPropertiesInitialized();
                if (!properties.isEmpty()) {
                    marketDataEventVO.setProperties(properties);
                }

                processedMarketDataEventVOs.add(marketDataEventVO);
            }
            return processedMarketDataEventVOs;
        }

    }

    @Override
    protected List<PositionVO> handleGetDataPositions() throws Exception {

        return getLookupService().getOpenPositionsVO(StrategyUtil.getStartedStrategyName());
    }

    @Override
    protected Collection<BalanceVO> handleGetDataBalances() throws Exception {

        String strategyName = StrategyUtil.getStartedStrategyName();
        if (strategyName.equals(StrategyImpl.BASE)) {
            return getPortfolioService().getBalances();
        } else {
            return new ArrayList<BalanceVO>();
        }
    }

    @Override
    protected List<TransactionVO> handleGetDataTransactions() throws Exception {

        return getLookupService().getTransactionsVO(StrategyUtil.getStartedStrategyName());
    }

    @Override
    protected Collection<OrderStatusVO> handleGetDataOrders() throws Exception {

        return getLookupService().getOpenOrdersVO(StrategyUtil.getStartedStrategyName());
    }

    @Override
    protected Map<Object, Object> handleGetProperties() throws Exception {

        return new TreeMap<Object, Object>(getConfiguration().getProperties());
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
    protected void handleSendOrder(int securityId, long quantity, String sideString, String type, String marketChannelString, String propertiesString) throws Exception {

        Side side = Side.fromValue(sideString);
        String strategyName = StrategyUtil.getStartedStrategyName();

        Strategy strategy = getLookupService().getStrategyByName(strategyName);
        final Security security = getLookupService().getSecurity(securityId);

        // instantiate the order
        Order order;
        if ("M".equals(type)) {
            order = MarketOrder.Factory.newInstance();
        } else if ("L".equals(type)) {
            order = LimitOrder.Factory.newInstance();
        } else if ("S".equals(type)) {
            order = StopOrder.Factory.newInstance();
        } else if ("SL".equals(type)) {
            order = StopLimitOrder.Factory.newInstance();
        } else if ("TI".equals(type)) {
            order = TickwiseIncrementalOrder.Factory.newInstance();
        } else if ("VI".equals(type)) {
            order = VariableIncrementalOrder.Factory.newInstance();
        } else if ("SLI".equals(type)) {
            order = SlicingOrder.Factory.newInstance();
        } else {

            // create the order from an OrderPreference
            OrderPreference orderPreference = getLookupService().getOrderPreferenceByName(type);
            if (orderPreference != null) {
                order = orderPreference.createOrder();
            } else {
                throw new IllegalArgumentException("unknown OrderType or OrderPreference");
            }
        }

        // set common values
        order.setStrategy(strategy);
        order.setSecurity(security);
        order.setQuantity(Math.abs(quantity));
        order.setSide(side);

        // set the marketChannel (if defined)
        if (!"".equals(marketChannelString)) {
            order.setMarketChannel(MarketChannel.fromString(marketChannelString));
        }

        // set additional properties
        if (!"".equals(propertiesString)) {

            // get the properties
            Map<String, String> properties = new HashMap<String, String>();
            for (String nameValue : propertiesString.split(",")) {
                properties.put(nameValue.split("=")[0], nameValue.split("=")[1]);
            }

            // populte the properties
            BeanUtil.populate(order, properties);
        }

        // send orders
        getOrderService().sendOrder(order);
    }

    @Override
    protected void handleCancelOrder(int orderNumber) throws Exception {

        getOrderService().cancelOrder(orderNumber);
    }

    @Override
    protected void handleModifyOrder(int orderNumber, String propertiesString) throws Exception {

        // get the properties
        Map<String, String> properties = new HashMap<String, String>();
        for (String nameValue : propertiesString.split(",")) {
            properties.put(nameValue.split("=")[0], nameValue.split("=")[1]);
        }

        getOrderService().modifyOrder(orderNumber, properties);
    }

    @Override
    protected void handleClosePosition(int positionId) throws Exception {

        getPositionService().closePosition(positionId, false);
    }

    @Override
    protected void handleReducePosition(int positionId, int quantity) throws Exception {

        getPositionService().reducePosition(positionId, quantity);
    }

    @Override
    protected void handleReduceCombination(int combinationId, double ratio) throws Exception {

        getCombinationService().reduceCombination(combinationId, StrategyUtil.getStartedStrategyName(), ratio);
    }

    @Override
    protected void handleSetExitValue(int positionId, double exitValue) throws Exception {

        getPositionService().setExitValue(positionId, exitValue, true);
    }

    @Override
    protected void handleSetVariableValue(String variableName, String value) {

        EsperManager.setVariableValueFromString(StrategyUtil.getStartedStrategyName(), variableName, value);
    }

    @Override
    protected void handleSubscribe(int securityid) throws Exception {

        getSubscriptionService().subscribeMarketDataEvent(StrategyUtil.getStartedStrategyName(), securityid);
    }

    @Override
    protected void handleUnsubscribe(int securityid) throws Exception {

        getSubscriptionService().unsubscribeMarketDataEvent(StrategyUtil.getStartedStrategyName(), securityid);
    }

    @Override
    protected void handleAddProperty(int propertyHolderId, String name, String value, String type) throws Exception {

        Object obj;
        if ("INT".equals(type)) {
            obj = Integer.parseInt(value);
        } else if ("DOUBLE".equals(type)) {
            obj = Double.parseDouble(value);
        } else if ("MONEY".equals(type)) {
            obj = new BigDecimal(value);
        } else if ("TEXT".equals(type)) {
            obj = value;
        } else if ("DATE".equals(type)) {
            obj = (new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")).parse(value);
        } else if ("BOOLEAN".equals(type)) {
            obj = Boolean.parseBoolean(value);
        } else {
            throw new IllegalArgumentException("unknown type " + type);
        }

        getPropertyService().addProperty(propertyHolderId, name, obj, false);
    }

    @Override
    protected void handleRemoveProperty(int propertyHolderId, String name) throws Exception {

        getPropertyService().removeProperty(propertyHolderId, name);
    }

    @Override
    protected void handleSetComponentQuantity(int combinationId, int securityId, long quantity) throws Exception {

        getCombinationService().setComponentQuantity(combinationId, securityId, quantity);
    }

    @Override
    protected void handleRemoveComponent(int combinationId, final int securityId) {

        getCombinationService().removeComponent(combinationId, securityId);
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

    private List<MarketDataEventVO> getMarketDataEventVOs(List<MarketDataEvent> marketDataEvents) {

        // create MarketDataEventVOs based on the MarketDataEvents (have to do this manually since we have no access to the Dao)
        List<MarketDataEventVO> marketDataEventVOs = new ArrayList<MarketDataEventVO>();
        for (MarketDataEvent marketDataEvent : marketDataEvents) {

            MarketDataEventVO marketDataEventVO;
            if (marketDataEvent instanceof Tick) {

                Tick tick = (Tick) marketDataEvent;
                TickVO tickVO = new TickVO();
                tickVO.setLast(tick.getLast());
                tickVO.setLastDateTime(tick.getLastDateTime());
                tickVO.setVolBid(tick.getVolBid());
                tickVO.setVolAsk(tick.getVolAsk());
                tickVO.setBid(tick.getBid());
                tickVO.setAsk(tick.getAsk());

                marketDataEventVO = tickVO;

            } else if (marketDataEvent instanceof Bar) {

                Bar bar = (Bar) marketDataEvent;
                BarVO barVO = new BarVO();
                barVO.setOpen(bar.getOpen());
                barVO.setHigh(bar.getHigh());
                barVO.setLow(bar.getLow());
                barVO.setClose(bar.getClose());

                marketDataEventVO = barVO;

            } else {
                throw new IllegalArgumentException("unknown marketDataEvent type");
            }

            marketDataEventVO.setDateTime(marketDataEvent.getDateTime());
            marketDataEventVO.setVol(marketDataEvent.getVol());
            marketDataEventVO.setOpenIntrest(marketDataEvent.getOpenIntrest());
            marketDataEventVO.setSettlement(marketDataEvent.getSettlement());
            marketDataEventVO.setSecurityId(marketDataEvent.getSecurity().getId());
            marketDataEventVO.setCurrentValue(marketDataEvent.getCurrentValue());

            marketDataEventVOs.add(marketDataEventVO);
        }

        return marketDataEventVOs;
    }

    private MarketDataEventVO getMarketDataEventVO(List<MarketDataEventVO> marketDataEventVOs, final Security security) {

        // get the marketDataEventVO matching the securityId
        MarketDataEventVO marketDataEventVO = CollectionUtils.find(marketDataEventVOs, new Predicate<MarketDataEventVO>() {
            @Override
            public boolean evaluate(MarketDataEventVO MarketDataEventVO) {
                return MarketDataEventVO.getSecurityId() == security.getId();
            }
        });

        // create an empty MarketDataEventVO if non exists
        if (marketDataEventVO == null) {
            marketDataEventVO = new MarketDataEventVO();
        }

        // set db data
        marketDataEventVO.setSecurityId(security.getId());
        marketDataEventVO.setName(security.toString());
        return marketDataEventVO;
    }
}
