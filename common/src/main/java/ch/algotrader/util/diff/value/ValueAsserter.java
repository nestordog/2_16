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

/**
 * Extension of {@link ValueConverter} also supporting comparing and asserting of two
 * values.
 *
 * @param <T> the value type
 */
public interface ValueAsserter<T> extends ValueConverter<T> {
    /**
     * Returns true if the two values are considered equal and false otherwise.
     *
     * @param expectedValue the expected value
     * @param actualValue   the actual value
     * @return true if both values are considered equal
     */
    boolean equalValues(T expectedValue, Object actualValue);

    /**
     * Asserts equality of the two values. Throws an {@link AssertionError} if the
     * two values are not considered equal.
     *
     * @param expectedValue the expected value
     * @param actualValue   the actual value
     * @throws AssertionError if the two values are different, that is, iff
     *      {@link #assertValue(String, Object, String, Object) assertValue(..)}
     *      returns false
     */
    void assertValue(T expectedValue, Object actualValue);
}
