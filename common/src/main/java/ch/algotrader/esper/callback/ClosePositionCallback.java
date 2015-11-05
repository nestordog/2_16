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
package ch.algotrader.esper.callback;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.entity.PositionVO;
import ch.algotrader.esper.Engine;
import ch.algotrader.util.metric.MetricsUtil;

/**
 * Base Esper Callback Class that will be invoked as soon as a Position on the given Security passed to {@link ch.algotrader.esper.Engine#addClosePositionCallback} has been closed.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public abstract class ClosePositionCallback extends AbstractEngineCallback {

    private static final Logger LOGGER = LogManager.getLogger(ClosePositionCallback.class);

    /**
     * Called by the "ON_CLOSE_POSITION" statement. Should not be invoked directly.
     */
    public void update(PositionVO positionVO) throws Exception {

        // get the statement alias based on all security ids
        String alias = "ON_CLOSE_POSITION_" + positionVO.getSecurityId();

        // undeploy the statement
        Engine engine = getEngine();
        if (engine != null) {
            engine.undeployStatement(alias);
        }

        long startTime = System.nanoTime();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("onClosePosition start {}", positionVO.getSecurityId());
        }
        // call orderCompleted
        onClosePosition(positionVO);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("onClosePosition end {}", positionVO.getSecurityId());
        }

        MetricsUtil.accountEnd("ClosePositionCallback." + positionVO.getStrategyId(), startTime);
    }

    /**
     * Will be exectued by the Esper Engine as soon as a Position on the given Security has been closed.
     * Needs to be overwritten by implementing classes.
     */
    public abstract void onClosePosition(PositionVO positionVO) throws Exception;
}
