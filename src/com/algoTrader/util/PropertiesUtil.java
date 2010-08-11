package com.algoTrader.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;

import org.apache.log4j.Logger;

public class PropertiesUtil {

    private static Properties props;
    private static Logger logger = MyLogger.getLogger(PropertiesUtil.class.getName());
    private static String fileName = "/conf.properties";

    public static String getProperty(String key) {

        if (props == null) loadProps();

        String property = props.getProperty(key);

        if (property == null) logger.warn("property was not found: " + key);

        return property;
    }

    public static String getProperty(String parentKey, String key) {
        return getProperty(parentKey + "." + key);
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

    public static long getLongProperty(String key){

        return Long.parseLong(getProperty(key));
    }

    public static Collection<String> getChildKeys(String parentKey) {

        if (props == null) loadProps();

        Collection<String> keys = new HashSet<String>();
        for (Enumeration<?> e = props.propertyNames(); e.hasMoreElements();) {
            String key = (String)e.nextElement();
            if (key.startsWith(parentKey)) keys.add(key.substring(parentKey.length() + 1));
        }
        return keys;
    }

    private static void loadProps() {

        props = new Properties();
        try {
            InputStream is = PropertiesUtil.class.getResourceAsStream(fileName);
            props.load(is);

        } catch (IOException e) {
            logger.error("could not load properties", e);
        }
    }

    public static void setProperty(String param, String value) {

        if (props == null) loadProps();

        props.put(param, value);
    }

    public static void setEsperOrConfigProperty(String param, String value) {

        if (PropertiesUtil.hasProperty(param)) {
            PropertiesUtil.setProperty(param, value);
        } else if (EsperService.hasVariable(param)) {
            EsperService.setVariableValue(param, value);
        } else {
            throw new IllegalArgumentException("param " + param + " was not found");
        }
    }

    public static boolean hasProperty(String param) {

        if (props == null) loadProps();

        return props.containsKey(param);
    }
}
