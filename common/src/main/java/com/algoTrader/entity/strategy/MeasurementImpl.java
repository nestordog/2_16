package com.algoTrader.entity.strategy;

import java.math.BigDecimal;

public class MeasurementImpl extends Measurement {

    private static final long serialVersionUID = 6810736380413592621L;

    @Override
    public String toString() {

        String value;
        if (getIntValue() != null) {
            value = getIntValue().toString();
        } else if (getDoubleValue() != null) {
            value = getDoubleValue().toString();
        } else if (getMoneyValue() != null) {
            value = getMoneyValue().toString();
        } else if (getBooleanValue() != null) {
            value = getBooleanValue().toString();
        } else {
            value = "";
        }

        return getName() + " " + getDate() + " " + value;
    }

    @Override
    public void setValue(Object value) {

        // set all values to null to prevent double value settings
        setIntValue(null);
        setDoubleValue(null);
        setMoneyValue(null);
        setBooleanValue(null);

        // set the value of the correct type
        if (value instanceof Integer) {
            setIntValue((Integer) value);
        } else if (value instanceof Double) {
            setDoubleValue((Double) value);
        } else if (value instanceof BigDecimal) {
            setMoneyValue((BigDecimal) value);
        } else if (value instanceof Boolean) {
            setBooleanValue((Boolean) value);
        } else {
            throw new IllegalArgumentException("unsupport value type " + value.getClass());
        }
    }
}
