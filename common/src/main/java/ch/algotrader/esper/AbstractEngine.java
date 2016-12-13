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
 * Abstract implementation of an {@link Engine}
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public abstract class AbstractEngine implements Engine {

    private final String strategyName;

    private volatile boolean internalClock;

    protected AbstractEngine(final String strategyName) {
        this.strategyName = strategyName;
    }

    @Override
    public String getStrategyName() {

        return this.strategyName;
    }

    @Override
    public void initialize() {
    }

    @Override
    public void initServices() {
    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean isDestroyed() {
        return false;
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
    public void deployStatement(String moduleName, String statementName, String alias, Object[] params, Object callback, boolean force) {

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
    public void undeployAllStatements() {

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
    public Object executeSingelObjectQuery(String query, String objectName) {

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
    public long getCurrentTimeInMillis() {

        return 0;
    }

    @Override
    public Date getCurrentTime() {

        return new Date(0);
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
    public void addTradeCallback(Collection<String> orders, BiConsumer<String, List<OrderStatusVO>> consumer) {

    }

    @Override
    public void addFullExecutionCallback(Collection<String> orders) {

    }

    @Override
    public void addTradePersistedCallback(Collection<String> orders, Consumer<List<OrderCompletionVO>> consumer) {

    }

    @Override
    public void addFirstTickCallback(Collection<Long> securities, BiConsumer<String, List<TickVO>> consumer) {

    }

    @Override
    public void addOpenPositionCallback(long securityId, Consumer<PositionVO> consumer) {

    }

    @Override
    public void addClosePositionCallback(long securityId, Consumer<PositionVO> consumer) {

    }

    @Override
    public void addTimerCallback(Date dateTime, String name, Consumer<Date> consumer) {

    }

    @Override
    public String toString() {

        return this.strategyName;
    }
}
