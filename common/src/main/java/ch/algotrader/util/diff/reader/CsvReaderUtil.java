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
package ch.algotrader.util.diff.reader;

/**
 * Utility with static helper methods.
 */
public class CsvReaderUtil {

    /**
     * Returns a string in square brackets with line and file information from the
     * specified reader argument.
     *
     * @param reader the reader
     * @return a string with file and current line location of the given {@code reader}
     */
    public static String getFileLocation(CsvReader reader) {
        return "[line=" + reader.getLine() + ", file=" + reader.getFile().getName() + "]";
    }

    /**
     * Returns a string in curly brackets with line and file information from the
     * specified reader arguments.
     *
     * @param expected the reader of the expected CSV file
     * @param actual the reader of the actual CSV file
     * @return a string with file and current line location of the given readers
     */
    public static String getFileLocations(CsvReader expected, CsvReader actual) {
        return "{exp=" + getFileLocation(expected) + ", act=" + getFileLocation(actual) + "}";
    }

    //no instances
    private CsvReaderUtil() {
        super();
    }

}
