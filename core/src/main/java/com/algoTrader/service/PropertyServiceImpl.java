package com.algoTrader.service;

import com.algoTrader.entity.Property;
import com.algoTrader.entity.PropertyHolder;
import com.algoTrader.util.HibernateUtil;

public class PropertyServiceImpl extends PropertyServiceBase {

    @Override
    protected PropertyHolder handleAddProperty(PropertyHolder configurable, String name, Object value) throws Exception {

        // reattach the configurable
        configurable = (PropertyHolder) HibernateUtil.reattach(this.getSessionFactory(), configurable);

        Property property = configurable.getProperties().get(name);
        if (property == null) {

            // create the property
            property = Property.Factory.newInstance();
            property.setName(name);
            property.setValue(value);

            getPropertyDao().create(property);

            configurable.getProperties().put(name, property);
            getPropertyHolderDao().update(configurable);

        } else {

            property.setValue(value);
            getPropertyDao().update(property);
        }

        return configurable;
    }

    @Override
    protected PropertyHolder handleRemoveProperty(PropertyHolder configurable, String name) throws Exception {

        Property property = configurable.getProperties().get(name);
        if (property != null) {

            getPropertyDao().remove(property);

            configurable.getProperties().remove(name);
        }

        return configurable;
    }
}
