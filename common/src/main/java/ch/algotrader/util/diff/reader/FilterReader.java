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

/**
 * Reader that skips certain lines based on a {@link #Filter} criteria.
 */
public class FilterReader implements CsvReader {

    /**
     * Filter to apply to a line. Accepting a line means that it is returned by
     * the {@link FilterReader#readLine()} method. If accept returns false the
     * line is suppressed and the next line is read instead.
     */
    public static interface Filter {
        /**
         * Returns true to accept and return the given line and false if the line
         * should be suppressed.
         *
         * @param line the line to test
         * @return true to accept and false to reject the line
         */
        boolean accept(CsvLine line);
    }

    private final CsvReader delegate;
    private final Filter filter;

    public FilterReader(CsvReader delegate, Filter filter) {
        this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
        this.filter = Objects.requireNonNull(filter, "filter cannot be null");
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

    public Filter getFilter() {
        return filter;
    }

    @Override
    public int getLine() {
        return delegate.getLine();
    }

    @Override
    public CsvLine readLine() throws IOException {
        CsvLine line = delegate.readLine();
        while (line != null && !filter.accept(line)) {
            line = delegate.readLine();
        }
        return line;
    }

}
