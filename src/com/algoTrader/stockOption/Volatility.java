package com.algoTrader.stockOption;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.solvers.UnivariateRealSolver;
import org.apache.commons.math.analysis.solvers.UnivariateRealSolverFactory;

import com.algoTrader.enumeration.OptionType;
import com.algoTrader.util.PropertiesUtil;

public class Volatility {

    private static int strikeDistance = PropertiesUtil.getIntProperty("simulation.strikeDistance");
    private static double intrest = PropertiesUtil.getDoubleProperty("intrest");
    private static double dividend = PropertiesUtil.getDoubleProperty("dividend");
    private static double beta = PropertiesUtil.getDoubleProperty("beta");
    private static double volVol = PropertiesUtil.getDoubleProperty("volVol");
    private static double correlation = -PropertiesUtil.getDoubleProperty("correlation");

    public static double getIndexVola(double underlayingSpot, double atmVola, double years, double intrest, double dividend, double beta, double volVol, double correlation) {

        double accumulation = Math.exp(years * intrest);
        double forward = underlayingSpot * (1 - years * dividend) * Math.exp(years * intrest);
        double atmStrike = Math.round(underlayingSpot / 50.0) * 50.0;

        double factorSum = 0.0;

        // process atm strike
        {
            double sabrVola = Sabr.getSabrVolatility(atmStrike, forward, years, atmVola, beta, volVol, correlation);
            double call = StockOptionUtil.getOptionPriceBS(underlayingSpot, atmStrike, sabrVola, years, intrest, dividend, OptionType.CALL);
            double put = StockOptionUtil.getOptionPriceBS(underlayingSpot, atmStrike, sabrVola, years, intrest, dividend, OptionType.PUT);
            double outOfTheMoneyPrice = (put + call) / 2;
            double factor = getFactor(atmStrike, accumulation, strikeDistance, outOfTheMoneyPrice);
            factorSum += factor;
        }

        // process strikes below atm
        double strike = atmStrike - strikeDistance;
        while (true) {
            double sabrVola = Sabr.getSabrVolatility(strike, forward, years, atmVola, beta, volVol, correlation);
            double put = StockOptionUtil.getOptionPriceBS(underlayingSpot, strike, sabrVola, years, intrest, dividend, OptionType.PUT);
            if (put < 0.5) break;
            double factor = getFactor(strike, accumulation, strikeDistance, put);
            factorSum += factor;
            strike -= strikeDistance;
        }

        // process strikes above atm
        strike = atmStrike + strikeDistance;
        while (true) {
            double sabrVola = Sabr.getSabrVolatility(strike, forward, years, atmVola, beta, volVol, correlation);
            double call = StockOptionUtil.getOptionPriceBS(underlayingSpot, strike, sabrVola, years, intrest, dividend, OptionType.CALL);
            if (call < 0.5) break;
            double factor = getFactor(strike, accumulation, strikeDistance, call);
            factorSum += factor;
            strike += strikeDistance;
        }

        return Math.sqrt((factorSum * 2 - Math.pow(forward / atmStrike-1 , 2)) / years);
    }

    public static double getAtmVola(final double underlayingSpot, final double indexVola, final double years, final double intrest, final double dividend, final double beta, final double volVol, final double correlation) throws ConvergenceException, FunctionEvaluationException, IllegalArgumentException {

        UnivariateRealFunction function = new UnivariateRealFunction () {
            public double value(double atmVola) throws FunctionEvaluationException {
                double currentIndexVola = getIndexVola(underlayingSpot, atmVola, years, intrest, dividend, beta, volVol, correlation);
                double difference = currentIndexVola - indexVola;
                return difference;
            }};

        UnivariateRealSolverFactory factory = UnivariateRealSolverFactory.newInstance();
        UnivariateRealSolver solver = factory.newDefaultSolver();


        return solver.solve(function, indexVola * 0.7 , indexVola * 1.1, indexVola);
    }

    public static double getAtmVola(final double underlayingSpot, final double indexVola, final double years) throws ConvergenceException, FunctionEvaluationException, IllegalArgumentException {

        return getAtmVola(underlayingSpot, indexVola, years, intrest, dividend, beta, volVol, correlation);
    }

    private static double getFactor(double strike, double accumulation, int strikeDistance, double outOfTheMoneyPrice) {

        return outOfTheMoneyPrice * accumulation * (strikeDistance / (strike * strike));
    }
}
