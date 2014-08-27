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
package ch.algotrader.esper;

import java.util.Collection;
import java.util.List;

import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.esper.callback.ClosePositionCallback;
import ch.algotrader.esper.callback.OpenPositionCallback;
import ch.algotrader.esper.callback.TickCallback;
import ch.algotrader.esper.callback.TradeCallback;

import com.espertech.esperio.CoordinatedAdapter;

/**
 * Abstract implementation of an {@link Engine}
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class AbstractEngine implements Engine {

    protected String engineName;
    protected boolean internalClock;

    @Override
    public String getName() {

        return this.engineName;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void deployStatement(String moduleName, String statementName) {

    }

    @Override
    public void deployStatement(String moduleName, String statementName, String alias, Object[] params) {

    }

    @Override
    public void deployStatement(String moduleName, String statementName, String alias, Object[] params, Object callback) {

    }

    @Override
    public void deployAllModules() {

    }

    @Override
    public void deployInitModules() {

    }

    @Override
    public void deployRunModules() {

    }

    @Override
    public void deployModule(String moduleName) {

    }

    @Override
    public boolean isDeployed(String statementNameRegex) {

        return false;
    }

    @Override
    public void undeployStatement(String statementName) {

    }

    @Override
    public void restartStatement(String statementName) {

    }

    @Override
    public void undeployModule(String moduleName) {

    }

    @Override
    public void addEventType(String eventTypeName, String eventClassName) {

    }

    @Override
    public void sendEvent(Object obj) {

    }

    @SuppressWarnings("rawtypes")
    @Override
    public List executeQuery(String query) {

        return null;
    }

    @Override
    public Object executeSingelObjectQuery(String query) {

        return null;
    }

    @Override
    public Object getLastEvent(String statementName) {

        return null;
    }

    @Override
    public Object getLastEventProperty(String statementName, String propertyName) {

        return null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List getAllEvents(String statementName) {

        return null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List getAllEventsProperty(String statementName, String property) {

        return null;
    }

    @Override
    public void setInternalClock(boolean internalClock) {

        this.internalClock = internalClock;
    }

    @Override
    public boolean isInternalClock() {

        return this.internalClock;
    }

    @Override
    public long getCurrentTime() {

        return 0;
    }

    @Override
    public void initCoordination() {

    }

    @Override
    public void coordinate(CoordinatedAdapter inputAdapter) {

    }

    @Override
    public void startCoordination() {

    }

    @Override
    public void setVariableValue(String variableName, Object value) {

    }

    @Override
    public void setVariableValueFromString(String variableName, String value) {

    }

    @Override
    public Object getVariableValue(String variableName) {

        return null;
    }

    @Override
    public void addTradeCallback(Collection<Order> orders, TradeCallback callback) {

    }

    @Override
    public void addFirstTickCallback(Collection<Security> securities, TickCallback callback) {

    }

    @Override
    public void addOpenPositionCallback(int securityId, OpenPositionCallback callback) {

    }

    @Override
    public void addClosePositionCallback(int securityId, ClosePositionCallback callback) {

    }

    @Override
    public String toString() {

        return this.engineName;
    }
}
