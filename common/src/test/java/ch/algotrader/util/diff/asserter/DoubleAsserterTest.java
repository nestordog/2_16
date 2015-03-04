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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.algotrader.util.diff.asserter.DoubleAsserter.Mode;

/**
 * Unit test for {@link DoubleAsserter}
 */
public class DoubleAsserterTest {

    private final double TOLERANCE = 1e-4;
    private final DoubleAsserter absAsserter = new DoubleAsserter(Mode.ABSOLUTE, TOLERANCE);
    private final DoubleAsserter relAsserter = new DoubleAsserter(Mode.RELATIVE, TOLERANCE);

    @Test
    public void nullShouldEqualNull() {
        assertTrue("null should equal null", absAsserter.equalValues(null, null));
        assertTrue("null should equal null", relAsserter.equalValues(null, null));
        //should pass w/o exception
        absAsserter.assertValue(null, null);
        relAsserter.assertValue(null, null);
    }

    @Test
    public void nullShouldNotEqualNonNull() {
        assertFalse("null should not equal value", absAsserter.equalValues(null, 1.0));
        assertFalse("null should not equal value", relAsserter.equalValues(null, 1.0));
        assertFalse("value should not equal null", absAsserter.equalValues(1.0, null));
        assertFalse("value should not equal null", relAsserter.equalValues(1.0, null));
        assertFalse("null should not equal NaN", absAsserter.equalValues(null, Double.NaN));
        assertFalse("null should not equal NaN", relAsserter.equalValues(null, Double.NaN));
        assertFalse("NaN should not equal null", absAsserter.equalValues(Double.NaN, null));
        assertFalse("NaN should not equal null", relAsserter.equalValues(Double.NaN, null));
    }
    @Test(expected=AssertionError.class)
    public void nullShouldNotEqualNonNullEx() {
        absAsserter.assertValue(null, 1.0);
    }

    @Test
    public void nanShouldEqualNan() {
        assertTrue("NaN should equal NaN", absAsserter.equalValues(Double.NaN, Double.NaN));
        assertTrue("NaN should equal NaN", relAsserter.equalValues(Double.NaN, Double.NaN));
        //should pass w/o exception
        absAsserter.assertValue(Double.NaN, Double.NaN);
        relAsserter.assertValue(Double.NaN, Double.NaN);
    }

    @Test
    public void shouldPassWithAbsAsserter() {
        assertTrue("values should be equal", absAsserter.equalValues(1.0, 1.0));
        assertTrue("values should be equal", absAsserter.equalValues(1001.0, 1001.0));
        assertTrue("values should be equal with absolute tolerance", absAsserter.equalValues(1.0, 1.0 + TOLERANCE));
        assertTrue("values should be equal with absolute tolerance", absAsserter.equalValues(1.0, 1.0 - TOLERANCE));
        assertTrue("values should be equal with absolute tolerance", absAsserter.equalValues(1001.0, 1001.0 + TOLERANCE));
        assertTrue("values should be equal with absolute tolerance", absAsserter.equalValues(1001.0, 1001.0 - TOLERANCE));
        //should pass w/o exception
        absAsserter.assertValue(1.0, 1.0);
        absAsserter.assertValue(1001.0, 1001.0);
        absAsserter.assertValue(1.0, 1.0 + TOLERANCE);
        absAsserter.assertValue(1.0, 1.0 - TOLERANCE);
        absAsserter.assertValue(1001.0, 1001.0 + TOLERANCE);
        absAsserter.assertValue(1001.0, 1001.0 - TOLERANCE);
    }
    @Test
    public void shouldFailWithAbsAsserter() {
        assertFalse("values should not be equal with absolute tolerance", absAsserter.equalValues(1.0, 1.0 + TOLERANCE*1.01));
        assertFalse("values should not be equal with absolute tolerance", absAsserter.equalValues(1.0, 1.0 - TOLERANCE*1.01));
        assertFalse("values should not be equal with absolute tolerance", absAsserter.equalValues(1001.0, 1001.0 + TOLERANCE*1.01));
        assertFalse("values should not be equal with absolute tolerance", absAsserter.equalValues(1001.0, 1001.0 - TOLERANCE*1.01));
    }
    @Test(expected=AssertionError.class)
    public void shouldFailWithAbsAsserterEx() {
        //should throw exception
        absAsserter.assertValue(1.0, 1.0 + TOLERANCE*1.01);
    }
    @Test
    public void shouldPassWithRelAsserter() {
        assertTrue("values should be equal", relAsserter.equalValues(1.0, 1.0));
        assertTrue("values should be equal", relAsserter.equalValues(1001.0, 1001.0));
        assertTrue("values should be equal with relative tolerance", relAsserter.equalValues(1.0, 1.0 + TOLERANCE));
        assertTrue("values should be equal with relative tolerance", relAsserter.equalValues(1.0 + TOLERANCE, 1.0));
        assertTrue("values should be equal with relative tolerance", relAsserter.equalValues(1001.0, 1001.0 + 1000*TOLERANCE));
        assertTrue("values should be equal with relative tolerance", relAsserter.equalValues(1001.0, 1001.0 - 1000*TOLERANCE));
        //should pass w/o exception
        relAsserter.assertValue(1.0, 1.0);
        relAsserter.assertValue(1001.0, 1001.0);
        relAsserter.assertValue(1.0, 1.0 + TOLERANCE);
        relAsserter.assertValue(1.0 + TOLERANCE, 1.0);
        relAsserter.assertValue(1001.0, 1001.0 + 1000*TOLERANCE);
        relAsserter.assertValue(1001.0 + 1000*TOLERANCE, 1001.0);
    }

    @Test
    public void shouldPassPriceLimitTest() {
        //given
        final double tol = 1e-6;
        final DoubleAsserter asserter = new DoubleAsserter(Mode.ABSOLUTE, tol);
        final double exp = 26.8506556875;
        final double act = 26.850655687500;
        //when + then
        assertTrue("values should be equal with absolute tolerance " + tol + " expected:<"  + exp + "> but was:<" + act + ">", asserter.equalValues(exp, act));
    }

    @Test
    public void shouldPassCommissionTest() {
        //given
        final double relTol = 5e-4;
        final double absTol = 1e-2;
        final DoubleAsserter absAsserter = new DoubleAsserter(Mode.ABSOLUTE, absTol);
        final DoubleAsserter relAsserter = new DoubleAsserter(Mode.RELATIVE, relTol);
        final double exp = 2.4405569926365E9;
        final double act = 2440556992.64;
        //when
        final boolean pass = absAsserter.equalValues(exp, act) || relAsserter.equalValues(exp, act);
        //then
        assertTrue("values should be equal with absolute tolerance " + absTol + " or with relative tolerance " + relTol + " expected:<"  + exp + "> but was:<" + act + ">", pass);
    }
}
