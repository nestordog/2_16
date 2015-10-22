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

import java.util.Objects;

/**
 * Asserter for two strings where one string can be a prefix of the other.
 */
public class StringPrefixAsserter extends AbstractTypeSpecificAsserter<String> {

    public static enum Mode {
        EXPECTED_PREFIX_OF_ACTUAL {
            @Override
            public boolean matches(String exp, String act) {
                return act.startsWith(exp);
            }
        },
        ACTUAL_PREFIX_OF_EXPECTED {
            @Override
            public boolean matches(String exp, String act) {
                return exp.startsWith(act);
            }
        },
        ONE_PREFIX_OF_OTHER {
            @Override
            public boolean matches(String exp, String act) {
                return EXPECTED_PREFIX_OF_ACTUAL.matches(exp, act) || ACTUAL_PREFIX_OF_EXPECTED.matches(exp, act);
            }
        };

        abstract public boolean matches(String exp, String act);
    }

    private final Mode mode;

    public StringPrefixAsserter(Mode mode) {
        this.mode = Objects.requireNonNull(mode, "mode cannot be null");
    }

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
            return mode.matches(expectedValue, actualValue);
        }
        return false;
    }

    @Override
    protected void assertValueTyped(String expectedValue, String actualValue) {
        if (!equalValues(expectedValue, actualValue)) {
            Assert.assertEquals("Values don't match according to mode=" + mode, expectedValue, actualValue);
        }
    }
}
