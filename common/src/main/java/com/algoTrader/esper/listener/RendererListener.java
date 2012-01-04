package com.algoTrader.esper.listener;

import org.apache.log4j.Logger;

import com.algoTrader.util.MyLogger;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.StatementAwareUpdateListener;
import com.espertech.esper.client.util.XMLEventRenderer;

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
