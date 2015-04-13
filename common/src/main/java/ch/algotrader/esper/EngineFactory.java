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
package ch.algotrader.esper;

import java.net.URL;
import java.util.Collection;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.ConfigurationEngineDefaults;

import ch.algotrader.config.ConfigParams;
import ch.algotrader.config.DependencyLookup;

/**
 * {@link Engine} factory class.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class EngineFactory {

    private final DependencyLookup dependencyLookup;
    private final ConfigParams configParams;

    public EngineFactory(final DependencyLookup dependencyLookup, final ConfigParams configParams) {
        this.dependencyLookup = dependencyLookup;
        this.configParams = configParams;
    }

    public Engine createServer(final Collection<URL> configResources, final Collection<String> initModules) {
        Configuration configuration = new Configuration();
        for (URL configResource: configResources) {
            configuration.configure(configResource);
        }
        boolean simulation = this.configParams.getBoolean("simulation");
        if (!simulation) {

            ConfigurationEngineDefaults.Threading threading = configuration.getEngineDefaults().getThreading();
            threading.setThreadPoolOutbound(true);
            threading.setThreadPoolOutboundNumThreads(this.configParams.getInteger("misc.outboundThreads"));
        }
        String engineName = "SERVER";
        return new EngineImpl(engineName, engineName, this.dependencyLookup,
                configuration,
                initModules != null ? initModules.toArray(new String[initModules.size()]) : null,
                null,
                this.configParams);
    }

    public Engine createStrategy(
            final String engineName, final String strategyName,
            final Collection<URL> configResources, final Collection<String> initModules, final Collection<String> runModules) {
        Configuration configuration = new Configuration();
        for (URL configResource: configResources) {
            configuration.configure(configResource);
        }
        return new EngineImpl(engineName, strategyName, this.dependencyLookup,
                configuration,
                initModules != null ? initModules.toArray(new String[initModules.size()]) : null,
                runModules != null ? runModules.toArray(new String[runModules.size()]) : null,
                this.configParams);
    }

}
