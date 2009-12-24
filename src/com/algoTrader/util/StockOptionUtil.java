package com.algoTrader.util;

import java.math.BigDecimal;
import java.util.Date;

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
    private static final double TRADING_DAYS_PER_YEAR = 256;

    private static double intrest = Double.parseDouble(PropertiesUtil.getProperty("intrest"));
    private static double dividend = Double.parseDouble(PropertiesUtil.getProperty("dividend"));
    private static double volaPeriod = Double.parseDouble(PropertiesUtil.getProperty("volaPeriod"));

    // Black-Scholes formula
    public static double callPrice(double spot, double strike, double volatility, double years, double intrest, double dividend) {
        double adjustedSpot = spot * Math.exp(-dividend * years);
        double d1 = (Math.log(adjustedSpot/strike) + (intrest + volatility * volatility/2) * years) / (volatility * Math.sqrt(years));
        double d2 = d1 - volatility * Math.sqrt(years);
        return adjustedSpot * Gaussian.Phi(d1) - strike * Math.exp(-intrest * years) * Gaussian.Phi(d2);
    }

    // Black-Scholes formula
    public static double putPrice(double spot, double strike, double volatility, double years, double intrest, double dividend) {
        double adjustedSpot = spot * Math.exp(-dividend * years);
        double d1 = (Math.log(adjustedSpot/strike) + (intrest + volatility * volatility/2) * years) / (volatility * Math.sqrt(years));
        double d2 = d1 - volatility * Math.sqrt(years);
        return strike * Math.exp(-intrest * years) * Gaussian.Phi(-d2) - adjustedSpot * Gaussian.Phi(-d1);
    }

    public static BigDecimal getFairValue(Security security, BigDecimal spot, BigDecimal vola) {

        StockOption option = (StockOption)security;

        double years = (option.getExpiration().getTime() - (new Date()).getTime()) / MILLISECONDS_PER_YEAR ;

        if (option.getType().equals(OptionType.CALL)) {

            return BigDecimalUtil.getBigDecimal(callPrice(spot.doubleValue(), option.getStrike().doubleValue(), vola.doubleValue(), years, intrest, dividend));
        } else {

            return BigDecimalUtil.getBigDecimal(putPrice(spot.doubleValue(), option.getStrike().doubleValue(), vola.doubleValue(), years, intrest, dividend));
        }
    }

    public static BigDecimal getExitValue(Security security, BigDecimal spot, BigDecimal vola) {

        StockOption option = (StockOption)security;

        BigDecimal exitLevel = new BigDecimal(spot.doubleValue() * (1 - vola.doubleValue() / Math.sqrt(TRADING_DAYS_PER_YEAR / volaPeriod)));

        return getFairValue(option, exitLevel, vola);

    }

    public static void main(String[] args) {

        /*
        double spot         = 23.75;
        double strike         = 15.00;
        double vola         = 0.35;
        double years         = 0.5;
        double intrest         = 0.01;
        double dividend     = 0.0;
        */

        /*
        double spot         = 6372;
        double strike         = 6300;
        double vola         = 0.227;
        double years         = 0.05;
        double intrest         = 0.018;
        double dividend     = 0.02;


        System.out.println(callPrice(spot, strike, vola, years, intrest, dividend));
        System.out.println(putPrice(spot, strike, vola, years, intrest, dividend));
        */

        /*
        ServiceLocator locator = ServiceLocator.instance();
        StockOption option = (StockOption)locator.getEntityService().getSecurity(1);

        System.out.println(getFairValue(option, 6595, 0.1658));
        System.out.println(getExitValue(option, 6595, 0.1658));
        */

    }
}

