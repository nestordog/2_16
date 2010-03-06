package com.algoTrader.stockOption;

import java.math.BigDecimal;
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
import com.algoTrader.util.RoundUtil;

public class StockOptionUtil {

    private static final double MILLISECONDS_PER_YEAR = 1000l * 60l * 60l * 24l * 365l;
    private static final double DAYS_PER_YEAR = 365;

    private static double intrest = Double.parseDouble(PropertiesUtil.getProperty("intrest"));
    private static double dividend = Double.parseDouble(PropertiesUtil.getProperty("dividend"));
    private static double volaPeriod = Double.parseDouble(PropertiesUtil.getProperty("volaPeriod"));
    private static double marginParameter = Double.parseDouble(PropertiesUtil.getProperty("marginParameter"));

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

    public static BigDecimal getFairValue(Security security, BigDecimal spot, BigDecimal vola) throws RuntimeException {

        StockOption stockOption = (StockOption)security;
        Date currentTime = DateUtil.getCurrentEPTime();

        double years = (stockOption.getExpiration().getTime() - currentTime.getTime()) / MILLISECONDS_PER_YEAR ;

        double fairValue = getOptionPrice(spot.doubleValue(), stockOption.getStrike().doubleValue(), vola.doubleValue(), years, intrest, dividend, stockOption.getType());
        return RoundUtil.getBigDecimal(fairValue);
    }

    public static BigDecimal getExitValue(Security security, BigDecimal spot, BigDecimal optionValue) throws ConvergenceException, FunctionEvaluationException {

        StockOption stockOption = (StockOption)security;

        double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / MILLISECONDS_PER_YEAR ;

        double volatility = getVolatility(spot.doubleValue(), stockOption.getStrike().doubleValue(), optionValue.doubleValue(), years, intrest, dividend, stockOption.getType());

        BigDecimal exitLevel = RoundUtil.getBigDecimal(spot.doubleValue() * (1 - volatility / Math.sqrt(DAYS_PER_YEAR / volaPeriod)));

        return getFairValue(stockOption, exitLevel, RoundUtil.getBigDecimal(volatility));
    }

    public static BigDecimal getMargin(StockOption stockOption, BigDecimal settlement, BigDecimal underlaying) throws ConvergenceException, FunctionEvaluationException {

        double marginLevel = underlaying.doubleValue() * (1.0 - marginParameter);

        double strike = stockOption.getStrike().doubleValue();

        double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / MILLISECONDS_PER_YEAR ;

        double volatility = StockOptionUtil.getVolatility(underlaying.doubleValue(), strike , settlement.doubleValue(), years, intrest, dividend, stockOption.getType());

        double margin = getOptionPrice(marginLevel, strike, volatility, years, intrest, dividend, stockOption.getType());

        return RoundUtil.getBigDecimal(margin);
    }
}

