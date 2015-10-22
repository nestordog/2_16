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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import ch.algotrader.util.diff.define.CsvColumn;

/**
 * Represents a line or row from a CSV file.
 */
public class CsvLine {

    private final String rawLine;
    private final Map<CsvColumn, Object> values;
    private final int lineIndex;

    private CsvLine(String rawLine, Map<CsvColumn, Object> values, int lineIndex) {
        this.rawLine = rawLine;
        this.values = values;
        this.lineIndex = lineIndex;
    }

    public static CsvLine getLine(CsvReader reader, String rawLine, Map<CsvColumn, Object> values) {
        Objects.requireNonNull(reader, "reader cannot be null");
        Objects.requireNonNull(rawLine, "rawLine cannot be null");
        Objects.requireNonNull(values, "values cannot be null");
        return new CsvLine(rawLine, values, reader.getLineIndex());
    }

    public static CsvLine getEofLine(CsvReader reader) {
        Objects.requireNonNull(reader, "reader cannot be null");
        return new CsvLine(null, Collections.<CsvColumn, Object>emptyMap(), reader.getLineIndex());
    }

    /**
     * Returns the raw line, maybe null.
     */
    public String getRawLine() {
        return rawLine;
    }

    /**
     * Returns the values map, maybe empty but never null.
     */
    public Map<CsvColumn, Object> getValues() {
        return values;
    }

    /**
     * Returns the zero-based line index
     */
    public int getLineIndex() {
        return lineIndex;
    }

    /**
     * Returns true if this is a valid line, and false if the underlying line
     * was null for instance because the end of file or group was reached.
     *
     * @return true if valid and false if {@link #getRawLine() raw line} is null
     */
    public boolean isValid() {
        return null != rawLine;
    }

    @Override
    public String toString() {
        return String.valueOf(rawLine);
    }

}
