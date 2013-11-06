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
package ch.algotrader.stockOption;

/**
 * Function to compute the Gaussian pdf (probability density function) and the Gaussian cdf (cumulative density function)
 *
 * <pre>
 * % java Gaussian 820 1019 209
 * 0.17050966869132111
 *
 * % java Gaussian 1500 1019 209
 * 0.9893164837383883
 *
 * % java Gaussian 1500 1025 231
 * 0.9801220907365489
 * </pre>
 *
 * The approximation is accurate to absolute error less than 8 * 10^(-16).
 * Reference: Evaluating the Normal Distribution by George Marsaglia: http://www.jstatsoft.org/v11/a04/paper
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class Gaussian {

    /**
     *  returns phi(x) = standard Gaussian pdf
     */
    public static double phi(double x) {
        return Math.exp(-x * x / 2) / Math.sqrt(2 * Math.PI);
    }

    /**
     *  returns phi(x, mu, signma) = Gaussian pdf with mean mu and stddev sigma
     */
    public static double phi(double x, double mu, double sigma) {
        return phi((x - mu) / sigma) / sigma;
    }

    /**
     *  returns Phi(z) = standard Gaussian cdf using Taylor approximation
     */
    public static double Phi(double z) {
        if (z < -8.0) {
            return 0.0;
        }
        if (z > 8.0) {
            return 1.0;
        }
        double sum = 0.0, term = z;
        for (int i = 3; sum + term != sum; i += 2) {
            sum = sum + term;
            term = term * z * z / i;
        }
        return 0.5 + sum * phi(z);
    }

    /**
     * returns Phi(z, mu, sigma) = Gaussian cdf with mean mu and stddev sigma
     */
    public static double Phi(double z, double mu, double sigma) {
        return Phi((z - mu) / sigma);
    }

    /**
     *  Computes such that Phi(z) = y via bisection search
     */
    public static double PhiInverse(double y) {
        return PhiInverse(y, .00000001, -8, 8);
    }

    // bisection search
    private static double PhiInverse(double y, double delta, double lo, double hi) {
        double mid = lo + (hi - lo) / 2;
        if (hi - lo < delta) {
            return mid;
        }
        if (Phi(mid) > y) {
            return PhiInverse(y, delta, lo, mid);
        } else {
            return PhiInverse(y, delta, mid, hi);
        }
    }

    /**
     * main method for testing
     */
    public static void main(String[] args) {
        double z = Double.parseDouble(args[0]);
        double mu = Double.parseDouble(args[1]);
        double sigma = Double.parseDouble(args[2]);
        System.out.println(Phi(z, mu, sigma));
        double y = Phi(z);
        System.out.println(PhiInverse(y));
    }
}
