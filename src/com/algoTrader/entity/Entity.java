package com.algoTrader.entity;

import org.apache.commons.lang.builder.ToStringBuilder;
import com.algoTrader.util.CustomToStringStyle;

public class Entity {

    public String toString() {

        return ToStringBuilder.reflectionToString(this, CustomToStringStyle.getInstance());
    }
}
