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
package ch.algotrader.option;

import java.util.Date;

import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math.analysis.solvers.UnivariateRealSolver;
import org.apache.commons.math.analysis.solvers.UnivariateRealSolverFactory;

import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.OptionFamily;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.OptionType;

/**
 * Utility class containing static methods around {@link Option Options}.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class OptionUtil {

    private static final double beta = 0.999;

    /**
     * Gets the fair-price of a {@link Option} based on the price of the {@code underlyingSpot} and {@code volatility}.
     * {@code duration}, {@code intrest} and {@code dividend} are retrieved from the {@link OptionFamily}.
     */
    public static double getOptionPrice(Option option, double underlyingSpot, double vola, Date now) {

        OptionFamily family = (OptionFamily) option.getSecurityFamily();

        double years = (option.getExpiration().getTime() - now.getTime()) / (double) Duration.YEAR_1.getValue();

        return getOptionPrice(underlyingSpot, option.getStrike().doubleValue(), vola, years, family.getIntrest(), family.getDividend(), option.getType());
    }

    /**
     * Gets the fair-price of a {@link Option}.
     */
    public static double getOptionPrice(double underlyingSpot, double strike, double volatility, double years, double intrest, double dividend, OptionType type) {

        if (years <= 0) {
            return getIntrinsicValue(underlyingSpot, strike, type);
        }

        double costOfCarry = intrest - dividend;
        double d1 = (Math.log(underlyingSpot / strike) + (costOfCarry + volatility * volatility / 2.0) * years) / (volatility * Math.sqrt(years));
        double d2 = d1 - volatility * Math.sqrt(years);
        double term1 = underlyingSpot * Math.exp((costOfCarry - intrest) * years);
        double term2 = strike * Math.exp(-intrest * years);

        double result = 0.0;
        if (OptionType.CALL.equals(type)) {
            result = term1 * Gaussian.Phi(d1) - term2 * Gaussian.Phi(d2);
        } else {
            result = term2 * Gaussian.Phi(-d2) - term1 * Gaussian.Phi(-d1);
        }

        return result;
    }

    /**
     * Gets the implied volatility of a {@link Option} using a {@link UnivariateRealSolverFactory}.
     * {@code duration}, {@code intrest} and {@code dividend} are retrieved from the {@link OptionFamily}.
     */
    public static double getImpliedVolatility(Option option, double underlyingSpot, final double currentValue, Date now) throws MathException {

        OptionFamily family = (OptionFamily) option.getSecurityFamily();

        double years = (option.getExpiration().getTime() - now.getTime()) / (double) Duration.YEAR_1.getValue();

        return getImpliedVolatility(underlyingSpot, option.getStrike().doubleValue(), currentValue, years, family.getIntrest(), family.getDividend(), option.getType());
    }

    /**
     * Gets the implied volatility of a {@link Option} using a {@link UnivariateRealSolverFactory}.
     */
    @SuppressWarnings("deprecation")
    public static double getImpliedVolatility(final double underlyingSpot, final double strike, final double currentValue, final double years, final double intrest,
            final double dividend, final OptionType type) throws MathException {

        if (years < 0) {
            throw new IllegalArgumentException("years cannot be negative");
        }

        double intrinsicValue = getIntrinsicValue(underlyingSpot, strike, type);
        if (currentValue <= intrinsicValue) {
            throw new IllegalArgumentException("cannot calculate volatility if optionValue is below intrinsic Value");
        }

        UnivariateRealFunction function = volatility -> getOptionPrice(underlyingSpot, strike, volatility, years, intrest, dividend, type) - currentValue;

        UnivariateRealSolverFactory factory = UnivariateRealSolverFactory.newInstance();
        UnivariateRealSolver solver = factory.newDefaultSolver();
        solver.setAbsoluteAccuracy(0.0001);

        return solver.solve(function, 0.01, 2.0);
    }

    /**
     * Gets the implied volatility of a {@link Option} using the Newton Rapson Method.
     * {@code duration}, {@code intrest} and {@code dividend} are retrieved from the {@link OptionFamily}.
     */
    public static double getImpliedVolatilityNR(Option option, double underlyingSpot, double currentValue, Date now) throws MathException {

        OptionFamily family = (OptionFamily) option.getSecurityFamily();

        double years = (option.getExpiration().getTime() - now.getTime()) / (double) Duration.YEAR_1.getValue();

        return getImpliedVolatilityNR(underlyingSpot, option.getStrike().doubleValue(), currentValue, years, family.getIntrest(), family.getDividend(), option.getType());
    }

    /**
     * Gets the implied volatility of a {@link Option} using the Newton Rapson Method.
     */
    public static double getImpliedVolatilityNR(final double underlyingSpot, final double strike, final double currentValue, final double years,
            final double intrest, final double dividend, final OptionType type) throws MathException {

        double e = 0.1;

        double vi = Math.sqrt(Math.abs(Math.log(strike / strike) + intrest * years) * 2 / years);
        double ci = getOptionPrice(underlyingSpot, strike, vi, years, intrest, dividend, type);
        double vegai = getVega(underlyingSpot, strike, vi, years, intrest, dividend);
        double minDiff = Math.abs(currentValue - ci);

        while ((Math.abs(currentValue - ci) >= e) && (Math.abs(currentValue - ci) <= minDiff)) {
            vi = vi - (ci - currentValue) / vegai;
            ci = getOptionPrice(underlyingSpot, strike, vi, years, intrest, dividend, type);
            vegai = getVega(underlyingSpot, strike, vi, years, intrest, dividend);
            minDiff = Math.abs(currentValue - ci);
        }

        if (Math.abs(currentValue - ci) < e) {
            return vi;
        } else {
            throw new IllegalArgumentException("cannot calculate volatility");
        }
    }

    /**
     * Gets the implied volatility of a {@link Option} based on a {@link SABRSurfaceVO}.
     * {@code duration}, {@code intrest} and {@code dividend} are retrieved from the {@link OptionFamily}.
     */
    public static double getImpliedVolatilitySABR(final Option option, double underlyingSpot, final SABRSurfaceVO surface, final Date now) throws MathException {

        OptionFamily family = (OptionFamily) option.getSecurityFamily();

        double years = (option.getExpiration().getTime() - now.getTime()) / (double) Duration.YEAR_1.getValue();

        return getImpliedVolatilitySABR(underlyingSpot, option.getStrike().doubleValue(), years, family.getIntrest(), family.getDividend(), option.getType(), surface);
    }

    /**
     * Gets the implied volatility of a {@link Option} based on a {@link SABRSurfaceVO}.
     */
    public static double getImpliedVolatilitySABR(final double underlyingSpot, final double strike, final double years, final double intrest, final double dividend,
            final OptionType type, final SABRSurfaceVO surface) throws MathException {

        double forward = getForward(underlyingSpot, years, intrest, dividend);

        // get sabrVolas for all durations at the specified strike
        int i = 0;
        double[] yearsArray = new double[surface.getSmiles().size()];
        double[] volArray = new double[surface.getSmiles().size()];
        for (SABRSmileVO smile : surface.getSmiles()) {

            double vol = SABR.volByAtmVol(forward, strike, smile.getAtmVol(), smile.getYears(), beta, smile.getRho(), smile.getVolVol());

            yearsArray[i] = smile.getYears();
            volArray[i] = vol;
            i++;
        }

        // spline interpolation for years
        SplineInterpolator interpolator = new SplineInterpolator();
        PolynomialSplineFunction function = interpolator.interpolate(yearsArray, volArray);

        return function.value(years);
    }

    /**
     * Gets the intrinsic value of a {@link Option}.
     */
    public static double getIntrinsicValue(Option option, double underlyingSpot) throws RuntimeException {

        return getIntrinsicValue(underlyingSpot, option.getStrike().doubleValue(), option.getType());
    }

    /**
     * Gets the intrinsic value of a {@link Option}.
     */
    public static double getIntrinsicValue(double underlyingSpot, double strike, OptionType type) {

        if (OptionType.CALL.equals(type)) {
            return Math.max(underlyingSpot - strike, 0d);
        } else {
            return Math.max(strike - underlyingSpot, 0d);
        }
    }

    /**
     * Gets the delta of a {@link Option}
     * {@code duration}, {@code intrest} and {@code dividend} are retrieved from the {@link OptionFamily}.
     */
    public static double getDelta(Option option, double currentValue, double underlyingSpot, final Date now) throws MathException {

        OptionFamily family = (OptionFamily) option.getSecurityFamily();

        double strike = option.getStrike().doubleValue();
        double years = (option.getExpiration().getTime() - now.getTime()) / (double) Duration.YEAR_1.getValue();
        double volatility = getImpliedVolatility(underlyingSpot, strike, currentValue, years, family.getIntrest(), family.getDividend(), option.getType());
        return OptionUtil.getDelta(underlyingSpot, strike, volatility, years, family.getIntrest(), family.getDividend(), option.getType());

    }

    /**
     * Gets the delta of a {@link Option}
     */
    public static double getDelta(double underlyingSpot, double strike, double volatility, double years, double intrest, double dividend, OptionType type) {

        if (years < 0) {
            throw new IllegalArgumentException("years cannot be negative");
        }

        double costOfCarry = intrest - dividend;
        double d1 = (Math.log(underlyingSpot / strike) + (costOfCarry + volatility * volatility / 2) * years) / (volatility * Math.sqrt(years));

        if (OptionType.CALL.equals(type)) {
            return Math.exp((costOfCarry - intrest) * years) * Gaussian.Phi(d1);
        } else {
            return -Math.exp((costOfCarry - intrest) * years) * Gaussian.Phi(-d1);
        }
    }

    /**
     * Gets the vega of a {@link Option}
     * {@code duration}, {@code intrest} and {@code dividend} are retrieved from the {@link OptionFamily}.
     */
    public static double getVega(Option option, double currentValue, double underlyingSpot, final Date now) throws MathException {

        OptionFamily family = (OptionFamily) option.getSecurityFamily();

        double strike = option.getStrike().doubleValue();
        double years = (option.getExpiration().getTime() - now.getTime()) / (double) Duration.YEAR_1.getValue();
        double volatility = getImpliedVolatility(underlyingSpot, strike, currentValue, years, family.getIntrest(), family.getDividend(), option.getType());
        return OptionUtil.getVega(underlyingSpot, strike, volatility, years, family.getIntrest(), family.getDividend());
    }

    /**
     * Gets the vega of a {@link Option}
     */
    public static double getVega(double underlyingSpot, double strike, double volatility, double years, double intrest, double dividend) {

        double costOfCarry = intrest - dividend;
        double d1 = (Math.log(underlyingSpot / strike) + (costOfCarry + volatility * volatility / 2) * years) / (volatility * Math.sqrt(years));
        double n = standardNormalDensity(d1);

        return underlyingSpot * Math.exp((costOfCarry - intrest) * years) * n * Math.sqrt(years);

    }

    /**
     * Gets the theta of a {@link Option}
     * {@code duration}, {@code intrest} and {@code dividend} are retrieved from the {@link OptionFamily}.
     */
    public static double getTheta(Option option, double currentValue, double underlyingSpot, Date now) throws MathException {

        OptionFamily family = (OptionFamily) option.getSecurityFamily();

        double strike = option.getStrike().doubleValue();
        double years = (option.getExpiration().getTime() - now.getTime()) / (double) Duration.YEAR_1.getValue();
        double volatility = getImpliedVolatility(underlyingSpot, strike, currentValue, years, family.getIntrest(), family.getDividend(), option.getType());
        return OptionUtil.getTheta(underlyingSpot, strike, volatility, years, family.getIntrest(), family.getDividend(), option.getType());

    }

    /**
     * Gets the theta of a {@link Option}
     */
    public static double getTheta(double underlyingSpot, double strike, double volatility, double years, double intrest, double dividend, OptionType type) {

        double costOfCarry = intrest - dividend;
        double d1 = (Math.log(underlyingSpot / strike) + (costOfCarry + volatility * volatility / 2) * years) / (volatility * Math.sqrt(years));
        double d2 = d1 - volatility * Math.sqrt(years);

        double term1 = -underlyingSpot * Math.exp((costOfCarry - intrest) * years) * standardNormalDensity(d1) * volatility / (2.0 * Math.sqrt(years));
        double term2 = (costOfCarry - intrest) * underlyingSpot * Math.exp((costOfCarry - intrest) * years);
        double term3 = intrest * strike * Math.exp(-intrest * years);

        if (OptionType.CALL.equals(type)) {
            double N1 = Gaussian.Phi(d1);
            double N2 = Gaussian.Phi(d2);
            return term1 - term2 * N1 - term3 * N2;
        } else {
            double N1 = Gaussian.Phi(-d1);
            double N2 = Gaussian.Phi(-d2);
            return term1 + term2 * N1 + term3 * N2;
        }
    }

    /**
     * Gets the forward price of a {@link Option}
     */
    public static double getForward(double spot, double years, double intrest, double dividend) {

        return spot * Math.exp(years * (intrest - dividend));
    }

    /**
     * Gets the moneyness of a {@link Option}
     */
    public static double getMoneyness(Option option, double underlyingSpot) {

        if (OptionType.CALL.equals(option.getType())) {
            return (underlyingSpot - option.getStrike().doubleValue()) / underlyingSpot;
        } else {
            return (option.getStrike().doubleValue() - underlyingSpot) / underlyingSpot;
        }
    }

    /**
     * Gets the strike of a {@link Option} based on its delta.
     * Based on FX conventions as reported in "FX Volatility Smile Construction" by Dimitri Reiswich and Uwe Wystrup
     */
    public static double getStrikeByDelta(double delta, double impliedVol, double years, double forward, double intrest, OptionType type) {

        double midTerm = 1.0;
        if (years <= 1) {
            midTerm = Math.exp(intrest * years);
        }

        return forward * Math.exp((OptionType.CALL.equals(type) ? -1.0 : 1.0) * Gaussian.PhiInverse(midTerm * delta) * impliedVol * Math.sqrt(years) + 0.5 * Math.pow(impliedVol, 2) * years);
    }

    private static double standardNormalDensity(double input) {

        return 1.0 / Math.sqrt(2.0 * Math.PI) * Math.exp(-(input * input) / 2.0);
    }
}
