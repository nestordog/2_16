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
package ch.algotrader.util;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Helper class to retrieve properties from conf.properties and system properties before SpringContext is initialized
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
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
