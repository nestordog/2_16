/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package com.algoTrader.stockOption;

import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.direct.NelderMead;

import com.algoTrader.vo.SABRSmileVO;

/**
 * @author <a href="mailto:eburgene@gmail.com">Emanuel Burgener</a>
 *
 * @version $Revision$ $Date$
 */
public class SABR {

    private static double beta = 0.999;

    public static SABRSmileVO calibrate(final Double[] strikes, final Double[] volatilities, final double atmVol, final double forward, final double years) throws Exception {

        MultivariateRealFunction estimateRhoAndVol = new MultivariateRealFunction() {
            @Override
            public double value(double[] x) {

                double r = x[0];
                double v = x[1];
                double alpha = findAlpha(forward, forward, atmVol, years, beta, x[0], x[1]);
                double sumErrors = 0;

                for (int i=0; i< volatilities.length; i++) {

                    double modelVol = vol(forward, strikes[i], years, alpha, beta, r, v);
                    sumErrors += Math.pow(modelVol - volatilities[i], 2);
                }

                if (Math.abs(r) > 1) {
                    sumErrors = 1e100;
                }

                return sumErrors;
            }
        };

        NelderMead nelderMead = new NelderMead();
        RealPointValuePair result = nelderMead.optimize(estimateRhoAndVol, GoalType.MINIMIZE, new double[] { -0.5, 2.6 });

        double rho = result.getPoint()[0];
        double volVol = result.getPoint()[1];

        SABRSmileVO params = new SABRSmileVO();
        params.setYears(years);
        params.setRho(rho);
        params.setVolVol(volVol);
        params.setAlpha(findAlpha(forward, forward, atmVol, years, beta, rho, volVol));
        params.setAtmVol(atmVol);

        return params;
    }

    public static double volByAtmVol(double forward, double strike, double atmVola, double years, double b, double r, double v) {

        double alpha = findAlpha(forward, strike, atmVola, years, b, r, v);
        return vol(forward, strike, years, alpha, b, r, v);
    }

    private static double vol(double forward, double strike, double years, double a, double b, double r, double v) {

        if (Math.abs(forward - strike) <= 0.001) { // ATM vol

            double term1 = a / Math.pow(forward, (1 - b));
            double term2 = ((1 - b) * (1 - b) / 24 * a * a / Math.pow(forward, (2 - 2 * b)) + r * b * a * v / 4 / Math.pow(forward, (1 - b)) + (2 - 3 * r * r)
                    * v * v / 24);
            return term1 * (1 + term2 * years);

        } else { // Non-ATM vol

            double fk = forward * strike;
            double z = v / a * Math.pow(fk, ((1 - b) / 2)) * Math.log(forward / strike);
            double x = Math.log((Math.sqrt(1 - 2 * r * z + z * z) + z - r) / (1 - r));
            double term1 = a
                    / Math.pow(fk, ((1 - b) / 2))
                    / (1 + (1 - b) * (1 - b) / 24 * Math.pow(Math.log(forward / strike), 2) + Math.pow((1 - b), 4) / 1920
                            * Math.pow(Math.log(forward / strike), 4));

            double term2;
            if (Math.abs(x - z) < 1e-10) {
                term2 = 1;
            } else {
                term2 = z / x;
            }

            double term3 = 1
                    + (Math.pow((1 - b), 2) / 24 * a * a / Math.pow(fk, (1 - b)) + r * b * v * a / 4 / Math.pow(fk, ((1 - b) / 2)) + (2 - 3 * r * r) / 24 * v
                            * v) * years;
            return term1 * term2 * term3;
        }
    }

    private static double findAlpha(double forward, double strike, double atmVol, double years, double b, double r, double v) {

        double constant = -atmVol * Math.pow(forward, 1 - b);
        double linear = 1 + (2 - 3 * Math.pow(r, 2)) * Math.pow(v, 2) * years / 24.0;
        double quadratic = r * b * v * years / 4.0 / Math.pow(forward, 1 - b);
        double cubic = Math.pow(1 - b, 2) * years / 24.0 / Math.pow(forward, 2 - 2 * b);

        return getSmallestRoot(cubic, quadratic, linear, constant);
    }

    // CF Haug, p. 269
    private static double getSmallestRoot(double cubic, double quadratic, double linear, double constant) {

        double a = quadratic / cubic;
        double b = linear / cubic;
        double C = constant / cubic;
        double Q = (Math.pow(a, 2) - 3 * b) / 9.0;
        double r = (2 * Math.pow(a, 3) - 9 * a * b + 27 * C) / 54.0;

        double root = 0;

        if (Math.pow(r, 2) - Math.pow(Q, 3) >= 0) {

            double capA = -Math.signum(r) * Math.pow(Math.abs(r) + Math.sqrt(Math.pow(r, 2) - Math.pow(Q, 3)), 1.0 / 3.0);
            double capB = 0;
            if (capA != 0) {
                capB = Q / capA;
            }
            root = capA + capB - a / 3.0;

        } else {

            double theta = Math.acos(r / Math.pow(Q, 1.5));
            double root1 = -2 * Math.sqrt(Q) * Math.cos(theta / 3.0) - a / 3.0;
            double root2 = -2 * Math.sqrt(Q) * Math.cos(theta / 3.0 + 2.0943951023932) - a / 3.0;
            double root3 = -2 * Math.sqrt(Q) * Math.cos(theta / 3.0 - 2.0943951023932) - a / 3.0;

            // find the smallest positive one
            if (root1 > 0) {
                root = root1;
            } else if (root2 > 0) {
                root = root2;
            } else if (root3 > 0) {
                root = root3;
            }

            if (root2 > 0 && root2 < root) {
                root = root2;
            }
            if (root3 > 0 && root3 < root) {
                root = root3;
            }
        }

        return root;
    }
}
