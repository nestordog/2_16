package com.algoTrader.stockOption;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.solvers.UnivariateRealSolver;
import org.apache.commons.math.analysis.solvers.UnivariateRealSolverFactory;

import com.algoTrader.enumeration.OptionType;
import com.algoTrader.sabr.SABRVol;

public class VolatilityUtil {

    public static double getIndexVola(double underlayingSpot, double putAtmVola, double years, double intrest, double dividend, double strikeDistance, double beta, double rhoCall, double volVolCall,
            double rhoPut, double volVolPut) {

        double accumulation = Math.exp(years * intrest);
        double forward = StockOptionUtil.getForward(underlayingSpot, years, intrest, dividend);
        double atmStrike = Math.round(underlayingSpot / 50.0) * 50.0;

        double callAtmVola = getCallAtmVolaFromPutAtmVola(putAtmVola, years);

        double factorSum = 0.0;

        // process atm strike
        {
            double volaCall = SABRVol.volByAtmVol(forward, atmStrike, callAtmVola, years, beta, rhoCall, volVolCall);
            double volaPut = SABRVol.volByAtmVol(forward, atmStrike, putAtmVola, years, beta, rhoPut, volVolPut);
            double call = StockOptionUtil.getOptionPriceBS(underlayingSpot, atmStrike, volaCall, years, intrest, dividend, OptionType.CALL);
            double put = StockOptionUtil.getOptionPriceBS(underlayingSpot, atmStrike, volaPut, years, intrest, dividend, OptionType.PUT);
            double outOfTheMoneyPrice = (put + call) / 2;
            double factor = outOfTheMoneyPrice * accumulation * (strikeDistance / (atmStrike * atmStrike));
            factorSum += factor;
        }

        // process strikes below atm
        double strike = atmStrike - strikeDistance;
        while (true) {
            double volaPut = SABRVol.volByAtmVol(forward, strike, putAtmVola, years, beta, rhoPut, volVolPut);
            double put = StockOptionUtil.getOptionPriceBS(underlayingSpot, strike, volaPut, years, intrest, dividend, OptionType.PUT);
            if (put < 0.5)
                break;
            double factor = put * accumulation * (strikeDistance / (strike * strike));
            if ((factor / factorSum) < 0.0001)
                break;
            factorSum += factor;
            strike -= strikeDistance;
        }

        // process strikes above atm
        strike = atmStrike + strikeDistance;
        while (true) {
            double volaCall = SABRVol.volByAtmVol(forward, strike, callAtmVola, years, beta, rhoCall, volVolCall);
            double call = StockOptionUtil.getOptionPriceBS(underlayingSpot, strike, volaCall, years, intrest, dividend, OptionType.CALL);
            if (call < 0.5)
                break;
            double factor = call * accumulation * (strikeDistance / (strike * strike));
            if ((factor / factorSum) < 0.0001)
                break;
            factorSum += factor;
            strike += strikeDistance;
        }

        return Math.sqrt((factorSum * 2 - Math.pow(forward / atmStrike - 1, 2)) / years);
    }

    public static double getPutAtmVola(final double underlayingSpot, final double indexVola, final double years, final double intrest, final double dividend, final double strikeDistance,
            final double beta, final double rhoCall, final double volVolCall, final double rhoPut, final double volVolPut) throws ConvergenceException, FunctionEvaluationException, IllegalArgumentException {

        UnivariateRealFunction function = new UnivariateRealFunction () {
            public double value(double putAtmVola) throws FunctionEvaluationException {
                double currentIndexVola = getIndexVola(underlayingSpot, putAtmVola, years, intrest, dividend, strikeDistance, beta, rhoCall, volVolCall, rhoPut, volVolPut);
                return currentIndexVola - indexVola;
            }};

        UnivariateRealSolverFactory factory = UnivariateRealSolverFactory.newInstance();
        UnivariateRealSolver solver = factory.newDefaultSolver();
        solver.setAbsoluteAccuracy(0.001);

        try {
            // normaly atmVola is above 80% of indexVola
            return solver.solve(function, indexVola * 0.8, indexVola, indexVola);
        } catch (Exception e) {
            // in rare cases atmVola is as low as 10% or as high as 120% of indexVola
            return solver.solve(function, indexVola * 0.1, indexVola * 1.2, indexVola);
        }
    }

    public static double getCallAtmVola(final double underlayingSpot, final double indexVola, final double years, final double intrest, final double dividend, final double strikeDistance,
            final double beta, final double rhoCall, final double volVolCall, final double rhoPut, final double volVolPut) throws ConvergenceException, FunctionEvaluationException,
            IllegalArgumentException {

        double putAtmVola = getPutAtmVola(underlayingSpot, indexVola, years, intrest, dividend, strikeDistance, beta, rhoCall, volVolCall, rhoPut, volVolPut);
        return getCallAtmVolaFromPutAtmVola(putAtmVola, years);
    }

    private static double getCallAtmVolaFromPutAtmVola(double putAtmVola, double years) {

        double delta = 1.5636 * years - 0.0107;
        double callAtmVola = (1 + delta) * putAtmVola;
        return callAtmVola;
    }
}
