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
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.entity.trade.OrderStatusVO;
import ch.algotrader.util.metric.MetricsUtil;

/**
 * Base Esper Callback Class that will be invoked as soon as all {@code orders} passed to {@link BiConsumer}
 * have been fully executed or cancelled.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
final class TradeCallback {

    private static final Logger LOGGER = LogManager.getLogger(TradeCallback.class);

    private final Engine engine;
    private final String alias;
    private final BiConsumer<String, List<OrderStatusVO>> consumer;
    private final boolean expectFullExecution;


    TradeCallback(final Engine engine, final String alias, final boolean expectFullExecution, final BiConsumer<String, List<OrderStatusVO>> consumer) {
        this.engine = engine;
        this.alias = alias;
        this.expectFullExecution = expectFullExecution;
        this.consumer = consumer;
    }

    /**
     * Called by the "ON_TRADE_COMPLETED" statement. Should not be invoked directly.
     */
    public void update(String strategyName, OrderStatusVO[] orderStati) throws Exception {

        // check full execution if needed
        if (this.expectFullExecution) {
            for (OrderStatusVO orderStatus : orderStati) {
                if (orderStatus.getRemainingQuantity() > 0) {
                    LOGGER.error("order {} has not been fully executed, filledQty: {} remainingQty: {}", orderStatus.getIntId(), orderStatus.getFilledQuantity(),
                            orderStatus.getRemainingQuantity());
                }
            }
        }

        // get the securityIds sorted ascending
        List<OrderStatusVO> orderStatusList = Arrays.asList(orderStati);

        // undeploy the statement
        if (this.engine != null && this.alias != null) {
            this.engine.undeployStatement(this.alias);
        }

        long startTime = System.nanoTime();
        if (LOGGER.isDebugEnabled()) {
            List<String> orderIntIds = orderStatusList.stream()
                    .map(OrderStatusVO::getIntId)
                    .collect(Collectors.toList());
            LOGGER.debug("onTradeCompleted start {} {}", orderIntIds, strategyName);
        }

        if (this.consumer != null) {
            this.consumer.accept(strategyName, orderStatusList);
        }

        if (LOGGER.isDebugEnabled()) {
            List<String> orderIntIds = orderStatusList.stream()
                    .map(OrderStatusVO::getIntId)
                    .collect(Collectors.toList());
            LOGGER.debug("onTradeCompleted end {} {}", orderIntIds, strategyName);
        }
        MetricsUtil.accountEnd("TradeCallback." + strategyName, startTime);
    }

}
