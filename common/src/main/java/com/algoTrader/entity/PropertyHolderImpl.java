package com.algoTrader.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class PropertyHolderImpl extends PropertyHolder {

    private static final long serialVersionUID = 2154726089257967279L;

    @Override
    public int getIntProperty(String name) {

        Property property = getPropertiesInitialized().get(name);
        if (property != null) {
            if (property.getIntValue() != null) {
                return property.getIntValue();
            } else {
                throw new IllegalArgumentException("property " + name + " does not have a int value");
            }
        } else {
            throw new IllegalArgumentException("property " + name + " is not defined");
        }
    }

    @Override
    public double getDoubleProperty(String name) {

        Property property = getPropertiesInitialized().get(name);
        if (property != null) {
            if (property.getDoubleValue() != null) {
                return property.getDoubleValue();
            } else {
                throw new IllegalArgumentException("property " + name + " does not have a double value");
            }
        } else {
            throw new IllegalArgumentException("property " + name + " is not defined");
        }
    }

    @Override
    public BigDecimal getMoneyProperty(String name) {

        Property property = getPropertiesInitialized().get(name);
        if (property != null) {
            if (property.getMoneyValue() != null) {
                return property.getMoneyValue();
            } else {
                throw new IllegalArgumentException("property " + name + " does not have a Money value");
            }
        } else {
            throw new IllegalArgumentException("property " + name + " is not defined");
        }
    }

    @Override
    public String getTestProperty(String name) {

        Property property = getPropertiesInitialized().get(name);
        if (property != null) {
            if (property.getTextValue() != null) {
                return property.getTextValue();
            } else {
                throw new IllegalArgumentException("property " + name + " does not have a Text value");
            }
        } else {
            throw new IllegalArgumentException("property " + name + " is not defined");
        }
    }

    @Override
    public Date getDateProperty(String name) {

        Property property = getPropertiesInitialized().get(name);
        if (property != null) {
            if (property.getDateValue() != null) {
                return property.getDateValue();
            } else {
                throw new IllegalArgumentException("property " + name + " does not have a Date value");
            }
        } else {
            throw new IllegalArgumentException("property " + name + " is not defined");
        }
    }

    @Override
    public boolean getBooleanProperty(String name) {

        Property property = getPropertiesInitialized().get(name);
        if (property != null) {
            if (property.getBooleanValue() != null) {
                return property.getBooleanValue();
            } else {
                throw new IllegalArgumentException("property " + name + " does not have a Boolean value");
            }
        } else {
            throw new IllegalArgumentException("property " + name + " is not defined");
        }
    }

    @Override
    public Boolean hasProperty(String name) {

        return getProperties().containsKey(name);
    }

    @Override
    public Map<String, Object> getPropertyValueMap() {

        Map<String, Object> values = new HashMap<String, Object>();
        for (Property property : getProperties().values()) {
            values.put(property.getName(), property.getValue());
        }
        return values;
    }
}
