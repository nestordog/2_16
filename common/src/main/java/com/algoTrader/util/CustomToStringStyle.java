/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package com.algoTrader.util;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.builder.StandardToStringStyle;
import org.hibernate.Hibernate;

import com.algoTrader.entity.PrintableI;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
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
