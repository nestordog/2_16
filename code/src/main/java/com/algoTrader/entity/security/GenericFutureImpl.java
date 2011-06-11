package com.algoTrader.entity.security;

import java.util.Date;

import com.algoTrader.util.DateUtil;

public class GenericFutureImpl extends GenericFuture {

    private static final long serialVersionUID = -5567218864363234118L;

    public Date getExpiration() {

        // calculate expiration based on defined months
        Date date = DateUtil.getCurrentEPTime();

        return DateUtil.get30DayPrioToNextThirdFridayNMonths(date, getDuration());
    }
}
