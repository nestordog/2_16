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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Logically combines two asserters with AND or OR.
 */
public class CompositeAsserter implements ValueAsserter {

    public static enum Mode {
        /** Assertion succeeds only if all asserters are happy */
        AND,
        /** Assertion succeeds if at least one of the asserters is happy */
        OR;
    }

    private final Mode mode;
    private final Collection<? extends ValueAsserter> asserters;

    public CompositeAsserter(Mode mode, Collection<? extends ValueAsserter> asserters) {
        this.mode = Objects.requireNonNull(mode, "mode cannot be null");
        this.asserters = Objects.requireNonNull(asserters, "asserters cannot be null");
    }

    public static CompositeAsserter and(ValueAsserter... asserters) {
        return new CompositeAsserter(Mode.AND, Arrays.asList(asserters));
    }

    public static CompositeAsserter or(ValueAsserter... asserters) {
        return new CompositeAsserter(Mode.OR, Arrays.asList(asserters));
    }

    @Override
    public boolean equalValues(Object expectedValue, Object actualValue) {
        final boolean initialResult = mode == Mode.AND ? true : false;
        boolean result = initialResult;
        for (final ValueAsserter asserter : asserters) {
            final boolean partial = asserter.equalValues(expectedValue, actualValue);
            result = mode == Mode.AND ? (result & partial) : (result | partial);
            if (result != initialResult) {
                return result;
            }
        }
        return result;
    }

    @Override
    public void assertValue(Object expectedValue, Object actualValue) {
        if (mode == Mode.AND) {
            for (final ValueAsserter asserter : asserters) {
                asserter.assertValue(expectedValue, actualValue);
            }
        } else {
            final List<String> assertErrors = new ArrayList<>(asserters.size());
            for (final ValueAsserter asserter : asserters) {
                try {
                    asserter.assertValue(expectedValue, actualValue);
                    //assert succeeded, we're done
                    return;
                } catch (AssertionError e) {
                    assertErrors.add(e.getMessage());
                }
            }
            if (assertErrors.isEmpty()) {
                Assert.fail("OR assertion failed with empty asserter set");
            } else {
                Assert.fail("None of the assertions was successful: " + assertErrors);
            }
        }
    }

}
