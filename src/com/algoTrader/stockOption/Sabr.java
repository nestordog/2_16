/*
 Copyright (C) 2008 Richard Gomes

 This source code is release under the BSD License.

 This file is part of JQuantLib, a free-software/open-source library
 for financial quantitative analysts and developers - http://jquantlib.org/

 JQuantLib is free software: you can redistribute it and/or modify it
 under the terms of the JQuantLib license.  You should have received a
 copy of the license along with this program; if not, please email
 <jquant-devel@lists.sourceforge.net>. The license is also available online at
 <http://www.jquantlib.org/index.php/LICENSE.TXT>.

 This program is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE.  See the license for more details.

 JQuantLib is based on QuantLib. http://quantlib.org/
 When applicable, the original copyright notice follows this notice.
 */

package com.algoTrader.stockOption;

import java.sql.Time;

import com.algoTrader.util.PropertiesUtil;

/**
 * Implements the Black equivalent volatility for the S.A.B.R. model.
 *
 * @author <Richard Gomes>
 *
 */
public class Sabr {

    public final static double QL_EPSILON = Math.ulp(1.0);
    private static double beta = PropertiesUtil.getDoubleProperty("strategie.beta");
    private static double volVol = PropertiesUtil.getDoubleProperty("strategie.volVol");
    private static double correlation = PropertiesUtil.getDoubleProperty("strategie.correlation");

    /**
     *
     * Computes the S.A.B.R. volatility
     * <p>
     * Checks S.A.B.R. model parameters using {@code
     * #validateSabrParameters(Real, Real, Real, Real)}
     * <p>
     * Checks the terms and conditions;
     * <ol>
     * <li><code>strike</code> > 0.0</li>
     * <li><code>forward</code> > 0.0</li>
     * <li><code>expiryTime</code> >= 0.0</li>
     * </ol>
     *
     * @param strike
     * @param forward
     * @param years
     * @param atmVola
     * @param beta
     * @param volVol
     * @param correlation
     * @return
     *
     * @see #unsafeSabrVolatility(Rate, Rate, Time, Real, Real, Real, Real)
     * @see #validateSabrParameters(Real, Real, Real, Real)
     */
    public static double getSabrVolatility(final double strike, final double forward, final double years, final double atmVola, final double beta, final double volVol, final double correlation) {

        if (strike < 0.0)
            throw new IllegalArgumentException("strike must be positive");
        if (forward < 0.0)
            throw new IllegalArgumentException("forward must be positive");
        if (years <= 0.0)
            throw new IllegalArgumentException("expiry time must be non-negative");
        if (atmVola <= 0.0)
            throw new IllegalArgumentException("alpha must be positive");
        if (beta < 0.0 || beta > 1.0)
            throw new IllegalArgumentException("beta must be in (0.0, 1.0)");
        if (volVol < 0.0)
            throw new IllegalArgumentException("nu must be non negative");
        if (correlation * correlation >= 1.0)
            throw new IllegalArgumentException("rho square must be less than one");

        final double oneMinusBeta = 1.0 - beta;
        final double A = Math.pow(forward * strike, oneMinusBeta);
        final double sqrtA = Math.sqrt(A);
        double logM;
        if (!isClose(forward, strike))
            logM = Math.log(forward / strike);
        else {
            final double epsilon = (forward - strike) / strike;
            logM = epsilon - .5 * epsilon * epsilon;
        }
        final double z = (volVol / atmVola) * sqrtA * logM;
        final double B = 1.0 - 2.0 * correlation * z + z * z;
        final double C = oneMinusBeta * oneMinusBeta * logM * logM;
        final double tmp = (Math.sqrt(B) + z - correlation) / (1.0 - correlation);
        final double xx = Math.log(tmp);
        final double D = sqrtA * (1.0 + C / 24.0 + C * C / 1920.0);
        final double d = 1.0 + years * (oneMinusBeta * oneMinusBeta * atmVola * atmVola / (24.0 * A) + 0.25 * correlation * beta * volVol * atmVola / sqrtA + (2.0 - 3.0 * correlation * correlation) * (volVol * volVol / 24.0));

        double multiplier;
        // computations become precise enough if the square of z worth
        // slightly more than the precision machine (hence the m)
        final double m = 10;
        if (Math.abs(z * z) > QL_EPSILON * m)
            multiplier = z / xx;
        else {
            final double talpha = (0.5 - correlation * correlation) / (1.0 - correlation);
            final double tbeta = atmVola - .5;
            final double tgamma = correlation / (1 - correlation);
            multiplier = 1.0 - beta * z + (tgamma - talpha + tbeta * tbeta * .5) * z * z;
        }
        return (atmVola / D) * multiplier * d;

    }

    public static double getSabrVolatility(final double strike, final double forward, final double years, final double atmVola) {

        return getSabrVolatility(strike, forward, years, atmVola, beta, volVol, correlation);
    }

    private static boolean isClose(final double x, final double y) {
        final double diff = Math.abs(x - y);
        final double tolerance = 42 * QL_EPSILON;
        return diff <= tolerance * Math.abs(x) && diff <= tolerance * Math.abs(y);
    }
}
