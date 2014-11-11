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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import ch.algotrader.util.diff.define.CsvColumn;
import ch.algotrader.util.diff.reader.CsvLine;

/**
 * Thrown to indicate that an assertion of two CSV files has failed.
 */
public class CsvAssertionError extends AssertionError {

    private static final long serialVersionUID = 1L;

    private final File expectedFile;
    private final File actualFile;
    private final List<DiffEntry> diffs;

    public CsvAssertionError(File expectedFile, File actualFile, List<DiffEntry> diffs) {
        this.expectedFile = Objects.requireNonNull(expectedFile, "expectedFile cannot be null");
        this.actualFile = Objects.requireNonNull(actualFile, "actualFile cannot be null");
        this.diffs = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(diffs, "diffs cannot be null")));
    }

    public CsvAssertionError(//
            final String message, final List<Object> groupValues, //
            final File expFile, final CsvLine expLine, final CsvColumn expColumn, final Object expValue,//
            final File actFile, final CsvLine actLine, final CsvColumn actColumn, final Object actValue) {
        this(expFile, actFile, Collections.<DiffEntry> singletonList(new DefaultCsvDiff(message, groupValues, expFile, expLine, expColumn, expValue, actFile, actLine, actColumn, actValue)));
    }

    public CsvAssertionError addGroupValues(List<?> groupValues) {
        final List<DiffEntry> groupedDiffs = new ArrayList<DiffEntry>(diffs.size());
        for (final DiffEntry diff : diffs) {
            final List<Object> grpVals = new ArrayList<>(groupValues);
            grpVals.addAll(diff.getGroupValues());
            final DiffEntry.Context expContext = diff.getExpectedContext();
            final DiffEntry.Context actContext = diff.getActualContext();
            groupedDiffs.add(new DefaultCsvDiff(diff.getMessage(), grpVals, //
                    expContext.getFile(), expContext.getLine(), expContext.getColumn(), expContext.getValue(), //
                    actContext.getFile(), actContext.getLine(), actContext.getColumn(), actContext.getValue()));
        }
        final CsvAssertionError error = new CsvAssertionError(getExpectedFile(), getActualFile(), groupedDiffs);
        error.initCause(this);
        return error;
    }

    public File getExpectedFile() {
        return expectedFile;
    }

    public File getActualFile() {
        return actualFile;
    }

    public List<DiffEntry> getDiffs() {
        return diffs;
    }

    public String getGroupString() {
        final List<Object> groups = new ArrayList<>(diffs.size());
        for (final DiffEntry diff : diffs) {
            groups.add(diff.getGroupValues());
        }
        //is there only a single group?
        if (new HashSet<Object>(groups).size() == 1) {
            return groups.get(0).toString();
        }
        return groups.toString();
    }

    public String getLineString(boolean expected, boolean actual) {
        if (!expected && !actual) {
            return null;
        }
        final List<Integer> exp = new ArrayList<>(diffs.size());
        final List<Integer> act = new ArrayList<>(diffs.size());
        for (final DiffEntry diff : diffs) {
            exp.add(diff.getExpectedContext().getLine().getLineIndex());
            act.add(diff.getActualContext().getLine().getLineIndex());
        }
        //is there only a single line in both lists?
        if (new HashSet<Integer>(exp).size() == 1 && new HashSet<Integer>(act).size() == 1) {
            if (expected & actual) {
                return exp.get(0).toString() + "/" + act.get(0).toString();
            }
            return expected ? exp.get(0).toString() : act.get(0).toString();
        }
        //non-unique lines, print'em all
        if (expected & actual) {
            final StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (int i = 0; i < exp.size(); i++) {
                sb.append(exp.get(i));
                sb.append('/');
                sb.append(act.get(i));
                sb.append(',');
            }
            sb.setCharAt(sb.length() - 1, ']');
            return sb.toString();
        }
        return expected ? exp.toString() : act.toString();
    }

    @Override
    public String getMessage() {
        if (diffs.isEmpty()) {
            return null;
        }
        if (diffs.size() == 1) {
            return diffs.get(0).toString();
        }

        //multiple diffs

        //1) # of diffs
        final StringBuilder sb = new StringBuilder();
        sb.append(diffs.size()).append(" diffs");

        //2) lines
        sb.append(" at lines[exp/act]=").append(getLineString(true, true));

        //3) columns
        sb.append(" in columns[exp/act]=[");
        for (final DiffEntry diff : diffs) {
            sb.append(diff.getExpectedContext().getColumn());
            sb.append('/');
            sb.append(diff.getActualContext().getColumn());
            sb.append(',');
        }
        sb.setCharAt(sb.length() - 1, ']');

        //4) files
        sb.append(" {exp-file=").append(getExpectedFile().getName());
        sb.append(", act-file=").append(getActualFile().getName());
        return sb.append('}').toString();
    }

}
