package com.algoTrader.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.log4j.Logger;

public class ConfigurationUtil {

    private static String baseFileName = "base.properties";

    private static CompositeConfiguration baseConfig;
    private static Map<String, CompositeConfiguration> strategyConfigMap = new HashMap<String, CompositeConfiguration>();

    private static Logger logger = MyLogger.getLogger(ConfigurationUtil.class.getName());


    public static Configuration getBaseConfig() {

        if (baseConfig == null) {
            baseConfig = new CompositeConfiguration();
            baseConfig.setThrowExceptionOnMissing(true);

            baseConfig.addConfiguration(new SystemConfiguration());
            try {
                baseConfig.addConfiguration(new PropertiesConfiguration(baseFileName));
            } catch (ConfigurationException e) {
                logger.error("error loading base.properties", e);
            }
        }
        return baseConfig;
    }

    public static Configuration getStrategyConfig(String strategyName) {

        if (!strategyConfigMap.containsKey(strategyName)) {
            CompositeConfiguration strategyConfig = new CompositeConfiguration();
            strategyConfig.setThrowExceptionOnMissing(true);

            strategyConfig.addConfiguration(new SystemConfiguration());
            try {
                strategyConfig.addConfiguration(new PropertiesConfiguration(strategyName + ".properties"));
            } catch (ConfigurationException e) {
                logger.error("error loading " + strategyName + ".properties", e);
            }
            strategyConfigMap.put(strategyName, strategyConfig);
        }
        return baseConfig;
    }
}
