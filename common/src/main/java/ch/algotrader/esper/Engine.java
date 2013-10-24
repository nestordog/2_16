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
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface Engine {

    public String getName();

    public void destroy();

    public void deployStatement(String moduleName, String statementName);

    public void deployStatement(String moduleName, String statementName, String alias, Object[] params);

    public void deployStatement(String moduleName, String statementName, String alias, Object[] params, Object callback);

    public void deployAllModules();

    public void deployInitModules();

    public void deployRunModules();

    public void deployModule(String moduleName);

    public boolean isDeployed(final String statementNameRegex);

    public void undeployStatement(String statementName);

    public void restartStatement(String statementName);

    public void undeployModule(String moduleName);

    public void addEventType(String eventTypeName, String eventClassName);

    public void sendEvent(Object obj);

    @SuppressWarnings("rawtypes")
    public List executeQuery(String query);

    public Object executeSingelObjectQuery(String query);

    public Object getLastEvent(String statementName);

    public Object getLastEventProperty(String statementName, String propertyName);

    @SuppressWarnings("rawtypes")
    public List getAllEvents(String statementName);

    @SuppressWarnings("rawtypes")
    public List getAllEventsProperty(String statementName, String property);

    public void setInternalClock(boolean internal);

    public boolean isInternalClock();

    public long getCurrentTime();

    public void initCoordination();

    public void coordinate(CSVInputAdapterSpec csvInputAdapterSpec);

    @SuppressWarnings("rawtypes")
    public void coordinate(Collection collection, String timeStampProperty);

    public void coordinateTicks(Date startDate);

    public void startCoordination();

    public void setVariableValue(String variableName, Object value);

    public void setVariableValueFromString(String variableName, String value);

    public Object getVariableValue(String variableName);

    public void addTradeCallback(Collection<Order> orders, TradeCallback callback);

    public void addFirstTickCallback(Collection<Security> securities, TickCallback callback);

    public void addOpenPositionCallback(int securityId, OpenPositionCallback callback);

    public void addClosePositionCallback(int securityId, ClosePositionCallback callback);
}
