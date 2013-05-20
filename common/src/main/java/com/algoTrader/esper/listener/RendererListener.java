/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package com.algoTrader.esper.listener;

import org.apache.log4j.Logger;

import com.algoTrader.util.MyLogger;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.StatementAwareUpdateListener;
import com.espertech.esper.client.util.XMLEventRenderer;

/**
 * Prints all values to the Log in XML format.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class RendererListener implements StatementAwareUpdateListener {

    private static Logger logger = MyLogger.getLogger(RendererListener.class.getName());

    private static XMLEventRenderer renderer;

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement statement, EPServiceProvider epServiceProvider) {

        // get the renderer
        if (renderer == null) {
            renderer = epServiceProvider.getEPRuntime().getEventRenderer().getXMLRenderer(statement.getEventType());
        }

        // print the values
        for (EventBean event : newEvents) {
            String eventText = renderer.render(statement.getEventType().getName(), event);
            logger.info("\n" + eventText);
        }
    }
}
