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

import ch.algotrader.util.diff.CsvAssertionError;
import ch.algotrader.util.diff.DiffEntry;
import ch.algotrader.util.diff.asserter.CaseInsensitiveStringAsserter;
import ch.algotrader.util.diff.asserter.CompositeAsserter;
import ch.algotrader.util.diff.asserter.DoubleAsserter;
import ch.algotrader.util.diff.asserter.EqualityValueAsserter;
import ch.algotrader.util.diff.asserter.TolerantLongAsserter;
import ch.algotrader.util.diff.asserter.ValueAsserter;
import ch.algotrader.util.diff.define.CsvColumn;
import ch.algotrader.util.diff.reader.CsvLine;
import ch.algotrader.util.diff.reader.CsvReader;
import ch.algotrader.util.diff.reader.CsvReaderUtil;

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

    private final List<ColumnAsserter> asserters;

    /**
     * Private constructor with column asserters. Use a {@link Builder} to construct an instance of {@link SimpleDiffer}.
     * @param asserters the asserters
     */
    private SimpleDiffer(List<ColumnAsserter> asserters) {
        this.asserters = Objects.requireNonNull(asserters, "asserters cannot be null");
    }

    /**
     * Builder for {@link SimpleDiffer} with convenience methods to add common
     * {@link ValueAsserter} instances.
     */
    public static class Builder {
        private final List<ColumnAsserter> asserters = new ArrayList<ColumnAsserter>();

        /**
         * Asserts the given expected/actual column pair using the specified asserter.
         *
         * @param expectedColumn    the definition of the expected CSV column
         * @param actualColumn      the definition of the actual CSV column
         * @param asserter  the asserter to use for the value assertion
         * @return this builder for method chaining to add other column asserters
         */
        public Builder add(CsvColumn expectedColumn, CsvColumn actualColumn, ValueAsserter asserter) {
            asserters.add(new ColumnAsserter(expectedColumn, actualColumn, asserter));
            return this;
        }

        /**
         * Asserts the given expected/actual column pair using the specified asserters. An
         * asserted value is accepted if all asserters accept the value.
         *
         * @param expectedColumn    the definition of the expected CSV column
         * @param actualColumn      the definition of the actual CSV column
         * @param asserters  the asserters to use for the value assertion concatenated with a logical AND operation
         * @return this builder for method chaining to add other column asserters
         */
        public Builder allOf(CsvColumn expectedColumn, CsvColumn actualColumn, ValueAsserter... asserters) {
            return add(expectedColumn, actualColumn, CompositeAsserter.and(asserters));
        }

        /**
         * Asserts the given expected/actual column pair using the specified asserters. An
         * asserted value is accepted if any of the given asserters accepts the value.
         *
         * @param expectedColumn    the definition of the expected CSV column
         * @param actualColumn      the definition of the actual CSV column
         * @param asserters  the asserters to use for the value assertion concatenated with a logical OR operation
         * @return this builder for method chaining to add other column asserters
         */
        public Builder anyOf(CsvColumn expectedColumn, CsvColumn actualColumn, ValueAsserter... asserters) {
            return add(expectedColumn, actualColumn, CompositeAsserter.or(asserters));
        }

        /**
         * Asserts the given expected/actual column pair using equality assertion.
         * Two null values are considered equal.
         *
         * @param expectedColumn    the definition of the expected CSV column
         * @param actualColumn      the definition of the actual CSV column
         * @return this builder for method chaining to add other column asserters
         */
        public Builder assertEqual(CsvColumn expectedColumn, CsvColumn actualColumn) {
            return add(expectedColumn, actualColumn, EqualityValueAsserter.INSTANCE);
        }

        /**
         * Asserts the given expected/actual column pair using string comparison
         * ignoring the case. Two null values are considered equal.
         *
         * @param expectedColumn    the definition of the expected CSV column
         * @param actualColumn      the definition of the actual CSV column
         * @return this builder for method chaining to add other column asserters
         */
        public Builder assertStringsIgnoreCase(CsvColumn expectedColumn, CsvColumn actualColumn) {
            return add(expectedColumn, actualColumn, CaseInsensitiveStringAsserter.INSTANCE);
        }

        /**
         * Asserts the given expected/actual column pair using double assertion with
         * the specified absolute tolerance. Two values are considered equal if
         * their difference is no more than the specified tolerance.
         * <p>
         * Two null values are considered equal.
         *
         * @param expectedColumn    the definition of the expected CSV column
         * @param actualColumn      the definition of the actual CSV column
         * @param tolerance         the absolute tolerance still considered zero difference
         * @return this builder for method chaining to add other column asserters
         */
        public Builder assertDouble(CsvColumn expectedColumn, CsvColumn actualColumn, double tolerance) {
            return add(expectedColumn, actualColumn, new DoubleAsserter(tolerance));
        }

        /**
         * Asserts the given expected/actual column pair using double assertion with
         * the specified relative tolerance. Two values are considered equal if
         * their difference --- realtive to the magnitude of the two values --- is no
         * more than the tolerance.
         * <p>
         * More precisely two values are considered equal if their difference is no more
         * than
         * <pre>
         * <code>10<sup>min(log10(abs(expected)), log10(abs(actual))) + log10(tolerance)</sup></code>
         * </pre>
         * Two null values are considered equal.
         *
         * @param expectedColumn    the definition of the expected CSV column
         * @param actualColumn      the definition of the actual CSV column
         * @param tolerance         the relative tolerance still considered zero difference
         * @return this builder for method chaining to add other column asserters
         */
        public Builder assertDoubleWithRelativeTolerance(CsvColumn expectedColumn, CsvColumn actualColumn, double tolerance) {
            return add(expectedColumn, actualColumn, new DoubleAsserter(DoubleAsserter.Mode.RELATIVE, tolerance));
        }

        /**
         * Asserts the given expected/actual column pair using long assertion with
         * the specified absolute tolerance. Two values are considered equal if
         * their difference is no more than the specified tolerance.
         * <p>
         * Two null values are considered equal.
         *
         * @param expectedColumn    the definition of the expected CSV column
         * @param actualColumn      the definition of the actual CSV column
         * @param tolerance         the absolute tolerance still considered zero difference
         * @return this builder for method chaining to add other column asserters
         */
        public Builder assertLongWithAbsoluteTolerance(CsvColumn expectedColumn, CsvColumn actualColumn, long tolerance) {
            return add(expectedColumn, actualColumn, new TolerantLongAsserter(TolerantLongAsserter.Mode.ABSOLUTE, tolerance));
        }

        /**
         * Asserts the given expected/actual column pair using long assertion with
         * the specified relative tolerance. Two values are considered equal if
         * their difference --- realtive to the magnitude of the two values --- is no
         * more than the tolerance.
         * <p>
         * More precisely two values are considered equal if their difference is no more
         * than
         * <pre>
         * <code>floor(10<sup>min(log10(abs(expected)), log10(abs(actual))) + tolerance</sup>)</code>
         * </pre>
         * Two null values are considered equal.
         *
         * @param expectedColumn    the definition of the expected CSV column
         * @param actualColumn      the definition of the actual CSV column
         * @param tolerance         the relative tolerance still considered zero difference
         * @return this builder for method chaining to add other column asserters
         */
        public Builder assertLongWithRelativeTolerance(CsvColumn expectedColumn, CsvColumn actualColumn, int precision) {
            return add(expectedColumn, actualColumn, new TolerantLongAsserter(TolerantLongAsserter.Mode.RELATIVE, precision));
        }

        /**
         * Builds and returns a {@link SimpleDiffer} performing the previously defined assertions.
         * @return a {@link SimpleDiffer} performing the previously defined assertions.
         */
        public SimpleDiffer build() {
            return new SimpleDiffer(asserters);
        }
    }

    @Override
    public int diffLines(CsvReader expectedReader, CsvReader actualReader) throws IOException {
        CsvLine expLine = null;
        CsvLine actLine = null;
        try {
            int linesCompared = 0;
            while ((expLine = expectedReader.readLine()).isValid() & (actLine = actualReader.readLine()).isValid()) {
                assertLine(expectedReader, expLine, actualReader, actLine);
                linesCompared++;
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
            return linesCompared;
        } catch (Exception e) {
            throw new RuntimeException("unexpected exception " + CsvReaderUtil.getFileLocations(expectedReader, actualReader), e);
        }
    }

    public void assertLine(CsvReader expReader, CsvLine expLine, CsvReader actReader, CsvLine actLine) {
        List<DiffEntry> diffs = null;
        for (final ColumnAsserter columnAsserter : asserters) {
            try {
                columnAsserter.assertValue(expReader, expLine, actReader, actLine);
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

    private static class ColumnAsserter {
        private final CsvColumn expectedColumn;
        private final CsvColumn actualColumn;
        private final ValueAsserter asserter;

        public ColumnAsserter(CsvColumn expectedColumn, CsvColumn actualColumn, ValueAsserter asserter) {
            this.expectedColumn = Objects.requireNonNull(expectedColumn, "expectedColumn cannot be null");
            this.actualColumn = Objects.requireNonNull(actualColumn, "actualColumn cannot be null");
            this.asserter = Objects.requireNonNull(asserter, "asserter cannot be null");
        }

        public void assertValue(CsvReader expReader, CsvLine expLine, CsvReader actReader, CsvLine actLine) {
            final Object expVal = expLine.getValues().get(expectedColumn);
            final Object actVal = actLine.getValues().get(actualColumn);
            try {
                asserter.assertValue(expVal, actVal);
            } catch (AssertionError e) {
                throw new CsvAssertionError(e.getMessage(), Collections.emptyList(),//
                        expReader.getFile(), expLine, expectedColumn, expVal, actReader.getFile(), actLine, actualColumn, actVal);
            }
        }
    }
}
