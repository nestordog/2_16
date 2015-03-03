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
 * Value asserter for double values comparing two values with a tolerance.
 * The tolerance can be absolute or relative to the magnitude of the compared values.
 */
public class DoubleAsserter extends AbstractTypeSpecificAsserter<Double> {

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
    protected Class<Double> type() {
        return Double.class;
    }

    @Override
    protected boolean equalValuesTyped(Double expectedValue, Double actualValue) {
        if (expectedValue == actualValue) {
            return true;
        }
        if (expectedValue != null) {
            final double exp = expectedValue;
            if (actualValue != null) {
                if (expectedValue.equals(actualValue)) {
                    return true;
                }
                final double act = actualValue;
                final double tol = mode.getTolerance(tolerance, exp, act);
                return Math.abs(exp - act) <= tol;
            }
        }
        return false;//null==null has been checked with first statement
    }

    @Override
    protected void assertValueTyped(Double expectedValue, Double actualValue) {
        if (!equalValues(expectedValue, actualValue)) {
            if (expectedValue == null || actualValue == null) {
                Assert.assertEquals("Values don't match", expectedValue, actualValue);
            } else {
                final double tol = mode.getTolerance(tolerance, expectedValue, actualValue);
                Assert.assertEquals("Values don't match with " + mode.name().toLowerCase() + " tolerance=<" + tolerance + ">", expectedValue, actualValue, tol);
            }
        }
    }
}
