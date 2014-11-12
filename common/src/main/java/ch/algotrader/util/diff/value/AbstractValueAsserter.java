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
 * Base class suitable for most value asserters.
 *
 * @param <T> the value type
 */
abstract public class AbstractValueAsserter<T> implements ValueAsserter<T> {

    private final Class<? extends T> type;

    public AbstractValueAsserter(Class<? extends T> type) {
        this.type = type;
    }

    @Override
    public Class<? extends T> type() {
        return type;
    }

    @Override
    public boolean equalValues(T expectedValue, Object actualValue) {
        return expectedValue == actualValue || (expectedValue != null && expectedValue.equals(actualValue));
    }

    @Override
    public void assertValue(T expectedValue, Object actualValue) {
        Assert.assertEquals("Values don't match", expectedValue, actualValue);
    }

}
