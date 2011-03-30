package com.algoTrader.future;

import com.algoTrader.entity.Future;
import com.algoTrader.entity.FutureFamily;
import com.algoTrader.util.DateUtil;

public class FutureUtil {

    private static final double MILLISECONDS_PER_YEAR = 31536000000l;

    public static double getFuturePrice(Future future, double underlayingSpot) {

        FutureFamily family = (FutureFamily) future.getSecurityFamily();

        double years = (future.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / MILLISECONDS_PER_YEAR;

        return getFuturePrice(underlayingSpot, years, family.getIntrest(), family.getDividend());
    }

    public static double getFuturePrice(double underlayingSpot, double years, double intrest, double dividend) {

        return underlayingSpot + (underlayingSpot * intrest - dividend) * years;
    }

    public static double getMaintenanceMargin(Future future) {

        FutureFamily family = (FutureFamily) future.getSecurityFamily();
        return family.getMarginParameter();
    }
}
