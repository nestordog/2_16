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
package ch.algotrader.esper;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.esper.callback.ClosePositionCallback;
import ch.algotrader.esper.callback.OpenPositionCallback;
import ch.algotrader.esper.callback.TickCallback;
import ch.algotrader.esper.callback.TradeCallback;

import com.espertech.esperio.csv.CSVInputAdapterSpec;

/**
 * Interface representing a CEP Engine.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface Engine {

    /**
     * returns the name of this strategy
     */
    public String getName();

    /**
     * Destroys the specified Engine.
     */
    public void destroy();

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
     * In addition the given {@code alias}, Prepared Statement {@code params} and {@code callback} are set on the statement.
     */
    public void deployStatement(String moduleName, String statementName, String alias, Object[] params, Object callback);

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
     * Retruns the current time of the given Engine
     */
    public long getCurrentTime();

    /**
     * Prepares the given Engine for coordinated input of CSV-Files
     */
    public void initCoordination();

    /**
     * Queues the specified {@code csvInputAdapterSpec} for coordination with the given Engine.
     */
    public void coordinate(CSVInputAdapterSpec csvInputAdapterSpec);

    /**
     * Queues the specified {@code collection} for coordination with the given Engine.
     * The property by the name of {@code timeStampProperty} is used to identify the current time.
     */
    @SuppressWarnings("rawtypes")
    public void coordinate(Collection collection, String timeStampProperty);

    /**
     * Queues subscribed Ticks for coordination with the given Engine.
     */
    public void coordinateTicks(Date startDate);

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
     * Adds a {@link TradeCallback} to the given Engine that will be invoked as soon as all {@code orders} have been
     * fully exectured or cancelled.
     */
    public void addTradeCallback(Collection<Order> orders, TradeCallback callback);

    /**
     * Adds a {@link TickCallback} to the given Engine that will be invoked as soon as at least one Tick has arrived
     * for each of the specified {@code securities}
     */
    public void addFirstTickCallback(Collection<Security> securities, TickCallback callback);

    /**
     * Adds a {@link OpenPositionCallback} to the given Engine that will be invoked as soon as a new Position
     * on the given Security has been opened.
     */
    public void addOpenPositionCallback(int securityId, OpenPositionCallback callback);

    /**
     * Adds a {@link OpenPositionCallback} to the given Engine that will be invoked as soon as a Position
     * on the given Security has been closed.
     */
    public void addClosePositionCallback(int securityId, ClosePositionCallback callback);
}
