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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.esper.Engine;
import ch.algotrader.util.metric.MetricsUtil;
import ch.algotrader.vo.OpenPositionVO;

/**
 * Base Esper Callback Class that will be invoked as soon as a new Position on the given Security passed to {@link ch.algotrader.esper.Engine#addOpenPositionCallback} has been opened.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class OpenPositionCallback extends AbstractEngineCallback {

    private static final Logger LOGGER = LogManager.getLogger(OpenPositionCallback.class);

    /**
     * Called by the "ON_OPEN_POSITION" statement. Should not be invoked directly.
     */
    public void update(OpenPositionVO positionVO) throws Exception {

        // get the statement alias based on all security ids
        String alias = "ON_OPEN_POSITION_" + positionVO.getSecurityId();

        // undeploy the statement
        Engine engine = getEngine();
        if (engine != null) {
            engine.undeployStatement(alias);
        }

        long startTime = System.nanoTime();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("onOpenPosition start {}", positionVO.getSecurityId());
        }
        // call orderCompleted
        onOpenPosition(positionVO);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("onOpenPosition end {}", positionVO.getSecurityId());
        }
        MetricsUtil.accountEnd("OpenPositionCallback." + positionVO.getStrategy(), startTime);
    }

    /**
     * Will be exectued by the Esper Engine as soon as a new Position on the given Security has been opened.
     * Needs to be overwritten by implementing classes.
     */
    public abstract void onOpenPosition(OpenPositionVO positionVO) throws Exception;
}
