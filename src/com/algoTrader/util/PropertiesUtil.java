package com.algoTrader.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class PropertiesUtil {

    private static Properties props;
    private static Logger logger = MyLogger.getLogger(PropertiesUtil.class.getName());
    private static String fileName = "/conf.properties";

    public static String getProperty(String key) {

        if (props == null) {
            props = new Properties();
            try {
                InputStream is = PropertiesUtil.class.getResourceAsStream(fileName);
                props.load(is);

            } catch (IOException e) {
                logger.error("could not load properties", e);
            }
        }

        return props.getProperty(key);
    }

    public static int getIntProperty(String key) {

        return Integer.parseInt(getProperty(key));
    }

    public static double getDoubleProperty(String key){

        return Double.parseDouble(getProperty(key));
    }

    public static boolean getBooleanProperty (String key) {

        return new Boolean(getProperty(key)).booleanValue();
    }
}
