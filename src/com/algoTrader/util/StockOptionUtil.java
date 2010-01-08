package com.algoTrader.util;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.solvers.UnivariateRealSolver;
import org.apache.commons.math.analysis.solvers.UnivariateRealSolverFactory;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.enumeration.OptionType;

/*************************************************************************
 *
 *  Information calculated based on closing data on Monday, June 9th 2003.
 *
 *      Microsoft:   share price:                 23.75
 *                   strike price:                15.00
 *                   risk-free interest rate:      1%
 *                   volatility:                  35%          (historical estimate)
 *                   time until expiration:        0.5 years
 *                   dividend:                    0%
 *        Result          8.879159279691955              (actual =  9.10)
 *
 *************************************************************************/
public class StockOptionUtil {

    private static final double MILLISECONDS_PER_YEAR = 1000l * 60l * 60l * 24l * 365l;
    private static final double DAYS_PER_YEAR = 365;

    private static double intrest = Double.parseDouble(PropertiesUtil.getProperty("intrest"));
    private static double dividend = Double.parseDouble(PropertiesUtil.getProperty("dividend"));
    private static double volaPeriod = Double.parseDouble(PropertiesUtil.getProperty("volaPeriod"));
    private static double marginParameter = Double.parseDouble(PropertiesUtil.getProperty("marginParameter"));

    // Black-Scholes formula
    public static double getOptionPrice(double spot, double strike, double volatility, double years, double intrest, double dividend, OptionType type) {
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

    public static BigDecimal getFairValue(Security security, BigDecimal spot, BigDecimal vola) {

        StockOption option = (StockOption)security;

        double years = (option.getExpiration().getTime() - (new Date()).getTime()) / MILLISECONDS_PER_YEAR ;

        return SwissquoteUtil.getBigDecimal(getOptionPrice(spot.doubleValue(), option.getStrike().doubleValue(), vola.doubleValue(), years, intrest, dividend, option.getType()));
    }

    public static BigDecimal getExitValue(Security security, BigDecimal spot, BigDecimal vola) {

        StockOption option = (StockOption)security;

        BigDecimal exitLevel = new BigDecimal(spot.doubleValue() * (1 - vola.doubleValue() / Math.sqrt(DAYS_PER_YEAR / volaPeriod)));

        return getFairValue(option, exitLevel, vola);

    }

    public static BigDecimal getMargin(StockOption option, BigDecimal settlement, BigDecimal underlaying) throws ConvergenceException, FunctionEvaluationException {

        double marginLevel = underlaying.doubleValue() * (1.0 - marginParameter);

        double strike = option.getStrike().doubleValue();

        double years = (option.getExpiration().getTime() - (new Date()).getTime()) / MILLISECONDS_PER_YEAR ;

        double volatility = StockOptionUtil.getVolatility(underlaying.doubleValue(), strike , settlement.doubleValue(), years, intrest, dividend, option.getType());

        double margin = getOptionPrice(marginLevel, strike, volatility, years, intrest, dividend, option.getType());

        int contractSize = option.getContractSize();

        return SwissquoteUtil.getBigDecimal(margin * contractSize);
    }

    public static void main(String[] args) throws ConvergenceException, FunctionEvaluationException {

        /*
        double spot         = 23.75;
        double strike         = 15.00;
        double vola         = 0.35;
        double years         = 0.5;
        double intrest         = 0.01;
        double dividend     = 0.0;
        */


        double spot         = 6579.98;
        double strike         = 6550;
        double vola         = 0.1406;
        double years         = 0.0625;
        double intrest         = 0.0025;
        double dividend     = 0.039;

        double callValue     = 100.6;
        double putValue     = 84.7;

        /*
        System.out.println(getOptionPrice(spot, strike, vola, years, intrest, dividend, OptionType.CALL));
        System.out.println(getOptionPrice(spot, strike, vola, years, intrest, dividend, OptionType.PUT));
        System.out.println(getVolatility(spot, strike, callValue, years, intrest, dividend, OptionType.CALL));
        System.out.println(getVolatility(spot, strike, putValue, years, intrest, dividend, OptionType.PUT));
        */

        ServiceLocator locator = ServiceLocator.instance();
        StockOption option = (StockOption)locator.getEntityService().getSecurity(75);

        //System.out.println(getFairValue(option, 6595, 0.1658));
        //System.out.println(getExitValue(option, 6595, 0.1658));

        Position position = option.getPosition();
        BigDecimal settlement = new BigDecimal(49.70);
        BigDecimal underlaying = new BigDecimal(6608.44);

        //System.out.println(getMargin(option, settlement, underlaying));

    }
}

