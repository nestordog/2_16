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

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.entity.trade.OrderCompletionVO;
import ch.algotrader.util.metric.MetricsUtil;

/**
 * Base Esper Callback Class that will be invoked as soon as all {@code orders} passed to {@link Engine#addTradePersistedCallback(
 *  java.util.Collection, TradePersistedCallback)} have been fully executed or cancelled and fully persisted.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
final class TradePersistedCallback {

    private static final Logger LOGGER = LogManager.getLogger(TradePersistedCallback.class);

    private final Engine engine;
    private final String alias;
    private final Consumer<List<OrderCompletionVO>> consumer;

    TradePersistedCallback(final Engine engine, final String alias, final Consumer<List<OrderCompletionVO>> consumer) {

        this.engine = engine;
        this.alias = alias;
        this.consumer = consumer;
    }

    /**
     * Called by the "ON_TRADE_PERSISTED" statement. Should not be invoked directly.
     */
    public void update(String strategyName, OrderCompletionVO[] orderCompletions) throws Exception {

        // get the securityIds sorted ascending
        List<OrderCompletionVO> orderCompletionList = Arrays.asList(orderCompletions);

        // undeploy the statement
        if (this.engine != null && this.alias != null) {
            this.engine.undeployStatement(this.alias);
        }

        long startTime = System.nanoTime();
        if (LOGGER.isDebugEnabled()) {
            List<String> orderIntIds = orderCompletionList.stream()
                    .map(OrderCompletionVO::getOrderIntId)
                    .collect(Collectors.toList());
            LOGGER.debug("onTradePersisted start {} {}", orderIntIds, strategyName);
        }

        if (this.consumer != null) {
            this.consumer.accept(orderCompletionList);
        }

        if (LOGGER.isDebugEnabled()) {
            List<String> orderIntIds = orderCompletionList.stream()
                    .map(OrderCompletionVO::getOrderIntId)
                    .collect(Collectors.toList());
            LOGGER.debug("onTradePersisted end {} {}", orderIntIds, strategyName);
        }
        MetricsUtil.accountEnd("TradePersisted." + strategyName, startTime);
    }

}
