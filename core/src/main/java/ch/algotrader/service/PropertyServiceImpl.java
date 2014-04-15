/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.service;

import org.apache.log4j.Logger;

import ch.algotrader.entity.property.Property;
import ch.algotrader.entity.property.PropertyHolder;
import ch.algotrader.util.MyLogger;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class PropertyServiceImpl extends PropertyServiceBase {

    private static Logger logger = MyLogger.getLogger(PropertyServiceImpl.class.getName());

    @Override
    protected PropertyHolder handleAddProperty(int propertyHolderId, String name, Object value, boolean persistent) throws Exception {

        // reattach the propertyHolder
        PropertyHolder propertyHolder = getPropertyHolderDao().load(propertyHolderId);

        Property property = propertyHolder.getProps().get(name);
        if (property == null) {

            // create the property
            property = Property.Factory.newInstance();
            property.setName(name);
            property.setValue(value);
            property.setPersistent(persistent);

            // associate the propertyHolder
            property.setPropertyHolder(propertyHolder);

            getPropertyDao().create(property);

            // reverse-associate the propertyHolder (after property has received an id)
            propertyHolder.getProps().put(name, property);

        } else {

            property.setValue(value);
        }

        logger.info("added property " + name + " value " + value + " to " + propertyHolder);

        return propertyHolder;
    }

    @Override
    protected PropertyHolder handleRemoveProperty(int propertyHolderId, String name) throws Exception {

        PropertyHolder propertyHolder = getPropertyHolderDao().load(propertyHolderId);
        Property property = propertyHolder.getProps().get(name);

        if (property != null) {

            getPropertyDao().remove(property.getId());

            propertyHolder.removeProps(name);
        }

        logger.info("removed property " + name + " from " + propertyHolder);

        return propertyHolder;
    }
}
