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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    private static final Logger LOG = LogManager.getLogger(GroupDiffer.class);

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
    public int diffLines(CsvReader expectedReader, CsvReader actualReader) throws IOException {
        return assertLines(new BufferedReader(expectedReader), new BufferedReader(actualReader));
    }

    private int assertLines(BufferedReader expReader, BufferedReader actReader) throws IOException {
        int linesCompared = 0;
        CsvLine expLine = expReader.readLineIntoBuffer();
        while (expLine.isValid()) {
            final List<Object> groupValues = getGroupValues(expLine);
            final int expSkip = readGroupIntoBuffer(expectedGroupColumns, groupValues, expReader);
            final int actSkip = readGroupIntoBuffer(actualGroupColumns, groupValues, actReader);
            final LinkedListReader expSubReader = expReader.readBufferAsReader(expReader.getBufferSize() - expSkip);
            final LinkedListReader actSubReader = actReader.readBufferAsReader(actReader.getBufferSize() - actSkip);
            try {
                LOG.debug("asserting group: " + groupValues + " [exp=" + getLines(expSubReader) + ", act=" + getLines(actSubReader) + "]");
                linesCompared += delegate.diffLines(expSubReader, actSubReader);
            } catch (CsvAssertionError e) {
                throw e.addGroupValues(groupValues);
                //            } catch (AssertionError e) {
                //                throw new AssertionError("[group-values=" + groupValues + "] " + e.getMessage() + " " + CsvUtil.getFileLocations(expSubReader, actSubReader), e);
            } catch (Exception e) {
                throw new RuntimeException("[group-values=" + groupValues + "] unexpected exception " + CsvReaderUtil.getFileLocations(expSubReader, actSubReader), e);
            }
            expLine = expSkip > 0 ? expReader.getFirstLineInBuffer() : expReader.readLineIntoBuffer();
        }
        final CsvLine actLine = actReader.readLine();
        if (actLine.isValid()) {
            throw new CsvAssertionError("found more lines in actual group or file when expecting end", Collections.emptyList(),//
                    expReader.getFile(), expLine, null, null, //
                    actReader.getFile(), actLine, null, null);
        }
        return linesCompared;
    }

    private static String getLines(LinkedListReader reader) {
        return reader.getLineIndex() + ":" + (reader.getLineIndex() + reader.getLineCount() - 1);
    }

    private List<Object> getGroupValues(CsvLine line) {
        final List<Object> groupValues = new ArrayList<>(expectedGroupColumns.size());
        for (CsvColumn col : expectedGroupColumns) {
            groupValues.add(line.getValues().get(col));
        }
        return groupValues;
    }

    private static int readGroupIntoBuffer(List<CsvColumn> groupColumns, List<Object> groupValues, BufferedReader reader) throws IOException {
        CsvLine line = reader.readLineIntoBuffer();
        while (line.isValid() && matches(groupColumns, groupValues, line)) {
            line = reader.readLineIntoBuffer();
        }
        return line.isValid() ? 1 : 0;
    }

    private static boolean matches(List<CsvColumn> groupColumns, List<Object> groupValues, CsvLine line) {
        for (int i = 0; i < groupColumns.size(); i++) {
            if (!Objects.equals(groupValues.get(i), line.getValues().get(groupColumns.get(i)))) {
                return false;
            }
        }
        return true;
    }

}
