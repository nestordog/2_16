package com.algoTrader.entity.security;

import com.algoTrader.future.FutureUtil;
import com.algoTrader.util.DateUtil;


public class FutureImpl extends Future {

    private static final long serialVersionUID = -7436972192801577685L;

    public double getLeverage() {

        return 1.0;
    }

    public double getMargin() {

        return FutureUtil.getMaintenanceMargin(this) * getSecurityFamily().getContractSize();
    }

    public long getTimeToExpiration() {

        return getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime();
    }
}
