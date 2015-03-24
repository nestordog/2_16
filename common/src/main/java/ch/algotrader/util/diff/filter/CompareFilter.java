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
package ch.algotrader.util.diff.filter;

import java.util.Comparator;
import java.util.Objects;

import ch.algotrader.util.diff.define.CsvColumn;
import ch.algotrader.util.diff.reader.CsvLine;

/**
 * Filter for a CSV line selecting rows which match a comparison criteria
 * for a particular column. The matched values must either implement {@link Comparable}
 * or a {@link Comparator} must be specified when constructing the filter.
 */
public class CompareFilter implements CsvLineFilter {

    private final Mode mode;
    private final Comparator<Object> comparator;
    private final Object value;
    private final CsvColumn column;

    /**
     * Comparison mode for the filter.
     */
    public static enum Mode {
        /** Accepts rows with a value strictly less than the reference value*/
        LESS {
            @Override
            public boolean acceptComparingActualToReference(int comparisonResult) {
                return comparisonResult < 0;
            }
        },
        /** Accepts rows with a value less than or equal to the reference value*/
        LESS_OR_EQUAL {
            @Override
            public boolean acceptComparingActualToReference(int comparisonResult) {
                return comparisonResult <= 0;
            }
        },
        /** Accepts rows with a value equal to the reference value*/
        EQUAL {
            @Override
            public boolean acceptComparingActualToReference(int comparisonResult) {
                return comparisonResult == 0;
            }
        },
        /** Accepts rows with a value greater than or equal to the reference value*/
        GREATER_OR_EQUAL {
            @Override
            public boolean acceptComparingActualToReference(int comparisonResult) {
                return comparisonResult >= 0;
            }
        },
        /** Accepts rows with a value strictly greater than the reference value*/
        GREATER {
            @Override
            public boolean acceptComparingActualToReference(int comparisonResult) {
                return comparisonResult > 0;
            }
        };

        abstract public boolean acceptComparingActualToReference(int comparisonResult);
    }

    /**
     * Constructor with comparison mode, reference value and CSV column for
     * the comparison of {@link Comparable} values.
     *
     * @param mode      the comparison mode
     * @param value     the reference value
     * @param column    the CSV column containing the filter value value
     */
    public <V extends Comparable<V>> CompareFilter(Mode mode, V value, CsvColumn column) {
        this(mode, comparableComparatorFor(value), value, column);
    }

    /**
     * Constructor with comparison mode, comparator, reference value and CSV column.
     *
     * @param mode          the comparison mode
     * @param comparator    the comparator used for the comparison
     * @param value         the reference value
     * @param column        the CSV column containing the filter value value
     */
    @SuppressWarnings("unchecked")
    public <V> CompareFilter(Mode mode, Comparator<? super V> comparator, V value, CsvColumn column) {
        this.mode = Objects.requireNonNull(mode, "mode cannot be null");
        this.comparator = (Comparator<Object>)Objects.requireNonNull(comparator, "comparator cannot be null");
        this.value = Objects.requireNonNull(value, "value cannot be null");
        this.column = Objects.requireNonNull(column, "column cannot be null");
    }

    /**
     * Returns a {@code CompareFilter} which accepts rows with a filter value strictly
     * less than the specified reference {@code value}.
     *
     * @param value the reference value
     * @param column the CSV column containing the filter value
     * @return a new filter which accepts (in pseudo code): {@code row[column] < value}
     */
    public static <V extends Comparable<V>> CompareFilter less(V value, CsvColumn column) {
        return new CompareFilter(Mode.LESS, value, column);
    }

    /**
     * Returns a {@code DateFilter} which accepts rows with a filter value less
     * than or equal to the specified reference {@code value}.
     *
     * @param value the reference value
     * @param column the CSV column containing the filter value
     * @return a new filter which accepts (in pseudo code): {@code row[column] <= value}
     */
    public static <V extends Comparable<V>> CompareFilter lessOrEqual(V value, CsvColumn column) {
        return new CompareFilter(Mode.LESS_OR_EQUAL, value, column);
    }

    /**
     * Returns a {@code DateFilter} which accepts rows with a filter value equal
     * to the specified reference {@code value}.
     *
     * @param value the reference value
     * @param column the CSV column containing the filter value
     * @return a new filter which accepts (in pseudo code): {@code row[column] == value}
     */
    public static <V extends Comparable<V>> CompareFilter at(V value, CsvColumn column) {
        return new CompareFilter(Mode.EQUAL, value, column);
    }

    /**
     * Returns a {@code DateFilter} which accepts rows with a filter value greater
     * than or equal to the specified reference {@code value}.
     *
     * @param value the reference value
     * @param column the CSV column containing the filter value
     * @return a new filter which accepts (in pseudo code): {@code row[column] >= value}
     */
    public static <V extends Comparable<V>> CompareFilter greaterOrEqual(V value, CsvColumn column) {
        return new CompareFilter(Mode.GREATER_OR_EQUAL, value, column);
    }

    /**
     * Returns a {@code DateFilter} which accepts rows with a filter value strictly
     * greater than the specified reference {@code value}.
     *
     * @param value the reference value
     * @param column the CSV column containing the filter value
     * @return a new filter which accepts (in pseudo code): {@code row[column] > value}
     */
    public static <V extends Comparable<V>> CompareFilter greater(V value, CsvColumn column) {
        return new CompareFilter(Mode.GREATER, value, column);
    }

    /**
     * Returns a filter which accepts rows with a filter value greater
     * than or equal to {@code from} and less than or equal to {@code to}.
     *
     * @param from the from-value, inclusive
     * @param from the to-value, inclusive
     * @param column the CSV column containing the filter value
     * @return a new filter which accepts (in pseudo code): {@code from <= row[column] <= to}
     */
    public static <V extends Comparable<V>> CsvLineFilter between(final V from, final V to, final CsvColumn column) {
        return new CsvLineFilter() {
            private final CompareFilter fromFilter = greaterOrEqual(from, column);
            private final CompareFilter toFilter = lessOrEqual(to, column);

            @Override
            public boolean accept(CsvLine line) {
                return fromFilter.accept(line) && toFilter.accept(line);
            }
        };
    }

    @Override
    public boolean accept(CsvLine line) {
        final Object value = line.getValues().get(column);
        if (value instanceof Object) {
            final int cmp = comparator.compare(this.value, value);
            return mode.acceptComparingActualToReference(-cmp);
        }
        return false;
    }

    private static final Comparator<Object> COMPARABLE_COMPARATOR = new Comparator<Object>() {
        @Override
        public int compare(Object o1, Object o2) {
            return compare((Comparable<?>)o1, (Comparable<?>)o2);
        }
        @SuppressWarnings("unchecked")
        public <V> int compare(Comparable<V> o1, Comparable<?> o2) {
            return o1.compareTo((V)o2);
        }
    };
    private static <V extends Comparable<V>> Comparator<? super V> comparableComparatorFor(V value) {
        return COMPARABLE_COMPARATOR;
    }

}
