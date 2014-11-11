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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import ch.algotrader.util.diff.define.CsvDefinition;

/**
 * Reader of a CSV file. The current read position in the file represents the
 * state of this reader.
 */
public interface CsvReader {
    /**
     * Returns the CSV file definition.
     */
    CsvDefinition getCsvDefinition();
    /**
     * Returns the CSV file.
     */
    File getFile();
    /**
     * Returns the CSV buffered reader.
     */
    BufferedReader getReader();
    /**
     * Returns the zero-based line index of the line read next --- or equivalently
     * the number of lines already read.
     */
    int getLine();
    /**
     * Reads the next line and returns it. Returns null if the end of file is
     * reached.
     *
     * @return the next line or null if EOF
     * @throws IOException if an I/O exception occurs
     */
    CsvLine readLine() throws IOException;
}
