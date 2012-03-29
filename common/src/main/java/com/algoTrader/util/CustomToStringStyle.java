package com.algoTrader.util;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.builder.StandardToStringStyle;
import org.hibernate.Hibernate;

import com.algoTrader.entity.PrintableI;

public class CustomToStringStyle extends StandardToStringStyle {

    private static final long serialVersionUID = 4268907286858926178L;

    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss,SSS");

    private static CustomToStringStyle style;

    public static CustomToStringStyle getInstance() {

        if (style == null) {
            style = new CustomToStringStyle();
            style.setUseClassName(false);
            style.setUseIdentityHashCode(false);
        }
        return style;
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected void appendDetail(StringBuffer buffer, String fieldName, Collection col) {

        buffer.append(col.size());
    }

    @Override
    protected void appendDetail(StringBuffer buffer, String fieldName, Object value) {

        if (value instanceof PrintableI) {
            return;
        } else if (value instanceof Date) {
            buffer.append(format.format(value));
        } else {
            super.appendDetail(buffer, fieldName, value);
        }
    }

    @Override
    public void append(StringBuffer buffer, String fieldName, Object value, Boolean fullDetail) {

        if (value instanceof PrintableI) {
            return;
        } else if (Hibernate.isInitialized(value)) {
            super.append(buffer, fieldName, value, fullDetail);
        }
    }
}
