package com.algoTrader.service;

import com.algoTrader.entity.Configurable;
import com.algoTrader.entity.Property;
import com.algoTrader.util.HibernateUtil;

public class PropertyServiceImpl extends PropertyServiceBase {

    @Override
    protected Configurable handleAddProperty(Configurable configurable, String name, Object value) throws Exception {

        // lock and merge the configurable
        if (!HibernateUtil.lock(this.getSessionFactory(), configurable)) {
            configurable = (Configurable) HibernateUtil.merge(this.getSessionFactory(), configurable);
        }

        Property property = configurable.getProperties().get(name);
        if (property == null) {

            // create the property
            property = Property.Factory.newInstance();
            property.setName(name);
            property.setValue(value);

            getPropertyDao().create(property);

            configurable.getProperties().put(name, property);
            getConfigurableDao().update(configurable);

        } else {

            property.setValue(value);
            getPropertyDao().update(property);
        }

        return configurable;
    }

    @Override
    protected Configurable handleRemoveProperty(Configurable configurable, String name) throws Exception {

        Property property = configurable.getProperties().get(name);
        if (property != null) {

            getPropertyDao().remove(property);

            configurable.getProperties().remove(name);
        }

        return configurable;
    }
}
