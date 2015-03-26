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
package ch.algotrader.esper.subscriber;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.ServiceLocator;
import ch.algotrader.vo.GenericEventVO;

/**
 * Esper event subscriber for {@link ch.algotrader.event.dispatch.EventDispatcher#broadcastLocal(Object)}.
 */
public class PropagateGenericEventSubscriber {

    private static final Logger LOGGER = LogManager.getLogger(PropagateGenericEventSubscriber.class.getName());

    public void update(final GenericEventVO genericEvent) {

        // security.toString & marketDataEvent.toString is expensive, so only log if debug is enabled
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(genericEvent);
        }

        ServiceLocator.instance().getEventDispatcher().broadcastLocal(genericEvent);
    }
}
