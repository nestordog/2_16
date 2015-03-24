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

import java.util.Objects;

import ch.algotrader.util.diff.define.CsvColumn;
import ch.algotrader.util.diff.reader.CsvLine;

/**
 * Filter for a CSV line selecting rows which match a criteria for a particular
 * column based on the {@link Object#equals(Object) equals(..)} method of the matched
 * value.
 */
public class EqualsFilter implements CsvLineFilter {

    private final Mode mode;
    private final Object value;
    private final CsvColumn column;

    /**
     * Comparison mode for the filter.
     */
    public static enum Mode {
        /** Accepts rows which equal the reference value*/
        Equals,
        /** Rejects rows which equal the reference value*/
        NotEquals
    }

    /**
     * Constructor with comparison mode, reference value and CSV column.
     *
     * @param mode      the comparison mode
     * @param value     the reference value
     * @param column    the CSV column containing the filter value
     */
    public EqualsFilter(Mode mode, Object value, CsvColumn column) {
        this.mode = Objects.requireNonNull(mode, "mode cannot be null");
        this.value = Objects.requireNonNull(value, "value cannot be null");
        this.column = Objects.requireNonNull(column, "column cannot be null");
    }

    /**
     * Returns an {@code EqualsFilter} which accepts rows with a filter value
     * equal to the specified reference {@code value}.
     *
     * @param value the reference value
     * @param column the CSV column containing the filter value
     * @return a new filter which accepts (in pseudo code): {@code row[column].equals(value)}
     */
    public static EqualsFilter equals(Object value, CsvColumn column) {
        return new EqualsFilter(Mode.Equals, value, column);
    }

    /**
     * Returns an {@code EqualsFilter} which rejects rows with a filter value
     * equal to the specified reference {@code value}.
     *
     * @param value the reference value
     * @param column the CSV column containing the filter value
     * @return a new filter which accepts (in pseudo code): {@code !row[column].equals(value)}
     */
    public static EqualsFilter notEquals(Object value, CsvColumn column) {
        return new EqualsFilter(Mode.NotEquals, value, column);
    }

    @Override
    public boolean accept(CsvLine line) {
        final Object value = line.getValues().get(column);
        return mode == Mode.Equals ? Objects.equals(this.value, value) : !Objects.equals(this.value, value);
    }

}
