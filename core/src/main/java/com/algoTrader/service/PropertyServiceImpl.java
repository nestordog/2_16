package com.algoTrader.service;

import java.util.Map;

import com.algoTrader.entity.Configurable;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.Property;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.Subscription;
import com.algoTrader.entity.Transaction;
import com.algoTrader.util.HibernateUtil;

public class PropertyServiceImpl extends PropertyServiceBase {

    @Override
    protected Configurable handleAddProperty(Configurable configurable, String name, Object value) throws Exception {

        // lock and merge the configurable
        if (!HibernateUtil.lock(this.getSessionFactory(), configurable)) {
            configurable = (Strategy) HibernateUtil.merge(this.getSessionFactory(), configurable);
        }

        Property property = getProperties(configurable).get(name);
        if (property == null) {

            // create the property
            property = Property.Factory.newInstance();
            property.setName(name);
            property.setValue(value);

            getPropertyDao().create(property);

            getProperties(configurable).put(name, property);
            updateConfigurable(configurable);

        } else {

            property.setValue(value);
            getPropertyDao().update(property);
        }

        return configurable;
    }

    @Override
    protected Configurable handleRemoveProperty(Configurable configurable, String name) throws Exception {

        Property property = getProperties(configurable).get(name);
        if (property != null) {

            getPropertyDao().remove(property);

            getProperties(configurable).remove(name);
        }

        return configurable;
    }

    private Map<String, Property> getProperties(Configurable configurable) {

        // needs to be done this way, because mapped relations are not possible on Interfaces
        // Configurable is not an abstract Entity because this would be too infasive on the entire model
        if (configurable instanceof Strategy) {
            return ((Strategy) configurable).getProperties();
        } else if (configurable instanceof Position) {
            return ((Position) configurable).getProperties();
        } else if (configurable instanceof Transaction) {
            return ((Transaction) configurable).getProperties();
        } else if (configurable instanceof Subscription) {
            return ((Subscription) configurable).getProperties();
        } else {
            throw new IllegalArgumentException("unsupport configurable type " + configurable.getClass());
        }
    }

    private void updateConfigurable(Configurable configurable) {

        // this will set the xx_FK in property table
        if (configurable instanceof Strategy) {
            getStrategyDao().update((Strategy) configurable);
        } else if (configurable instanceof Position) {
            getPositionDao().update((Position) configurable);
        } else if (configurable instanceof Transaction) {
            getTransactionDao().update((Transaction) configurable);
        } else if (configurable instanceof Subscription) {
            getSubscriptionDao().update((Subscription) configurable);
        } else {
            throw new IllegalArgumentException("unsupport configurable type " + configurable.getClass());
        }
    }
}
