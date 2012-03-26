package com.algoTrader.entity;

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
}
