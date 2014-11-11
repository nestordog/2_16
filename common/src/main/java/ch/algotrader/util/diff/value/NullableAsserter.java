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
package ch.algotrader.util.diff.value;

import java.util.Objects;

/**
 * Wrapper around another asserter to allow nulls and empty strings.
 */
public class NullableAsserter<T> implements ValueAsserter<T> {

    private static enum Mode {
        NULL_TO_NULL,
        EMPTY_STRING_TO_NULL,
        NULL_OR_EMPTY_STRING_TO_NULL;

        public boolean convertToNull(String value) {
            if (value == null && this != EMPTY_STRING_TO_NULL) {
                return true;
            }
            if (value != null && value.isEmpty() && this != NULL_TO_NULL) {
                return true;
            }
            return false;
        }
    }

    private final Mode mode;
    private final ValueAsserter<T> delegate;

    public NullableAsserter(Mode mode, ValueAsserter<T> delegate) {
        this.mode = Objects.requireNonNull(mode, "mode cannot be null");
        this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
    }

    public static <T> NullableAsserter<T> nullable(ValueAsserter<T> delegate) {
        return new NullableAsserter<T>(Mode.NULL_TO_NULL, delegate);
    }
    public static <T> NullableAsserter<T> emptyStringToNull(ValueAsserter<T> delegate) {
        return new NullableAsserter<T>(Mode.EMPTY_STRING_TO_NULL, delegate);
    }
    public static <T> NullableAsserter<T> nullOrEmptyStringToNull(ValueAsserter<T> delegate) {
        return new NullableAsserter<T>(Mode.NULL_OR_EMPTY_STRING_TO_NULL, delegate);
    }

    @Override
    public Class<? extends T> type() {
        return delegate.type();
    }
    @Override
    public T convert(String column, String value) {
        if (mode.convertToNull(value)) {
            return null;
        }
        return delegate.convert(column, value);
    }

    @Override
    public boolean equalValues(T expectedValue, Object actualValue) {
        return delegate.equalValues(expectedValue, actualValue);
    }
    @Override
    public void assertValue(T expectedValue, Object actualValue) {
        delegate.assertValue(expectedValue, actualValue);
    }

}
