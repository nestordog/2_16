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

import java.util.Collections;
import java.util.Map;

import ch.algotrader.util.diff.define.CsvColumn;

/**
 * Represents a line or row from a CSV file.
 */
public class CsvLine {

    private final String rawLine;
    private final Map<CsvColumn, Object> values;
    private final int lineIndex;

    public CsvLine(String rawLine, Map<CsvColumn, Object> values, int lineIndex) {
        this.rawLine = rawLine;
        this.values = values == null ? Collections.<CsvColumn, Object>emptyMap() : values;
        this.lineIndex = lineIndex;
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

    @Override
    public String toString() {
        return String.valueOf(rawLine);
    }

}
