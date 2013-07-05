/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.util;

import java.io.PrintStream;

import org.apache.log4j.Logger;

/**
 * Redirects all System.out and System.err prints to log4j.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class StdOutErrLog {

    private static final Logger logger = MyLogger.getLogger(StdOutErrLog.class);

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
                    logger.error(string);
                } else {
                    logger.info(string);
                }
            }

            @Override
            public void print(final Object obj) {
                if (err) {
                    logger.error(obj);
                } else {
                    logger.info(obj);
                }
            }

            @Override
            public void println(final String string) {
                if (err) {
                    logger.error(string);
                } else {
                    logger.info(string);
                }
            }

            @Override
            public void println(final Object obj) {
                if (err) {
                    logger.error(obj);
                } else {
                    logger.info(obj);
                }
            }
        };
    }
}
