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
package ch.algotrader.util.diff.differ;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import ch.algotrader.util.diff.DiffStats;
import ch.algotrader.util.diff.define.CsvDefinition;
import ch.algotrader.util.diff.reader.CsvReader;

/**
 * Diffs and asserts two CSV files based on a delegate {@link CsvDiffer}.
 */
public class FileDiffer {

    private final CsvDefinition expectedDef;
    private final CsvDefinition actualDef;
    private final CsvDiffer differ;

    /**
     * Constructs an file asserter based on the expected and actual CSV file definitions. The assert
     * functionality is delegated to the specified {@code asserter}.
     *
     * @param expectedDef   the definition for the expected CSV file
     * @param actualDef     the definition for the actual CSV file
     * @param differ        the differ that performs the actual diffs and line assertions
     */
    public FileDiffer(CsvDefinition expectedDef, CsvDefinition actualDef, CsvDiffer differ) {
        this.expectedDef = Objects.requireNonNull(expectedDef, "expectedDef cannot be null");
        this.actualDef = Objects.requireNonNull(actualDef, "actualDef cannot be null");
        this.differ = Objects.requireNonNull(differ, "differ cannot be null");
    }

    /**
     * Diffs the two files and returns the (max) number of lines diff'ed during
     * the operation.
     *
     * @param expected the file with expected CSV data
     * @param actual    the file with actual CSV data
     * @return the max number of lines read, that is, {@code max(lines(expected), lines(actual))}
     * @throws IOException if an I/O exception occurs
     */
    public DiffStats diffFiles(File expected, File actual) throws IOException {
        CsvReader expReader = null;
        CsvReader actReader = null;
        try {
            expReader = expectedDef.open(expected);
            actReader = actualDef.open(actual);
            final int linesCompared = differ.diffLines(expReader, actReader);
            return new DiffStats(expReader.getLineIndex(), actReader.getLineIndex(), linesCompared);
        } finally {
            close(expReader);
            close(actReader);
        }
    }

    private void close(CsvReader reader) {
        if (reader != null) {
            try {
                reader.getReader().close();
            } catch (IOException e) {
                //ignore, it's just the close that failed
            }
        }
    }

}
