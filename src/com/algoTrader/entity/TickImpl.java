package com.algoTrader.entity;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.algoTrader.util.CustomToStringStyle;

public class TickImpl extends Tick {

    private static final long serialVersionUID = -5959287168728366157L;

    public String toString() {

        return ToStringBuilder.reflectionToString(this, CustomToStringStyle.getInstance());
       }
}
