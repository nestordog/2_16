package com.algoTrader.service.theta;

import org.apache.commons.math.MathException;

import com.algoTrader.entity.StockOption;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.stockOption.StockOptionUtil;
import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.DateUtil;
import com.algoTrader.vo.ExitValueVO;

public class ThetaUtil {

    private static final double MILLISECONDS_PER_YEAR = 31536000000l;
    private static final double DAYS_PER_YEAR = 365;

    public static boolean isDeltaTooLow(String strategyName, StockOption stockOption, double currentValue, double underlayingSpot) throws MathException {

        double minExpirationYears = ConfigurationUtil.getStrategyConfig(strategyName).getDouble("minExpirationYears");
        double minDelta = ConfigurationUtil.getStrategyConfig(strategyName).getDouble("minDelta");

        if (currentValue == 0)
            return false;

        double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / MILLISECONDS_PER_YEAR;

        if (years < minExpirationYears) {
            return false;
        }

        double delta = StockOptionUtil.getDelta(stockOption, currentValue, underlayingSpot);

        if (Math.abs(delta) > minDelta) {
            return false;
        } else {
            return true;
        }
    }

    public static ExitValueVO getExitValue(String strategyName, StockOption stockOption, double underlayingSpot, double volatility) throws MathException {

        ExitValueVO vo = new ExitValueVO(getExitValueDouble(strategyName, stockOption, underlayingSpot, volatility));
        return vo;
    }

    public static double getExitValueDouble(String strategyName, StockOption stockOption, double underlayingSpot, double volatility) throws MathException {

        double exitLevel;
        if (OptionType.CALL.equals(stockOption.getType())) {
            double callVolaPeriod = ConfigurationUtil.getStrategyConfig(strategyName).getDouble("callVolaPeriod");
            exitLevel = underlayingSpot * (1 + volatility / Math.sqrt(DAYS_PER_YEAR / callVolaPeriod));
        } else {
            double putVolaPeriod = ConfigurationUtil.getStrategyConfig(strategyName).getDouble("putVolaPeriod");
            exitLevel = underlayingSpot * (1 - volatility / Math.sqrt(DAYS_PER_YEAR / putVolaPeriod));
        }

        return StockOptionUtil.getOptionPrice(stockOption, exitLevel, volatility);
    }
}
