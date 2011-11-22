package com.algoTrader.esper.io.jms;

import com.algoTrader.ServiceLocator;
import com.algoTrader.util.ConfigurationUtil;
import com.espertech.esper.adapter.Adapter;
import com.espertech.esper.adapter.AdapterSPI;
import com.espertech.esper.adapter.AdapterState;
import com.espertech.esper.plugin.PluginLoader;
import com.espertech.esper.plugin.PluginLoaderInitContext;

/**
 * Loader for Spring-configured input and output adapters.
 */
public class SpringContextLoader implements PluginLoader {

    private static final String INPUT_ADAPTER_BEAN_NAME = "inputAdapterBeanName";
    private static boolean simulation = ConfigurationUtil.getBaseConfig().getBoolean("simulation");

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
        this.adapter = (Adapter) ServiceLocator.commonInstance().getService(beanName);
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
