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
import java.util.HashMap;
import java.util.Map;

import ch.algotrader.entity.property.Property;
import ch.algotrader.entity.property.PropertyHolder;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class PropertyHolderImpl extends PropertyHolder {

    private static final long serialVersionUID = 2154726089257967279L;

    @Override
    public int getIntProperty(String name) {

        Property property = getPropsInitialized().get(name);
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

        Property property = getPropsInitialized().get(name);
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

        Property property = getPropsInitialized().get(name);
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
    public String getTextProperty(String name) {

        Property property = getPropsInitialized().get(name);
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

        Property property = getPropsInitialized().get(name);
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

        Property property = getPropsInitialized().get(name);
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

        return getProps().containsKey(name);
    }

    @Override
    public Map<String, Object> getPropertyNameValueMap() {

        Map<String, Object> nameValuePairs = new HashMap<String, Object>();
        for (Property property : getProps().values()) {
            nameValuePairs.put(property.getName(), property.getValue());
        }
        return nameValuePairs;
    }
}
