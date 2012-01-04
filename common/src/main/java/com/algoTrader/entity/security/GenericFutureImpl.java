package com.algoTrader.entity.security;

import java.util.Date;

import com.algoTrader.util.DateUtil;

public class GenericFutureImpl extends GenericFuture {

    private static final long serialVersionUID = -5567218864363234118L;

    @Override
    public Date getExpiration() {

        GenericFutureFamily family = (GenericFutureFamily) getSecurityFamily();

        return DateUtil.getExpirationDateNMonths(family.getExpirationType(), DateUtil.getCurrentEPTime(), getDuration());
    }
}
