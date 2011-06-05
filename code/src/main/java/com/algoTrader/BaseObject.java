package com.algoTrader;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.algoTrader.util.CustomToStringStyle;

public class BaseObject {

    public String toLongString() {

        return ToStringBuilder.reflectionToString(this, CustomToStringStyle.getInstance());
    }
}
