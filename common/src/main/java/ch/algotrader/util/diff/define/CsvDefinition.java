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
package ch.algotrader.util.diff.define;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

import ch.algotrader.util.diff.reader.CsvReader;
import ch.algotrader.util.diff.reader.DefaultCsvReader;

/**
 * Defines a CSV file.
 */
public class CsvDefinition {

    private final boolean skipHeaderLine;
    private final List<CsvColumn> columns;

    public CsvDefinition(boolean skipHeaderLine, CsvColumn... columns) {
        this.skipHeaderLine = skipHeaderLine;
        this.columns = Arrays.asList(columns);
    }

    public boolean isSkipHeaderLine() {
        return skipHeaderLine;
    }
    public List<CsvColumn> getColumns() {
        return columns;
    }

    public CsvReader open(File file) throws FileNotFoundException {
        return new DefaultCsvReader(this, file);
    }

}
