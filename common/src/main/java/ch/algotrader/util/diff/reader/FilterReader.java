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
package ch.algotrader.util.diff.reader;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import ch.algotrader.util.diff.define.CsvDefinition;
import ch.algotrader.util.diff.filter.CsvLineFilter;

/**
 * Reader that skips certain lines based on a {@link ch.algotrader.util.diff.filter.CsvLineFilter CsvLineFilter} criteria.
 */
public class FilterReader implements CsvReader {

    private final CsvReader delegate;
    private final CsvLineFilter csvLineFilter;

    public FilterReader(CsvReader delegate, CsvLineFilter csvLineFilter) {
        this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
        this.csvLineFilter = Objects.requireNonNull(csvLineFilter, "csvLineFilter cannot be null");
    }

    @Override
    public CsvDefinition getCsvDefinition() {
        return this.delegate.getCsvDefinition();
    }

    @Override
    public File getFile() {
        return this.delegate.getFile();
    }

    @Override
    public java.io.BufferedReader getReader() {
        return this.delegate.getReader();
    }

    public CsvReader getDelegate() {
        return this.delegate;
    }

    public CsvLineFilter getFilter() {
        return this.csvLineFilter;
    }

    @Override
    public int getLineIndex() {
        return this.delegate.getLineIndex();
    }

    @Override
    public CsvLine readLine() throws IOException {
        CsvLine line = this.delegate.readLine();
        while (line.isValid() && !this.csvLineFilter.accept(line)) {
            line = this.delegate.readLine();
        }
        return line;
    }

}
