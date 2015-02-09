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
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ch.algotrader.vo.BalanceVO;
import ch.algotrader.vo.MarketDataEventVO;
import ch.algotrader.vo.OrderStatusVO;
import ch.algotrader.vo.PositionVO;
import ch.algotrader.vo.TransactionVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface ManagementService {

    /**
     * Gets the current System Time
     */
    public Date getCurrentTime();

    /**
     * Gets all available Currency Balances (only available for AlgoTrader Server)
     */
    public Collection<BalanceVO> getDataBalances();

    /**
     * Gets current open Orders
     */
    public Collection<OrderStatusVO> getDataOrders();

    /**
     * Gets current open Positions
     */
    public List<PositionVO> getDataPositions();

    /**
     * Gets the latest Transactions
     */
    public List<TransactionVO> getDataTransactions();

    /**
     * Gets the latest MarketDataEvents of all subscribed Securities
     */
    public List<MarketDataEventVO> getMarketDataEvents();

    /**
     * Gets the Properties that are defined for this Strategy (or AlgoTrader Server)
     */
    public Map getProperties();

    /**
     * Gets the available Funds of this Strategy (or the entire System if called from the AlgoTrader Server)
     */
    public BigDecimal getStrategyAvailableFunds();

    /**
     * Gets the Allocation that is assigned to this Strategy (or to the AlgoTrader Server)
     */
    public double getStrategyAllocation();

    /**
     * Gets the Cash Balance of this Strategy (or the entire System if called from the AlgoTrader Server)
     */
    public BigDecimal getStrategyCashBalance();

    /**
     * Gets the current Leverage of this Strategy
     */
    public double getStrategyLeverage();

    /**
     * Gets the Maintenance Margin of this Strategy (or the entire System if called from the AlgoTrader Server)
     */
    public BigDecimal getStrategyMaintenanceMargin();

    /**
     * Gets the name of this Strategy
     */
    public String getStrategyName();

    /**
     * Gets the Net-Liquidation-Value of this Strategy (or the entire System if called from the
     * AlgoTrader Server)
     */
    public BigDecimal getStrategyNetLiqValue();

    /**
     * Gets the performance since the beginning of the month of this Strategy (or the entire System
     * if called from the AlgoTrader Server)
     */
    public double getStrategyPerformance();

    /**
     * Gets the total Market Value of all Positions of this Strategy (or the entire System if called
     * from the AlgoTrader Server)
     */
    public BigDecimal getStrategySecuritiesCurrentValue();

    /**
     * Deploy the specified Statement
     * @param moduleName Name of the module. (e.g. 'signal' for file named 'module-signal.epl')
     */
    public void deployStatement(String moduleName, String statementName);

    /**
     * Deploy the specified Module
     * @param moduleName Name of the module. (e.g. 'signal' for file named 'module-signal.epl')
     */
    public void deployModule(String moduleName);

    /**
     * Send an order
     * @param security
     * <ul>
     * <li>securityId (e.g. 123)</li>
     * <li>symbol (e.g. GOOG)</li>
     * <li>isin, prefix with &quot;isin:&quot;, (e.g. &quot;isin:EU0009654078&quot;)</li>
     * <li>bbgid, prefix with &quot;bbgid:&quot;, (e.g. &quot;bbgid:BBG005NHP5P9&quot;)</li>
     * <li>ric, prefix with &quot;ric:&quot;, (e.g. &quot;ric:.SPX&quot;)</li>
     * <li>conid, prefix with &quot;conid:&quot;, (e.g. &quot;conid:12087817&quot;)</li>
     * </ul>
     * @param quantity The requested quantity (positive value)
     * @param side
     * Side:
     * <ul>
     * <li>B (BUY)</li>
     * <li>S (SELL)</li>
     * <li>SS (SELL_SHORT)</li>
     * </ul>
     * @param type
     * Order type:
     * <ul>
     * <li>M (Market)</li>
     * <li>L (Limit)</li>
     * <li>S (Stop)</li>
     * <li>SL (StopLimit)</li>
     * <li>TI (TickwiseIncremental)</li>
     * <li>VI (VariableIncremental)</li>
     * <li>SLI (Slicing)</li>
     * </ul>
     * or order preference (e.g. 'FVIX' or 'OVIX')
     * @param properties Additional properties to be set on the order as a comma separated list (e.g. stop=12.0,limit=12.5)
     */
    public void sendOrder(String security, long quantity, String side, String type, String accountName, String properties);

    /**
     * Cancel an Order
     * @param intId Internal Id of the Order
     */
    public void cancelOrder(String intId);

    /**
     * Modify an Order
     * @param intId Internal Id of the Order
     * @param properties Additional properties to be set on the order as a comma separated list (e.g. stop=12.0,limit=12.5)
     */
    public void modifyOrder(String intId, String properties);

    /**
     * Closes the specified Position by using the defined default OrderPreference
     */
    public void closePosition(int positionId);

    /**
     * Reduces the Position by the specified amount by using the defined default OrderPreference
     */
    public void reducePosition(int positionId, int quantity);

    /**
     * Reduce the Component quantities and the associated Position by the specified ratio
     * @param combination
     * <ul>
     * <li>securityId (e.g. 123)</li>
     * <li>symbol (e.g. GOOG)</li>
     * <li>isin, prefix with &quot;isin:&quot;, (e.g. &quot;isin:EU0009654078&quot;)</li>
     * <li>bbgid, prefix with &quot;bbgid:&quot;, (e.g. &quot;bbgid:BBG005NHP5P9&quot;)</li>
     * <li>ric, prefix with &quot;ric:&quot;, (e.g. &quot;ric:.SPX&quot;)</li>
     * <li>conid, prefix with &quot;conid:&quot;, (e.g. &quot;conid:12087817&quot;)</li>
     * </ul>
     */
    public void reduceCombination(String combination, double ratio);

    /**
     * Set or modify the ExitValue of the specified Position
     */
    public void setExitValue(int positionId, double exitValue);

    /**
     * Remove the ExitValue from the specified Position
     */
    public void removeExitValue(int positionId);

    /**
     * Set the value of the specified Esper variable
     */
    public void setVariableValue(String variableName, String value);

    /**
     * Subscribe to the specified Security.
     *  
     *  
     * @param security
     * <ul>
     * <li>securityId (e.g. 123)</li>
     * <li>symbol (e.g. GOOG)</li>
     * <li>isin, prefix with &quot;isin:&quot;, (e.g. &quot;isin:EU0009654078&quot;)</li>
     * <li>bbgid, prefix with &quot;bbgid:&quot;, (e.g. &quot;bbgid:BBG005NHP5P9&quot;)</li>
     * <li>ric, prefix with &quot;ric:&quot;, (e.g. &quot;ric:.SPX&quot;)</li>
     * <li>conid, prefix with &quot;conid:&quot;, (e.g. &quot;conid:12087817&quot;)</li>
     * </ul>
     * @param feedType The market data feed to use (e.g. IB, BB or DC)
     */
    public void subscribe(String security, String feedType);

    /**
     * Unsubscribe the specified Security
     * @param security
     * <ul>
     * <li>securityId (e.g. 123)</li>
     * <li>symbol (e.g. GOOG)</li>
     * <li>isin, prefix with &quot;isin:&quot;, (e.g. &quot;isin:EU0009654078&quot;)</li>
     * <li>bbgid, prefix with &quot;bbgid:&quot;, (e.g. &quot;bbgid:BBG005NHP5P9&quot;)</li>
     * <li>ric, prefix with &quot;ric:&quot;, (e.g. &quot;ric:.SPX&quot;)</li>
     * <li>conid, prefix with &quot;conid:&quot;, (e.g. &quot;conid:12087817&quot;)</li>
     * </ul>
     * @param feedType The market data feed to use (e.g. IB, BB or DC)
     */
    public void unsubscribe(String security, String feedType);

    /**
     * Request the latest Market Data Events of all subscribed Securities
     */
    public void requestCurrentTicks();

    /**
     * Add or modify a Property
     * @param propertyHolderId Id of the PropertyHolder (e.g. Subscription, Position or Strategy)
     * @param name Name of the Property
     * @param type
     * Type of the value:
     * <ul>
     * <li>INT</li>
     * <li>DOUBLE</li>
     * <li>MONEY</li>
     * <li>TEXT</li>
     * <li>DATE (Format: dd.mm.yyyy hh:mm:ss)</li>
     * <li>BOOLEAN</li>
     * </ul>
     */
    public void addProperty(int propertyHolderId, String name, String value, String type);

    /**
     * Remove the specified property
     * @param propertyHolderId Id of the PropertyHolder (e.g. Subscription, Position or Strategy)
     * @param name name of the property
     */
    public void removeProperty(int propertyHolderId, String name);

    /**
     * creates a Combination of the specified type, securityFamilyId and optional underlying.
     * Returns the id of the newly created combination
     * @param combinationType
     * <ul>
     * <li>VERTICAL_SPREAD</li>
     * <li>COVERED_CALL</li>
     * <li>RATIO_SPREAD</li>
     * <li>STRADDLE</li>
     * <li>STRANGLE</li>
     * <li>BUTTERFLY</li>
     * <li>CALENDAR_SPREAD</li>
     * <li>IRON_CONDOR</li>
     * </ul>
     * @param underlying Underlying Security:
     * <ul>
     * <li>securityId (e.g. 123)</li>
     * <li>symbol (e.g. GOOG)</li>
     * <li>isin, prefix with &quot;isin:&quot;, (e.g. &quot;isin:EU0009654078&quot;)</li>
     * <li>bbgid, prefix with &quot;bbgid:&quot;, (e.g. &quot;bbgid:BBG005NHP5P9&quot;)</li>
     * <li>ric, prefix with &quot;ric:&quot;, (e.g. &quot;ric:.SPX&quot;)</li>
     * <li>conid, prefix with &quot;conid:&quot;, (e.g. &quot;conid:12087817&quot;)</li>
     * </ul>
     */
    public int createCombination(String combinationType, int securityFamilyId, String underlying);

    /**
     * Set the quantity of the specified Component
     * @param combinationId the id of the combination
     * @param component
     * Component:
     * <ul>
     * <li>securityId (e.g. 123)</li>
     * <li>symbol (e.g. GOOG)</li>
     * <li>isin, prefix with &quot;isin:&quot;, (e.g. &quot;isin:EU0009654078&quot;)</li>
     * <li>bbgid, prefix with &quot;bbgid:&quot;, (e.g. &quot;bbgid:BBG005NHP5P9&quot;)</li>
     * <li>ric, prefix with &quot;ric:&quot;, (e.g. &quot;ric:.SPX&quot;)</li>
     * <li>conid, prefix with &quot;conid:&quot;, (e.g. &quot;conid:12087817&quot;)</li>
     * </ul>
     */
    public void setComponentQuantity(int combinationId, String component, long quantitiy);

    /**
     * Remove the specified Component from the specified Combination
     * @param combinationId the id of the combination
     * @param component
     * Component:
     * <ul>
     * <li>securityId (e.g. 123)</li>
     * <li>symbol (e.g. GOOG)</li>
     * <li>isin, prefix with &quot;isin:&quot;, (e.g. &quot;isin:EU0009654078&quot;)</li>
     * <li>bbgid, prefix with &quot;bbgid:&quot;, (e.g. &quot;bbgid:BBG005NHP5P9&quot;)</li>
     * <li>ric, prefix with &quot;ric:&quot;, (e.g. &quot;ric:.SPX&quot;)</li>
     * <li>conid, prefix with &quot;conid:&quot;, (e.g. &quot;conid:12087817&quot;)</li>
     * </ul>
     */
    public void removeComponent(int combinationId, String component);

    /**
     * deletes a Combination
     * @param combinationId the id of the combination
     */
    public void deleteCombination(int combinationId);

    /**
     * Checks if the System is alive
     */
    public void checkIsAlive();

    /**
     * Shutdown this JVM
     */
    public void shutdown();

}
