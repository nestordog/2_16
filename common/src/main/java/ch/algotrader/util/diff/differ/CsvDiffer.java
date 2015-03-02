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
package ch.algotrader.util.diff.differ;

import java.io.IOException;

import ch.algotrader.util.diff.CsvAssertionError;
import ch.algotrader.util.diff.reader.CsvReader;

/**
 * Diff tool for two CSV files or a two blocks of files based on two readers.
 */
public interface CsvDiffer {
    /**
     * Diffs and asserts lines from the given readers.
     *
     * @param expectedReader    the reader of the expected CSV file
     * @param actualReader      the reader of the actual CSV file
     * @return the number of lines that were actually compared (excluding filtered lines)
     * @throws IOException if an I/O exception occurs
     * @throws CsvAssertionError if an assertion error occurs
     */
    int diffLines(CsvReader expectedReader, CsvReader actualReader) throws IOException, CsvAssertionError;
}
