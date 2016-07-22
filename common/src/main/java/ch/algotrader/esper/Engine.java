/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.esper;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.espertech.esperio.CoordinatedAdapter;

import ch.algotrader.entity.PositionVO;
import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.entity.trade.OrderCompletionVO;
import ch.algotrader.entity.trade.OrderStatusVO;

/**
 * Interface representing a CEP Engine.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public interface Engine {

    /**
     * returns the name of this strategy
     */
    public String getStrategyName();

    /**
     * Initializes the specified Engine.
     */
    public void initialize();

    /**
     * Makes services available via variables
     */
    void initServices();

    /**
     * Destroys the specified Engine.
     */
    public void destroy();

    /**
     * returns true if the Engine is destroyed.
     */
    boolean isDestroyed();

    /**
     * Deploys the statement from the given {@code moduleName} with the given {@code statementName} into the specified Engine.
     */
    public void deployStatement(String moduleName, String statementName);

    /**
     * Deploys the statement from the given {@code moduleName} with the given {@code statementName} into the specified Engine.
     * In addition the given {@code alias} and Prepared Statement {@code params} are set on the statement
     */
    public void deployStatement(String moduleName, String statementName, String alias, Object[] params);

    /**
     * Deploys the statement from the given {@code moduleName} with the given {@code statementName} into the specified Engine.
     * In addition the given {@code alias}, Prepared Statement {@code params} and {@code callback} are set on the statement. If
     * the a statement with the same name exists the command is aborted
     */
    public void deployStatement(String moduleName, String statementName, String alias, Object[] params, Object callback);

    /**
     * Deploys the statement from the given {@code moduleName} with the given {@code statementName} into the specified Engine.
     * In addition the given {@code alias}, Prepared Statement {@code params} and {@code callback} are set on the statement. If
     * {@code force} is true the statement will be deployed even if a statement with the same name already exsists.
     */
    public void deployStatement(String moduleName, String statementName, String alias, Object[] params, Object callback, boolean force);

    /**
     * Deploys all modules defined for given Strategy
     */
    public void deployAllModules();

    /**
     * Deploys all init-modules defined for given Strategy
     */
    public void deployInitModules();

    /**
     * Deploys all run-modules defined for given Strategy
     */
    public void deployRunModules();

    /**
     * Deploys the specified module into the specified Engine.
     */
    public void deployModule(String moduleName);

    /**
     * Returns true if the statement by the given {@code statementNameRegex} is deployed.
     *
     * @param statementNameRegex statement name regular expression
     */
    public boolean isDeployed(final String statementNameRegex);

    /**
     * Undeploys all statements from the specified Engine.
     */
    public void undeployAllStatements();

    /**
     * Undeploys the specified statement from the specified Engine.
     */
    public void undeployStatement(String statementName);

    /**
     * Restarts the given Statement
     */
    public void restartStatement(String statementName);

    /**
     * Undeploys the specified module from the specified Engine.
     */
    public void undeployModule(String moduleName);

    /**
     * Adds the given Event Type to the specified Engine.
     * this method can be used if an Event Type is not known at compile time and can therefore not be configured
     * inside an {@code esper-xxx.cfg.xml} file
     */
    public void addEventType(String eventTypeName, String eventClassName);

    /**
     * Sends an Event into this Engine.
     */
    public void sendEvent(Object obj);

    /**
     * Executes an arbitrary EPL query on the Engine.
     */
    @SuppressWarnings("rawtypes")
    public List executeQuery(String query);

    /**
     * Executes an arbitrary EPL query that is supposed to return one single object on the Engine.
     *
     * @param query EPL expression
     * @param attributeName name of the attribute if the query produces a result
     *                      represented by a map of attributes.
     */
    public Object executeSingelObjectQuery(String query, String attributeName);

    /**
     * Executes an arbitrary EPL query that is supposed to return one single object on the Engine.
     */
    public Object executeSingelObjectQuery(String query);

    /**
     * Retrieves the last event currently held by the given statement.
     */
    public Object getLastEvent(String statementName);

    /**
     * Retrieves the last event currently held by the given statement and returns the property by the {@code propertyName}
     */
    public Object getLastEventProperty(String statementName, String propertyName);

    /**
     * Retrieves all events currently held by the given statement.
     */
    @SuppressWarnings("rawtypes")
    public List getAllEvents(String statementName);

    /**
     * Retrieves all events currently held by the given statement and returns the property by the {@code propertyName} for all events.
     */
    @SuppressWarnings("rawtypes")
    public List getAllEventsProperty(String statementName, String property);

    /**
     * Sets the internal clock of the specified Engine
     */
    public void setInternalClock(boolean internal);

    /**
     * Returns true if the clock is set to internal timing
     */
    public boolean isInternalClock();

    /**
     * Returns the current time of the given Engine
     */
    public long getCurrentTimeInMillis();

    /**
     * Returns the current time of the given Engine
     */
    public Date getCurrentTime();

    /**
     * Prepares the given Engine for coordinated input of CSV-Files
     */
    public void initCoordination();

    /**
     * Coordinates the given InputAdapter.
     */
    public void coordinate(CoordinatedAdapter inputAdapter);

    /**
     * Starts coordination for the given Engine.
     */
    public void startCoordination();

    /**
     * sets the value of the specified Variable
     */
    public void setVariableValue(String variableName, Object value);

    /**
     * sets the value of the specified Variable by parsing the given String value.
     */
    public void setVariableValueFromString(String variableName, String value);

    /**
     * Returns the value of the specified Variable
     */
    public Object getVariableValue(String variableName);

    /**
     * Adds a {@link BiConsumer} to the given Engine that will be invoked as soon as all {@code orders} have been
     * fully executed, rejected or cancelled.
     */
    public void addTradeCallback(Collection<String> orders, BiConsumer<String, List<OrderStatusVO>> consumer);

    /**
     * Adds a callback to the given Engine that will throw an exception unless all {@code orders} have been fully
     * executed.
     */
    public void addFullExecutionCallback(Collection<String> orders);

    /**
     * Adds a {@link Consumer} to the given Engine that will be invoked as soon as all {@code orders}
     * have been fully executed or cancelled and fully persisted.
     */
    public void addTradePersistedCallback(Collection<String> orders, Consumer<List<OrderCompletionVO>> consumer);

    /**
     * Adds a {@link BiConsumer} to the given Engine that will be invoked as soon as at least one Tick has arrived
     * for each of the specified {@code securities}
     */
    public void addFirstTickCallback(Collection<Long> securities, BiConsumer<String, List<TickVO>> consumer);

    /**
     * Adds a {@link Consumer} to the given Engine that will be invoked as soon as a new Position
     * on the given Security has been opened.
     */
    public void addOpenPositionCallback(long securityId, Consumer<PositionVO> consumer);

    /**
     * Adds a {@link Consumer} to the given Engine that will be invoked as soon as a Position
     * on the given Security has been closed.
     */
    public void addClosePositionCallback(long securityId, Consumer<PositionVO> consumer);

    /**
     * Adds a {@link Consumer} to the given Engine that will be invoked at the give time.
     * An optional name can be added which will be appended to the statement name.
     */
    public void addTimerCallback(Date dateTime, String name, Consumer<Date> consumer);
}
