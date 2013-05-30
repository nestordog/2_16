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
package com.algoTrader.esper.io.jms;

import com.algoTrader.ServiceLocator;
import com.espertech.esper.adapter.Adapter;
import com.espertech.esper.adapter.AdapterSPI;
import com.espertech.esper.adapter.AdapterState;
import com.espertech.esper.plugin.PluginLoader;
import com.espertech.esper.plugin.PluginLoaderInitContext;

/**
 * Loader for Spring-configured input and output adapters.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class SpringContextLoader implements PluginLoader {

    private static final String INPUT_ADAPTER_BEAN_NAME = "inputAdapterBeanName";
    private static boolean simulation = ServiceLocator.instance().getConfiguration().getSimulation();

    private Adapter adapter;

    /**
     * Default Ctor needed for reflection instantiation.
     */
    public SpringContextLoader() {
    }

    @Override
    public void destroy() {

        if (simulation)
            return;

        if (this.adapter.getState() == AdapterState.STARTED) {
            this.adapter.stop();
        }
        if ((this.adapter.getState() == AdapterState.OPENED) || (this.adapter.getState() == AdapterState.PAUSED)) {
            this.adapter.destroy();
        }
    }

    @Override
    public void init(PluginLoaderInitContext context) {

        if (simulation)
            return;

        String beanName = context.getProperties().getProperty(INPUT_ADAPTER_BEAN_NAME);

        if (!ServiceLocator.instance().containsService(beanName)) {
            return;
        }

        this.adapter = ServiceLocator.instance().getService(beanName, Adapter.class);
        if (this.adapter instanceof AdapterSPI) {
            AdapterSPI spi = (AdapterSPI) this.adapter;
            spi.setEPServiceProvider(context.getEpServiceProvider());
        }
        this.adapter.start();
    }

    @Override
    public void postInitialize() {
        // no action required
    }
}
