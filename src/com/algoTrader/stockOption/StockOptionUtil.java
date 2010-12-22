package com.algoTrader.stockOption;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.solvers.UnivariateRealSolver;
import org.apache.commons.math.analysis.solvers.UnivariateRealSolverFactory;

import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.StockOptionFamily;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.sabr.SABRVol;
import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.DateUtil;

public class StockOptionUtil {

    private static final double MILLISECONDS_PER_YEAR = 31536000000l;
    private static boolean sabrEnabled = ConfigurationUtil.getBaseConfig().getBoolean("sabrEnabled");
    private static double beta = ConfigurationUtil.getBaseConfig().getDouble("sabrBeta");

    public static double getOptionPrice(StockOption stockOption, double underlayingSpot, double vola) throws MathException, IllegalArgumentException {

        if (sabrEnabled) {
            return getOptionPriceSabr(stockOption, underlayingSpot, vola);
        } else {
            return getOptionPriceBS(stockOption, underlayingSpot, vola);
        }
    }

    public static double getOptionPriceSabr(double underlayingSpot, double strike, double vola, double years, double intrest, double dividend, OptionType type, double strikeDistance, double beta,
            double correlation, double volVol) throws MathException, IllegalArgumentException {

        if (years <= 0 ) {
            return getIntrinsicValue(underlayingSpot, strike, type);
        } else if (years < 1.0/365.0){
            return getOptionPriceBS(underlayingSpot, strike, vola, years, intrest, dividend, type); //sabr evaluates to zero on the last day before expiration
        }

        double atmVola = Volatility.getAtmVola(underlayingSpot, vola, years, intrest, dividend, strikeDistance, beta, correlation, volVol);
        double forward = getForward(underlayingSpot, years, intrest, dividend);
        double sabrVola = SABRVol.volByAtmVol(forward, strike, atmVola, years, beta, correlation, volVol);

        return getOptionPriceBS(underlayingSpot, strike, sabrVola, years, intrest, dividend, type);
    }

    public static double getOptionPriceSabr(StockOption stockOption, double underlayingSpot, double vola) throws MathException, IllegalArgumentException {

        StockOptionFamily family = (StockOptionFamily) stockOption.getSecurityFamily();

        double correlation = Volatility.getCorrelation(stockOption);
        double volVol = Volatility.getVolVol(stockOption);

        double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / MILLISECONDS_PER_YEAR ;

        return getOptionPriceSabr(underlayingSpot, stockOption.getStrike().doubleValue(), vola, years,
                family.getIntrest(), family.getDividend(), stockOption.getType(), family.getStrikeDistance(),
                beta, correlation, volVol);
    }

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

    public static double getOptionPriceBS(StockOption stockOption, double underlayingSpot, double vola) {

        StockOptionFamily family = (StockOptionFamily) stockOption.getSecurityFamily();

        double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / MILLISECONDS_PER_YEAR ;

        return getOptionPriceBS(underlayingSpot, stockOption.getStrike().doubleValue(), vola, years, family.getIntrest(), family.getDividend(), stockOption.getType());
    }

    public static double getVolatility(final double underlayingSpot, final double strike, final double currentValue, final double years, final double intrest, final double dividend, final OptionType type) throws MathException {

        if (years < 0)
            throw new IllegalArgumentException("years cannot be negative");

        double intrinsicValue = getIntrinsicValue(underlayingSpot, strike, type);
        if (currentValue <= intrinsicValue)
            throw new IllegalArgumentException("cannot calculate volatility if optionValue is below intrinsic Value");

        UnivariateRealFunction function = new UnivariateRealFunction () {
            public double value(double volatility) throws FunctionEvaluationException {
                return getOptionPriceBS(underlayingSpot, strike, volatility, years, intrest, dividend, type) - currentValue;
            }};

        UnivariateRealSolverFactory factory = UnivariateRealSolverFactory.newInstance();
        UnivariateRealSolver solver = factory.newDefaultSolver();
        solver.setAbsoluteAccuracy(0.0001);

        return solver.solve(function, 0.01, 0.90);
    }

    public static double getVolatility(StockOption stockOption, double underlayingSpot, final double currentValue) throws MathException {

        StockOptionFamily family = (StockOptionFamily) stockOption.getSecurityFamily();

        double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / MILLISECONDS_PER_YEAR;

        return getVolatility(underlayingSpot, stockOption.getStrike().doubleValue(), currentValue, years, family.getIntrest(), family.getDividend(), stockOption.getType());
    }

    public static double getIntrinsicValue(double underlayingSpot, double strike, OptionType type) {

        if (OptionType.CALL.equals(type)) {
            return Math.max(underlayingSpot - strike, 0d) ;
        } else {
            return Math.max(strike  - underlayingSpot, 0d);
        }
    }

    public static double getIntrinsicValue(StockOption stockOption, double underlayingSpot) throws RuntimeException {

        return getIntrinsicValue(underlayingSpot, stockOption.getStrike().doubleValue(), stockOption.getType());
    }

    public static double getDelta(double underlayingSpot, double strike, double volatility, double years, double intrest, OptionType type) {

        if (years < 0)
            throw new IllegalArgumentException("years cannot be negative");

        double d1 = (Math.log(underlayingSpot/strike) + (intrest + volatility * volatility/2) * years) / (volatility * Math.sqrt(years));

        if (OptionType.CALL.equals(type)) {
            return Gaussian.Phi(d1);
        } else {
            return Gaussian.Phi(d1) -1;
        }
    }

    public static double getDelta(StockOption stockOption, double currentValue, double underlayingSpot) throws MathException {

        StockOptionFamily family = (StockOptionFamily) stockOption.getSecurityFamily();

        double strike = stockOption.getStrike().doubleValue();
        double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / MILLISECONDS_PER_YEAR;
        double volatility = getVolatility(underlayingSpot, strike, currentValue, years, family.getIntrest(), family.getDividend(), stockOption.getType());
        return StockOptionUtil.getDelta(underlayingSpot, strike, volatility, years, family.getIntrest(), stockOption.getType());

    }

    public static double getTotalMargin(double underlayingSettlement, double strike, double stockOptionSettlement, double years, double intrest, OptionType type, double marginParameter)
            throws MathException {

        double marginLevel;
        if (OptionType.CALL.equals(type)) {
            marginLevel = underlayingSettlement * (1.0 + marginParameter);
        } else {
            marginLevel = underlayingSettlement * (1.0 - marginParameter);
        }

        // in margin calculations the dividend is not used!

        double volatility = StockOptionUtil.getVolatility(underlayingSettlement, strike, stockOptionSettlement, years, intrest, 0, type);

        return getOptionPriceBS(marginLevel, strike, volatility, years, intrest, 0, type);
    }

    public static double getTotalMargin(StockOption stockOption, double stockOptionSettlement, double underlayingSettlement) throws MathException {

        StockOptionFamily family = (StockOptionFamily) stockOption.getSecurityFamily();

        double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / MILLISECONDS_PER_YEAR ;

        return getTotalMargin(underlayingSettlement, stockOption.getStrike().doubleValue(), stockOptionSettlement, years, family.getIntrest(), stockOption.getType(), family.getMarginParameter());
    }

    public static double getMaintenanceMargin(StockOption stockOption, double stockOptionSettlement, double underlayingSettlement) throws MathException {

        return getTotalMargin(stockOption, stockOptionSettlement, underlayingSettlement) - stockOptionSettlement;
    }

    public static double getForward(double spot, double years, double intrest, double dividend) {

        return spot * (1 - years * dividend) * Math.exp(years * intrest);
    }
}
