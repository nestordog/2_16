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
package ch.algotrader.esper.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.StatementAwareUpdateListener;

/**
 * Prints all values including the statement name to the Log by using the {@code toString} method of the event object.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class StatementAwareTestListener implements StatementAwareUpdateListener {

    private static final Logger LOGGER = LogManager.getLogger(StatementAwareTestListener.class);

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement statement, EPServiceProvider epServiceProvider) {
        if (LOGGER.isInfoEnabled()) {
            for (EventBean event : newEvents) {
                LOGGER.info("{} {}", statement.getName(), event.getUnderlying());
            }
        }
    }
}
