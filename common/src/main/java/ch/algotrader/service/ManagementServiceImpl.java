/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
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
import java.util.TreeMap;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.springframework.beans.factory.annotation.Value;

import ch.algotrader.ServiceLocator;
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
import ch.algotrader.enumeration.MarketDataType;
import ch.algotrader.enumeration.Side;
import ch.algotrader.esper.EsperManager;
import ch.algotrader.util.BeanUtil;
import ch.algotrader.vo.BalanceVO;
import ch.algotrader.vo.BarVO;
import ch.algotrader.vo.MarketDataEventVO;
import ch.algotrader.vo.OrderStatusVO;
import ch.algotrader.vo.PositionVO;
import ch.algotrader.vo.TickVO;
import ch.algotrader.vo.TransactionVO;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class ManagementServiceImpl extends ManagementServiceBase {

    private static final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss");

    private @Value("${misc.displayClosedPositions}") boolean displayClosedPositions;

    @Override
    protected String handleGetCurrentTime() throws Exception {

        return format.format(new Date(EsperManager.getCurrentTime(getConfiguration().getStartedStrategyName())));
    }

    @Override
    protected String handleGetStrategyName() throws Exception {

        return getConfiguration().getStartedStrategyName();
    }

    @Override
    protected BigDecimal handleGetStrategyCashBalance() throws Exception {

        String strategyName = getConfiguration().getStartedStrategyName();
        if (strategyName.equals(StrategyImpl.BASE)) {
            return getPortfolioService().getCashBalance();
        } else {
            return getPortfolioService().getCashBalance(strategyName);
        }
    }

    @Override
    protected BigDecimal handleGetStrategySecuritiesCurrentValue() throws Exception {

        String strategyName = getConfiguration().getStartedStrategyName();
        if (strategyName.equals(StrategyImpl.BASE)) {
            return getPortfolioService().getSecuritiesCurrentValue();
        } else {
            return getPortfolioService().getSecuritiesCurrentValue(strategyName);
        }
    }

    @Override
    protected BigDecimal handleGetStrategyMaintenanceMargin() throws Exception {

        String strategyName = getConfiguration().getStartedStrategyName();
        if (strategyName.equals(StrategyImpl.BASE)) {
            return getPortfolioService().getMaintenanceMargin();
        } else {
            return getPortfolioService().getMaintenanceMargin(strategyName);
        }
    }

    @Override
    protected BigDecimal handleGetStrategyNetLiqValue() throws Exception {

        String strategyName = getConfiguration().getStartedStrategyName();
        if (strategyName.equals(StrategyImpl.BASE)) {
            return getPortfolioService().getNetLiqValue();
        } else {
            return getPortfolioService().getNetLiqValue(strategyName);
        }
    }

    @Override
    protected BigDecimal handleGetStrategyAvailableFunds() throws Exception {

        String strategyName = getConfiguration().getStartedStrategyName();
        if (strategyName.equals(StrategyImpl.BASE)) {
            return getPortfolioService().getAvailableFunds();
        } else {
            return getPortfolioService().getAvailableFunds(strategyName);
        }
    }

    @Override
    protected double handleGetStrategyAllocation() throws Exception {

        String strategyName = getConfiguration().getStartedStrategyName();
        return getLookupService().getStrategyByName(strategyName).getAllocation();
    }

    @Override
    protected double handleGetStrategyLeverage() throws Exception {

        String strategyName = getConfiguration().getStartedStrategyName();
        if (strategyName.equals(StrategyImpl.BASE)) {
            return getPortfolioService().getLeverage();
        } else {
            return getPortfolioService().getLeverage(strategyName);
        }
    }

    @Override
    protected double handleGetStrategyPerformance() throws Exception {

        String strategyName = getConfiguration().getStartedStrategyName();
        if (strategyName.equals(StrategyImpl.BASE)) {
            return getPortfolioService().getPerformance();
        } else {
            return getPortfolioService().getPerformance(strategyName);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<MarketDataEventVO> handleGetMarketDataEvents() {

        String strategyName = getConfiguration().getStartedStrategyName();
        List<MarketDataEvent> marketDataEvents = EsperManager.getAllEventsProperty(strategyName, "CURRENT_MARKET_DATA_EVENT", "marketDataEvent");

        List<MarketDataEventVO> marketDataEventVOs = getMarketDataEventVOs(marketDataEvents);

        // get all subscribed securities

        if (getConfiguration().isStartedStrategyBASE()) {

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
                Map<String, Property> properties = subscription.getPropsInitialized();
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

        return getLookupService().getPositionsVO(getConfiguration().getStartedStrategyName(), this.displayClosedPositions);
    }

    @Override
    protected Collection<BalanceVO> handleGetDataBalances() throws Exception {

        String strategyName = getConfiguration().getStartedStrategyName();
        if (strategyName.equals(StrategyImpl.BASE)) {
            return getPortfolioService().getBalances();
        } else {
            return getPortfolioService().getBalances(strategyName);
        }
    }

    @Override
    protected List<TransactionVO> handleGetDataTransactions() throws Exception {

        return getLookupService().getTransactionsVO(getConfiguration().getStartedStrategyName());
    }

    @Override
    protected Collection<OrderStatusVO> handleGetDataOrders() throws Exception {

        return getLookupService().getOpenOrdersVO(getConfiguration().getStartedStrategyName());
    }

    @Override
    protected Map<Object, Object> handleGetProperties() throws Exception {

        return new TreeMap<Object, Object>(getConfiguration().getProperties());
    }

    @Override
    protected void handleDeployStatement(String moduleName, String statementName) throws Exception {

        EsperManager.deployStatement(getConfiguration().getStartedStrategyName(), moduleName, statementName);
    }

    @Override
    protected void handleDeployModule(String moduleName) throws Exception {

        EsperManager.deployModule(getConfiguration().getStartedStrategyName(), moduleName);
    }

    @Override
    protected void handleSendOrder(int securityId, long quantity, String sideString, String type, String accountName, String propertiesString) throws Exception {

        Side side = Side.fromValue(sideString);
        String strategyName = getConfiguration().getStartedStrategyName();

        Strategy strategy = getLookupService().getStrategyByName(strategyName);
        Security security = getLookupService().getSecurity(securityId);

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
    protected void handleReduceCombination(int combinationId, double ratio) throws Exception {

        getCombinationService().reduceCombination(combinationId, getConfiguration().getStartedStrategyName(), ratio);
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

        EsperManager.setVariableValueFromString(getConfiguration().getStartedStrategyName(), variableName, value);
    }

    @Override
    protected void handleSubscribe(int securityid) throws Exception {

        getSubscriptionService().subscribeMarketDataEvent(getConfiguration().getStartedStrategyName(), securityid);
    }

    @Override
    protected void handleUnsubscribe(int securityid) throws Exception {

        getSubscriptionService().unsubscribeMarketDataEvent(getConfiguration().getStartedStrategyName(), securityid);
    }

    @Override
    protected void handleRequestCurrentTicks() throws Exception {

        getMarketDataService().requestCurrentTicks(getConfiguration().getStartedStrategyName());
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
    protected void handleCheckIsAlive() throws Exception {

        getLookupService().getCurrentDBTime();
    }

    @Override
    protected void handleShutdown() throws Exception {

        // cancel all orders if we called from base
        if (getConfiguration().isStartedStrategyBASE()) {
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
            if (MarketDataType.TICK.equals(getConfiguration().getDataSetType())) {
                marketDataEventVO = new TickVO();
            } else if (MarketDataType.BAR.equals(getConfiguration().getDataSetType())) {
                marketDataEventVO = new BarVO();
            } else {
                throw new IllegalStateException("unknown dataSetType " + getConfiguration().getDataSetType());
            }
        }

        // set db data
        marketDataEventVO.setSecurityId(security.getId());
        marketDataEventVO.setName(security.toString());
        return marketDataEventVO;
    }
}
