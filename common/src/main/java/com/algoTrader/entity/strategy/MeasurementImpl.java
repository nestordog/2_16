package com.algoTrader.entity.strategy;

import java.math.BigDecimal;

public class MeasurementImpl extends Measurement {

    private static final long serialVersionUID = 6810736380413592621L;

    @Override
    public String toString() {

        return getName() + " " + getDate() + " " + getValue();
    }

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
        } else if (value instanceof Boolean) {
            setBooleanValue((Boolean) value);
        } else {
            throw new IllegalArgumentException("unsupport value type " + value.getClass());
        }
    }
}
