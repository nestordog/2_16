package com.algoTrader.stockOption;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.solvers.UnivariateRealSolver;
import org.apache.commons.math.analysis.solvers.UnivariateRealSolverFactory;

import com.algoTrader.entity.ExitValue;
import com.algoTrader.entity.ExitValueImpl;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.sabr.SABRVol;
import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.DateUtil;

public class StockOptionUtil {

    private static final double MILLISECONDS_PER_YEAR = 31536000000l;
    private static final double DAYS_PER_YEAR = 365;

    private static double intrest = ConfigurationUtil.getBaseConfig().getDouble("strategie.intrest");
    private static double dividend = ConfigurationUtil.getBaseConfig().getDouble("strategie.dividend");
    private static double marginParameter = ConfigurationUtil.getBaseConfig().getDouble("strategie.marginParameter");
    private static double spreadSlope = ConfigurationUtil.getBaseConfig().getDouble("strategie.spreadSlope");
    private static double spreadConstant = ConfigurationUtil.getBaseConfig().getDouble("strategie.spreadConstant");

    private static double beta = ConfigurationUtil.getBaseConfig().getDouble("strategie.beta");
    private static double volVol = ConfigurationUtil.getBaseConfig().getDouble("strategie.volVol");
    private static double correlation = ConfigurationUtil.getBaseConfig().getDouble("strategie.correlation");

    private static double minExpirationYears = ConfigurationUtil.getBaseConfig().getDouble("minExpirationYears");
    private static boolean sabrEnabled = ConfigurationUtil.getBaseConfig().getBoolean("sabrEnabled");

    public static double getOptionPrice(Security security, double underlayingSpot, double vola) throws MathException, IllegalArgumentException {

        if (sabrEnabled) {
            return getOptionPriceSabr(security, underlayingSpot, vola);
        } else {
            return getOptionPriceBS(security, underlayingSpot, vola);
        }
    }

    public static double getOptionPriceSabr(double underlayingSpot, double strike, double vola, double years, double intrest, double dividend, OptionType type) throws MathException,
            IllegalArgumentException {

        if (years <= 0 ) {
            return getIntrinsicValue(underlayingSpot, strike, type);
        } else if (years < 1.0/365.0){
            return getOptionPriceBS(underlayingSpot, strike, vola, years, intrest, dividend, type); //sabr evaluates to zero on the last day before expiration
        }

        double atmVola = Volatility.getAtmVola(underlayingSpot, vola, years);
        double forward = getForward(underlayingSpot, years, intrest, dividend);
        double sabrVola = SABRVol.volByAtmVol(forward, strike, atmVola, years, beta, correlation, volVol);

        return getOptionPriceBS(underlayingSpot, strike, sabrVola, years, intrest, dividend, type);
    }

    public static double getOptionPriceSabr(Security security, double underlayingSpot, double vola) throws MathException, IllegalArgumentException {

        StockOption stockOption = (StockOption)security;

        double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / MILLISECONDS_PER_YEAR ;

        return getOptionPriceSabr(underlayingSpot, stockOption.getStrike().doubleValue(), vola, years, intrest, dividend, stockOption.getType());
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

    public static double getOptionPriceBS(Security security, double underlayingSpot, double vola) {

        StockOption stockOption = (StockOption)security;

        double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / MILLISECONDS_PER_YEAR ;

        return getOptionPriceBS(underlayingSpot, stockOption.getStrike().doubleValue(), vola, years, intrest, dividend, stockOption.getType());
    }

    public static double getVolatility(final double underlayingSpot, final double strike, final double optionValue, final double years, final double intrest, final double dividend, final OptionType type) throws MathException {

        if (years < 0)
            throw new IllegalArgumentException("years cannot be negative");

        double intrinsicValue = getIntrinsicValue(underlayingSpot, strike, type);
        if (optionValue <= intrinsicValue)
            throw new IllegalArgumentException("cannot calculate volatility if optionValue is below intrinsic Value");

        UnivariateRealFunction function = new UnivariateRealFunction () {
            public double value(double volatility) throws FunctionEvaluationException {
                return getOptionPriceBS(underlayingSpot, strike, volatility, years, intrest, dividend, type) - optionValue;
            }};

        UnivariateRealSolverFactory factory = UnivariateRealSolverFactory.newInstance();
        UnivariateRealSolver solver = factory.newDefaultSolver();
        solver.setAbsoluteAccuracy(0.0001);

        return solver.solve(function, 0.01, 0.90);
    }

    public static double getVolatility(final double underlayingSpot, final double strike, final double optionValue, final double years, final OptionType type) throws MathException {

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

    public static double getDelta(Security security, double currentValue, double underlayingSpot) throws MathException {

        if (security instanceof StockOption) {
            StockOption stockOption = (StockOption) security;

            double strike = stockOption.getStrike().doubleValue();
            double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / MILLISECONDS_PER_YEAR;
            double volatility = getVolatility(underlayingSpot, strike, currentValue, years, intrest, dividend, stockOption.getType());
            return StockOptionUtil.getDelta(underlayingSpot, strike, volatility, years, intrest, stockOption.getType());
        } else {
            throw new IllegalArgumentException("isDeltaToLow cannot be called with: " + security.getClass().getName());
        }
    }

    public static double getExitValueDouble(Security security, double underlayingSpot, double volatility) throws MathException {

        StockOption stockOption = (StockOption)security;

        double exitLevel;
        if (OptionType.CALL.equals(stockOption.getType())) {
            double callVolaPeriod = ConfigurationUtil.getBaseConfig().getDouble("callVolaPeriod");
            exitLevel = underlayingSpot * (1 + volatility / Math.sqrt(DAYS_PER_YEAR / callVolaPeriod));
        } else {
            double putVolaPeriod = ConfigurationUtil.getBaseConfig().getDouble("putVolaPeriod");
            exitLevel = underlayingSpot * (1 - volatility / Math.sqrt(DAYS_PER_YEAR / putVolaPeriod));
        }

        return getOptionPrice(stockOption, exitLevel, volatility);
    }

    public static ExitValue getExitValue(Security security, double underlayingSpot, double volatility) throws MathException {

        ExitValue exitValue = new ExitValueImpl();
        exitValue.setValue(getExitValueDouble(security, underlayingSpot, volatility));
        return exitValue;
    }

    public static double getTotalMargin(double underlayingSettlement, double strike, double stockOptionSettlement, double years, OptionType type) throws MathException {

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

        double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / MILLISECONDS_PER_YEAR ;

        return getTotalMargin(underlayingSettlement, stockOption.getStrike().doubleValue(), stockOptionSettlement, years, stockOption.getType());
    }

    public static double getMaintenanceMargin(StockOption stockOption, double stockOptionSettlement, double underlayingSettlement) throws MathException {

        return getTotalMargin(stockOption, stockOptionSettlement, underlayingSettlement) - stockOptionSettlement;
    }

    /**
     * spread depends on the pricePerContract (i.e. spread should be the same
     * for 12.- à contractSize 10 as for 1.20 à contractSize 100)
     *
     * @return price per option
     */
    public static double getDummyBid(double price, int contractSize) {

        double pricePerContract = price * contractSize;
        double spread = pricePerContract * spreadSlope + spreadConstant;
        return (pricePerContract - (spread / 2.0)) / contractSize;
    }

    /**
     * spread depends on the pricePerContract (i.e. spread should be the same
     * for 12.- à contractSize 10 as for 1.20 à contractSize 100)
     *
     * @return price per option
     */
    public static double getDummyAsk(double price, int contractSize) {

        double pricePerContract = price * contractSize;
        double spread = pricePerContract * spreadSlope + spreadConstant;
        return (pricePerContract + (spread / 2.0)) / contractSize;
    }

    public static boolean isDeltaTooLow(Security security, double currentValue, double underlayingSpot) throws MathException {

        if (security instanceof StockOption) {

            if (currentValue == 0)
                return false;

            StockOption stockOption = (StockOption) security;

            double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / MILLISECONDS_PER_YEAR;

            if (years < minExpirationYears) {
                return false;
            }

            double delta = getDelta(security, currentValue, underlayingSpot);
            double minDelta = ConfigurationUtil.getBaseConfig().getDouble("minDelta");

            if (Math.abs(delta) > minDelta) {
                return false;
            } else {
                return true;
            }
        } else {
            throw new IllegalArgumentException("isDeltaToLow cannot be called with: " + security.getClass().getName());
        }
    }

    public static double getForward(double spot, double years, double intrest, double dividend) {

        return spot * (1 - years * dividend) * Math.exp(years * intrest);
    }
}
