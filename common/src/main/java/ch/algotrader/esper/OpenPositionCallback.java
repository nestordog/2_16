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

import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.entity.PositionVO;
import ch.algotrader.util.metric.MetricsUtil;

/**
 * Base Esper Callback Class that will be invoked as soon as a new Position on the given Security passed
 * to {@link Consumer} has been opened.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
final class OpenPositionCallback {

    private static final Logger LOGGER = LogManager.getLogger(OpenPositionCallback.class);

    private final Engine engine;
    private final String alias;
    private final Consumer<PositionVO> consumer;

    OpenPositionCallback(final Engine engine, final String alias, final Consumer<PositionVO> consumer) {

        this.engine = engine;
        this.alias = alias;
        this.consumer = consumer;
    }

    /**
     * Called by the "ON_OPEN_POSITION" statement. Should not be invoked directly.
     */
    public void update(PositionVO positionVO) throws Exception {

        // undeploy the statement
        if (this.engine != null && this.alias != null) {
            this.engine.undeployStatement(this.alias);
        }

        long startTime = System.nanoTime();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("onOpenPosition start {}", positionVO.getSecurityId());
        }
        if (this.consumer != null) {
            this.consumer.accept(positionVO);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("onOpenPosition end {}", positionVO.getSecurityId());
        }
        MetricsUtil.accountEnd("OpenPositionCallback." + positionVO.getStrategyId(), startTime);
    }

}
