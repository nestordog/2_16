package com.algoTrader.service;

import com.algoTrader.entity.Property;
import com.algoTrader.entity.PropertyHolder;
import com.algoTrader.util.HibernateUtil;

public class PropertyServiceImpl extends PropertyServiceBase {

    @Override
    protected PropertyHolder handleAddProperty(PropertyHolder propertyHolder, String name, Object value, boolean persistent) throws Exception {

        // reattach the propertyHolder
        propertyHolder = (PropertyHolder) HibernateUtil.reattach(this.getSessionFactory(), propertyHolder);

        Property property = propertyHolder.getProperties().get(name);
        if (property == null) {

            // create the property
            property = Property.Factory.newInstance();
            property.setName(name);
            property.setValue(value);
            property.setPersistent(persistent);
            property.setPropertyHolder(propertyHolder);

            getPropertyDao().create(property);

            propertyHolder.getProperties().put(name, property);
            getPropertyHolderDao().update(propertyHolder);

        } else {

            property.setValue(value);
            getPropertyDao().update(property);
        }

        return propertyHolder;
    }

    @Override
    protected PropertyHolder handleRemoveProperty(PropertyHolder propertyHolder, String name) throws Exception {

        Property property = propertyHolder.getProperties().get(name);
        if (property != null) {

            getPropertyDao().remove(property);

            propertyHolder.removeProperties(name);
        }

        return propertyHolder;
    }
}
