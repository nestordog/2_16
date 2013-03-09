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

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math.analysis.solvers.UnivariateRealSolver;
import org.apache.commons.math.analysis.solvers.UnivariateRealSolverFactory;

import com.algoTrader.entity.security.StockOption;
import com.algoTrader.entity.security.StockOptionFamily;
import com.algoTrader.enumeration.Duration;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.util.DateUtil;
import com.algoTrader.vo.SABRSmileVO;
import com.algoTrader.vo.SABRSurfaceVO;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class StockOptionUtil {

    private static double beta = 1.0;

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

    public static double getOptionPrice(StockOption stockOption, double underlyingSpot, double vola) {

        StockOptionFamily family = (StockOptionFamily) stockOption.getSecurityFamily();

        double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / Duration.YEAR_1.getValue();

        return getOptionPrice(underlyingSpot, stockOption.getStrike().doubleValue(), vola, years, family.getIntrest(), family.getDividend(), stockOption.getType());
    }

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

        UnivariateRealFunction function = new UnivariateRealFunction() {
            @Override
            public double value(double volatility) throws FunctionEvaluationException {
                return getOptionPrice(underlyingSpot, strike, volatility, years, intrest, dividend, type) - currentValue;
            }
        };

        UnivariateRealSolverFactory factory = UnivariateRealSolverFactory.newInstance();
        UnivariateRealSolver solver = factory.newDefaultSolver();
        solver.setAbsoluteAccuracy(0.0001);

        return solver.solve(function, 0.01, 2.0);
    }

    public static double getImpliedVolatility(StockOption stockOption, double underlyingSpot, final double currentValue) throws MathException {

        StockOptionFamily family = (StockOptionFamily) stockOption.getSecurityFamily();

        double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / Duration.YEAR_1.getValue();

        return getImpliedVolatility(underlyingSpot, stockOption.getStrike().doubleValue(), currentValue, years, family.getIntrest(), family.getDividend(),
                stockOption.getType());
    }

    /**
     * Newton Rapson Method
     * about as fast as getVolatility()
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

    public static double getImpliedVolatilityNR(StockOption stockOption, double underlyingSpot, final double currentValue) throws MathException {

        StockOptionFamily family = (StockOptionFamily) stockOption.getSecurityFamily();

        double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / Duration.YEAR_1.getValue();

        return getImpliedVolatilityNR(underlyingSpot, stockOption.getStrike().doubleValue(), currentValue, years, family.getIntrest(), family.getDividend(),
                stockOption.getType());
    }

    public static double getImpliedVolatilitySABR(final double underlyingSpot, final double strike, final double years, final double intrest, final double dividend,
            final OptionType type, final SABRSurfaceVO surface) throws MathException {

        double forward = getForward(underlyingSpot, years, intrest, dividend);

        // get sabrVolas for all durations at the specified strike
        int i = 0;
        double[] yearsArray = new double[surface.getSmiles().size()];
        double[] volArray = new double[surface.getSmiles().size()];
        for (SABRSmileVO smile : surface.getSmiles()) {

            double vol = SABRVol.volByAtmVol(forward, strike, smile.getAtmVol(), smile.getYears(), beta, smile.getRho(), smile.getVolVol());

            yearsArray[i] = smile.getYears();
            volArray[i] = vol;
            i++;
        }

        // spline interpolation for years
        SplineInterpolator interpolator = new SplineInterpolator();
        PolynomialSplineFunction function = interpolator.interpolate(yearsArray, volArray);

        return function.value(years);
    }

    public static double getImpliedVolatilitySABR(StockOption stockOption, double underlyingSpot, final SABRSurfaceVO surface) throws MathException {

        StockOptionFamily family = (StockOptionFamily) stockOption.getSecurityFamily();

        double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / Duration.YEAR_1.getValue();

        return getImpliedVolatilitySABR(underlyingSpot, stockOption.getStrike().doubleValue(), years, family.getIntrest(), family.getDividend(), stockOption.getType(), surface);
    }

    public static double getIntrinsicValue(double underlyingSpot, double strike, OptionType type) {

        if (OptionType.CALL.equals(type)) {
            return Math.max(underlyingSpot - strike, 0d);
        } else {
            return Math.max(strike - underlyingSpot, 0d);
        }
    }

    public static double getIntrinsicValue(StockOption stockOption, double underlyingSpot) throws RuntimeException {

        return getIntrinsicValue(underlyingSpot, stockOption.getStrike().doubleValue(), stockOption.getType());
    }

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

    public static double getDelta(StockOption stockOption, double currentValue, double underlyingSpot) throws MathException {

        StockOptionFamily family = (StockOptionFamily) stockOption.getSecurityFamily();

        double strike = stockOption.getStrike().doubleValue();
        double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / Duration.YEAR_1.getValue();
        double volatility = getImpliedVolatility(underlyingSpot, strike, currentValue, years, family.getIntrest(), family.getDividend(), stockOption.getType());
        return StockOptionUtil.getDelta(underlyingSpot, strike, volatility, years, family.getIntrest(), family.getDividend(), stockOption.getType());

    }

    public static double getVega(double underlyingSpot, double strike, double volatility, double years, double intrest, double dividend) {

        double costOfCarry = intrest - dividend;
        double d1 = (Math.log(underlyingSpot / strike) + (costOfCarry + volatility * volatility / 2) * years) / (volatility * Math.sqrt(years));
        double n = StandardNormalDensity.n(d1);

        return underlyingSpot * Math.exp((costOfCarry - intrest) * years) * n * Math.sqrt(years);

    }

    public static double getVega(StockOption stockOption, double currentValue, double underlyingSpot) throws MathException {

        StockOptionFamily family = (StockOptionFamily) stockOption.getSecurityFamily();

        double strike = stockOption.getStrike().doubleValue();
        double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / Duration.YEAR_1.getValue();
        double volatility = getImpliedVolatility(underlyingSpot, strike, currentValue, years, family.getIntrest(), family.getDividend(), stockOption.getType());
        return StockOptionUtil.getVega(underlyingSpot, strike, volatility, years, family.getIntrest(), family.getDividend());
    }

    public static double getTheta(double underlyingSpot, double strike, double volatility, double years, double intrest, double dividend, OptionType type) {

        double costOfCarry = intrest - dividend;
        double d1 = (Math.log(underlyingSpot / strike) + (costOfCarry + volatility * volatility / 2) * years) / (volatility * Math.sqrt(years));
        double d2 = d1 - volatility * Math.sqrt(years);

        double term1 = -underlyingSpot * Math.exp((costOfCarry - intrest) * years) * StandardNormalDensity.n(d1) * volatility / (2.0 * Math.sqrt(years));
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

    public static double getTheta(StockOption stockOption, double currentValue, double underlyingSpot) throws MathException {

        StockOptionFamily family = (StockOptionFamily) stockOption.getSecurityFamily();

        double strike = stockOption.getStrike().doubleValue();
        double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / Duration.YEAR_1.getValue();
        double volatility = getImpliedVolatility(underlyingSpot, strike, currentValue, years, family.getIntrest(), family.getDividend(), stockOption.getType());
        return StockOptionUtil.getTheta(underlyingSpot, strike, volatility, years, family.getIntrest(), family.getDividend(), stockOption.getType());

    }

    public static double getTotalMargin(double underlyingSettlement, double strike, double stockOptionSettlement, double years, double intrest,
            double dividend, OptionType type, double marginParameter) throws MathException {

        double marginLevel;
        if (OptionType.CALL.equals(type)) {
            marginLevel = underlyingSettlement * (1.0 + marginParameter);
        } else {
            marginLevel = underlyingSettlement * (1.0 - marginParameter);
        }

        double volatility = StockOptionUtil.getImpliedVolatility(underlyingSettlement, strike, stockOptionSettlement, years, intrest, dividend, type);

        return getOptionPrice(marginLevel, strike, volatility, years, intrest, 0, type);
    }

    public static double getTotalMargin(StockOption stockOption, double stockOptionSettlement, double underlyingSettlement) throws MathException {

        StockOptionFamily family = (StockOptionFamily) stockOption.getSecurityFamily();

        double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / Duration.YEAR_1.getValue();

        return getTotalMargin(underlyingSettlement, stockOption.getStrike().doubleValue(), stockOptionSettlement, years, family.getIntrest(),
                family.getDividend(), stockOption.getType(), family.getMarginParameter());
    }

    public static double getMaintenanceMargin(StockOption stockOption, double stockOptionSettlement, double underlyingSettlement) throws MathException {

        return getTotalMargin(stockOption, stockOptionSettlement, underlyingSettlement) - stockOptionSettlement;
    }

    public static double getForward(double spot, double years, double intrest, double dividend) {

        return spot * Math.exp(years * (intrest - dividend));
    }

    public static double getMoneyness(StockOption stockOption, double underlyingSpot) {

        if (OptionType.CALL.equals(stockOption.getType())) {
            return (underlyingSpot - stockOption.getStrike().doubleValue()) / underlyingSpot;
        } else {
            return (stockOption.getStrike().doubleValue() - underlyingSpot) / underlyingSpot;
        }
    }

    /**
     * Based on FX conventions as reported in "FX Volatility Smile Construction" by Dimitri Reiswich and Uwe Wystrup
     */
    public static double getStrikeByDelta(double delta, double impliedVol, double years, double forward, double intrest, OptionType type) {

        double midTerm = 1.0;
        if (years <= 1) {
            midTerm = Math.exp(intrest * years);
        }

        return forward * Math.exp((OptionType.CALL.equals(type) ? -1.0 : 1.0) * Gaussian.PhiInverse(midTerm * delta) * impliedVol * Math.sqrt(years) + 0.5 * Math.pow(impliedVol, 2) * years);
    }
}
