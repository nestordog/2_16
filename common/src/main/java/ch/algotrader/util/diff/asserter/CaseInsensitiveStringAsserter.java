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
package ch.algotrader.util.diff.asserter;

/**
 * Asserter for two strings with case-insensitive string comparison.
 */
public class CaseInsensitiveStringAsserter extends AbstractTypeSpecificAsserter<String> {

    /**
     * The singleton instance.
     */
    public static final CaseInsensitiveStringAsserter INSTANCE = new CaseInsensitiveStringAsserter();

    @Override
    protected Class<String> type() {
        return String.class;
    }

    @Override
    protected boolean equalValuesTyped(String expectedValue, String actualValue) {
        if (expectedValue == actualValue) {
            return true;
        }
        if (expectedValue != null) {
            return expectedValue.equalsIgnoreCase(actualValue);
        }
        return false;
    }

    @Override
    protected void assertValueTyped(String expectedValue, String actualValue) {
        if (!equalValues(expectedValue, actualValue)) {
            Assert.assertEquals("Values don't match ignoring the case", expectedValue, actualValue);
        }
    }
}
