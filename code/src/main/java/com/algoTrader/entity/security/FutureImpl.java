package com.algoTrader.entity.security;

import java.util.Date;

import com.algoTrader.future.FutureUtil;
import com.algoTrader.util.DateUtil;

public class FutureImpl extends Future {

    private static final long serialVersionUID = -7436972192801577685L;

    @Override
    public double getLeverage() {

        return 1.0;
    }

    @Override
    public double getMargin() {

        return FutureUtil.getMaintenanceMargin(this) * getSecurityFamily().getContractSize();
    }

    @Override
    public long getTimeToExpiration() {

        return getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime();
    }

    @Override
    public int getDuration() {

        FutureFamily family = (FutureFamily) this.getSecurityFamily();
        Date nextExpDate = DateUtil.getExpirationDate(family.getExpirationType(), DateUtil.getCurrentEPTime());
        return 1 + (int) Math.round(((this.getExpiration().getTime() - nextExpDate.getTime()) / 2592000000d));
    }
}
