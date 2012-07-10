package com.algoTrader.entity;

import java.math.BigDecimal;
import java.util.Date;

public abstract class PropertyHolderImpl extends PropertyHolder {

    private static final long serialVersionUID = 2154726089257967279L;

    @Override
    public int getIntProperty(String name) {

        Property property = getPropertiesInitialized().get(name);
        if (property != null) {
            return property.getIntValue();
        } else {
            throw new RuntimeException("property " + name + " is not defined");
        }
    }

    @Override
    public double getDoubleProperty(String name) {

        Property property = getPropertiesInitialized().get(name);
        if (property != null) {
            return property.getDoubleValue();
        } else {
            throw new RuntimeException("property " + name + " is not defined");
        }
    }

    @Override
    public BigDecimal getMoneyProperty(String name) {

        Property property = getPropertiesInitialized().get(name);
        if (property != null) {
            return property.getMoneyValue();
        } else {
            throw new RuntimeException("property " + name + " is not defined");
        }
    }

    @Override
    public String getTestProperty(String name) {

        Property property = getPropertiesInitialized().get(name);
        if (property != null) {
            return property.getTextValue();
        } else {
            throw new RuntimeException("property " + name + " is not defined");
        }
    }

    @Override
    public Date getDateProperty(String name) {

        Property property = getPropertiesInitialized().get(name);
        if (property != null) {
            return property.getDateValue();
        } else {
            throw new RuntimeException("property " + name + " is not defined");
        }
    }

    @Override
    public boolean getBooleanProperty(String name) {

        Property property = getPropertiesInitialized().get(name);
        if (property != null) {
            return property.getBooleanValue();
        } else {
            throw new RuntimeException("property " + name + " is not defined");
        }
    }

    @Override
    public Boolean hasProperty(String name) {

        return getProperties().containsKey(name);
    }
}
