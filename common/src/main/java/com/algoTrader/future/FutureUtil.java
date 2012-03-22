package com.algoTrader.future;

import com.algoTrader.entity.security.Future;
import com.algoTrader.entity.security.FutureFamily;
import com.algoTrader.util.DateUtil;

public class FutureUtil {

    private static final double MILLISECONDS_PER_YEAR = 31536000000l;

    public static double getFuturePrice(Future future, double underlyingSpot) {

        FutureFamily family = (FutureFamily) future.getSecurityFamily();

        double years = (future.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / MILLISECONDS_PER_YEAR;

        return getFuturePrice(underlyingSpot, years, family.getIntrest(), family.getDividend());
    }

    public static double getFuturePrice(double underlyingSpot, double years, double intrest, double dividend) {

        return underlyingSpot + (underlyingSpot * intrest - dividend) * years;
    }

    public static double getMaintenanceMargin(Future future) {

        FutureFamily family = (FutureFamily) future.getSecurityFamily();
        return family.getMarginParameter();
    }
}
