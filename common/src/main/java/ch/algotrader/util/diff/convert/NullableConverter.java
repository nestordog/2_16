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
package ch.algotrader.util.diff.convert;

import java.util.Objects;

/**
 * Wrapper around another converter to allow nulls and empty strings.
 */
public class NullableConverter<T> implements ValueConverter<T> {

    private static enum Mode {
        /** Null is treated as null value*/
        NULL,
        /** An empty is treated as null value*/
        EMPTY_STRING,
        /** Null or an empty is treated as null value*/
        NULL_OR_EMPTY_STRING;

        public boolean appliesTo(String value) {
            if (value == null && this != EMPTY_STRING) {
                return true;
            }
            if (value != null && value.isEmpty() && this != NULL) {
                return true;
            }
            return false;
        }
    }

    private final Mode mode;
    private final ValueConverter<T> delegate;
    private final T emptyValue;

    public NullableConverter(Mode mode, ValueConverter<T> delegate, T emptyValue) {
        this.mode = Objects.requireNonNull(mode, "mode cannot be null");
        this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
        this.emptyValue = emptyValue;//usually null
    }

    /**
     * Returns null for null input and passes all other values to the {@code delegate} converter.
     *
     * @param delegate converter for non-null values
     * @return a converter that handles nulls and passes only non-null values to the delegate converter
     */
    public static <T> NullableConverter<T> nullable(ValueConverter<T> delegate) {
        return nullTo(null, delegate);
    }

    /**
     * Returns null for empty string input and passes all other values to the {@code delegate} converter.
     *
     * @param delegate converter for non-empty-string values
     * @return a converter that handles empty strings and passes only other values to the delegate converter
     */
    public static <T> NullableConverter<T> emptyStringToNull(ValueConverter<T> delegate) {
        return emptyStringTo(null, delegate);
    }

    /**
     * Returns null for null or empty string input and passes all other values to the {@code delegate} converter.
     *
     * @param delegate converter for non-empty values
     * @return a converter that handles nulls and empty strings and passes only other values to the delegate converter
     */
    public static <T> NullableConverter<T> nullOrEmptyStringToNull(ValueConverter<T> delegate) {
        return nullOrEmptyStringTo(null, delegate);
    }

    /**
     * Returns {@code nullValue} for null input and passes all other values to the {@code delegate} converter.
     *
     * @param delegate converter for non-null values
     * @return a converter that handles nulls and passes only non-null values to the delegate converter
     */
    public static <T> NullableConverter<T> nullTo(T nullValue, ValueConverter<T> delegate) {
        return new NullableConverter<T>(Mode.NULL, delegate, nullValue);
    }

    /**
     * Returns {@code emptyStringValue} for empty string input and passes all other values to the {@code delegate} converter.
     *
     * @param delegate converter for non-empty-string values
     * @return a converter that handles empty strings and passes only other values to the delegate converter
     */
    public static <T> NullableConverter<T> emptyStringTo(T emptyStringValue, ValueConverter<T> delegate) {
        return new NullableConverter<T>(Mode.EMPTY_STRING, delegate, emptyStringValue);
    }

    /**
     * Returns {@code nullOrEmptyStringValue} for null or empty string input and passes all other values to the {@code delegate} converter.
     *
     * @param delegate converter for non-empty values
     * @return a converter that handles nulls and empty strings and passes only other values to the delegate converter
     */
    public static <T> NullableConverter<T> nullOrEmptyStringTo(T nullOrEmptyStringValue, ValueConverter<T> delegate) {
        return new NullableConverter<>(Mode.NULL_OR_EMPTY_STRING, delegate, nullOrEmptyStringValue);
    }

    @Override
    public Class<? extends T> type() {
        return delegate.type();
    }

    @Override
    public T convert(String column, String value) {
        if (mode.appliesTo(value)) {
            return emptyValue;
        }
        return delegate.convert(column, value);
    }

}
