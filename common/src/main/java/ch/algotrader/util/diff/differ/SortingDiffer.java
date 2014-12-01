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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

import ch.algotrader.util.diff.define.CsvColumn;
import ch.algotrader.util.diff.reader.CsvLine;
import ch.algotrader.util.diff.reader.CsvReader;
import ch.algotrader.util.diff.reader.LinkedListReader;

/**
 * Sorts the underlying expected and actual lines according to some ORDER BY clause.
 * <p>
 * Sorting is useful if the lines are not in the same order in the expected and actual file.
 * <p>
 * Sorting can be applied to the whole file (not recommended) or within a GROUP BY
 * clause defined by a {@link GroupDiffer}. In the latter case sorting is performed
 * repeatedly within each group block which requires substantially less memory.
 * <p>
 * A {@link #Builder} can be used to add ORDER-BY columns and create a {@code SortingDiffer} instance.
 */
public class SortingDiffer implements CsvDiffer {

    private final CsvDiffer delegate;
    private final Comparator<CsvLine> expectedColumnComparator;
    private final Comparator<CsvLine> actualColumnComparator;

    public SortingDiffer(CsvColumn expectedSortColumn, CsvColumn actualSortColumn, CsvDiffer delegate) {
        this(Collections.singletonList(expectedSortColumn), Collections.singletonList(actualSortColumn), delegate);
    }

    public SortingDiffer(List<CsvColumn> expectedSortColumns, List<CsvColumn> actualSortColumns, CsvDiffer delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
        this.expectedColumnComparator = new ColumnComparator(expectedSortColumns);
        this.actualColumnComparator = new ColumnComparator(actualSortColumns);
    }

    public static class Builder {
        private final List<CsvColumn> expectedGrouColumns = new ArrayList<CsvColumn>();
        private final List<CsvColumn> actualGrouColumns = new ArrayList<CsvColumn>();

        public Builder add(CsvColumn expColumn, CsvColumn actColumn) {
            expectedGrouColumns.add(expColumn);
            actualGrouColumns.add(actColumn);
            return this;
        }

        public SortingDiffer build(CsvDiffer delegate) {
            return new SortingDiffer(expectedGrouColumns, actualGrouColumns, delegate);
        }
    }

    @Override
    public void diffLines(CsvReader expectedReader, CsvReader actualReader) throws IOException {
        final NavigableMap<CsvLine, Integer> expLines = new TreeMap<CsvLine, Integer>(expectedColumnComparator);
        final NavigableMap<CsvLine, Integer> actLines = new TreeMap<CsvLine, Integer>(actualColumnComparator);
        readAll(expectedReader, expLines);
        readAll(actualReader, actLines);
        Iterator<Map.Entry<CsvLine, Integer>> expIt = expLines.entrySet().iterator();
        Iterator<Map.Entry<CsvLine, Integer>> actIt = actLines.entrySet().iterator();
        while (expIt.hasNext() && actIt.hasNext()) {
            final Map.Entry<CsvLine, Integer> expEntry = expIt.next();
            final Map.Entry<CsvLine, Integer> actEntry = actIt.next();
            //use sub-reader for only this line to have correct line indexes in assertion errors
            final LinkedListReader expReader = new LinkedListReader(expectedReader, expEntry.getValue(), expEntry.getKey());
            final LinkedListReader actReader = new LinkedListReader(actualReader, actEntry.getValue(), actEntry.getKey());
            delegate.diffLines(expReader, actReader);
        }
        while (expIt.hasNext()) {
            final Map.Entry<CsvLine, Integer> expEntry = expIt.next();
            //use sub-reader for only this line to have correct line indexes in assertion errors
            final LinkedListReader expReader = new LinkedListReader(expectedReader, expEntry.getValue(), expEntry.getKey());
            delegate.diffLines(expReader, actualReader);
        }
        while (actIt.hasNext()) {
            final Map.Entry<CsvLine, Integer> actEntry = actIt.next();
            //use sub-reader for only this line to have correct line indexes in assertion errors
            final LinkedListReader actReader = new LinkedListReader(actualReader, actEntry.getValue(), actEntry.getKey());
            delegate.diffLines(expectedReader, actReader);
        }
    }

    private void readAll(CsvReader reader, NavigableMap<CsvLine, Integer> lines) throws IOException {
        CsvLine line = null;
        while ((line = reader.readLine()) != null) {
            lines.put(line, reader.getLine());
        }
    }

    private static class ColumnComparator implements Comparator<CsvLine> {
        private final List<? extends CsvColumn> columns;

        public ColumnComparator(List<? extends CsvColumn> columns) {
            this.columns = Objects.requireNonNull(columns, "columns cannot be null");
        }

        @Override
        public int compare(CsvLine o1, CsvLine o2) {
            for (final CsvColumn column : columns) {
                final Object v1 = o1.getValues().get(column);
                final Object v2 = o2.getValues().get(column);
                int result;
                if (v1 == v2) {
                    result = 0;
                } else if (v1 instanceof Comparable) {
                    @SuppressWarnings("unchecked")
                    final int cmp = ((Comparable<Object>) v1).compareTo(v2);
                    result = cmp;
                } else {
                    result = String.valueOf(v1).compareTo(String.valueOf(v2));
                }
                if (result != 0) {
                    return result;
                }
            }
            return 0;
        }
    }
}
