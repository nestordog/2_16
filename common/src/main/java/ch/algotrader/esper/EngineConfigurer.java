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

import java.math.BigDecimal;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.ConfigurationEngineDefaults.Threading;
import com.espertech.esper.client.ConfigurationVariable;
import com.espertech.esper.util.JavaClassHelper;

import ch.algotrader.config.ConfigParams;

/**
 * Utility class that initializes variables defined in {@link com.espertech.esper.client.Configuration}
 * with values from {@link ch.algotrader.config.ConfigParams}.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class EngineConfigurer {

    public void configure(final String strategyName, final Configuration configuration, final ConfigParams configParams) {

        Validate.notEmpty(strategyName, "Name is null");
        Validate.notNull(configuration, "Configuration is null");
        Validate.notNull(configParams, "ConfigParams is null");

        initVariables(configuration, configParams);
        // outbound threading for ALGOTRADER SERVER
        boolean simulation = configParams.getBoolean("simulation");
        if (!simulation && "SERVER".equalsIgnoreCase(strategyName)) {

            Threading threading = configuration.getEngineDefaults().getThreading();

            threading.setThreadPoolOutbound(true);
            threading.setThreadPoolOutboundNumThreads(configParams.getInteger("misc.outboundThreads"));
        }

        configuration.getVariables().get("strategyName").setInitializationValue(strategyName);
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
                } catch (ClassNotFoundException ex) {
                    throw new InternalEngineException("Unknown variable type: " + type);
                }
            }
        }
    }

}
