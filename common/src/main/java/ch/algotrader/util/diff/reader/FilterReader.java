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

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import ch.algotrader.util.diff.define.CsvDefinition;
import ch.algotrader.util.diff.filter.CsvLineFilter;

/**
 * Reader that skips certain lines based on a {@link #CsvLineFilter} criteria.
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
        return delegate.getCsvDefinition();
    }

    @Override
    public File getFile() {
        return delegate.getFile();
    }

    @Override
    public java.io.BufferedReader getReader() {
        return delegate.getReader();
    }

    public CsvReader getDelegate() {
        return delegate;
    }

    public CsvLineFilter getFilter() {
        return csvLineFilter;
    }

    @Override
    public int getLineIndex() {
        return delegate.getLineIndex();
    }

    @Override
    public CsvLine readLine() throws IOException {
        CsvLine line = delegate.readLine();
        while (line.isValid() && !csvLineFilter.accept(line)) {
            line = delegate.readLine();
        }
        return line;
    }

}
