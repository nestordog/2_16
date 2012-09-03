package com.algoTrader.util;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * helper class to retrieve properties from conf.properties and system properties before SpringContext is initialized
 */
public class ConfigurationUtil {

    private static Properties props;
    private static String fileName = File.separator + "conf.properties";

    static {
        try {
            props = new Properties();
            props.load(ConfigurationUtil.class.getResourceAsStream(fileName));
            props.putAll(System.getProperties());

        } catch (IOException e) {
            System.out.println("could not load properties");
        }
    };

    public static String getString(String key) {
        return props.getProperty(key);
    }

    public static int getInt(String key) {
        return Integer.valueOf(props.getProperty(key));
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(props.getProperty(key));
    }
}
