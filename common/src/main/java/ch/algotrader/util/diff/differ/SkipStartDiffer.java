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
import java.util.Objects;

import org.apache.log4j.Logger;

import ch.algotrader.util.diff.define.CsvColumn;
import ch.algotrader.util.diff.reader.BufferedReader;
import ch.algotrader.util.diff.reader.CsvLine;
import ch.algotrader.util.diff.reader.CsvReader;
import ch.algotrader.util.diff.reader.CsvReaderUtil;

/**
 * Skips some lines (other than header lines) at the beginning of the expected or
 * actual CSV file. This is used if one file contains some extra leading lines that are
 * not important for the assertion.
 */
public class SkipStartDiffer implements CsvDiffer {

    private static Logger LOG = Logger.getLogger(SkipStartDiffer.class);

    public static enum Mode {
        /** Skip expected rows until the value in the compared column matches the one in the actual row */
        SKIP_EXPECTED,
        /** Skip actual rows until the value in the compared column matches the one in the expected row */
        SKIP_ACTUAL,
        /** Skip expected or actual -- which ever value of the compared column is lesser --- until the values match (e.g. match date for ascending order) */
        SKIP_LESSER_VALUE,
        /** Skip expected or actual -- which ever value of the compared column is lesser --- until the values match (e.g. match date for descending order) */
        SKIP_GREATER_VALUE;

        public MatchResult match(CsvColumn expectedColumn, Object expVal, CsvColumn actualColumn, Object actVal) {
            if (Objects.equals(expVal, actVal)) {
                return MatchResult.MATCH;
            }
            if (this == SKIP_EXPECTED) {
                return MatchResult.SKIP_EXPECTED;
            }
            if (this == SKIP_ACTUAL) {
                return MatchResult.SKIP_ACTUAL;
            }
            final int cmp;
            if (expVal instanceof Comparable) {
                @SuppressWarnings("unchecked")
                int c = ((Comparable<Object>) expVal).compareTo(actVal);
                cmp = c;
            } else {
                cmp = String.valueOf(expVal).compareTo(String.valueOf(actVal));
            }
            if (cmp == 0) {
                throw new IllegalArgumentException("match returned false but comparator returned 0 for expVal=" + expVal + " and actVal=" + actVal);
            }
            if (this == SKIP_LESSER_VALUE) {
                return cmp < 0 ? MatchResult.SKIP_EXPECTED : MatchResult.SKIP_ACTUAL;
            }
            //else: this == SKIP_GREATER_VALUE
            return cmp > 0 ? MatchResult.SKIP_EXPECTED : MatchResult.SKIP_ACTUAL;
        }
    }

    private static enum MatchResult {
        SKIP_EXPECTED, SKIP_ACTUAL, MATCH;

        public boolean skip(BufferedReader expReader, BufferedReader actReader) throws IOException {
            if (this == SKIP_EXPECTED) {
                expReader.clearBuffer();
                expReader.readLineIntoBuffer();
                return true;
            }
            if (this == SKIP_ACTUAL) {
                actReader.clearBuffer();
                actReader.readLineIntoBuffer();
                return true;
            }
            return false;
        }
    }

    private final Mode mode;
    private final CsvColumn expectedColumn;
    private final CsvColumn actualColumn;

    public SkipStartDiffer(Mode mode, CsvColumn expectedColumn, CsvColumn actualColumn) {
        this.mode = Objects.requireNonNull(mode, "mode cannot be null");
        this.expectedColumn = Objects.requireNonNull(expectedColumn, "expectedColumn cannot be null");
        this.actualColumn = Objects.requireNonNull(actualColumn, "actualColumn cannot be null");
    }

    @Override
    public int diffLines(CsvReader expectedReader, CsvReader actualReader) throws IOException {
        assertLines(new BufferedReader(expectedReader), new BufferedReader(actualReader));
        return 0;//compare's nothing
    }

    private void assertLines(BufferedReader expReader, BufferedReader actReader) throws IOException {
        if (!isAtStart(expReader)) {
            throw new IOException("skipping is only allowed at the start of the asserted files, but expected was at " + CsvReaderUtil.getFileLocation(expReader));
        }
        if (!isAtStart(actReader)) {
            throw new IOException("skipping is only allowed at the start of the asserted files, but actual was at " + CsvReaderUtil.getFileLocation(actReader));
        }

        CsvLine expLine = expReader.readLineIntoBuffer();
        CsvLine actLine = actReader.readLineIntoBuffer();

        //skip until match
        while (expLine != null && actLine != null) {
            final Object expVal = expLine.getValues().get(expectedColumn);
            if (expVal == null) {
                throw new IOException("expected value not found for column " + expectedColumn + " " + CsvReaderUtil.getFileLocation(expReader));
            }
            final Object actVal = actLine.getValues().get(actualColumn);
            if (actVal == null) {
                throw new IOException("actual value not found for column " + actualColumn + " " + CsvReaderUtil.getFileLocation(actReader));
            }

            final MatchResult result = mode.match(expectedColumn, expVal, actualColumn, actVal);
            if (!result.skip(expReader, actReader)) {
                if (!isAtStart(expReader)) {
                    LOG.info("skipped expected lines, now at " + CsvReaderUtil.getFileLocation(expReader));
                }
                if (!isAtStart(actReader)) {
                    LOG.info("skipped actual lines, now at " + CsvReaderUtil.getFileLocation(actReader));
                }
                return;
            }
            expLine = expReader.getLastLineInBuffer();
            actLine = actReader.getLastLineInBuffer();
        }
        throw new IOException("no matching lines found for expected column '" + expectedColumn + "' and actual column '" + actualColumn + "' " + CsvReaderUtil.getFileLocations(expReader, actReader));
    }

    private boolean isAtStart(BufferedReader expReader) {
        return expReader.getLineIndex() == 0 || expReader.getLineIndex() == 1 && expReader.getCsvDefinition().isSkipHeaderLine();
    }

}
