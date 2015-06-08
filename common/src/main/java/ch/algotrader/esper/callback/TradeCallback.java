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
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.esper.Engine;
import ch.algotrader.util.metric.MetricsUtil;

/**
 * Base Esper Callback Class that will be invoked as soon as all {@code orders} passed to {@link ch.algotrader.esper.Engine#addTradeCallback} have been
 * fully executed or cancelled.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class TradeCallback extends AbstractEngineCallback {

    private static final Logger LOGGER = LogManager.getLogger(TradeCallback.class);

    private final boolean expectFullExecution;

    /**
     * param expectFullExecution if set to true an exception will be thrown unless all {@code orders} have been fully executed.
     */
    public TradeCallback(boolean expectFullExecution) {

        this.expectFullExecution = expectFullExecution;
    }

    /**
     * Called by the "ON_TRADE_COMPLETED" statement. Should not be invoked directly.
     */
    public void update(String strategyName, OrderStatus[] orderStati) throws Exception {

        // check full execution if needed
        if (this.expectFullExecution) {
            for (OrderStatus orderStatus : orderStati) {
                if (orderStatus.getRemainingQuantity() > 0) {
                    LOGGER.error("order on {} has not been fully executed, filledQty: {} remainingQty: {}", orderStatus.getOrder().getSecurity(), orderStatus.getFilledQuantity(),
                            orderStatus.getRemainingQuantity());
                }
            }
        }

        // get the securityIds sorted ascending
        List<OrderStatus> orderStatusList = Arrays.asList(orderStati);
        TreeSet<Long> sortedSecurityIds = new TreeSet<>(CollectionUtils.collect(orderStatusList, order -> {
            return order.getOrder().getSecurity().getId();
        }));

        String owningStrategyName = orderStati[0].getOrder().getStrategy().getName();

        // get the statement alias based on all security ids
        String alias = "ON_TRADE_COMPLETED_" + StringUtils.join(sortedSecurityIds, "_") + "_" + owningStrategyName;

        // undeploy the statement
        Engine engine = getEngine();
        if (engine != null) {
            engine.undeployStatement(alias);
        }

        long startTime = System.nanoTime();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("onTradeCompleted start {} {}", sortedSecurityIds, owningStrategyName);
        }
        // call orderCompleted
        onTradeCompleted(orderStatusList);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("onTradeCompleted end {} {}", sortedSecurityIds, owningStrategyName);
        }
        MetricsUtil.accountEnd("TradeCallback." + strategyName, startTime);
    }

    /**
     * Will be exectued by the Esper Engine as soon as all {@code orders} have been fully exectured or cancelled.
     * Needs to be overwritten by implementing classes.
     */
    public abstract void onTradeCompleted(List<OrderStatus> orderStatus) throws Exception;
}
