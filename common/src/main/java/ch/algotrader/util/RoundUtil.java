/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.util;

import java.math.BigDecimal;

import org.apache.commons.math.util.MathUtils;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.ConfigLocator;

/**
 * Provides general rounding methods.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class RoundUtil {

    public static double roundToNextN(double value, double n) {

        return MathUtils.round((value) / n, 0) * n;
    }

    public static BigDecimal roundToNextN(BigDecimal value, double n) {

        return RoundUtil.getBigDecimal(roundToNextN(value.doubleValue(), n), getDigits(n));
    }

    public static double roundToNextN(double value, double n, int roundingMethod) {

        return MathUtils.round((value) / n, 0, roundingMethod) * n;
    }

    public static BigDecimal roundToNextN(BigDecimal value, double n, int roundingMethod) {

        return RoundUtil.getBigDecimal(roundToNextN(value.doubleValue(), n, roundingMethod), getDigits(n));
    }

    public static BigDecimal getBigDecimal(double value) {

        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return null;
        } else {
            BigDecimal decimal = new BigDecimal(value);
            CommonConfig commonConfig = ConfigLocator.instance().getCommonConfig();
            return decimal.setScale(commonConfig.getPortfolioDigits(), BigDecimal.ROUND_HALF_UP);
        }
    }

    public static BigDecimal getBigDecimal(double value, int scale) {

        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return null;
        } else {
            BigDecimal decimal = new BigDecimal(value);
            return decimal.setScale(scale, BigDecimal.ROUND_HALF_UP);
        }
    }

    public static BigDecimal getBigDecimalNullSafe(Double value) {

        if (value == null) {
            return null;
        } else {
            return getBigDecimal(value.doubleValue());
        }
    }

    public static int getDigits(double n) {
        int exponent = -(int) Math.floor(Math.log10(n));
        int digits = exponent >= 0 ? exponent : 0;
        return digits;
    }
}
