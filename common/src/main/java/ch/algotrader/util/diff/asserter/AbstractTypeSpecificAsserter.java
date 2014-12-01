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
package ch.algotrader.util.diff.asserter;

/**
 * Asserter for two strings where one string can be a prefix of the other.
 */
abstract public class AbstractTypeSpecificAsserter<T> implements ValueAsserter {

    abstract protected Class<T> type();

    abstract protected boolean equalValuesTyped(T expectedValue, T actualValue);

    abstract protected void assertValueTyped(T expectedValue, T actualValue);

    private T convert(Object value, boolean expected) {
        try {
            return type().cast(value);
        } catch (ClassCastException e) {
            throw new AssertionError((expected ? "Expected" : "Actual") + " value should be a " + type().getSimpleName() + " but was " + (value == null ? null : value.getClass().getName()) + ": <"
                    + value + ">");
        }
    }

    @Override
    public boolean equalValues(Object expectedValue, Object actualValue) {
        final T exp, act;
        try {
            exp = convert(expectedValue, true);
            act = convert(actualValue, false);
        } catch (AssertionError e) {
            return false;
        }
        return equalValuesTyped(exp, act);
    }

    @Override
    public void assertValue(Object expectedValue, Object actualValue) {
        final T exp = convert(expectedValue, true);
        final T act = convert(actualValue, false);
        assertValueTyped(exp, act);
    }

}
