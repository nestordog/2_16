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
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.ConfigParams;
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
import ch.algotrader.entity.trade.OrderProperty;
import ch.algotrader.entity.trade.SlicingOrder;
import ch.algotrader.entity.trade.StopLimitOrder;
import ch.algotrader.entity.trade.StopOrder;
import ch.algotrader.entity.trade.TickwiseIncrementalOrder;
import ch.algotrader.entity.trade.VariableIncrementalOrder;
import ch.algotrader.enumeration.CombinationType;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.enumeration.MarketDataType;
import ch.algotrader.enumeration.OrderPropertyType;
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
@ManagedResource(objectName="ch.algotrader.service:name=ManagementService")
public class ManagementServiceImpl implements ManagementService {

    private final CommonConfig commonConfig;

    private final SubscriptionService subscriptionService;

    private final LookupService lookupService;

    private final PortfolioService portfolioService;

    private final OrderService orderService;

    private final PositionService positionService;

    private final CombinationService combinationService;

    private final PropertyService propertyService;

    private final MarketDataService marketDataService;

    private final ConfigParams configParams;

    public ManagementServiceImpl(final CommonConfig commonConfig,
            final SubscriptionService subscriptionService,
            final LookupService lookupService,
            final PortfolioService portfolioService,
            final OrderService orderService,
            final PositionService positionService,
            final CombinationService combinationService,
            final PropertyService propertyService,
            final MarketDataService marketDataService,
            final ConfigParams configParams) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(subscriptionService, "SubscriptionService is null");
        Validate.notNull(lookupService, "LookupService is null");
        Validate.notNull(portfolioService, "PortfolioService is null");
        Validate.notNull(orderService, "OrderService is null");
        Validate.notNull(positionService, "PositionService is null");
        Validate.notNull(combinationService, "CombinationService is null");
        Validate.notNull(propertyService, "PropertyService is null");
        Validate.notNull(marketDataService, "MarketDataService is null");
        Validate.notNull(configParams, "ConfigParams is null");

        this.commonConfig = commonConfig;
        this.subscriptionService = subscriptionService;
        this.lookupService = lookupService;
        this.portfolioService = portfolioService;
        this.orderService = orderService;
        this.positionService = positionService;
        this.combinationService = combinationService;
        this.propertyService = propertyService;
        this.marketDataService = marketDataService;
        this.configParams = configParams;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets the current System Time")
    public Date getCurrentTime() {

        try {
            String strategyName = this.commonConfig.getStartedStrategyName();
            return EngineLocator.instance().getEngine(strategyName).getCurrentTime();
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets all available Currency Balances (only available for Base)")
    public Collection<BalanceVO> getDataBalances() {

        try {
            String strategyName = this.commonConfig.getStartedStrategyName();
            if (StrategyImpl.BASE.equals(strategyName)) {
                return this.portfolioService.getBalances();
            } else {
                return this.portfolioService.getBalances(strategyName);
            }
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets current open Orders")
    public Collection<OrderStatusVO> getDataOrders() {

        try {
            return this.lookupService.getOpenOrdersVOByStrategy(this.commonConfig.getStartedStrategyName());
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets current open Positions")
    public List<PositionVO> getDataPositions() {

        try {
            return this.lookupService.getPositionsVO(this.commonConfig.getStartedStrategyName(), this.commonConfig.isDisplayClosedPositions());
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets the latest Transactions")
    public List<TransactionVO> getDataTransactions() {

        try {
            return this.lookupService.getTransactionsVO(this.commonConfig.getStartedStrategyName());
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    @ManagedAttribute(description = "Gets the latest MarketDataEvents of all subscribed Securities")
    public List<MarketDataEventVO> getMarketDataEvents() {

        try {
            String strategyName = this.commonConfig.getStartedStrategyName();
            List<MarketDataEvent> marketDataEvents = EngineLocator.instance().getEngine(strategyName).executeQuery("select marketDataEvent.* from MarketDataWindow order by securityId");

            List<MarketDataEventVO> marketDataEventVOs = getMarketDataEventVOs(marketDataEvents);

            // get all subscribed securities
            List<MarketDataEventVO> processedMarketDataEventVOs = new ArrayList<MarketDataEventVO>();
            if (strategyName.equalsIgnoreCase(StrategyImpl.BASE)) {

                // for base iterate over a distinct list of subscribed securities and feedType
                List<Map<String, Object>> subscriptions = this.lookupService.getSubscribedSecuritiesAndFeedTypeForAutoActivateStrategiesInclComponents();
                for (Map<String, Object> subscription : subscriptions) {

                    Security security = (Security) subscription.get("security");
                    FeedType feedType = (FeedType) subscription.get("feedType");

                    // try to get the processedMarketDataEvent
                    MarketDataEventVO marketDataEventVO = getMarketDataEventVO(marketDataEventVOs, security, feedType);

                    processedMarketDataEventVOs.add(marketDataEventVO);
                }

            } else {

                // for strategies iterate over all subscriptions
                List<Subscription> subscriptions = this.lookupService.getSubscriptionsByStrategyInclComponents(strategyName);
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
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets the Properties that are defined for this Strategy (or Base)")
    public Map getProperties() {

        try {
            ConfigProvider configProvider = this.configParams.getConfigProvider();
            Set<String> names = configProvider.getNames();
            Map<Object, Object> props = new HashMap<Object, Object>();
            for (String name : names) {

                props.put(name, configProvider.getParameter(name, String.class));
            }
            return props;
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets the available Funds of this Strategy (or the entire System if called from the Base)")
    public BigDecimal getStrategyAvailableFunds() {

        try {
            String strategyName = this.commonConfig.getStartedStrategyName();
            if (strategyName.equals(StrategyImpl.BASE)) {
                return this.portfolioService.getAvailableFunds();
            } else {
                return this.portfolioService.getAvailableFunds(strategyName);
            }
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets the Allocation that is assigned to this Strategy (or to the Base)")
    public double getStrategyAllocation() {

        try {
            String strategyName = this.commonConfig.getStartedStrategyName();
            return this.lookupService.getStrategyByName(strategyName).getAllocation();
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets the Cash Balance of this Strategy (or the entire System if called from the Base)")
    public BigDecimal getStrategyCashBalance() {

        try {
            String strategyName = this.commonConfig.getStartedStrategyName();
            if (strategyName.equals(StrategyImpl.BASE)) {
                return this.portfolioService.getCashBalance();
            } else {
                return this.portfolioService.getCashBalance(strategyName);
            }
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets the current Leverage of this Strategy")
    public double getStrategyLeverage() {

        try {
            String strategyName = this.commonConfig.getStartedStrategyName();
            if (strategyName.equals(StrategyImpl.BASE)) {
                return this.portfolioService.getLeverage();
            } else {
                return this.portfolioService.getLeverage(strategyName);
            }
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets the Maintenance Margin of this Strategy (or the entire System if called from the Base)")
    public BigDecimal getStrategyMaintenanceMargin() {

        try {
            String strategyName = this.commonConfig.getStartedStrategyName();
            if (strategyName.equals(StrategyImpl.BASE)) {
                return this.portfolioService.getMaintenanceMargin();
            } else {
                return this.portfolioService.getMaintenanceMargin(strategyName);
            }
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets the name of this Strategy")
    public String getStrategyName() {

        try {
            return this.commonConfig.getStartedStrategyName();
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets the Net-Liquidation-Value of this Strategy (or the entire System if called from the Base)")
    public BigDecimal getStrategyNetLiqValue() {

        try {
            String strategyName = this.commonConfig.getStartedStrategyName();
            if (strategyName.equals(StrategyImpl.BASE)) {
                return this.portfolioService.getNetLiqValue();
            } else {
                return this.portfolioService.getNetLiqValue(strategyName);
            }
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets the performance since the beginning of the month of this Strategy (or the entire System if called from the Base)")
    public double getStrategyPerformance() {

        try {
            String strategyName = this.commonConfig.getStartedStrategyName();
            if (strategyName.equals(StrategyImpl.BASE)) {
                return this.portfolioService.getPerformance();
            } else {
                return this.portfolioService.getPerformance(strategyName);
            }
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Gets the total Market Value of all Positions of this Strategy (or the entire System if called from the Base)")
    public BigDecimal getStrategySecuritiesCurrentValue() {

        try {
            String strategyName = this.commonConfig.getStartedStrategyName();
            if (strategyName.equals(StrategyImpl.BASE)) {
                return this.portfolioService.getSecuritiesCurrentValue();
            } else {
                return this.portfolioService.getSecuritiesCurrentValue(strategyName);
            }
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
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

        try {
            EngineLocator.instance().getEngine(this.commonConfig.getStartedStrategyName()).deployStatement(moduleName, statementName);
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Deploy the specified Module")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "moduleName", description = "Name of the module. (e.g. 'signal' for file named 'module-signal.epl')") })
    public void deployModule(final String moduleName) {

        Validate.notEmpty(moduleName, "Module name is empty");

        try {
            EngineLocator.instance().getEngine(this.commonConfig.getStartedStrategyName()).deployModule(moduleName);
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Send an order")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "security", description = "<ul> <li> securityId (e.g. 123) </li> <li> symbol (e.g. GOOG) </li> <li> isin, prefix with &quot;isin:&quot;, (e.g. &quot;isin:EU0009654078&quot;) </li> <li> bbgid, prefix with &quot;bbgid:&quot;, (e.g. &quot;bbgid:BBG005NHP5P9&quot;) </li> <li> ric, prefix with &quot;ric:&quot;, (e.g. &quot;ric:.SPX&quot;) </li> <li> conid, prefix with &quot;conid:&quot;, (e.g. &quot;conid:12087817&quot;) </li> </ul>"),
            @ManagedOperationParameter(name = "quantity", description = "The requested quantity (positive value)"),
            @ManagedOperationParameter(name = "side", description = "Side: <ul> <li> B (BUY) </li> <li> S (SELL) </li> <li> SS (SELL_SHORT) </li> </ul>"),
            @ManagedOperationParameter(name = "type", description = "Order type: <ul> <li> M (Market) </li> <li> L (Limit) </li> <li> S (Stop) </li> <li> SL (StopLimit) </li> <li> TI (TickwiseIncremental) </li> <li> VI (VariableIncremental) </li> <li> SLI (Slicing) </li> </ul> or order preference (e.g. 'FVIX' or 'OVIX')"),
            @ManagedOperationParameter(name = "accountName", description = "accountName"),
            @ManagedOperationParameter(name = "properties", description = "Additional properties to be set on the order as a comma separated list (e.g. stop=12.0,limit=12.5)") })
    public void sendOrder(final String security, final long quantity, final String side, final String type, final String accountName, final String properties) {

        Validate.notEmpty(security, "Security is empty");
        Validate.notEmpty(side, "Side is empty");
        Validate.notEmpty(type, "Type is empty");

        try {
            Side sideObject = Side.fromValue(side);
            String strategyName = this.commonConfig.getStartedStrategyName();

            Strategy strategy = this.lookupService.getStrategyByName(strategyName);
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
                order = TickwiseIncrementalOrder.Factory.newInstance();
            } else if ("VI".equals(type)) {
                order = VariableIncrementalOrder.Factory.newInstance();
            } else if ("SLI".equals(type)) {
                order = SlicingOrder.Factory.newInstance();
            } else {

                // create the order from an OrderPreference
                order = this.lookupService.getOrderByName(type);
            }

            // set common values
            order.setStrategy(strategy);
            order.setSecurity(securityObject);
            order.setQuantity(Math.abs(quantity));
            order.setSide(sideObject);

            // set the account (if defined)
            if (!"".equals(accountName)) {
                Account account = this.lookupService.getAccountByName(accountName);
                order.setAccount(account);
            }

            // set additional properties
            if (!"".equals(properties)) {

                // get the properties
                Map<String, String> propertiesMap = new HashMap<String, String>();
                for (String nameValue : properties.split(",")) {
                    propertiesMap.put(nameValue.split("=")[0], nameValue.split("=")[1]);
                }

                // separate properties that correspond to actual Order fields from the rest
                Map<String, String> fields = new HashMap<String, String>();
                PropertyDescriptor[] pds = BeanUtil.getPropertyDescriptors(order);
                for (PropertyDescriptor pd : pds) {
                    String name = pd.getName();
                    if (propertiesMap.containsKey(name)) {
                        fields.put(name, propertiesMap.get(name));
                        propertiesMap.remove(name);
                    }
                }

                // populate the fields
                BeanUtil.populate(order, fields);

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
                        orderProperty.setType(OrderPropertyType.INTERNAL);
                    }
                    orderProperty.setName(name);
                    orderProperty.setValue(entry.getValue());

                    order.addOrderProperties(name, orderProperty);
                }
            }

            // send orders
            this.orderService.sendOrder(order);
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Cancel an Order")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "intId", description = "Internal Id of the Order") })
    public void cancelOrder(final String intId) {

        Validate.notEmpty(intId, "Int id is empty");

        try {
            this.orderService.cancelOrder(intId);
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
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

        try {
            // get the properties
            Map<String, String> propertiesMap = new HashMap<String, String>();
            for (String nameValue : properties.split(",")) {
                propertiesMap.put(nameValue.split("=")[0], nameValue.split("=")[1]);
            }

            this.orderService.modifyOrder(intId, propertiesMap);
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Closes the specified Position by using the defined DefaultOrderPreference")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "positionId", description = "positionId") })
    public void closePosition(final int positionId) {

        try {
            this.positionService.closePosition(positionId, false);
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Reduces the Position by the specified amount by using the defined DefaultOrderPreference")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "positionId", description = "positionId"), @ManagedOperationParameter(name = "quantity", description = "quantity") })
    public void reducePosition(final int positionId, final int quantity) {

        try {
            this.positionService.reducePosition(positionId, quantity);
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Reduce the Component quantities and the associated Position by the specified ratio")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "combination", description = "<ul> <li> securityId (e.g. 123) </li> <li> symbol (e.g. GOOG) </li> <li> isin, prefix with &quot;isin:&quot;, (e.g. &quot;isin:EU0009654078&quot;) </li> <li> bbgid, prefix with &quot;bbgid:&quot;, (e.g. &quot;bbgid:BBG005NHP5P9&quot;) </li> <li> ric, prefix with &quot;ric:&quot;, (e.g. &quot;ric:.SPX&quot;) </li> <li> conid, prefix with &quot;conid:&quot;, (e.g. &quot;conid:12087817&quot;) </li> </ul>"),
            @ManagedOperationParameter(name = "ratio", description = "ratio") })
    public void reduceCombination(final String combination, final double ratio) {

        Validate.notEmpty(combination, "Combination is empty");

        try {
            this.combinationService.reduceCombination(getSecurityId(combination), this.commonConfig.getStartedStrategyName(), ratio);
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Set or modify the ExitValue of the specified Position")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "positionId", description = "positionId"), @ManagedOperationParameter(name = "exitValue", description = "exitValue") })
    public void setExitValue(final int positionId, final double exitValue) {

        try {
            this.positionService.setExitValue(positionId, new BigDecimal(exitValue), true);
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Remove the ExitValue from the specified Position")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "positionId", description = "positionId") })
    public void removeExitValue(final int positionId) {

        try {
            this.positionService.removeExitValue(positionId);
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
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

        try {
            EngineLocator.instance().getEngine(this.commonConfig.getStartedStrategyName()).setVariableValueFromString(variableName, value);
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Subscribe to the specified Security.    ")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "security", description = "<ul> <li> securityId (e.g. 123) </li> <li> symbol (e.g. GOOG) </li> <li> isin, prefix with &quot;isin:&quot;, (e.g. &quot;isin:EU0009654078&quot;) </li> <li> bbgid, prefix with &quot;bbgid:&quot;, (e.g. &quot;bbgid:BBG005NHP5P9&quot;) </li> <li> ric, prefix with &quot;ric:&quot;, (e.g. &quot;ric:.SPX&quot;) </li> <li> conid, prefix with &quot;conid:&quot;, (e.g. &quot;conid:12087817&quot;) </li> </ul>"),
            @ManagedOperationParameter(name = "feedType", description = "The market data feed to use (e.g. IB, BB or DC)") })
    public void subscribe(final String security, final String feedType) {

        Validate.notEmpty(security, "Security is empty");

        try {
            String startedStrategyName = this.commonConfig.getStartedStrategyName();
            if (!"".equals(feedType)) {
                this.subscriptionService.subscribeMarketDataEvent(startedStrategyName, getSecurityId(security), FeedType.fromString(feedType));
            } else {
                this.subscriptionService.subscribeMarketDataEvent(startedStrategyName, getSecurityId(security));
            }
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Unsubscribe the specified Security")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "security", description = "<ul> <li> securityId (e.g. 123) </li> <li> symbol (e.g. GOOG) </li> <li> isin, prefix with &quot;isin:&quot;, (e.g. &quot;isin:EU0009654078&quot;) </li> <li> bbgid, prefix with &quot;bbgid:&quot;, (e.g. &quot;bbgid:BBG005NHP5P9&quot;) </li> <li> ric, prefix with &quot;ric:&quot;, (e.g. &quot;ric:.SPX&quot;) </li> <li> conid, prefix with &quot;conid:&quot;, (e.g. &quot;conid:12087817&quot;) </li> </ul>"),
            @ManagedOperationParameter(name = "feedType", description = "The market data feed to use (e.g. IB, BB or DC)") })
    public void unsubscribe(final String security, final String feedType) {

        Validate.notEmpty(security, "Security is empty");

        try {
            String startedStrategyName = this.commonConfig.getStartedStrategyName();
            if (!"".equals(feedType)) {
                this.subscriptionService.unsubscribeMarketDataEvent(startedStrategyName, getSecurityId(security), FeedType.fromString(feedType));
            } else {
                this.subscriptionService.unsubscribeMarketDataEvent(startedStrategyName, getSecurityId(security));
            }
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Request the latest Market Data Events of all subscribed Securities")
    @ManagedOperationParameters({})
    public void requestCurrentTicks() {

        try {
            this.marketDataService.requestCurrentTicks(this.commonConfig.getStartedStrategyName());
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
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
            @ManagedOperationParameter(name = "type", description = "Type of the value: <ul> <li> INT </li> <li> DOUBLE </li> <li> MONEY </li> <li> TEXT </li> <li> DATE (Format: dd.mm.yyyy hh:mm:ss) </li> <li> BOOLEAN </li> </ul>") })
    public void addProperty(final int propertyHolderId, final String name, final String value, final String type) {

        Validate.notEmpty(name, "Name is empty");
        Validate.notEmpty(value, "Value is empty");
        Validate.notEmpty(type, "Type is empty");

        try {
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

            this.propertyService.addProperty(propertyHolderId, name, obj, false);
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Remove the specified property")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "propertyHolderId", description = "Id of the PropertyHolder (e.g. Subscription, Position or Strategy)"),
            @ManagedOperationParameter(name = "name", description = "name of the property") })
    public void removeProperty(final int propertyHolderId, final String name) {

        Validate.notEmpty(name, "Name is empty");

        try {
            this.propertyService.removeProperty(propertyHolderId, name);
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "creates a Combination of the specified type, securityFamilyId and optional underlying. Returns the id of the newly created combination")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "combinationType", description = "<ul> <li> VERTICAL_SPREAD </li> <li> COVERED_CALL </li> <li> RATIO_SPREAD </li> <li> STRADDLE </li> <li> STRANGLE </li> <li> BUTTERFLY </li> <li> CALENDAR_SPREAD </li> <li> IRON_CONDOR </li> </ul>"),
            @ManagedOperationParameter(name = "securityFamilyId", description = "securityFamilyId"),
            @ManagedOperationParameter(name = "underlying", description = "Underlying Security: <ul> <li> securityId (e.g. 123) </li> <li> symbol (e.g. GOOG) </li> <li> isin, prefix with &quot;isin:&quot;, (e.g. &quot;isin:EU0009654078&quot;) </li> <li> bbgid, prefix with &quot;bbgid:&quot;, (e.g. &quot;bbgid:BBG005NHP5P9&quot;) </li> <li> ric, prefix with &quot;ric:&quot;, (e.g. &quot;ric:.SPX&quot;) </li> <li> conid, prefix with &quot;conid:&quot;, (e.g. &quot;conid:12087817&quot;) </li> </ul>") })
    public int createCombination(final String combinationType, final int securityFamilyId, final String underlying) {

        Validate.notEmpty(combinationType, "Combination type is empty");

        try {
            if ("".equals(underlying)) {
                return this.combinationService.createCombination(CombinationType.fromString(combinationType), securityFamilyId).getId();
            } else {
                return this.combinationService.createCombination(CombinationType.fromString(combinationType), securityFamilyId, getSecurityId(underlying)).getId();
            }
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Set the quantity of the specified Component")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "combinationId", description = "the id of the combination"),
            @ManagedOperationParameter(name = "component", description = "Component: <ul> <li> securityId (e.g. 123) </li> <li> symbol (e.g. GOOG) </li> <li> isin, prefix with &quot;isin:&quot;, (e.g. &quot;isin:EU0009654078&quot;) </li> <li> bbgid, prefix with &quot;bbgid:&quot;, (e.g. &quot;bbgid:BBG005NHP5P9&quot;) </li> <li> ric, prefix with &quot;ric:&quot;, (e.g. &quot;ric:.SPX&quot;) </li> <li> conid, prefix with &quot;conid:&quot;, (e.g. &quot;conid:12087817&quot;) </li> </ul>"),
            @ManagedOperationParameter(name = "quantitiy", description = "quantitiy") })
    public void setComponentQuantity(final int combinationId, final String component, final long quantitiy) {

        Validate.notEmpty(component, "Component is empty");

        try {
            this.combinationService.setComponentQuantity(combinationId, getSecurityId(component), quantitiy);
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Remove the specified Component from the specified Combination")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "combinationId", description = "the id of the combination"),
            @ManagedOperationParameter(name = "component", description = "Component: <ul> <li> securityId (e.g. 123) </li> <li> symbol (e.g. GOOG) </li> <li> isin, prefix with &quot;isin:&quot;, (e.g. &quot;isin:EU0009654078&quot;) </li> <li> bbgid, prefix with &quot;bbgid:&quot;, (e.g. &quot;bbgid:BBG005NHP5P9&quot;) </li> <li> ric, prefix with &quot;ric:&quot;, (e.g. &quot;ric:.SPX&quot;) </li> <li> conid, prefix with &quot;conid:&quot;, (e.g. &quot;conid:12087817&quot;) </li> </ul>") })
    public void removeComponent(final int combinationId, final String component) {

        Validate.notEmpty(component, "Component is empty");

        try {
            this.combinationService.removeComponent(combinationId, getSecurityId(component));
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "deletes a Combination")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "combinationId", description = "the id of the combination") })
    public void deleteCombination(final int combinationId) {

        try {
            this.combinationService.deleteCombination(combinationId);
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Checks if the System is alive")
    @ManagedOperationParameters({})
    public void checkIsAlive() {

        try {
            this.lookupService.getCurrentDBTime();
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Shutdown this JVM")
    @ManagedOperationParameters({})
    public void shutdown() {

        try {
            // cancel all orders if we called from base
            if (this.commonConfig.isStartedStrategyBASE()) {
                orderService.cancelAllOrders();
            }
            // need to force exit because grafefull shutdown of esper-service (and esper-jmx) does not work
            System.exit(0);
        } catch (Exception ex) {
            throw new ManagementServiceException(ex.getMessage(), ex);
        }
    }

    private int getSecurityId(String securityString) {

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
            CommonConfig commonConfig = this.commonConfig;
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
