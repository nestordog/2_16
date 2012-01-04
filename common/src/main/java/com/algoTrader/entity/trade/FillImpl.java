package com.algoTrader.entity.trade;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.algoTrader.util.CustomToStringStyle;

public class FillImpl extends Fill {

    private static final long serialVersionUID = 1619681349145226990L;

    @Override
    public String toString() {

        return ToStringBuilder.reflectionToString(this, CustomToStringStyle.getInstance());
    }
}
