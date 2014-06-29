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

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.lang.math.NumberUtils;

import ch.algotrader.ServiceLocator;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.ConfigProvider;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.entity.marketData.MarketDataEvent;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.property.Property;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.SlicingOrder;
import ch.algotrader.entity.trade.StopLimitOrder;
import ch.algotrader.entity.trade.StopOrder;
import ch.algotrader.entity.trade.TickwiseIncrementalOrder;
import ch.algotrader.entity.trade.VariableIncrementalOrder;
import ch.algotrader.enumeration.CombinationType;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.enumeration.MarketDataType;
import ch.algotrader.enumeration.Side;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.util.BeanUtil;
import ch.algotrader.vo.BalanceVO;
import ch.algotrader.vo.BarVO;
import ch.algotrader.vo.MarketDataEventVO;
import ch.algotrader.vo.OrderStatusVO;
import ch.algotrader.vo.PositionVO;
import ch.algotrader.vo.TickVO;
import ch.algotrader.vo.TransactionVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class ManagementServiceImpl extends ManagementServiceBase {

    @Override
    protected Date handleGetCurrentTime() throws Exception {

        String strategyName = getCommonConfig().getStrategyName();
        return new Date(EngineLocator.instance().getEngine(strategyName).getCurrentTime());
    }

    @Override
    protected String handleGetStrategyName() throws Exception {

        return getCommonConfig().getStrategyName();
    }

    @Override
    protected BigDecimal handleGetStrategyCashBalance() throws Exception {

        String strategyName = getCommonConfig().getStrategyName();
        if (strategyName.equals(StrategyImpl.BASE)) {
            return getPortfolioService().getCashBalance();
        } else {
            return getPortfolioService().getCashBalance(strategyName);
        }
    }

    @Override
    protected BigDecimal handleGetStrategySecuritiesCurrentValue() throws Exception {

        String strategyName = getCommonConfig().getStrategyName();
        if (strategyName.equals(StrategyImpl.BASE)) {
            return getPortfolioService().getSecuritiesCurrentValue();
        } else {
            return getPortfolioService().getSecuritiesCurrentValue(strategyName);
        }
    }

    @Override
    protected BigDecimal handleGetStrategyMaintenanceMargin() throws Exception {

        String strategyName = getCommonConfig().getStrategyName();
        if (strategyName.equals(StrategyImpl.BASE)) {
            return getPortfolioService().getMaintenanceMargin();
        } else {
            return getPortfolioService().getMaintenanceMargin(strategyName);
        }
    }

    @Override
    protected BigDecimal handleGetStrategyNetLiqValue() throws Exception {

        String strategyName = getCommonConfig().getStrategyName();
        if (strategyName.equals(StrategyImpl.BASE)) {
            return getPortfolioService().getNetLiqValue();
        } else {
            return getPortfolioService().getNetLiqValue(strategyName);
        }
    }

    @Override
    protected BigDecimal handleGetStrategyAvailableFunds() throws Exception {

        String strategyName = getCommonConfig().getStrategyName();
        if (strategyName.equals(StrategyImpl.BASE)) {
            return getPortfolioService().getAvailableFunds();
        } else {
            return getPortfolioService().getAvailableFunds(strategyName);
        }
    }

    @Override
    protected double handleGetStrategyAllocation() throws Exception {

        String strategyName = getCommonConfig().getStrategyName();
        return getLookupService().getStrategyByName(strategyName).getAllocation();
    }

    @Override
    protected double handleGetStrategyLeverage() throws Exception {

        String strategyName = getCommonConfig().getStrategyName();
        if (strategyName.equals(StrategyImpl.BASE)) {
            return getPortfolioService().getLeverage();
        } else {
            return getPortfolioService().getLeverage(strategyName);
        }
    }

    @Override
    protected double handleGetStrategyPerformance() throws Exception {

        String strategyName = getCommonConfig().getStrategyName();
        if (strategyName.equals(StrategyImpl.BASE)) {
            return getPortfolioService().getPerformance();
        } else {
            return getPortfolioService().getPerformance(strategyName);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<MarketDataEventVO> handleGetMarketDataEvents() {

        String strategyName = getCommonConfig().getStrategyName();
        List<MarketDataEvent> marketDataEvents = EngineLocator.instance().getEngine(strategyName).getAllEventsProperty("CURRENT_MARKET_DATA_EVENT", "marketDataEvent");

        List<MarketDataEventVO> marketDataEventVOs = getMarketDataEventVOs(marketDataEvents);

        // get all subscribed securities
        List<MarketDataEventVO> processedMarketDataEventVOs = new ArrayList<MarketDataEventVO>();
        if (strategyName.equalsIgnoreCase(StrategyImpl.BASE)) {

            // for base iterate over a distinct list of subscribed securities and feedType
            List<Map<String, Object>> subscriptions = getLookupService().getSubscribedSecuritiesAndFeedTypeForAutoActivateStrategiesInclComponents();
            for (Map<String, Object> subscription : subscriptions) {

                Security security = (Security) subscription.get("security");
                FeedType feedType = (FeedType) subscription.get("feedType");

                // try to get the processedMarketDataEvent
                MarketDataEventVO marketDataEventVO = getMarketDataEventVO(marketDataEventVOs, security, feedType);

                processedMarketDataEventVOs.add(marketDataEventVO);
            }

        } else {

            // for strategies iterate over all subscriptions
            List<Subscription> subscriptions = getLookupService().getSubscriptionsByStrategyInclComponents(strategyName);
            for (Subscription subscription : subscriptions) {

                Security security = subscription.getSecurity();
                FeedType feedType = subscription.getFeedType();

                MarketDataEventVO marketDataEventVO = getMarketDataEventVO(marketDataEventVOs, security, feedType);

                // add properties from this strategies subscription
                Map<String, Property> properties = subscription.getPropsInitialized();
                if (!properties.isEmpty()) {
                    marketDataEventVO.setProperties(properties);
                }

                processedMarketDataEventVOs.add(marketDataEventVO);
            }
        }
        return processedMarketDataEventVOs;

    }

    @Override
    protected List<PositionVO> handleGetDataPositions() throws Exception {

        return getLookupService().getPositionsVO(getCommonConfig().getStrategyName(), getCommonConfig().isDisplayClosedPositions());
    }

    @Override
    protected Collection<BalanceVO> handleGetDataBalances() throws Exception {

        String strategyName = getCommonConfig().getStrategyName();
        if (StrategyImpl.BASE.equals(strategyName)) {
            return getPortfolioService().getBalances();
        } else {
            return getPortfolioService().getBalances(strategyName);
        }
    }

    @Override
    protected List<TransactionVO> handleGetDataTransactions() throws Exception {

        return getLookupService().getTransactionsVO(getCommonConfig().getStrategyName());
    }

    @Override
    protected Collection<OrderStatusVO> handleGetDataOrders() throws Exception {

        return getLookupService().getOpenOrdersVOByStrategy(getCommonConfig().getStrategyName());
    }

    @Override
    protected Map<Object, Object> handleGetProperties() throws Exception {

        ConfigProvider configProvider = getConfigParams().getConfigProvider();
        Set<String> names = configProvider.getNames();
        Map<Object, Object> props = new HashMap<Object, Object>();
        for (String name: names) {

            props.put(name, configProvider.getParameter(name, String.class));
        }
        return props;
    }

    @Override
    protected void handleDeployStatement(String moduleName, String statementName) throws Exception {

        EngineLocator.instance().getEngine(getCommonConfig().getStrategyName()).deployStatement(moduleName, statementName);
    }

    @Override
    protected void handleDeployModule(String moduleName) throws Exception {

        EngineLocator.instance().getEngine(getCommonConfig().getStrategyName()).deployModule(moduleName);
    }

    @Override
    protected void handleSendOrder(String securityString, long quantity, String sideString, String type, String accountName, String propertiesString) throws Exception {

        Side side = Side.fromValue(sideString);
        String strategyName = getCommonConfig().getStrategyName();

        Strategy strategy = getLookupService().getStrategyByName(strategyName);
        Security security = getLookupService().getSecurity(getSecurityId(securityString));

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
            order = getLookupService().getOrderByName(type);
        }

        // set common values
        order.setStrategy(strategy);
        order.setSecurity(security);
        order.setQuantity(Math.abs(quantity));
        order.setSide(side);

        // set the account (if defined)
        if (!"".equals(accountName)) {
            Account account = getLookupService().getAccountByName(accountName);
            order.setAccount(account);
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
    protected void handleCancelOrder(String intId) throws Exception {

        getOrderService().cancelOrder(intId);
    }

    @Override
    protected void handleModifyOrder(String intId, String propertiesString) throws Exception {

        // get the properties
        Map<String, String> properties = new HashMap<String, String>();
        for (String nameValue : propertiesString.split(",")) {
            properties.put(nameValue.split("=")[0], nameValue.split("=")[1]);
        }

        getOrderService().modifyOrder(intId, properties);
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
    protected void handleReduceCombination(String combination, double ratio) throws Exception {

        getCombinationService().reduceCombination(getSecurityId(combination), getCommonConfig().getStrategyName(), ratio);
    }

    @Override
    protected void handleSetExitValue(int positionId, double exitValue) throws Exception {

        getPositionService().setExitValue(positionId, new BigDecimal(exitValue), true);
    }

    @Override
    protected void handleRemoveExitValue(int positionId) throws Exception {

        getPositionService().removeExitValue(positionId);
    }

    @Override
    protected void handleSetVariableValue(String variableName, String value) {

        EngineLocator.instance().getEngine(getCommonConfig().getStrategyName()).setVariableValueFromString(variableName, value);
    }

    @Override
    protected void handleSubscribe(String securityString, String feedTypeString) throws Exception {

        String startedStrategyName = getCommonConfig().getStrategyName();
        if (!"".equals(feedTypeString)) {
            getSubscriptionService().subscribeMarketDataEvent(startedStrategyName, getSecurityId(securityString), FeedType.fromString(feedTypeString));
        } else {
            getSubscriptionService().subscribeMarketDataEvent(startedStrategyName, getSecurityId(securityString));
        }
    }

    @Override
    protected void handleUnsubscribe(String securityString, String feedTypeString) throws Exception {

        String startedStrategyName = getCommonConfig().getStrategyName();
        if (!"".equals(feedTypeString)) {
            getSubscriptionService().unsubscribeMarketDataEvent(startedStrategyName, getSecurityId(securityString), FeedType.fromString(feedTypeString));
        } else {
            getSubscriptionService().unsubscribeMarketDataEvent(startedStrategyName, getSecurityId(securityString));
        }
    }

    @Override
    protected void handleRequestCurrentTicks() throws Exception {

        getMarketDataService().requestCurrentTicks(getCommonConfig().getStrategyName());
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
    protected int handleCreateCombination(String combinationType, int securityFamilyId, String underlying) throws Exception {

        if ("".equals(underlying)) {
            return getCombinationService().createCombination(CombinationType.fromString(combinationType), securityFamilyId).getId();
        } else {
            return getCombinationService().createCombination(CombinationType.fromString(combinationType), securityFamilyId, getSecurityId(underlying)).getId();
        }
    }

    @Override
    protected void handleSetComponentQuantity(int combinationId, String component, long quantity) throws Exception {

        getCombinationService().setComponentQuantity(combinationId, getSecurityId(component), quantity);
    }

    @Override
    protected void handleRemoveComponent(int combinationId, String component) {

        getCombinationService().removeComponent(combinationId, getSecurityId(component));
    }

    @Override
    protected void handleDeleteCombination(int combinationId) throws Exception {

        getCombinationService().deleteCombination(combinationId);
    }

    @Override
    protected void handleCheckIsAlive() throws Exception {

        getLookupService().getCurrentDBTime();
    }

    @Override
    protected void handleShutdown() throws Exception {

        // cancel all orders if we called from base
        String startedStrategyName = getCommonConfig().getStrategyName();
        if (StrategyImpl.BASE.equals(startedStrategyName)) {
            ServiceLocator.instance().getOrderService().cancelAllOrders();
        }

        ServiceLocator.instance().shutdown();

        // need to force exit because grafefull shutdown of esper-service (and esper-jmx) does not work
        System.exit(0);
    }

    private int getSecurityId(String securityString) {

        if (NumberUtils.isDigits(securityString)) {
            return Integer.parseInt(securityString);

        } else {

            Security security;
            if (securityString.startsWith("isin:")) {
                security = getLookupService().getSecurityByIsin(securityString.substring(5));
            } else if (securityString.startsWith("bbgid:")) {
                security = getLookupService().getSecurityByBbgid(securityString.substring(6));
            } else if (securityString.startsWith("ric:")) {
                security = getLookupService().getSecurityByRic(securityString.substring(4));
            } else if (securityString.startsWith("conid:")) {
                security = getLookupService().getSecurityByConid(securityString.substring(6));
            } else {
                security = getLookupService().getSecurityBySymbol(securityString);
            }

            if (security == null) {
                throw new IllegalArgumentException("security was not found " + securityString);
            }

            return security.getId();
        }
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
                tickVO.setVol(tick.getVol());

                marketDataEventVO = tickVO;

            } else if (marketDataEvent instanceof Bar) {

                Bar bar = (Bar) marketDataEvent;
                BarVO barVO = new BarVO();
                barVO.setOpen(bar.getOpen());
                barVO.setHigh(bar.getHigh());
                barVO.setLow(bar.getLow());
                barVO.setClose(bar.getClose());
                barVO.setVol(bar.getVol());

                marketDataEventVO = barVO;

            } else {
                throw new IllegalArgumentException("unknown marketDataEvent type");
            }

            marketDataEventVO.setDateTime(marketDataEvent.getDateTime());
            marketDataEventVO.setSecurityId(marketDataEvent.getSecurity().getId());
            marketDataEventVO.setCurrentValue(marketDataEvent.getCurrentValue());
            marketDataEventVO.setFeedType(marketDataEvent.getFeedType());

            marketDataEventVOs.add(marketDataEventVO);
        }

        return marketDataEventVOs;
    }

    private MarketDataEventVO getMarketDataEventVO(List<MarketDataEventVO> marketDataEventVOs, final Security security, final FeedType feedType) {

        // get the marketDataEventVO matching the securityId
        MarketDataEventVO marketDataEventVO = CollectionUtils.find(marketDataEventVOs, new Predicate<MarketDataEventVO>() {
            @Override
            public boolean evaluate(MarketDataEventVO marketDataEventVO) {
                return marketDataEventVO.getSecurityId() == security.getId() && marketDataEventVO.getFeedType().equals(feedType);
            }
        });

        // create an empty MarketDataEventVO if non exists
        if (marketDataEventVO == null) {
            CommonConfig commonConfig = getCommonConfig();
            if (MarketDataType.TICK.equals(commonConfig.getDataSetType())) {
                marketDataEventVO = new TickVO();
            } else if (MarketDataType.BAR.equals(commonConfig.getDataSetType())) {
                marketDataEventVO = new BarVO();
            } else {
                throw new IllegalStateException("unknown dataSetType " + commonConfig.getDataSetType());
            }
        }

        // set db data
        marketDataEventVO.setSecurityId(security.getId());
        marketDataEventVO.setName(security.toString());
        return marketDataEventVO;
    }
}
