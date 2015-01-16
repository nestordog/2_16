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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.ConfigurationEngineDefaults.Threading;
import com.espertech.esper.client.ConfigurationVariable;
import com.espertech.esper.util.JavaClassHelper;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.ConfigParams;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.util.MyLogger;

/**
 * {@link ch.algotrader.esper.Engine} factory;
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class EngineFactory {

    private static final Logger LOGGER = MyLogger.getLogger(EngineImpl.class.getName());

    private final DependencyLookup dependencyLookup;

    public EngineFactory(final DependencyLookup dependencyLookup) {

        super();
        Validate.notNull(dependencyLookup, "DependencyLookup is null");
        this.dependencyLookup = dependencyLookup;
    }

    /**
     * Creates a new Engine with the given {@code engineName}.
     * The following steps are executed:
     * <ul>
     * <li>{@code corresponding esper-xxx.cfg.xml} files are loaded from the classpath</li>
     * <li>Esper variables are initilized</li>
     * <li>Esper threading is configured</li>
     * <li>Esper Time is set to zero</li>
     * </ul>
     */
    public Engine createEngine(final String engineName, final String[] initModules, final String[] runModules, final ConfigParams configParams, final CommonConfig commonConfig) throws IOException {

        Validate.notNull(configParams, "ConfigParams is null");
        Validate.notNull(commonConfig, "CommonConfig is null");

        Configuration configuration = new Configuration();

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath*:/META-INF/esper-**.cfg.xml");
        for (Resource resource : resources) {
            if (StrategyImpl.SERVER.equals(engineName)) {

                // for AlgoTrader Server only load esper-common.cfg.xml and esper-core.cfg.xml
                if (resource.toString().contains("esper-common.cfg.xml") || resource.toString().contains("esper-core.cfg.xml")) {
                    configuration.configure(resource.getURL());
                }
            } else {

                // for Strategies to not load esper-core.cfg.xml
                if (!resource.toString().contains("esper-core.cfg.xml")) {
                    configuration.configure(resource.getURL());
                }
            }
        }

        initVariables(configuration, configParams);

        // outbound threading for ALGOTRADER SERVER
        if (StrategyImpl.SERVER.equals(engineName) && !commonConfig.isSimulation()) {

            Threading threading = configuration.getEngineDefaults().getThreading();

            threading.setThreadPoolOutbound(true);
            threading.setThreadPoolOutboundNumThreads(configParams.getInteger("misc.outboundThreads"));
        }

        configuration.getVariables().get("strategyName").setInitializationValue(engineName);
        return new EngineImpl(engineName, this.dependencyLookup, configuration, initModules, runModules, configParams, commonConfig);
    }

    private void initVariables(final Configuration configuration, final ConfigParams configParams) {

        Map<String, ConfigurationVariable> variables = configuration.getVariables();
        for (Map.Entry<String, ConfigurationVariable> entry : variables.entrySet()) {
            String variableName = entry.getKey().replace("_", ".");
            String value = configParams.getString(variableName);
            if (value != null) {
                String type = entry.getValue().getType();
                try {
                    Class clazz = Class.forName(type);
                    Object typedObj;
                    if (clazz.isEnum()) {
                        typedObj = Enum.valueOf(clazz, value);
                    } else if (clazz == BigDecimal.class) {
                        typedObj = new BigDecimal(value);
                    } else {
                        typedObj = JavaClassHelper.parse(clazz, value);
                    }
                    entry.getValue().setInitializationValue(typedObj);
                } catch (ClassNotFoundException e) {
                    if (LOGGER.isEnabledFor(Level.WARN)) {
                        LOGGER.warn("Unknown variable type: " + type);
                    }
                }
            }
        }
    }

}
