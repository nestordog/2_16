package com.algoTrader.stockOption;

import java.util.Date;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.solvers.UnivariateRealSolver;
import org.apache.commons.math.analysis.solvers.UnivariateRealSolverFactory;

import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.PropertiesUtil;

public class StockOptionUtil {

    private static final double MILLISECONDS_PER_YEAR = 31536000000l;
    private static final double DAYS_PER_YEAR = 365;

    private static double intrest = PropertiesUtil.getDoubleProperty("intrest");
    private static double dividend = PropertiesUtil.getDoubleProperty("dividend");
    private static double volaPeriod = PropertiesUtil.getDoubleProperty("volaPeriod");
    private static double marginParameter = PropertiesUtil.getDoubleProperty("marginParameter");
    private static double spreadSlope = PropertiesUtil.getDoubleProperty("spreadSlope");
    private static double spreadConstant = PropertiesUtil.getDoubleProperty("spreadConstant");
    private static double expirationTimeFactor = PropertiesUtil.getDoubleProperty("expirationTimeFactor");
    private static double expectedProfit = PropertiesUtil.getDoubleProperty("expectedProfit");
    private static long minExpirationTime = PropertiesUtil.getIntProperty("minExpirationTime");

    // Black-Scholes formula
    public static double getOptionPrice(double spot, double strike, double volatility, double years, double intrest, double dividend, OptionType type) {

        if (years < 0 ) years = 0;

        double adjustedSpot = spot * Math.exp(-dividend * years);
        double d1 = (Math.log(adjustedSpot/strike) + (intrest + volatility * volatility/2) * years) / (volatility * Math.sqrt(years));
        double d2 = d1 - volatility * Math.sqrt(years);

        if (OptionType.CALL.equals(type)) {
            return adjustedSpot * Gaussian.Phi(d1) - strike * Math.exp(-intrest * years) * Gaussian.Phi(d2);
        } else {
            return strike * Math.exp(-intrest * years) * Gaussian.Phi(-d2) - adjustedSpot * Gaussian.Phi(-d1);
        }
    }

    public static double getVolatility(final double spot, final double strike, final double optionValue, final double years, final double intrest, final double dividend, final OptionType type) throws ConvergenceException, FunctionEvaluationException {

        UnivariateRealFunction function = new UnivariateRealFunction () {
            public double value(double volatility) throws FunctionEvaluationException {
                return getOptionPrice(spot, strike, volatility, years, intrest, dividend, type) - optionValue;
            }};

        UnivariateRealSolverFactory factory = UnivariateRealSolverFactory.newInstance();
        UnivariateRealSolver solver = factory.newDefaultSolver();

        return solver.solve(function, 0.1, 0.99, 0.2);
    }

    public static double getFairValue(Security security, double spot, double vola) throws RuntimeException {

        StockOption stockOption = (StockOption)security;
        Date currentTime = DateUtil.getCurrentEPTime();

        double years = (stockOption.getExpiration().getTime() - currentTime.getTime()) / MILLISECONDS_PER_YEAR ;

        return getOptionPrice(spot, stockOption.getStrike().doubleValue(), vola, years, intrest, dividend, stockOption.getType());
    }

    public static double getExitValue(Security security, double spot, double optionValue) throws ConvergenceException, FunctionEvaluationException {

        StockOption stockOption = (StockOption)security;

        double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / MILLISECONDS_PER_YEAR ;

        double volatility = getVolatility(spot, stockOption.getStrike().doubleValue(), optionValue, years, intrest, dividend, stockOption.getType());

        double exitLevel = spot * (1 - volatility / Math.sqrt(DAYS_PER_YEAR / volaPeriod));

        return getFairValue(stockOption, exitLevel, volatility);
    }

    public static double getMargin(StockOption stockOption, double settlement, double underlaying) throws ConvergenceException, FunctionEvaluationException {

        double marginLevel = underlaying * (1.0 - marginParameter);

        double strike = stockOption.getStrike().doubleValue();

        double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / MILLISECONDS_PER_YEAR ;

        double volatility = StockOptionUtil.getVolatility(underlaying, strike , settlement, years, intrest, dividend, stockOption.getType());

        return getOptionPrice(marginLevel, strike, volatility, years, intrest, dividend, stockOption.getType());
    }

    public static double getDummyBid(double meanValue) {

        double spread = meanValue * spreadSlope + spreadConstant;
        return meanValue - (spread / 2.0);
    }

    public static double getDummyAsk(double meanValue) {

        double spread = meanValue * spreadSlope + spreadConstant;
        return meanValue + (spread / 2.0);
    }

    public static boolean isExpirationTimeToLong(Security security, double currentValue, double settlement, double underlaying) throws ConvergenceException, FunctionEvaluationException {

        StockOption stockOption = (StockOption)security;

        OptionType type = stockOption.getType();
        double strike = stockOption.getStrike().doubleValue();
        long expirationMillis = stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime();
        double expirationYears = expirationMillis / MILLISECONDS_PER_YEAR ;

        double volatility = getVolatility(underlaying, strike , settlement, expirationYears, intrest, dividend, type);
        double margin = getMargin(stockOption, settlement, underlaying);
        double intrinsicValue = getOptionPrice(underlaying, strike, volatility, 0, intrest, dividend, type);

        long expectedExpirationMillis = (long)(Math.log((margin - intrinsicValue) / (margin - currentValue)) / expectedProfit * MILLISECONDS_PER_YEAR);

        if (expectedExpirationMillis < 0) {
            return false;
        } else if (expirationMillis < minExpirationTime) {
            return false;
        } else if (strike > underlaying) {
            return false;
        } else if (expectedExpirationMillis > expirationTimeFactor * expirationMillis ) {
            return false;
        } else {
            return true;
        }
    }
}
