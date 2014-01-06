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
package ch.algotrader.entity.property;

import java.math.BigDecimal;
import java.util.Date;

import ch.algotrader.util.ObjectUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class PropertyImpl extends Property {

    private static final long serialVersionUID = -3790829266158776151L;

    @Override
    public Object getValue() {

        if (getIntValue() != null) {
            return getIntValue();
        } else if (getDoubleValue() != null) {
            return getDoubleValue();
        } else if (getMoneyValue() != null) {
            return getMoneyValue();
        } else if (getTextValue() != null) {
            return getTextValue();
        } else if (getDateValue() != null) {
            return getDateValue();
        } else if (getBooleanValue() != null) {
            return getBooleanValue();
        } else {
            return null;
        }
    }

    @Override
    public void setValue(Object value) {

        // set all values to null to prevent double value settings
        setIntValue(null);
        setDoubleValue(null);
        setMoneyValue(null);
        setTextValue(null);
        setDateValue(null);
        setBooleanValue(null);

        // set the value of the correct type
        if (value instanceof Integer) {
            setIntValue((Integer) value);
        } else if (value instanceof Double) {
            setDoubleValue((Double) value);
        } else if (value instanceof BigDecimal) {
            setMoneyValue((BigDecimal) value);
        } else if (value instanceof String) {
            setTextValue((String) value);
        } else if (value instanceof Date) {
            setDateValue((Date) value);
        } else if (value instanceof Boolean) {
            setBooleanValue((Boolean) value);
        } else {
            throw new IllegalArgumentException("unsupport value type " + value.getClass());
        }
    }


    @Override
    public String toString() {

        return getValue().toString();
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj instanceof Property) {
            Property that = (Property) obj;
            return ObjectUtil.equalsNonNull(this.getPropertyHolder(), that.getPropertyHolder()) &&
                    ObjectUtil.equalsNonNull(this.getName(), that.getName());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {

        int hash = 17;
        hash = hash * 37 + ObjectUtil.hashCode(getPropertyHolder());
        hash = hash * 37 + ObjectUtil.hashCode(getName());
        return hash;
    }
}
