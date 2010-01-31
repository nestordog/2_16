package com.algoTrader.util;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.builder.StandardToStringStyle;

import com.algoTrader.entity.Entity;

public class CustomToStringStyle extends StandardToStringStyle {

    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss,SSS");

    private static CustomToStringStyle style;

    public static CustomToStringStyle getInstance() {

        if (style == null) {
            style = new CustomToStringStyle();
            style.setUseClassName(false);
            style.setUseIdentityHashCode(false);
        }
        return style;
    }

    protected void appendDetail(StringBuffer buffer, String fieldName, Collection col) {

        buffer.append(col.size());
    }

    protected void appendDetail(StringBuffer buffer, String fieldName, Object value) {

        if ( value instanceof Entity ) {
            return;
        } else if (value instanceof Date) {
            buffer.append(format.format(value));
        } else {
            super.appendDetail(buffer, fieldName, value);
        }
    }

    public void append(StringBuffer buffer, String fieldName, Object value, Boolean fullDetail) {

        if ( value instanceof Entity ) {
            return;
        } else {
            super.append(buffer, fieldName, value, fullDetail);
        }
    }
}
