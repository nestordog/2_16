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
package ch.algotrader.util.diff.differ;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.algotrader.util.diff.CsvAssertionError;
import ch.algotrader.util.diff.DiffEntry;
import ch.algotrader.util.diff.define.AssertableCsvColumn;
import ch.algotrader.util.diff.define.CsvColumn;
import ch.algotrader.util.diff.reader.CsvLine;
import ch.algotrader.util.diff.reader.CsvReader;
import ch.algotrader.util.diff.reader.CsvReaderUtil;
import ch.algotrader.util.diff.value.ValueAsserter;

/**
 * Asserts expected and actual data line by line. Each two lines are compared based on
 * a map of column pairs defined when constructing this asserter.
 * <p>
 * The assertion fails if
 * <ul>
 * <li>the assertion of two lines fails (triggered by a failed assertion of two column values)</li>
 * <li>if the actual file ends and the expected file contains more lines</li>
 * <li>if the expected file ends but the actual file has more lines</li>
 * </ul>
 * <p>
 * A {@link #Builder} can be used to add column-pairs and create a {@code SimpleDiffer} instance.
 */
public class SimpleDiffer implements CsvDiffer {

    private final Map<AssertableCsvColumn, CsvColumn> columnsToAssert;

    public SimpleDiffer(Map<AssertableCsvColumn, CsvColumn> columnsToAssert) {
        this.columnsToAssert = new LinkedHashMap<AssertableCsvColumn, CsvColumn>(columnsToAssert);
    }

    public static class Builder {
        private final Map<AssertableCsvColumn, CsvColumn> columnsToAssert = new LinkedHashMap<AssertableCsvColumn, CsvColumn>();

        public Builder add(AssertableCsvColumn expColumn, CsvColumn actColumn) {
            columnsToAssert.put(expColumn, actColumn);
            return this;
        }

        public SimpleDiffer build() {
            return new SimpleDiffer(columnsToAssert);
        }
    }

    @Override
    public void diffLines(CsvReader expectedReader, CsvReader actualReader) throws IOException {
        CsvLine expLine = null;
        CsvLine actLine = null;
        try {
            while ((expLine = expectedReader.readLine()) != null & (actLine = actualReader.readLine()) != null) {
                assertLine(expectedReader, expLine, actualReader, actLine);
                // System.out.println("assert OK: " + getFileLocations(expectedReader, actualReader));
            }
            if (expLine != null) {
                throw new CsvAssertionError("unexpected end of actual group or file", Collections.emptyList(),//
                        expectedReader.getFile(), expLine, null, null, //
                        actualReader.getFile(), new CsvLine(null, null, actualReader.getLine()), null, null);
            }
            if (actLine != null) {
                throw new CsvAssertionError("found more lines in actual group or file when expecting end", Collections.emptyList(),//
                        expectedReader.getFile(), new CsvLine(null, null, expectedReader.getLine()), null, null, //
                        actualReader.getFile(), actLine, null, null);
            }
        } catch (Exception e) {
            throw new RuntimeException("unexpected exception " + CsvReaderUtil.getFileLocations(expectedReader, actualReader), e);
        }
    }

    public void assertLine(CsvReader expReader, CsvLine expLine, CsvReader actReader, CsvLine actLine) {
        List<DiffEntry> diffs = null;
        for (final Map.Entry<AssertableCsvColumn, CsvColumn> entry : columnsToAssert.entrySet()) {
            final AssertableCsvColumn expCol = entry.getKey();
            final CsvColumn actCol = entry.getValue();
            try {
                assertValue(expCol.getValueAsserter(), expReader, expCol, expLine, actReader, actCol, actLine);
            } catch (CsvAssertionError e) {
                if (diffs == null)
                    diffs = new ArrayList<>(e.getDiffs());
                else
                    diffs.addAll(e.getDiffs());
            }
        }
        if (diffs != null) {
            throw new CsvAssertionError(expReader.getFile(), actReader.getFile(), diffs);
        }
    }

    private static <T> void assertValue(ValueAsserter<T> asserter, CsvReader expReader, AssertableCsvColumn expCol, CsvLine expLine, CsvReader actReader, CsvColumn actCol, CsvLine actLine) {
        final Object expVal = expCol.get(expLine);
        final Object actVal = actCol.get(actLine);
        try {
            asserter.assertValue(asserter.type().cast(expVal), actVal);
        } catch (AssertionError e) {
            throw new CsvAssertionError(e.getMessage(), Collections.emptyList(),//
                    expReader.getFile(), expLine, expCol, expVal, actReader.getFile(), actLine, actCol, actVal);
        }
    }
}
