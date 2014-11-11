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
package ch.algotrader.util.diff;

import java.io.File;
import java.util.Objects;

import ch.algotrader.util.diff.define.CsvColumn;
import ch.algotrader.util.diff.reader.CsvLine;

final class CsvDiffContext implements DiffEntry.Context {

    private final File file;
    private final CsvLine line;
    private final CsvColumn column;
    private final Object value;

    CsvDiffContext(final File file, final CsvLine line, final CsvColumn column, final Object value) {
        this.file = Objects.requireNonNull(file, "file cannot be null");
        this.line = Objects.requireNonNull(line, "line cannot be null");
        this.column = column; //nullable
        this.value = value; //nullable
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public CsvLine getLine() {
        return line;
    }

    @Override
    public CsvColumn getColumn() {
        return column;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "line=" + getLine().getLineIndex() + ", column=" + getColumn() + ", value=" + getValue();
    }
}