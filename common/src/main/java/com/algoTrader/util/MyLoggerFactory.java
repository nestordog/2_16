package com.algoTrader.util;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

public class MyLoggerFactory implements LoggerFactory {

    /**
     * The constructor should be public as it will be called by configurators in
     * different packages.
     */
    public MyLoggerFactory() {

        String commandLineLevel = System.getProperty("logLevel");

        if (commandLineLevel != null) {
            Level level = Level.toLevel(commandLineLevel);
            LogManager.getRootLogger().setLevel(level);
        }
    }

    @Override
    public Logger makeNewLoggerInstance(String name) {
        return new MyLogger(name);
    }
}
