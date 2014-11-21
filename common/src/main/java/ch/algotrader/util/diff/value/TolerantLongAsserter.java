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
 * Value asserter for long values comparing two values with a tolerance.
 * The tolerance can be absolute or relative to the magnitude of the compared values.
 */
public class TolerantLongAsserter implements ValueAsserter<Long> {

    public static enum Mode {
        /** Use absolute tolerance for comparison */
        ABSOLUTE,
        /** Use tolerance relative to the magnitude of the smaller value for comparison */
        RELATIVE;

        public long getTolerance(long tolerance, long value1, long value2) {
            if (this == ABSOLUTE) {
                return tolerance;
            }
            return (long)Math.floor(Math.pow(10d, Math.min(log10(value1), log10(value2)) + tolerance));
        }

        private static double log10(long value) {
            return Math.log10(Math.abs(value));
        }
    }

    private final Mode mode;
    private final long tolerance;

    public TolerantLongAsserter(long tolerance) {
        this(Mode.ABSOLUTE, tolerance);
    }
    public TolerantLongAsserter(Mode mode, long tolerance) {
        if (mode == Mode.RELATIVE && tolerance > -1) {
            throw new IllegalArgumentException("tolerance must be negative for mode " + Mode.RELATIVE);
        }
        this.mode = mode;
        this.tolerance = tolerance;
    }

    @Override
    public Long convert(String column, String value) {
        return LongAsserter.INSTANCE.convert(column, value);
    }

    @Override
    public Class<? extends Long> type() {
        return Long.class;
    }

    @Override
    public boolean equalValues(Long expectedValue, Object actualValue) {
        if (expectedValue == actualValue) return true;
        if (expectedValue != null && actualValue instanceof Long) {
            final Long act = (Long)actualValue;
            final double tol = mode.getTolerance(tolerance, expectedValue, act);
            return expectedValue.equals(act) || Math.abs(expectedValue - act) <= tol;
        }
        return false;
    }
    @Override
    public void assertValue(Long expectedValue, Object actualValue) {
        if (actualValue instanceof Long) {
            final Long act = (Long)actualValue;
            final double tol = mode.getTolerance(tolerance, expectedValue, act);
            Assert.assertEquals("Values don't match with " + mode.name().toLowerCase() + " tolerance=<" + tolerance + ">", expectedValue, act, tol);
        } else {
            Assert.fail("Actual value should be a long but was a " + (actualValue == null ? null : actualValue.getClass().getName()));
        }
    }
}
