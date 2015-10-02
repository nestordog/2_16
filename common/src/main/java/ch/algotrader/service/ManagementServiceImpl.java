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

import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.ConfigParams;
import ch.algotrader.config.ConfigProvider;
import ch.algotrader.dao.NamedParam;
import ch.algotrader.dao.PositionVOProducer;
import ch.algotrader.dao.TransactionVOProducer;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.entity.trade.ExecutionStatusVO;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderDetailsVO;
import ch.algotrader.entity.trade.OrderProperty;
import ch.algotrader.entity.trade.SlicingOrder;
import ch.algotrader.entity.trade.StopLimitOrder;
import ch.algotrader.entity.trade.StopOrder;
import ch.algotrader.entity.trade.TickwiseIncrementalOrder;
import ch.algotrader.entity.trade.VariableIncrementalOrder;
import ch.algotrader.enumeration.CombinationType;
import ch.algotrader.enumeration.MarketDataType;
import ch.algotrader.enumeration.OrderPropertyType;
import ch.algotrader.enumeration.QueryType;
import ch.algotrader.enumeration.Side;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.util.BeanUtil;
import ch.algotrader.util.DateTimeUtil;
import ch.algotrader.util.collection.Pair;
import ch.algotrader.vo.BalanceVO;
import ch.algotrader.vo.FxExposureVO;
import ch.algotrader.vo.client.BarVO;
import ch.algotrader.vo.client.MarketDataEventVO;
import ch.algotrader.vo.client.OrderStatusVO;
import ch.algotrader.vo.client.PositionVO;
import ch.algotrader.vo.client.TickVO;
import ch.algotrader.vo.client.TransactionVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@ManagedResource(objectName="ch.algotrader.service:name=ManagementService")
public class ManagementServiceImpl implements ManagementService, ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOGGER = LogManager.getLogger(ManagementServiceImpl.class);

    private final CommonConfig commonConfig;

    private final EngineManager engineManager;

    private final SubscriptionService subscriptionService;

    private final LookupService lookupService;

    private final LocalLookupService localLookupService;

    private final PortfolioService portfolioService;

    private final OrderService orderService;

    private final PositionService positionService;

    private final CombinationService combinationService;

    private final PropertyService propertyService;

    private final MarketDataService marketDataService;

    private final ConfigParams configParams;

    private volatile boolean serverMode;
    private volatile Engine engine;

    public ManagementServiceImpl(
            final CommonConfig commonConfig,
            final EngineManager engineManager,
            final SubscriptionService subscriptionService,
            final LookupService lookupService,
            final LocalLookupService localLookupService,
            final PortfolioService portfolioService,
            final OrderService orderService,
            final PositionService positionService,
            final CombinationService combinationService,
            final PropertyService propertyService,
            final MarketDataService marketDataService,
            final ConfigParams configParams) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(engineManager, "EngineManager is null");
        Validate.notNull(subscriptionService, "SubscriptionService is null");
        Validate.notNull(lookupService, "LookupService is null");
        Validate.notNull(localLookupService, "LocalLookupService is null");
        Validate.notNull(portfolioService, "PortfolioService is null");
        Validate.notNull(orderService, "OrderService is null");
        Validate.notNull(positionService, "PositionService is null");
        Validate.notNull(combinationService, "CombinationService is null");
        Validate.notNull(propertyService, "PropertyService is null");
        Validate.notNull(marketDataService, "MarketDataService is null");
        Validate.notNull(configParams, "ConfigParams is null");

        this.commonConfig = commonConfig;
        this.engineManager = engineManager;
        this.subscriptionService = subscriptionService;
        this.lookupService = lookupService;
        this.localLookupService = localLookupService;
        this.portfolioService = portfolioService;
        this.orderService = orderService;
        this.positionService = positionService;
        this.combinationService = combinationService;
        this.propertyService = propertyService;
        this.marketDataService = marketDataService;
        this.configParams = configParams;
    }

    private Engine getMainEngine() {
        Engine engine;
        Collection<Engine> strategyEngines = this.engineManager.getStrategyEngines();
        if (strategyEngines.isEmpty()) {
            throw new IllegalStateException("No strategy engine found");
        } else {
            Iterator<Engine> it = strategyEngines.iterator();
            engine = it.next();
            if (it.hasNext()) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Management services do not support multiple strategies. Using strategy {}", engine.getStrategyName());
                }
            }
        }
        return engine;
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent contextRefreshedEvent) {
        this.engine = getMainEngine();
        this.serverMode = engine.getStrategyName().equals(StrategyImpl.SERVER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets the current System Time")
    public Date getCurrentTime() {

        return this.engine.getCurrentTime();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets all available Currency Balances (only available for the AlgoTrader Server)")
    public Collection<BalanceVO> getDataBalances() {

        if (this.serverMode) {
            return this.portfolioService.getBalances();
        } else {
            return this.portfolioService.getBalances(this.engine.getStrategyName());
        }

    }

    @Override
    @ManagedAttribute(description = "Gets the Net FX Currency Exposure of all FX positions")
    public Collection<FxExposureVO> getDataFxExposure() {

        if (this.serverMode) {
            return this.portfolioService.getFxExposure();
        } else {
            return this.portfolioService.getFxExposure(this.engine.getStrategyName());
        }
    }

    private Collection<OrderStatusVO> convert(Collection<OrderDetailsVO> orderDetails) {
        return orderDetails.stream()
                .filter(entry -> this.serverMode || entry.getOrder().getStrategy().getName().equals(this.engine.getStrategyName()))
                .map(this::convert)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets current open Orders")
    public Collection<OrderStatusVO> getDataOrders() {
        List<OrderDetailsVO> openOrders = this.orderService.getOpenOrderDetails();
        Collections.sort(openOrders, (o1, o2) -> Objects.compare(o1.getOrder().getDateTime(), o2.getOrder().getDateTime(), Date::compareTo));
        return convert(openOrders);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets recently executed Orders")
    public Collection<OrderStatusVO> getDataRecentOrders() {
        return convert(this.orderService.getRecentOrderDetails());
    }

    private OrderStatusVO convert(final OrderDetailsVO entry) {

        Order order = entry.getOrder();
        ExecutionStatusVO details = entry.getExecutionStatus();

        OrderStatusVO orderStatusVO = new OrderStatusVO();
        orderStatusVO.setSide(order.getSide());
        orderStatusVO.setQuantity(order.getQuantity());
        orderStatusVO.setType(StringUtils.substringBefore(ClassUtils.getShortClassName(order.getClass()), "OrderImpl"));
        orderStatusVO.setName(order.getSecurity().toString());
        orderStatusVO.setStrategy(order.getStrategy().toString());
        orderStatusVO.setAccount(order.getAccount() != null ? order.getAccount().toString() : "");
        orderStatusVO.setExchange(order.getEffectiveExchange() != null ? order.getEffectiveExchange().toString() : "");
        orderStatusVO.setTif(order.getTif() != null ? order.getTif().toString() : "");
        orderStatusVO.setIntId(order.getIntId());
        orderStatusVO.setExtId(order.getExtId());
        orderStatusVO.setStatus(details.getStatus());
        orderStatusVO.setFilledQuantity(details.getFilledQuantity());
        orderStatusVO.setRemainingQuantity(details.getRemainingQuantity());
        orderStatusVO.setDescription(order.getExtDescription());

        return orderStatusVO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets current open Positions")
    public Collection<PositionVO> getDataPositions() {

        String baseQuery = "from PositionImpl as p join fetch p.strategy join fetch p.security as s join fetch s.securityFamily ";

        Collection<Position> positions;
        if (this.serverMode) {
            if (this.commonConfig.isDisplayClosedPositions()) {
                positions = this.lookupService.get(Position.class, baseQuery + "order by p.id", QueryType.HQL);
            } else {
                positions = this.lookupService.get(Position.class, baseQuery + "where p.quantity != 0 order by p.id", QueryType.HQL);
            }
        } else {
            if (this.commonConfig.isDisplayClosedPositions()) {
                positions = this.lookupService.get(Position.class, baseQuery + "where p.strategy.name = :strategyName order by p.id", QueryType.HQL, new NamedParam("strategyName", this.engine.getStrategyName()));
            } else {
                positions = this.lookupService.get(Position.class, baseQuery + "where p.strategy.name = :strategyName and p.quantity != 0 order by p.id", QueryType.HQL, new NamedParam("strategyName", this.engine.getStrategyName()));
            }
        }
        PositionVOProducer converter = new PositionVOProducer(this.localLookupService);
        return CollectionUtils.collect(positions, converter::convert);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets the latest Transactions")
    public Collection<TransactionVO> getDataTransactions() {

        Validate.notEmpty(this.engine.getStrategyName(), "Strategy name is empty");

        Collection<Transaction> transactions;
        if (this.serverMode) {
            transactions = this.lookupService.getDailyTransactionsDesc();
        } else {
            transactions = this.lookupService.getDailyTransactionsByStrategyDesc(this.engine.getStrategyName());
        }

        TransactionVOProducer converter = new TransactionVOProducer(this.commonConfig);
        return CollectionUtils.collect(transactions, converter::convert);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    @ManagedAttribute(description = "Gets the latest MarketDataEvents of all subscribed Securities")
    public Collection<MarketDataEventVO> getMarketDataEvents() {

        Map<Long, ch.algotrader.entity.marketData.MarketDataEventVO> marketDataEventMap = this.localLookupService.getCurrentMarketDataEvents();
        List<MarketDataEventVO> subscribedMarketDataEvent = new ArrayList<>();

        // get all subscribed securities
        if (this.serverMode) {

            // for the AlgoTrader Server iterate over a distinct list of subscribed securities and feedType
            List<Pair<Security, String>> subscribedSecurities = this.lookupService.getSubscribedSecuritiesAndFeedTypeForAutoActivateStrategiesInclComponents();
            for (Pair<Security, String> subscribedSecurity : subscribedSecurities) {

                Security security = subscribedSecurity.getFirst();
                String feedType = subscribedSecurity.getSecond();
                ch.algotrader.entity.marketData.MarketDataEventVO marketDataEvent = marketDataEventMap.get(security.getId());
                subscribedMarketDataEvent.add(convert(marketDataEvent, security, feedType));
            }
        } else {

            // for strategies iterate over all subscriptions
            List<Subscription> subscriptions = this.lookupService.getSubscriptionsByStrategyInclComponentsAndProps(this.engine.getStrategyName());
            for (Subscription subscription : subscriptions) {

                Security security = subscription.getSecurity();
                String feedType = subscription.getFeedType();
                ch.algotrader.entity.marketData.MarketDataEventVO marketDataEvent = marketDataEventMap.get(security.getId());
                subscribedMarketDataEvent.add(convert(marketDataEvent, security, feedType));
            }
        }
        return subscribedMarketDataEvent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets the Properties that are defined for this Strategy (or AlgoTrader Server)")
    public Map<String, Object> getProperties() {

        ConfigProvider configProvider = this.configParams.getConfigProvider();
        Set<String> names = configProvider.getNames();
        Map<String, Object> props = new HashMap<>();
        for (String name : names) {

            props.put(name, configProvider.getParameter(name, String.class));
        }
        return props;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets the Allocation that is assigned to this Strategy (or to the AlgoTrader Server)")
    public double getStrategyAllocation() {

        return this.lookupService.getStrategyByName(this.engine.getStrategyName()).getAllocation();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets the Cash Balance of this Strategy (or the entire System if called from the AlgoTrader Server)")
    public BigDecimal getStrategyCashBalance() {

        if (this.serverMode) {
            return this.portfolioService.getCashBalance();
        } else {
            return this.portfolioService.getCashBalance(this.engine.getStrategyName());
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets the current Leverage of this Strategy")
    public double getStrategyLeverage() {

        if (this.serverMode) {
            return this.portfolioService.getLeverage();
        } else {
            return this.portfolioService.getLeverage(this.engine.getStrategyName());
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets the name of this Strategy")
    public String getStrategyName() {

        return this.engine.getStrategyName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets the Net-Liquidation-Value of this Strategy (or the entire System if called from the AlgoTrader Server)")
    public BigDecimal getStrategyNetLiqValue() {

        if (this.serverMode) {
            return this.portfolioService.getNetLiqValue();
        } else {
            return this.portfolioService.getNetLiqValue(this.engine.getStrategyName());
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets the performance since the beginning of the month of this Strategy (or the entire System if called from the AlgoTrader Server)")
    public double getStrategyPerformance() {

        if (this.serverMode) {
            return this.portfolioService.getPerformance();
        } else {
            return this.portfolioService.getPerformance(this.engine.getStrategyName());
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets the total Market Value of all Positions of this Strategy (or the entire System if called from the AlgoTrader Server)")
    public BigDecimal getStrategySecuritiesCurrentValue() {

        if (this.serverMode) {
            return this.portfolioService.getSecuritiesCurrentValue();
        } else {
            return this.portfolioService.getSecuritiesCurrentValue(this.engine.getStrategyName());
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets the total UnrealizedPL of all Positions of this Strategy (or the entire System if called from the AlgoTrader Server)")
    public BigDecimal getStrategyUnrealizedPL() {

        if (this.serverMode) {
            return this.portfolioService.getUnrealizedPL();
        } else {
            return this.portfolioService.getUnrealizedPL(this.engine.getStrategyName());
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Deploy the specified Statement")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "moduleName", description = "Name of the module. (e.g. 'signal' for file named 'module-signal.epl')"),
            @ManagedOperationParameter(name = "statementName", description = "statementName") })
    public void deployStatement(final String moduleName, final String statementName) {

        Validate.notEmpty(moduleName, "Module name is empty");
        Validate.notEmpty(statementName, "Statement name is empty");

        this.engine.deployStatement(moduleName, statementName);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Deploy the specified Module")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "moduleName", description = "Name of the module. (e.g. 'signal' for file named 'module-signal.epl')") })
    public void deployModule(final String moduleName) {

        Validate.notEmpty(moduleName, "Module name is empty");

        this.engine.deployModule(moduleName);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Send an order")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "security", description = "<html><ul> <li> securityId (e.g. 123) </li> <li> symbol (e.g. GOOG) </li> <li> isin, prefix with &quot;isin:&quot;, (e.g. &quot;isin:EU0009654078&quot;) </li> <li> bbgid, prefix with &quot;bbgid:&quot;, (e.g. &quot;bbgid:BBG005NHP5P9&quot;) </li> <li> ric, prefix with &quot;ric:&quot;, (e.g. &quot;ric:.SPX&quot;) </li> <li> conid, prefix with &quot;conid:&quot;, (e.g. &quot;conid:12087817&quot;) </li> </ul></html>"),
            @ManagedOperationParameter(name = "quantity", description = "The requested quantity (positive value)"),
            @ManagedOperationParameter(name = "side", description = "<html>Side: <ul> <li> B (BUY) </li> <li> S (SELL) </li> <li> SS (SELL_SHORT) </li> </ul></html>"),
            @ManagedOperationParameter(name = "type", description = "<html>Order type: <ul> <li> M (Market) </li> <li> L (Limit) </li> <li> S (Stop) </li> <li> SL (StopLimit) </li> <li> TI (TickwiseIncremental) </li> <li> VI (VariableIncremental) </li> <li> SLI (Slicing) </li> </ul> or order preference (e.g. 'FVIX' or 'OVIX')</html>"),
            @ManagedOperationParameter(name = "accountName", description = "accountName (optional)"),
            @ManagedOperationParameter(name = "exchangeName", description = "exchangeName (optional)"),
            @ManagedOperationParameter(name = "properties", description = "<html>Additional properties to be set on the order as a comma separated list (e.g. stop=12.0,limit=12.5).<p> In addition custom properties can be set on the order (e.g. FIX123=12, INTERNALportfolio=TEST)</hmlt>") })
    public void sendOrder(final String security, final long quantity, final String side, final String type, String accountName, final String exchangeName, final String properties) {

        Validate.notEmpty(security, "Security is empty");
        Validate.notEmpty(side, "Side is empty");
        Validate.notEmpty(type, "Type is empty");

        Side sideObject = Side.fromValue(side);

        Strategy strategy = this.lookupService.getStrategyByName(this.engine.getStrategyName());
        Security securityObject = this.lookupService.getSecurity(getSecurityId(security));

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
            order = new TickwiseIncrementalOrder();
        } else if ("VI".equals(type)) {
            order = new VariableIncrementalOrder();
        } else if ("SLI".equals(type)) {
            order = new SlicingOrder();
        } else {

            // create the order from an OrderPreference
            order = this.orderService.createOrderByOrderPreference(type);
        }

        // set common values
        order.setStrategy(strategy);
        order.setSecurity(securityObject);
        order.setQuantity(Math.abs(quantity));
        order.setSide(sideObject);

        // set the account
        if ("".equals(accountName)) {
            accountName = this.commonConfig.getDefaultAccountName();
        }

        Account account = this.lookupService.getAccountByName(accountName);
        order.setAccount(account);

        // set the exchange
        if (!"".equals(exchangeName)) {
            Exchange exchange = this.lookupService.getExchangeByName(exchangeName);
            order.setExchange(exchange);
        }

        // set additional properties
        if (!"".equals(properties)) {

            // get the properties
            Map<String, String> propertiesMap = new HashMap<>();
            for (String nameValue : properties.split(",")) {
                propertiesMap.put(nameValue.split("=")[0], nameValue.split("=")[1]);
            }

            // separate properties that correspond to actual Order fields from the rest
            Map<String, String> fields = new HashMap<>();
            PropertyDescriptor[] pds = BeanUtil.getPropertyDescriptors(order);
            for (PropertyDescriptor pd : pds) {
                String name = pd.getName();
                if (propertiesMap.containsKey(name)) {
                    fields.put(name, propertiesMap.get(name));
                    propertiesMap.remove(name);
                }
            }

            // populate the fields
            try {
                BeanUtil.populate(order, fields);
            } catch (ReflectiveOperationException ex) {
                throw new ServiceException(ex);
            }

            // create OrderProperty Entities for the remaining properties
            for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {

                OrderProperty orderProperty = OrderProperty.Factory.newInstance();

                String name = entry.getKey();
                if (name.toUpperCase().startsWith(OrderPropertyType.FIX.toString())) {
                    name = name.substring(3);
                    orderProperty.setType(OrderPropertyType.FIX);
                } else if (name.toUpperCase().startsWith(OrderPropertyType.IB.toString())) {
                    name = name.substring(2);
                    orderProperty.setType(OrderPropertyType.IB);
                } else {
                    name = name.substring(8);
                    orderProperty.setType(OrderPropertyType.INTERNAL);
                }
                orderProperty.setName(name);
                orderProperty.setValue(entry.getValue());

                order.addOrderProperties(name, orderProperty);
            }
        }

        // send orders
        this.orderService.sendOrder(order);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Cancel an Order")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "intId", description = "Internal Id of the Order") })
    public void cancelOrder(final String intId) {

        Validate.notEmpty(intId, "Int id is empty");

        this.orderService.cancelOrder(intId);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Modify an Order")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "intId", description = "Internal Id of the Order"),
            @ManagedOperationParameter(name = "properties", description = "Additional properties to be set on the order as a comma separated list (e.g. stop=12.0,limit=12.5)") })
    public void modifyOrder(final String intId, final String properties) {

        Validate.notEmpty(intId, "Int id is empty");
        Validate.notEmpty(properties, "Properties are empty");

        // get the properties
        Map<String, String> propertiesMap = new HashMap<>();
        for (String nameValue : properties.split(",")) {
            propertiesMap.put(nameValue.split("=")[0], nameValue.split("=")[1]);
        }

        this.orderService.modifyOrder(intId, propertiesMap);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Closes the specified Position by using the defined default OrderPreference")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "positionId", description = "positionId") })
    public void closePosition(final long positionId) {

        this.positionService.closePosition(positionId, false);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Reduces the Position by the specified amount by using the defined default OrderPreference")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "positionId", description = "positionId"), @ManagedOperationParameter(name = "quantity", description = "quantity") })
    public void reducePosition(final long positionId, final int quantity) {

        this.positionService.reducePosition(positionId, quantity);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Reduce the Component quantities and the associated Position by the specified ratio")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "combination", description = "<html><ul> <li> securityId (e.g. 123) </li> <li> symbol (e.g. GOOG) </li> <li> isin, prefix with &quot;isin:&quot;, (e.g. &quot;isin:EU0009654078&quot;) </li> <li> bbgid, prefix with &quot;bbgid:&quot;, (e.g. &quot;bbgid:BBG005NHP5P9&quot;) </li> <li> ric, prefix with &quot;ric:&quot;, (e.g. &quot;ric:.SPX&quot;) </li> <li> conid, prefix with &quot;conid:&quot;, (e.g. &quot;conid:12087817&quot;) </li> </ul></html>"),
            @ManagedOperationParameter(name = "ratio", description = "ratio") })
    public void reduceCombination(final String combination, final double ratio) {

        Validate.notEmpty(combination, "Combination is empty");

        this.combinationService.reduceCombination(getSecurityId(combination), this.engine.getStrategyName(), ratio);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Set the value of the specified Esper variable")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "variableName", description = "variableName"), @ManagedOperationParameter(name = "value", description = "value") })
    public void setVariableValue(final String variableName, final String value) {

        Validate.notEmpty(variableName, "Variable name is empty");
        Validate.notEmpty(value, "Value is empty");

        this.engine.setVariableValueFromString(variableName, value);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Subscribe to the specified Security")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "security", description = "<html><ul> <li> securityId (e.g. 123) </li> <li> symbol (e.g. GOOG) </li> <li> isin, prefix with &quot;isin:&quot;, (e.g. &quot;isin:EU0009654078&quot;) </li> <li> bbgid, prefix with &quot;bbgid:&quot;, (e.g. &quot;bbgid:BBG005NHP5P9&quot;) </li> <li> ric, prefix with &quot;ric:&quot;, (e.g. &quot;ric:.SPX&quot;) </li> <li> conid, prefix with &quot;conid:&quot;, (e.g. &quot;conid:12087817&quot;) </li> </ul></html>"),
            @ManagedOperationParameter(name = "feedType", description = "The market data feed to use (e.g. IB, BB or DC)") })
    public void subscribe(final String security, final String feedType) {

        Validate.notEmpty(security, "Security is empty");

        if (!"".equals(feedType)) {
            this.subscriptionService.subscribeMarketDataEvent(this.engine.getStrategyName(), getSecurityId(security), feedType);
        } else {
            this.subscriptionService.subscribeMarketDataEvent(this.engine.getStrategyName(), getSecurityId(security));
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Unsubscribe the specified Security")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "security", description = "<html><ul> <li> securityId (e.g. 123) </li> <li> symbol (e.g. GOOG) </li> <li> isin, prefix with &quot;isin:&quot;, (e.g. &quot;isin:EU0009654078&quot;) </li> <li> bbgid, prefix with &quot;bbgid:&quot;, (e.g. &quot;bbgid:BBG005NHP5P9&quot;) </li> <li> ric, prefix with &quot;ric:&quot;, (e.g. &quot;ric:.SPX&quot;) </li> <li> conid, prefix with &quot;conid:&quot;, (e.g. &quot;conid:12087817&quot;) </li> </ul></html>"),
            @ManagedOperationParameter(name = "feedType", description = "The market data feed to use (e.g. IB, BB or DC)") })
    public void unsubscribe(final String security, final String feedType) {

        Validate.notEmpty(security, "Security is empty");

        if (!"".equals(feedType)) {
            this.subscriptionService.unsubscribeMarketDataEvent(this.engine.getStrategyName(), getSecurityId(security), feedType);
        } else {
            this.subscriptionService.unsubscribeMarketDataEvent(this.engine.getStrategyName(), getSecurityId(security));
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Request the latest Market Data Events of all subscribed Securities")
    @ManagedOperationParameters({})
    public void requestCurrentTicks() {

        this.marketDataService.requestCurrentTicks(this.engine.getStrategyName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Add or modify a Property")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "propertyHolderId", description = "Id of the PropertyHolder (e.g. Subscription, Position or Strategy)"),
            @ManagedOperationParameter(name = "name", description = "Name of the Property"),
            @ManagedOperationParameter(name = "value", description = "value"),
            @ManagedOperationParameter(name = "type", description = "<html>Type of the value: <ul> <li> INT </li> <li> DOUBLE </li> <li> MONEY </li> <li> TEXT </li> <li> DATE (Format: dd.mm.yyyy hh:mm:ss) </li> <li> BOOLEAN </li> </ul></html>") })
    public void addProperty(final long propertyHolderId, final String name, final String value, final String type) {

        Validate.notEmpty(name, "Name is empty");
        Validate.notEmpty(value, "Value is empty");
        Validate.notEmpty(type, "Type is empty");

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
            try {
                Instant instant = DateTimeUtil.parseLocalDateTime(value, Instant::from);
                obj = new Date(instant.toEpochMilli());
            } catch (DateTimeParseException ex) {
                throw new ServiceException(ex);
            }
        } else if ("BOOLEAN".equals(type)) {
            obj = Boolean.parseBoolean(value);
        } else {
            throw new IllegalArgumentException("unknown type " + type);
        }

        this.propertyService.addProperty(propertyHolderId, name, obj, false);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Remove the specified property")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "propertyHolderId", description = "Id of the PropertyHolder (e.g. Subscription, Position or Strategy)"),
            @ManagedOperationParameter(name = "name", description = "name of the property") })
    public void removeProperty(final long propertyHolderId, final String name) {

        Validate.notEmpty(name, "Name is empty");

        this.propertyService.removeProperty(propertyHolderId, name);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "creates a Combination of the specified type, securityFamilyId and optional underlying. Returns the id of the newly created combination")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "combinationType", description = "<html><ul> <li> VERTICAL_SPREAD </li> <li> COVERED_CALL </li> <li> RATIO_SPREAD </li> <li> STRADDLE </li> <li> STRANGLE </li> <li> BUTTERFLY </li> <li> CALENDAR_SPREAD </li> <li> IRON_CONDOR </li> </ul></html>"),
            @ManagedOperationParameter(name = "securityFamilyId", description = "securityFamilyId"),
            @ManagedOperationParameter(name = "underlying", description = "<html>Underlying Security: <ul> <li> securityId (e.g. 123) </li> <li> symbol (e.g. GOOG) </li> <li> isin, prefix with &quot;isin:&quot;, (e.g. &quot;isin:EU0009654078&quot;) </li> <li> bbgid, prefix with &quot;bbgid:&quot;, (e.g. &quot;bbgid:BBG005NHP5P9&quot;) </li> <li> ric, prefix with &quot;ric:&quot;, (e.g. &quot;ric:.SPX&quot;) </li> <li> conid, prefix with &quot;conid:&quot;, (e.g. &quot;conid:12087817&quot;) </li> </ul></html>") })
    public long createCombination(final String combinationType, final long securityFamilyId, final String underlying) {

        Validate.notEmpty(combinationType, "Combination type is empty");

        if ("".equals(underlying)) {
            return this.combinationService.createCombination(CombinationType.valueOf(combinationType), securityFamilyId).getId();
        } else {
            return this.combinationService.createCombination(CombinationType.valueOf(combinationType), securityFamilyId, getSecurityId(underlying)).getId();
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Set the quantity of the specified Component")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "combinationId", description = "the id of the combination"),
            @ManagedOperationParameter(name = "component", description = "<html>Component: <ul> <li> securityId (e.g. 123) </li> <li> symbol (e.g. GOOG) </li> <li> isin, prefix with &quot;isin:&quot;, (e.g. &quot;isin:EU0009654078&quot;) </li> <li> bbgid, prefix with &quot;bbgid:&quot;, (e.g. &quot;bbgid:BBG005NHP5P9&quot;) </li> <li> ric, prefix with &quot;ric:&quot;, (e.g. &quot;ric:.SPX&quot;) </li> <li> conid, prefix with &quot;conid:&quot;, (e.g. &quot;conid:12087817&quot;) </li> </ul></html>"),
            @ManagedOperationParameter(name = "quantitiy", description = "quantitiy") })
    public void setComponentQuantity(final long combinationId, final String component, final long quantitiy) {

        Validate.notEmpty(component, "Component is empty");

        this.combinationService.setComponentQuantity(combinationId, getSecurityId(component), quantitiy);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Remove the specified Component from the specified Combination")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "combinationId", description = "the id of the combination"),
            @ManagedOperationParameter(name = "component", description = "<html>Component: <ul> <li> securityId (e.g. 123) </li> <li> symbol (e.g. GOOG) </li> <li> isin, prefix with &quot;isin:&quot;, (e.g. &quot;isin:EU0009654078&quot;) </li> <li> bbgid, prefix with &quot;bbgid:&quot;, (e.g. &quot;bbgid:BBG005NHP5P9&quot;) </li> <li> ric, prefix with &quot;ric:&quot;, (e.g. &quot;ric:.SPX&quot;) </li> <li> conid, prefix with &quot;conid:&quot;, (e.g. &quot;conid:12087817&quot;) </li> </ul></html>") })
    public void removeComponent(final long combinationId, final String component) {

        Validate.notEmpty(component, "Component is empty");

        this.combinationService.removeComponent(combinationId, getSecurityId(component));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "deletes a Combination")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "combinationId", description = "the id of the combination") })
    public void deleteCombination(final long combinationId) {

        this.combinationService.deleteCombination(combinationId);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Checks if the System is alive")
    @ManagedOperationParameters({})
    public void checkIsAlive() {

        this.lookupService.getCurrentDBTime();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Shutdown this JVM")
    @ManagedOperationParameters({})
    public void shutdown() {

        // cancel all orders if we called from the AlgoTrader Server
        if (StrategyImpl.SERVER.equals(this.engine.getStrategyName())) {
            this.orderService.cancelAllOrders();
        }

    }

    private long getSecurityId(String securityString) {

        if (NumberUtils.isDigits(securityString)) {
            return Integer.parseInt(securityString);

        } else {

            Security security;
            if (securityString.startsWith("isin:")) {
                security = this.lookupService.getSecurityByIsin(securityString.substring(5));
            } else if (securityString.startsWith("bbgid:")) {
                security = this.lookupService.getSecurityByBbgid(securityString.substring(6));
            } else if (securityString.startsWith("ric:")) {
                security = this.lookupService.getSecurityByRic(securityString.substring(4));
            } else if (securityString.startsWith("conid:")) {
                security = this.lookupService.getSecurityByConid(securityString.substring(6));
            } else {
                security = this.lookupService.getSecurityBySymbol(securityString);
            }

            if (security == null) {
                throw new IllegalArgumentException("security was not found " + securityString);
            }

            return security.getId();
        }
    }

    private MarketDataEventVO convert(ch.algotrader.entity.marketData.MarketDataEventVO marketDataEvent, Security security, String feedType) {

        MarketDataEventVO marketDataEventVO;
        if (marketDataEvent instanceof ch.algotrader.entity.marketData.TickVO) {

            ch.algotrader.entity.marketData.TickVO tick = (ch.algotrader.entity.marketData.TickVO) marketDataEvent;
            TickVO tickVO = new TickVO();
            tickVO.setLast(tick.getLast());
            tickVO.setLastDateTime(tick.getLastDateTime());
            tickVO.setVolBid(tick.getVolBid());
            tickVO.setVolAsk(tick.getVolAsk());
            tickVO.setBid(tick.getBid());
            tickVO.setAsk(tick.getAsk());
            tickVO.setVol(tick.getVol());

            marketDataEventVO = tickVO;

        } else if (marketDataEvent instanceof ch.algotrader.entity.marketData.BarVO) {

            ch.algotrader.entity.marketData.BarVO bar = (ch.algotrader.entity.marketData.BarVO) marketDataEvent;
            BarVO barVO = new BarVO();
            barVO.setOpen(bar.getOpen());
            barVO.setHigh(bar.getHigh());
            barVO.setLow(bar.getLow());
            barVO.setClose(bar.getClose());
            barVO.setVol(bar.getVol());

            marketDataEventVO = barVO;
        } else {
            CommonConfig commonConfig = this.commonConfig;
            if (MarketDataType.TICK.equals(commonConfig.getDataSetType())) {
                marketDataEventVO = new TickVO();
            } else if (MarketDataType.BAR.equals(commonConfig.getDataSetType())) {
                marketDataEventVO = new BarVO();
            } else {
                throw new IllegalStateException("Unknown dataSetType " + commonConfig.getDataSetType());
            }
        }
        marketDataEventVO.setSecurityId(security.getId());
        marketDataEventVO.setName(security.toString());
        marketDataEventVO.setFeedType(feedType);

        if (marketDataEvent != null) {
            marketDataEventVO.setDateTime(marketDataEvent.getDateTime());
            marketDataEventVO.setSecurityId(marketDataEvent.getSecurityId());
            marketDataEventVO.setCurrentValue(marketDataEvent.getCurrentValue());
        }
        return marketDataEventVO;
    }

}
