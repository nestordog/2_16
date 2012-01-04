package com.algoTrader.entity.trade;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.algoTrader.util.CustomToStringStyle;

public class OrderStatusImpl extends OrderStatus {

    private static final long serialVersionUID = -423135654204518265L;

    @Override
    public String toString() {

        return ToStringBuilder.reflectionToString(this, CustomToStringStyle.getInstance());
    }
}
