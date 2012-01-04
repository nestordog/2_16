package com.algoTrader.sabr;

import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math.analysis.solvers.UnivariateRealSolver;
import org.apache.commons.math.analysis.solvers.UnivariateRealSolverFactory;

public class SABRVol {

    public static double vol(double forward, double strike, double years, double a, double b, double r, double v) {

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

    public static double findAlpha(double forward, double strike, double atmVol, double years, double b, double r, double v) {

        double c0 = -atmVol * Math.pow(forward, (1 - b));
        double c1 = (1 + (2 - 3 * r * r) * v * v * years / 24);
        double c2 = r * b * v * years / 4 / Math.pow(forward, (1 - b));
        double c3 = Math.pow((1 - b), 2) * years / 24 / Math.pow(forward, (2 - 2 * b));

        double[] coefficients = { c0, c1, c2, c3 };

        PolynomialFunction function = new PolynomialFunction(coefficients);

        UnivariateRealSolverFactory factory = UnivariateRealSolverFactory.newInstance();
        UnivariateRealSolver solver = factory.newDefaultSolver();
        solver.setAbsoluteAccuracy(0.0001);

        try {
            return solver.solve(function, 0.0001, 1.0, 0.2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static double volByAtmVol(double forward, double strike, double atmVola, double years, double b, double r, double v) {

        double alpha = findAlpha(forward, strike, atmVola, years, b, r, v);
        return vol(forward, strike, years, alpha, b, r, v);
    }
}
