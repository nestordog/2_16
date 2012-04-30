package com.algoTrader.entity;

import java.math.BigDecimal;
import java.util.Date;

public abstract class PropertyHolderImpl extends PropertyHolder {

    private static final long serialVersionUID = 2154726089257967279L;

    @Override
    public int getIntProperty(String name) {

        return getProperties().get(name).getIntValue();
    }

    @Override
    public double getDoubleProperty(String name) {

        return getProperties().get(name).getDoubleValue();
    }

    @Override
    public BigDecimal getMoneyProperty(String name) {

        return getProperties().get(name).getMoneyValue();
    }

    @Override
    public String getTestProperty(String name) {

        return getProperties().get(name).getTextValue();
    }

    @Override
    public Date getDateProperty(String name) {

        return getProperties().get(name).getDateValue();
    }

    @Override
    public boolean getBooleanProperty(String name) {

        return getProperties().get(name).getBooleanValue();
    }

    @Override
    public Boolean hasProperty(String name) {

        return getProperties().containsKey(name);
    }
}
