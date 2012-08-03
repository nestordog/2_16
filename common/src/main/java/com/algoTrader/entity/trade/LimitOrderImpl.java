package com.algoTrader.entity.trade;

import java.math.BigDecimal;

public class LimitOrderImpl extends LimitOrder {

    private static final long serialVersionUID = -3560878461518491161L;

    @Override
    public String getDescription() {
        return "limit: " + getLimit();
    }

    @Override
    public Order modifyLimit(BigDecimal limit) {

        LimitOrder order = (LimitOrder) cloneOrder();
        order.setLimit(limit);
        return order;
    }
}
