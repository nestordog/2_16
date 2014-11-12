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
 * Value asserter for double values comparing two values with a tolerance.
 * The tolerance can be absolute or relative to the magnitude of the compared values.
 */
public class DoubleAsserter implements ValueAsserter<Double> {

    public static enum Mode {
        /** Use absolute tolerance for comparison */
        ABSOLUTE,
        /** Use tolerance relative to the magnitude of the smaller value for comparison */
        RELATIVE;

        public double getTolerance(double tolerance, double value1, double value2) {
            if (this == ABSOLUTE) {
                return tolerance;
            }
            return Math.pow(10d, Math.min(log10(value1), log10(value2)) + log10(tolerance));
        }

        private static double log10(double value) {
            return Math.log10(Math.abs(value));
        }
    }

    private final Mode mode;
    private final double tolerance;

    public DoubleAsserter(double tolerance) {
        this(Mode.ABSOLUTE, tolerance);
    }

    public DoubleAsserter(Mode mode, double tolerance) {
        this.mode = mode;
        this.tolerance = tolerance;
    }

    @Override
    public Double convert(String column, String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("[" + column + "] cannot parse double value: " + value, e);
        }
    }

    @Override
    public Class<? extends Double> type() {
        return Double.class;
    }

    @Override
    public boolean equalValues(Double expectedValue, Object actualValue) {
        if (expectedValue == actualValue)
            return true;
        if (expectedValue != null && actualValue instanceof Double) {
            final Double act = (Double) actualValue;
            final double tol = mode.getTolerance(tolerance, expectedValue, act);
            return expectedValue.equals(act) || Math.abs(expectedValue - act) <= tol;
        }
        return false;
    }

    @Override
    public void assertValue(Double expectedValue, Object actualValue) {
        if (actualValue instanceof Double) {
            final Double act = (Double) actualValue;
            final double tol = mode.getTolerance(tolerance, expectedValue, act);
            Assert.assertEquals("Values don't match with " + mode.name().toLowerCase() + " tolerance=<" + tolerance + ">", expectedValue, act, tol);
        } else {
            Assert.fail("Actual value should be a double but was a " + (actualValue == null ? null : actualValue.getClass().getName()));
        }
    }
}
