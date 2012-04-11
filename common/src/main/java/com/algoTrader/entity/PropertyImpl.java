package com.algoTrader.entity;

import java.math.BigDecimal;
import java.util.Date;

public class PropertyImpl extends Property {

    private static final long serialVersionUID = -3790829266158776151L;

    @Override
    public String toString() {

        if (getIntValue() != null) {
            return getIntValue().toString();
        } else if (getDoubleValue() != null) {
            return getDoubleValue().toString();
        } else if (getMoneyValue() != null) {
            return getMoneyValue().toString();
        } else if (getTextValue() != null) {
            return getTextValue().toString();
        } else if (getDateValue() != null) {
            return getDateValue().toString();
        } else {
            return "";
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
        } else {
            throw new IllegalArgumentException("unsupport value type " + value.getClass());
        }
    }
}
