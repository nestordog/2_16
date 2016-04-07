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

import ch.algotrader.entity.marketData.MarketDataEventVO;
import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.util.metric.MetricsUtil;

/**
 * Base Esper Callback Class that will be invoked as soon as at least one Tick has arrived for each of the {@code securities}
 * passed to {@link BiConsumer}
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
final class TickCallback {

    private static final Logger LOGGER = LogManager.getLogger(TickCallback.class);

    private final Engine engine;
    private final String alias;
    private final BiConsumer<String, List<TickVO>> consumer;

    TickCallback(final Engine engine, final String alias, final BiConsumer<String, List<TickVO>> consumer) {
        this.engine = engine;
        this.alias = alias;
        this.consumer = consumer;
    }

    /**
     * Called by the "ON_FIRST_TICK" statement. Should not be invoked directly.
     */
    public void update(String strategyName, TickVO[] ticks) throws Exception {

        List<TickVO> tickList = Arrays.asList(ticks);

        // undeploy the statement
        if (this.engine != null && this.alias != null) {
            this.engine.undeployStatement(this.alias);
        }

        long startTime = System.nanoTime();
        if (LOGGER.isDebugEnabled()) {
            List<Long> sortedSecurityIds = tickList.stream()
                    .map(MarketDataEventVO::getSecurityId)
                    .collect(Collectors.toList());
            LOGGER.debug("onFirstTick start {}", sortedSecurityIds);
        }

        if (this.consumer != null) {
            this.consumer.accept(strategyName, tickList);
        }

        if (LOGGER.isDebugEnabled()) {
            List<Long> sortedSecurityIds = tickList.stream()
                    .map(MarketDataEventVO::getSecurityId)
                    .collect(Collectors.toList());
            LOGGER.debug("onFirstTick end {}", sortedSecurityIds);
        }

        MetricsUtil.accountEnd("TickCallback." + strategyName, startTime);
    }

}
