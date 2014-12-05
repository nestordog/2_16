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
import java.util.List;
import java.util.Objects;

import org.apache.log4j.Logger;

import ch.algotrader.util.diff.CsvAssertionError;
import ch.algotrader.util.diff.define.CsvColumn;
import ch.algotrader.util.diff.reader.BufferedReader;
import ch.algotrader.util.diff.reader.CsvLine;
import ch.algotrader.util.diff.reader.CsvReader;
import ch.algotrader.util.diff.reader.CsvReaderUtil;
import ch.algotrader.util.diff.reader.LinkedListReader;

/**
 * Groups the underlying expected and actual lines according to some GROUP BY clause
 * and asserts each of those group blocks individually based on a delegate asserter.
 * <p>
 * A {@link #Builder} can be used to add GROUP-BY columns and create a {@code GroupDiffer} instance.
 */
public class GroupDiffer implements CsvDiffer {

    private static Logger LOG = Logger.getLogger(GroupDiffer.class);

    private final List<CsvColumn> expectedGroupColumns;
    private final List<CsvColumn> actualGroupColumns;
    private final CsvDiffer delegate;

    public GroupDiffer(CsvColumn expectedGroupColumn, CsvColumn actualGroupColumn, CsvDiffer delegate) {
        this(Collections.singletonList(expectedGroupColumn), Collections.singletonList(actualGroupColumn), delegate);
    }

    public GroupDiffer(List<CsvColumn> expectedGroupColumns, List<CsvColumn> actualGroupColumns, CsvDiffer delegate) {
        this.expectedGroupColumns = Objects.requireNonNull(expectedGroupColumns, "expectedGroupColumns cannot be null");
        this.actualGroupColumns = Objects.requireNonNull(actualGroupColumns, "actualGroupColumns cannot be null");
        this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
    }

    public static class Builder {
        private final List<CsvColumn> expectedGrouColumns = new ArrayList<>();
        private final List<CsvColumn> actualGrouColumns = new ArrayList<>();

        public Builder add(CsvColumn expColumn, CsvColumn actColumn) {
            expectedGrouColumns.add(expColumn);
            actualGrouColumns.add(actColumn);
            return this;
        }

        public GroupDiffer build(CsvDiffer delegate) {
            return new GroupDiffer(expectedGrouColumns, actualGrouColumns, delegate);
        }
    }

    @Override
    public void diffLines(CsvReader expectedReader, CsvReader actualReader) throws IOException {
        assertLines(new BufferedReader(expectedReader), new BufferedReader(actualReader));
    }

    private void assertLines(BufferedReader expReader, BufferedReader actReader) throws IOException {
        CsvLine line = expReader.readLineIntoBuffer();
        while (line.isValid()) {
            final List<Object> groupValues = getGroupValues(line);
            final int expSkip = readGroupIntoBuffer(expectedGroupColumns, groupValues, expectedGroupColumns, expReader);
            final int actSkip = readGroupIntoBuffer(expectedGroupColumns, groupValues, actualGroupColumns, actReader);
            final LinkedListReader expSubReader = expReader.readBufferAsReader(expReader.getBufferSize() - expSkip);
            final LinkedListReader actSubReader = actReader.readBufferAsReader(actReader.getBufferSize() - actSkip);
            try {
                LOG.info("asserting group: " + groupValues + " [exp=" + getLines(expSubReader) + ", act=" + getLines(actSubReader) + "]");
                delegate.diffLines(expSubReader, actSubReader);
            } catch (CsvAssertionError e) {
                throw e.addGroupValues(groupValues);
                //            } catch (AssertionError e) {
                //                throw new AssertionError("[group-values=" + groupValues + "] " + e.getMessage() + " " + CsvUtil.getFileLocations(expSubReader, actSubReader), e);
            } catch (Exception e) {
                throw new RuntimeException("[group-values=" + groupValues + "] unexpected exception " + CsvReaderUtil.getFileLocations(expSubReader, actSubReader), e);
            }
            line = expSkip > 0 ? expReader.getFirstLineInBuffer() : expReader.readLineIntoBuffer();
        }
    }

    private static String getLines(LinkedListReader reader) {
        return reader.getLine() + ":" + (reader.getLine() + reader.getLineCount() - 1);
    }

    private List<Object> getGroupValues(CsvLine line) {
        final List<Object> groupValues = new ArrayList<Object>(expectedGroupColumns.size());
        for (CsvColumn col : expectedGroupColumns) {
            groupValues.add(line.getValues().get(col));
        }
        return groupValues;
    }

    private static int readGroupIntoBuffer(List<CsvColumn> expectedGroupColumns, List<Object> groupValues, List<? extends CsvColumn> columns, BufferedReader reader) throws IOException {
        CsvLine line = reader.readLineIntoBuffer();
        while (line != null && matches(expectedGroupColumns, groupValues, columns, line)) {
            line = reader.readLineIntoBuffer();
        }
        return line == null ? 0 : 1;
    }

    private static boolean matches(List<CsvColumn> expectedGroupColumns, List<Object> groupValues, List<? extends CsvColumn> columns, CsvLine line) {
        for (int i = 0; i < expectedGroupColumns.size(); i++) {
            if (!Objects.equals(groupValues.get(i), line.getValues().get(columns.get(i)))) {
                return false;
            }
        }
        return true;
    }

}
