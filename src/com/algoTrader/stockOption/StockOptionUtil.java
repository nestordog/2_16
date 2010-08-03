package com.algoTrader.stockOption;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.solvers.UnivariateRealSolver;
import org.apache.commons.math.analysis.solvers.UnivariateRealSolverFactory;

import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.EsperService;
import com.algoTrader.util.PropertiesUtil;

public class StockOptionUtil {

    private static final double MILLISECONDS_PER_YEAR = 31536000000l;
    private static final double DAYS_PER_YEAR = 365;

    private static double intrest = PropertiesUtil.getDoubleProperty("intrest");
    private static double dividend = PropertiesUtil.getDoubleProperty("dividend");
    private static double marginParameter = PropertiesUtil.getDoubleProperty("marginParameter");
    private static double spreadSlope = PropertiesUtil.getDoubleProperty("spreadSlope");
    private static double spreadConstant = PropertiesUtil.getDoubleProperty("spreadConstant");
    private static double expirationTimeFactor = PropertiesUtil.getDoubleProperty("expirationTimeFactor");
    private static double expectedProfit = PropertiesUtil.getDoubleProperty("expectedProfit");
    private static long minExpirationTime = PropertiesUtil.getIntProperty("minExpirationTime");
    private static boolean sabrEnabled = PropertiesUtil.getBooleanProperty("sabrEnabled");

    public static double getOptionPrice(Security security, double underlayingSpot, double vola) throws ConvergenceException, FunctionEvaluationException, IllegalArgumentException  {

        if (sabrEnabled) {
            return getOptionPriceSabr(security, underlayingSpot, vola);
        } else {
            return getOptionPriceBS(security, underlayingSpot, vola);
        }
    }

    public static double getOptionPriceSabr(Security security, double underlayingSpot, double vola) throws ConvergenceException, FunctionEvaluationException, IllegalArgumentException  {

        StockOption stockOption = (StockOption)security;

        double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / MILLISECONDS_PER_YEAR ;
        if (years <= 0 ) {
            return getIntrinsicValue(underlayingSpot, stockOption.getStrike().doubleValue(), stockOption.getType());
        } else if (years < 1.0/365.0){
            return getOptionPriceBS(security, underlayingSpot, vola); //sabr
        }

        double atmVola = Volatility.getAtmVola(underlayingSpot, vola, years);
        double forward = underlayingSpot * (1 - years * dividend) * Math.exp(years * intrest);
        double sabrVola = Sabr.getSabrVolatility(stockOption.getStrike().doubleValue(), forward, years, atmVola);

        return getOptionPriceBS(underlayingSpot, stockOption.getStrike().doubleValue(), sabrVola, years, intrest, dividend, stockOption.getType());
    }

    /**
    /*Black-Scholes formula
     */
    public static double getOptionPriceBS(double underlayingSpot, double strike, double volatility, double years, double intrest, double dividend, OptionType type) {

        if (years <= 0 ) {
            return getIntrinsicValue(underlayingSpot, strike, type);
        }

        double adjustedSpot = underlayingSpot * Math.exp(-dividend * years);
        double d1 = (Math.log(adjustedSpot/strike) + (intrest + volatility * volatility/2) * years) / (volatility * Math.sqrt(years));
        double d2 = d1 - volatility * Math.sqrt(years);

        if (OptionType.CALL.equals(type)) {
            return adjustedSpot * Gaussian.Phi(d1) - strike * Math.exp(-intrest * years) * Gaussian.Phi(d2);
        } else {
            return strike * Math.exp(-intrest * years) * Gaussian.Phi(-d2) - adjustedSpot * Gaussian.Phi(-d1);
        }
    }

    public static double getOptionPriceBS(Security security, double underlayingSpot, double vola) {

        StockOption stockOption = (StockOption)security;

        double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / MILLISECONDS_PER_YEAR ;

        return getOptionPriceBS(underlayingSpot, stockOption.getStrike().doubleValue(), vola, years, intrest, dividend, stockOption.getType());
    }

    public static double getVolatility(final double underlayingSpot, final double strike, final double optionValue, final double years, final double intrest, final double dividend, final OptionType type) throws ConvergenceException, FunctionEvaluationException {

        UnivariateRealFunction function = new UnivariateRealFunction () {
            public double value(double volatility) throws FunctionEvaluationException {
                return getOptionPriceBS(underlayingSpot, strike, volatility, years, intrest, dividend, type) - optionValue;
            }};

        UnivariateRealSolverFactory factory = UnivariateRealSolverFactory.newInstance();
        UnivariateRealSolver solver = factory.newDefaultSolver();

        return solver.solve(function, 0.08, 0.90, 0.2);
    }

    public static double getVolatility(final double underlayingSpot, final double strike, final double optionValue, final double years, final OptionType type) throws ConvergenceException, FunctionEvaluationException {

        return getVolatility(underlayingSpot, strike, optionValue, years, intrest, dividend, type);
    }

    public static double getIntrinsicValue(double underlayingSpot, double strike, OptionType type) {

        if (OptionType.CALL.equals(type)) {
            return Math.max(underlayingSpot - strike, 0d) ;
        } else {
            return Math.max(strike  - underlayingSpot, 0d);
        }
    }

    public static double getIntrinsicValue(Security security, double underlayingSpot) throws RuntimeException {

        StockOption stockOption = (StockOption)security;

        return getIntrinsicValue(underlayingSpot, stockOption.getStrike().doubleValue(), stockOption.getType());
    }

    public static double getExitValue(Security security, double underlayingSpot, double optionValue) throws ConvergenceException, FunctionEvaluationException {

        StockOption stockOption = (StockOption)security;

        double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / MILLISECONDS_PER_YEAR ;

        double volatility = getVolatility(underlayingSpot, stockOption.getStrike().doubleValue(), optionValue, years, intrest, dividend, stockOption.getType());

        double exitLevel;
        if (OptionType.CALL.equals(stockOption.getType())) {
            double callVolaPeriod = (Double)EsperService.getVariableValue("callVolaPeriod");
            exitLevel = underlayingSpot * (1 + volatility / Math.sqrt(DAYS_PER_YEAR / callVolaPeriod));
        } else {
            double putVolaPeriod = (Double)EsperService.getVariableValue("putVolaPeriod");
            exitLevel = underlayingSpot * (1 - volatility / Math.sqrt(DAYS_PER_YEAR / putVolaPeriod));
        }

        return getOptionPrice(stockOption, exitLevel, volatility);
    }

    public static double getMargin(double underlayingSpot, double strike, double settlement, double years, OptionType type) throws ConvergenceException, FunctionEvaluationException {

        double marginLevel;
        if (OptionType.CALL.equals(type)) {
            marginLevel = underlayingSpot * (1.0 + marginParameter);
        } else {
            marginLevel = underlayingSpot * (1.0 - marginParameter);
        }

        // in margin calculations the dividend is not used!

        double volatility = StockOptionUtil.getVolatility(underlayingSpot, strike , settlement, years, intrest, 0, type);

        return getOptionPriceBS(marginLevel, strike, volatility, years, intrest, 0, type);
    }

    public static double getMargin(StockOption stockOption, double settlement, double underlayingSpot) throws ConvergenceException, FunctionEvaluationException {

        double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / MILLISECONDS_PER_YEAR ;

        return getMargin(underlayingSpot, stockOption.getStrike().doubleValue(), settlement, years, stockOption.getType());
    }

    public static double getDummyBid(double meanValue) {

        double spread = meanValue * spreadSlope + spreadConstant;
        return meanValue - (spread / 2.0);
    }

    public static double getDummyAsk(double meanValue) {

        double spread = meanValue * spreadSlope + spreadConstant;
        return meanValue + (spread / 2.0);
    }

    public static boolean isExpirationTimeToLong(Security security, double currentValue, double settlement, double underlayingSpot) throws ConvergenceException, FunctionEvaluationException {

        StockOption stockOption = (StockOption)security;

        OptionType type = stockOption.getType();
        double strike = stockOption.getStrike().doubleValue();
        long expirationMillis = stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime();
        double expirationYears = expirationMillis / MILLISECONDS_PER_YEAR ;

        double volatility = getVolatility(underlayingSpot, strike , settlement, expirationYears, intrest, dividend, type);
        double margin = getMargin(stockOption, settlement, underlayingSpot);
        double intrinsicValue = getOptionPriceBS(underlayingSpot, strike, volatility, 0, intrest, dividend, type);

        long expectedExpirationMillis = (long)(Math.log((margin - intrinsicValue) / (margin - currentValue)) / expectedProfit * MILLISECONDS_PER_YEAR);

        if (expectedExpirationMillis < 0) {
            return false;
        } else if (expirationMillis < minExpirationTime) {
            return false;
        } else if (strike > underlayingSpot) {
            return false;
        } else if (expectedExpirationMillis > expirationTimeFactor * expirationMillis ) {
            return false;
        } else {
            return true;
        }
    }
}
