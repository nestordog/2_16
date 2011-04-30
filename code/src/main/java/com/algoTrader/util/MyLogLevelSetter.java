package com.algoTrader.util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class MyLogLevelSetter {

    public static void setLogLevelFromCommandLine() {
        String level = ConfigurationUtil.getBaseConfig().getString("rootLogLevel");
        Logger.getRootLogger().setLevel(Level.toLevel(level));
    }
}
