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

    public static double getOptionPriceSabr(double underlayingSpot, double strike, double vola, double years, double intrest, double dividend, OptionType type, double strikeDistance)
            throws MathException, IllegalArgumentException {

        if (years <= 0 ) {
            return getIntrinsicValue(underlayingSpot, strike, type);
        } else if (years < 1.0/365.0){
            return getOptionPriceBS(underlayingSpot, strike, vola, years, intrest, dividend, type); //sabr evaluates to zero on the last day before expiration
        }

        double forward = getForward(underlayingSpot, years, intrest, dividend);
        double days = years * 365;

        double rhoCall = Math.exp(0.5623 - 0.02989 * Math.log(days) - 0.01095 * days + 0.002167 * Math.log(days) * days) - 2.0;
        double rhoPut = Math.exp(1.111 - 0.131 * Math.log(days) - 0.062 * days + 0.0128 * Math.log(days) * days) - 2.0;
        double volVolCall = Math.exp(3.332 - 0.65 * Math.log(days) + 0.0325 * days - 0.0048 * Math.log(days) * days) - 2.0;
        double volVolPut = Math.exp(3.589 - 0.908 * Math.log(days) + 0.056 * days - 0.00827 * Math.log(days) * days) - 2.0;

        double sabrVola;
        if (OptionType.CALL.equals(type)) {
            double callAtmVola = VolatilityUtil.getCallAtmVola(underlayingSpot, vola, years, intrest, dividend, strikeDistance, beta, rhoCall, volVolCall, rhoPut, volVolPut);
            sabrVola = SABRVol.volByAtmVol(forward, strike, callAtmVola, years, beta, rhoCall, volVolCall);
        } else {
            double putAtmVola = VolatilityUtil.getPutAtmVola(underlayingSpot, vola, years, intrest, dividend, strikeDistance, beta, rhoCall, volVolCall, rhoPut, volVolPut);
            sabrVola = SABRVol.volByAtmVol(forward, strike, putAtmVola, years, beta, rhoPut, volVolPut);
        }

        return getOptionPriceBS(underlayingSpot, strike, sabrVola, years, intrest, dividend, type);
    }

    public static double getOptionPriceSabr(StockOption stockOption, double underlayingSpot, double vola) throws MathException, IllegalArgumentException {

        StockOptionFamily family = (StockOptionFamily) stockOption.getSecurityFamily();

        double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / MILLISECONDS_PER_YEAR;


        return getOptionPriceSabr(underlayingSpot, stockOption.getStrike().doubleValue(), vola, years,
                family.getIntrest(), family.getDividend(), stockOption.getType(), family.getStrikeDistance());
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

    /**
     * Newton Rapson Method
     * about as fast as getVolatility()
     */
    public static double getVolatilityNR(final double underlayingSpot, final double strike, final double currentValue, final double years, final double intrest, final double dividend, final OptionType type) throws MathException {

        double e = 0.1;

        double vi = Math.sqrt(Math.abs(Math.log(strike / strike) + intrest * years) * 2 / years);
        double ci = getOptionPriceBS(underlayingSpot, strike, vi, years, intrest, dividend, type);
        double vegai = getVega(underlayingSpot, strike, vi, years, intrest, dividend);
        double minDiff = Math.abs(currentValue - ci);

        while ((Math.abs(currentValue - ci) >= e) && (Math.abs(currentValue - ci) <= minDiff)) {
            vi = vi - (ci - currentValue) / vegai;
            ci = getOptionPriceBS(underlayingSpot, strike, vi, years, intrest, dividend, type);
            vegai = getVega(underlayingSpot, strike, vi, years, intrest, dividend);
            minDiff = Math.abs(currentValue - ci);
        }

        if (Math.abs(currentValue - ci) < e) {
            return vi;
        } else {
            throw new IllegalArgumentException("cannot calculate volatility");
        }
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

    public static double getDelta(double underlayingSpot, double strike, double volatility, double years, double intrest, double dividend, OptionType type) {

        if (years < 0)
            throw new IllegalArgumentException("years cannot be negative");

        double costOfCarry = intrest - dividend;
        double d1 = (Math.log(underlayingSpot / strike) + (costOfCarry + volatility * volatility / 2) * years) / (volatility * Math.sqrt(years));

        if (OptionType.CALL.equals(type)) {
            return Math.exp((costOfCarry - intrest) * years) * Gaussian.Phi(d1);
        } else {
            return -Math.exp((costOfCarry - intrest) * years) * Gaussian.Phi(-d1);
        }
    }

    public static double getVega(double underlayingSpot, double strike, double volatility, double years, double intrest, double dividend) {

        double costOfCarry = intrest - dividend;
        double d1 = (Math.log(underlayingSpot / strike) + (intrest + volatility * volatility / 2) * years) / (volatility * Math.sqrt(years));

        return underlayingSpot * Math.exp((costOfCarry - intrest) * years) * Gaussian.Phi(d1) * Math.sqrt(years);

    }

    public static double getDelta(StockOption stockOption, double currentValue, double underlayingSpot) throws MathException {

        StockOptionFamily family = (StockOptionFamily) stockOption.getSecurityFamily();

        double strike = stockOption.getStrike().doubleValue();
        double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / MILLISECONDS_PER_YEAR;
        double volatility = getVolatility(underlayingSpot, strike, currentValue, years, family.getIntrest(), family.getDividend(), stockOption.getType());
        return StockOptionUtil.getDelta(underlayingSpot, strike, volatility, years, family.getIntrest(), family.getDividend(), stockOption.getType());

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
