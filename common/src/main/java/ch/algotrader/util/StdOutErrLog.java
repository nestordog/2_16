/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.util;

import java.io.PrintStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Redirects all System.out and System.err prints to log4j.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class StdOutErrLog {

    private static final Logger LOGGER = LogManager.getLogger(StdOutErrLog.class);

    static {
        tieSystemOutAndErrToLog();
    }

    public static void tieSystemOutAndErrToLog() {

        System.setOut(createLoggingProxy(System.out, false));
        System.setErr(createLoggingProxy(System.err, true));
    }

    public static PrintStream createLoggingProxy(final PrintStream realPrintStream, final boolean err) {

        return new PrintStream(realPrintStream) {
            @Override
            public void print(final String string) {
                if (err) {
                    LOGGER.error(string);
                } else {
                    LOGGER.info(string);
                }
            }

            @Override
            public void print(final Object obj) {
                if (err) {
                    LOGGER.error(obj);
                } else {
                    LOGGER.info(obj);
                }
            }

            @Override
            public void println(final String string) {
                if (err) {
                    LOGGER.error(string);
                } else {
                    LOGGER.info(string);
                }
            }

            @Override
            public void println(final Object obj) {
                if (err) {
                    LOGGER.error(obj);
                } else {
                    LOGGER.info(obj);
                }
            }
        };
    }
}
