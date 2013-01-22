package com.algoTrader.entity.trade;

import java.util.Date;

import org.apache.commons.beanutils.BeanUtils;
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
            + " extId: " + getExtId()
            + (getMarketChannel() != null ? " marketChannel: " + getMarketChannel() : "")
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

    public Order cloneOrder() {
        try {
            return (Order) BeanUtils.cloneBean(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Order modifyQuantity(long quantity) {

        Order order = cloneOrder();
        order.setQuantity(quantity);
        return order;
    }

    @Override
    public Order modifyTIFDate(Date date) {

        Order order = cloneOrder();
        order.setTifDate(date);
        return order;
    }
}
