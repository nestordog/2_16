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
package ch.algotrader.esper.callback;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.entity.trade.OrderCompletionVO;
import ch.algotrader.esper.Engine;
import ch.algotrader.util.metric.MetricsUtil;

/**
 * Base Esper Callback Class that will be invoked as soon as all {@code orders} passed to {@link Engine#addTradePersistedCallback(
 *  java.util.Collection, TradePersistedCallback)} have been fully executed or cancelled and fully persisted.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class TradePersistedCallback extends AbstractEngineCallback {

    private static final Logger LOGGER = LogManager.getLogger(TradePersistedCallback.class);

    /**
     * Called by the "ON_TRADE_PERSISTED" statement. Should not be invoked directly.
     */
    public void update(String strategyName, OrderCompletionVO[] orderCompletions) throws Exception {

        // get the securityIds sorted ascending
        List<OrderCompletionVO> orderCompletionList = Arrays.asList(orderCompletions);
        Set<String> orderIntIds = orderCompletionList.stream()
                .map(OrderCompletionVO::getOrderIntId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // get the statement alias based on all security ids
        String alias = "ON_TRADE_PERSISTED_" + StringUtils.join(orderIntIds, "_");

        // undeploy the statement
        Engine engine = getEngine();
        if (engine != null) {
            engine.undeployStatement(alias);
        }

        long startTime = System.nanoTime();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("onTradePersisted start {} {}", orderIntIds, strategyName);
        }
        // call onTradePersisted
        onTradePersisted(orderCompletionList);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("onTradePersisted end {} {}", orderIntIds, strategyName);
        }
        MetricsUtil.accountEnd("TradePersisted." + strategyName, startTime);
    }

    /**
     * Will be executed by the Esper Engine as soon as all {@code orders} have been fully executed or cancelled
     * and fully persisted. Needs to be overwritten by implementing classes.
     */
    public abstract void onTradePersisted(List<OrderCompletionVO> orderCompletionList) throws Exception;
}
