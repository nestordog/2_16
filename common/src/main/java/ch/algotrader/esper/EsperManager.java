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

import ch.algotrader.entity.marketData.MarketDataEvent;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.esper.callback.ClosePositionCallback;
import ch.algotrader.esper.callback.OpenPositionCallback;
import ch.algotrader.esper.callback.TickCallback;
import ch.algotrader.esper.callback.TradeCallback;
import ch.algotrader.vo.GenericEventVO;

import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esperio.csv.CSVInputAdapterSpec;

@Deprecated
/**
 * Main class for management of Esper Engine Instances.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class EsperManager {

    /**
     * Initializes a new Esper Engine by the given {@code strategyName}.
     * The following steps are exectued:
     * <ul>
     * <li>{@code corresponding esper-xxx.cfg.xml} files are loaded from the classpath</li>
     * <li>Esper variables are initilized</li>
     * <li>Esper threading is configured</li>
     * <li>The {@link Strategy} itself is configured as an Esper variable {@code engineStrategy}</li>
     * <li>Esper Time is set to zero</li>
     * </ul>
     */
    public static void initServiceProvider(String strategyName) {

        EngineLocator.instance().initEngine(strategyName);
    }

    /**
     * Returns true if the specified Esper Engine is initialized.
     */
    public static boolean isInitialized(String strategyName) {

        return EngineLocator.instance().hasEngine(strategyName);
    }

    /**
     * Destroys the specified Esper Engine.
     */
    public static void destroyServiceProvider(String strategyName) {

        EngineLocator.instance().destroyEngine(strategyName);
    }

    /**
     * Deploys the statement from the given {@code moduleName} with the given {@code statementName} into the specified Esper Engine.
     */
    public static void deployStatement(String strategyName, String moduleName, String statementName) {

        EngineLocator.instance().getEngine(strategyName).deployStatement(moduleName, statementName);
    }

    /**
     * Deploys the statement from the given {@code moduleName} with the given {@code statementName} into the specified Esper Engine.
     * In addition the given {@code alias} and Prepared Statement {@code params} are set on the statement
     */
    public static void deployStatement(String strategyName, String moduleName, String statementName, String alias, Object[] params) {

        EngineLocator.instance().getEngine(strategyName).deployStatement(moduleName, statementName, alias, params);
    }

    /**
     * Deploys the statement from the given {@code moduleName} with the given {@code statementName} into the specified Esper Engine.
     * In addition the given {@code alias}, Prepared Statement {@code params} and {@code callback} are set on the statement.
     */
    public static void deployStatement(String strategyName, String moduleName, String statementName, String alias, Object[] params, Object callback) {

        EngineLocator.instance().getEngine(strategyName).deployStatement(moduleName, statementName, alias, params, callback);
    }

    /**
     * Deploys all modules defined for given Strategy
     */
    public static void deployAllModules(String strategyName) {

        EngineLocator.instance().getEngine(strategyName).deployAllModules();
    }

    /**
     * Deploys all init-modules defined for given Strategy
     */
    public static void deployInitModules(String strategyName) {

        EngineLocator.instance().getEngine(strategyName).deployInitModules();
    }

    /**
     * Deploys all run-modules defined for given Strategy
     */
    public static void deployRunModules(String strategyName) {

        EngineLocator.instance().getEngine(strategyName).deployRunModules();
    }

    /**
     * Deploys the specified module into the specified Esper Engine.
     */
    public static void deployModule(String strategyName, String moduleName) {

        EngineLocator.instance().getEngine(strategyName).deployModule(moduleName);
    }

    /**
     * Returns true if the statement by the given {@code statementNameRegex} is deployed.
     *
     * @param statementNameRegex statement name regular expression
     */
    public static boolean isDeployed(String strategyName, final String statementNameRegex) {

        return EngineLocator.instance().getEngine(strategyName).isDeployed(statementNameRegex);
    }

    /**
     * Undeploys the specified statement from the specified Esper Engine.
     */
    public static void undeployStatement(String strategyName, String statementName) {

        EngineLocator.instance().getEngine(strategyName).undeployStatement(statementName);
    }

    /**
     * Restarts the given Statement
     */
    public static void restartStatement(String strategyName, String statementName) {

        EngineLocator.instance().getEngine(strategyName).restartStatement(statementName);
    }

    /**
     * Undeploys the specified module from the specified Esper Engine.
     */
    public static void undeployModule(String strategyName, String moduleName) {

        EngineLocator.instance().getEngine(strategyName).undeployModule(moduleName);
    }

    /**
     * Adds the given Event Type to the specified Esper Engine.
     * this method can be used if an Event Type is not known at compile time and can therefore not be configured
     * inside an {@code esper-xxx.cfg.xml} file
     */
    public static void addEventType(String strategyName, String eventTypeName, String eventClassName) {

        EngineLocator.instance().getEngine(strategyName).addEventType(eventTypeName, eventClassName);
    }

    /**
     * Sends an Event into the corresponding Esper Engine.
     */
    public static void sendEvent(String strategyName, Object obj) {

        EngineLocator.instance().sendEvent(strategyName, obj);
    }

    /**
     * Sends a MarketDataEvent into the corresponding Esper Engine.
     * In Live-Trading the {@code marketDataTemplate} will be used.
     */
    public static void sendMarketDataEvent(final MarketDataEvent marketDataEvent) {

        EngineLocator.instance().sendMarketDataEvent(marketDataEvent);
    }

    /**
     * Sends a MarketDataEvent into the corresponding Esper Engine.
     * In Live-Trading the {@code genericTemplate} will be used.
     */
    public static void sendGenericEvent(final GenericEventVO genericEvent) {

        EngineLocator.instance().sendGenericEvent(genericEvent);
    }

    /**
     * Executes an arbitrary EPL query on the Esper Engine.
     */
    @SuppressWarnings("rawtypes")
    public static List executeQuery(String strategyName, String query) {

        return EngineLocator.instance().getEngine(strategyName).executeQuery(query);
    }

    /**
     * Executes an arbitrary EPL query that is supposed to return one single object on the Esper Engine.
     */
    public static Object executeSingelObjectQuery(String strategyName, String query) {

        return EngineLocator.instance().getEngine(strategyName).executeSingelObjectQuery(query);
    }

    /**
     * Retrieves the last event currently held by the given statement.
     */
    public static Object getLastEvent(String strategyName, String statementName) {

        return EngineLocator.instance().getEngine(strategyName).getLastEvent(statementName);
    }

    /**
     * Retrieves the last event currently held by the given statement and returns the property by the {@code propertyName}
     */
    public static Object getLastEventProperty(String strategyName, String statementName, String propertyName) {

        return EngineLocator.instance().getEngine(strategyName).getLastEventProperty(statementName, propertyName);
    }

    /**
     * Retrieves all events currently held by the given statement.
     */
    @SuppressWarnings("rawtypes")
    public static List getAllEvents(String strategyName, String statementName) {

        return EngineLocator.instance().getEngine(strategyName).getAllEvents(statementName);
    }

    /**
     * Retrieves all events currently held by the given statement and returns the property by the {@code propertyName} for all events.
     */
    @SuppressWarnings("rawtypes")
    public static List getAllEventsProperty(String strategyName, String statementName, String property) {

        return EngineLocator.instance().getEngine(strategyName).getAllEventsProperty(statementName, property);
    }

    /**
     * Sets the internal clock of the specified Esper Engine
     */
    public static void setInternalClock(String strategyName, boolean internal) {

        EngineLocator.instance().getEngine(strategyName).setInternalClock(internal);
    }

    /**
     * Returns true if the specified Esper Engine uses internal clock
     */
    public static boolean isInternalClock(String strategyName) {

        return EngineLocator.instance().getEngine(strategyName).isInternalClock();
    }

    /**
     * Sends a {@link com.espertech.esper.client.time.CurrentTimeEvent} to all local Esper Engines
     */
    public static void setCurrentTime(CurrentTimeEvent currentTimeEvent) {

        EngineLocator.instance().sendEventToAllEngines(currentTimeEvent);
    }

    /**
     * Retruns the current time of the given Esper Engine
     */
    public static long getCurrentTime(String strategyName) {

        return EngineLocator.instance().getEngine(strategyName).getCurrentTime();
    }

    /**
     * Prepares the given Esper Engine for coordinated input of CSV-Files
     */
    public static void initCoordination(String strategyName) {

        EngineLocator.instance().getEngine(strategyName).initCoordination();
    }

    /**
     * Queues the specified {@code csvInputAdapterSpec} for coordination with the given Esper Engine.
     */
    public static void coordinate(String strategyName, CSVInputAdapterSpec csvInputAdapterSpec) {

        EngineLocator.instance().getEngine(strategyName).coordinate(csvInputAdapterSpec);
    }

    /**
     * Queues the specified {@code collection} for coordination with the given Esper Engine.
     * The property by the name of {@code timeStampProperty} is used to identify the current time.
     */
    @SuppressWarnings({ "rawtypes" })
    public static void coordinate(String strategyName, Collection collection, String timeStampProperty) {

        EngineLocator.instance().getEngine(strategyName).coordinate(collection, timeStampProperty);
    }

    /**
     * Queues subscribed Ticks for coordination with the given Esper Engine.
     */
    public static void coordinateTicks(String strategyName, Date startDate) {

        EngineLocator.instance().getEngine(strategyName).coordinateTicks(startDate);
    }

    /**
     * Starts coordination for the given Esper Engine.
     */
    public static void startCoordination(String strategyName) {

        EngineLocator.instance().getEngine(strategyName).startCoordination();
    }

    /**
     * sets the value of the specified Esper Variable
     */
    public static void setVariableValue(String strategyName, String variableName, Object value) {

        EngineLocator.instance().getEngine(strategyName).setVariableValue(variableName, value);
    }

    /**
     * sets the value of the specified Esper Variable by parsing the given String value.
     */
    public static void setVariableValueFromString(String strategyName, String variableName, String value) {

        EngineLocator.instance().getEngine(strategyName).setVariableValueFromString(variableName, value);
    }

    /**
     * Returns the value of the specified Esper Variable
     */
    public static Object getVariableValue(String strategyName, String variableName) {

        return EngineLocator.instance().getEngine(strategyName).getVariableValue(variableName);
    }

    /**
     * Adds a {@link TradeCallback} to the given Esper Engine that will be invoked as soon as all {@code orders} have been
     * fully exectured or cancelled.
     */
    public static void addTradeCallback(String strategyName, Collection<Order> orders, TradeCallback callback) {

        EngineLocator.instance().getEngine(strategyName).addTradeCallback(orders, callback);
    }

    /**
     * Adds a {@link TickCallback} to the given Esper Engine that will be invoked as soon as at least one Tick has arrived
     * for each of the specified {@code securities}
     */
    public static void addFirstTickCallback(String strategyName, Collection<Security> securities, TickCallback callback) {

        EngineLocator.instance().getEngine(strategyName).addFirstTickCallback(securities, callback);
    }

    /**
     * Adds a {@link OpenPositionCallback} to the given Esper Engine that will be invoked as soon as a new Position
     * on the given Security has been opened.
     */
    public static void addOpenPositionCallback(String strategyName, int securityId, OpenPositionCallback callback) {

        EngineLocator.instance().getEngine(strategyName).addOpenPositionCallback(securityId, callback);
    }

    /**
     * Adds a {@link OpenPositionCallback} to the given Esper Engine that will be invoked as soon as a Position
     * on the given Security has been closed.
     */
    public static void addClosePositionCallback(String strategyName, int securityId, ClosePositionCallback callback) {

        EngineLocator.instance().getEngine(strategyName).addClosePositionCallback(securityId, callback);
    }

    /**
     * Prints all statement metrics.
     */
    public static void logStatementMetrics() {

        EngineLocator.instance().logStatementMetrics();
    }

    /**
     * Resets all statement metrics.
     */
    public static void resetStatementMetrics() {

        EngineLocator.instance().resetStatementMetrics();
    }
}
