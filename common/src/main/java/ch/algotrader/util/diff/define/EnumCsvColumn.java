/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.util.diff.define;

import java.util.Objects;

import ch.algotrader.util.diff.convert.ConverterRegistry;
import ch.algotrader.util.diff.convert.ValueConverter;

/**
 * CSV Column definition based on an enum and a {@link ValueConverter}.
 */
public class EnumCsvColumn implements CsvColumn {

    private final Enum<?> column;
    private final ValueConverter<?> converter;

    public EnumCsvColumn(Enum<?> column, Class<?> type) {
        this(column, type, ConverterRegistry.DEFAULT);
    }

    public EnumCsvColumn(Enum<?> column, Class<?> type, ConverterRegistry converterRegistry) {
        this(column, getConverterByType(type, converterRegistry));
    }

    public EnumCsvColumn(Enum<?> column, ValueConverter<?> converter) {
        this.column = Objects.requireNonNull(column, "column cannot be null");
        this.converter = Objects.requireNonNull(converter, "converter cannot be null");
    }

    private static <T> ValueConverter<? extends T> getConverterByType(Class<T> type, ConverterRegistry converterRegistry) {
        final ValueConverter<? extends T> converter = converterRegistry.getConverterByType(type);
        if (converter != null) {
            return converter;
        }
        throw new IllegalArgumentException("no value converter is defined in the converter registry for the type " + type.getName());
    }

    @Override
    public String name() {
        return column.name();
    }

    @Override
    public int index() {
        return column.ordinal();
    }

    @Override
    public ValueConverter<?> converter() {
        return converter;
    }

    @Override
    public int hashCode() {
        return column.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof EnumCsvColumn) {
            final EnumCsvColumn other = (EnumCsvColumn) obj;
            return column.equals(other.column) && converter.equals(other.converter);
        }
        return false;
    }

    @Override
    public String toString() {
        return name() + "[type=" + converter.type().getSimpleName() + "]";
    }

}
