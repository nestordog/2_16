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
package ch.algotrader.option;

/**
 *
 * A library of static methods to generate random numbers from
 * different distributions (bernoulli, uniform, gaussian,
 * discrete, and exponential). Also includes a method for
 * shuffling an array.
 *
 * <pre>
 * % java StdRandom 5
 * 90 26.36076 false 8.79269 0
 * 13 18.02210 false 9.03992 1
 * 58 56.41176 true  8.80501 0
 * 29 16.68454 false 8.90827 0
 * 85 86.24712 true  8.95228 0
 * </pre>
 *
 * Remark:
 * <ul>
 * <li>Uses Math.random() which generates a pseudorandom real number in [0, 1) </li>
 * <li>This library does not allow you to set the pseudorandom number seed. See java.util.Random.</li>
 * <li>See http://www.honeylocust.com/RngPack/ for an industrial strength random number generator in Java</li>
 * </ul>
 *  For additional documentation, see <a href="http://www.cs.princeton.edu/introcs/22library">Section 2.2</a> of
 *  <i>Introduction to Programming in Java: An Interdisciplinary Approach</i> by Robert Sedgewick and Kevin Wayne.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class StdRandom {

    /**
     * Returns real number uniformly in [0, 1).
     */
    public static double uniform() {
        return Math.random();
    }

    /**
     * Returns real number uniformly in [a, b).
     */
    public static double uniform(double a, double b) {
        return a + Math.random() * (b - a);
    }

    /**
     * Returns an integer uniformly between 0 and N-1.
     */
    public static int uniform(int N) {
        return (int) (Math.random() * N);
    }

    /**
     * Returns a boolean, which is true with probability p, and false otherwise.
     */
    public static boolean bernoulli(double p) {
        return Math.random() < p;
    }

    /**
     * Returns a boolean, which is true with probability .5, and false otherwise.
     */
    public static boolean bernoulli() {
        return bernoulli(0.5);
    }

    /**
     * Returns a real number with a standard Gaussian distribution.
     */
    public static double gaussian() {
        // use the polar form of the Box-Muller transform
        double r, x, y;
        do {
            x = uniform(-1.0, 1.0);
            y = uniform(-1.0, 1.0);
            r = x * x + y * y;
        } while (r >= 1 || r == 0);
        return x * Math.sqrt(-2 * Math.log(r) / r);

        // Remark:  y * Math.sqrt(-2 * Math.log(r) / r)
        // is an independent random gaussian
    }

    /**
     * Returns a real number from a gaussian distribution with given mean and stddev
     */
    public static double gaussian(double mean, double stddev) {
        return mean + stddev * gaussian();
    }

    /**
     * Returns an integer with a geometric distribution with mean 1/p.
     */
    public static int geometric(double p) {
        // using algorithm given by Knuth
        return (int) Math.ceil(Math.log(uniform()) / Math.log(1.0 - p));
    }

    /**
     * Returns an integer with a Poisson distribution with mean lambda.
     */
    public static int poisson(double lambda) {
        // using algorithm given by Knuth
        // see http://en.wikipedia.org/wiki/Poisson_distribution
        int k = 0;
        double p = 1.0;
        double L = Math.exp(-lambda);
        do {
            k++;
            p *= uniform();
        } while (p >= L);
        return k - 1;
    }

    /**
     * Returns a real number with a Pareto distribution with parameter alpha.
     */
    public static double pareto(double alpha) {
        return Math.pow(1 - uniform(), -1.0 / alpha) - 1.0;
    }

    /**
     * Returns a real number with a Cauchy distribution.
     */
    public static double cauchy() {
        return Math.tan(Math.PI * (uniform() - 0.5));
    }

    /**
     * Returns a number from a discrete distribution: i with probability a[i].
     */
    public static int discrete(double[] a) {
        // precondition: sum of array entries equals 1
        double r = Math.random();
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            sum = sum + a[i];
            if (sum >= r) {
                return i;
            }
        }
        //assert (false);
        return -1;
    }

    /**
     * Returns a real number from an exponential distribution with rate lambda.
     */
    public static double exp(double lambda) {
        return -Math.log(1 - Math.random()) / lambda;
    }

    /**
     * Rearranges the elements of an array in random order.
     */
    public static void shuffle(Object[] a) {
        int N = a.length;
        for (int i = 0; i < N; i++) {
            int r = i + uniform(N - i); // between i and N-1
            Object temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }

    /**
     * Rearranges the elements of a double array in random order.
     */
    public static void shuffle(double[] a) {
        int N = a.length;
        for (int i = 0; i < N; i++) {
            int r = i + uniform(N - i); // between i and N-1
            double temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }

    /**
     * Rearranges the elements of an int array in random order.
     */
    public static void shuffle(int[] a) {
        int N = a.length;
        for (int i = 0; i < N; i++) {
            int r = i + uniform(N - i); // between i and N-1
            int temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }

    /**
     * Public main method for testing
     */
    public static void main(String[] args) {
        int N = Integer.parseInt(args[0]);

        double[] t = { .5, .3, .1, .1 };

        for (int i = 0; i < N; i++) {
            System.out.print("%2d " + uniform(100));
            System.out.print("%8.5f " + uniform(10.0, 99.0));
            System.out.print("%5b " + bernoulli(.5));
            System.out.print("%7.5f " + gaussian(9.0, .2));
            System.out.print("%2d " + discrete(t));
            System.out.println();
        }
    }
}
