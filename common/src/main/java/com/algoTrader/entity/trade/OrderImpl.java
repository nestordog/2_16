package com.algoTrader.entity.trade;

import org.apache.commons.lang.ClassUtils;

public abstract class OrderImpl extends Order {

    private static final long serialVersionUID = -6501807818853981164L;

    @Override
    public String toString() {

        //@formatter:off
        return getSide()
            + " " + getQuantity()
            + " " + ClassUtils.getShortClassName(this.getClass())
            + " " + getSecurity()
            + " " + getStrategy()
            + (getIntId() != null ? " intId: " + getIntId() : "")
            + (getAccount() != null ? " account: " + getAccount() : "")
            + (!"".equals(getDescription()) ? " " + getDescription() : "");
        //@formatter:on
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public void setQuantity(long quantityIn) {

        // always set a positive quantity
        super.setQuantity(Math.abs(quantityIn));
    }

    @Override
    public String getRootIntId() {

        // for FIX Orders remove the Order Version
        if (getIntId().contains(".")) {
            return getIntId().split("\\.")[0];
        } else {
            return getIntId();
        }
    }
}
