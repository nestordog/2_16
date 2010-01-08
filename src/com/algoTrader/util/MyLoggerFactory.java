package com.algoTrader.util;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

/**
 * A factory that makes new {@link MyLogger} objects.
 *
 * See <b><a href="doc-files/MyLoggerFactory.java">source code</a></b> for more
 * details.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class MyLoggerFactory implements LoggerFactory {

    /**
     * The constructor should be public as it will be called by configurators in
     * different packages.
     */
    public MyLoggerFactory() {
    }

    public Logger makeNewLoggerInstance(String name) {
        return new MyLogger(name);
    }
}
