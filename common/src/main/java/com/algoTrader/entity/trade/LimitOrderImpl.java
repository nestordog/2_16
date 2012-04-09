package com.algoTrader.entity.trade;

import org.apache.commons.lang.ClassUtils;

public class LimitOrderImpl extends LimitOrder {

    private static final long serialVersionUID = -3560878461518491161L;

    @Override
    public String toString() {

        return getSide() + " " + getQuantity() + " " + ClassUtils.getShortClassName(this.getClass()) + " " + getSecurity() + " limit " + getLimit();
    }
}
