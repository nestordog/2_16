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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.algotrader.util.diff.define.CsvColumn;
import ch.algotrader.util.diff.reader.CsvLine;

public class DefaultCsvDiff implements DiffEntry {

    private final String message;
    private final List<Object> unmodifiableGroupValues;
    private final Context expectedContext;
    private final Context actualContext;

    public DefaultCsvDiff(final String message, final List<Object> groupValues, //
            final File expFile, final CsvLine expLine, final CsvColumn expColumn, final Object expValue,//
            final File actFile, final CsvLine actLine, final CsvColumn actColumn, final Object actValue) {
        this.message = Objects.requireNonNull(message, "expValue cannot be null");
        unmodifiableGroupValues = Collections.unmodifiableList(new ArrayList<Object>(groupValues));
        expectedContext = new CsvDiffContext(expFile, expLine, expColumn, expValue);
        actualContext = new CsvDiffContext(actFile, actLine, actColumn, actValue);
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public List<?> getGroupValues() {
        return unmodifiableGroupValues;
    }

    @Override
    public Context getExpectedContext() {
        return expectedContext;
    }

    @Override
    public Context getActualContext() {
        return actualContext;
    }

    @Override
    public String toString() {
        return "diff at line[exp/act]=[" + getExpectedContext().getLine().getLineIndex() + "/" + getActualContext().getLine().getLineIndex() + "]" + //
                " in columns[exp/act]=[" + getExpectedContext().getColumn() + "/" + getActualContext().getColumn() + "]" + //
                ": " + getMessage() //
                + " {exp-file=" + getActualContext().getFile().getName() //
                + ", act-file=" + getActualContext().getFile().getName() + "}";
    }
}
