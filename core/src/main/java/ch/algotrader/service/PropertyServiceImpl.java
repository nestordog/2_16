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

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.entity.property.Property;
import ch.algotrader.entity.property.PropertyDao;
import ch.algotrader.entity.property.PropertyHolder;
import ch.algotrader.entity.property.PropertyHolderDao;
import ch.algotrader.util.MyLogger;
import ch.algotrader.util.spring.HibernateSession;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@HibernateSession
public class PropertyServiceImpl implements PropertyService {

    private static Logger logger = MyLogger.getLogger(PropertyServiceImpl.class.getName());

    private final PropertyDao propertyDao;

    private final PropertyHolderDao propertyHolderDao;

    public PropertyServiceImpl(final PropertyDao propertyDao, final PropertyHolderDao propertyHolderDao) {

        Validate.notNull(propertyDao, "PropertyDao is null");
        Validate.notNull(propertyHolderDao, "PropertyHolderDao is null");

        this.propertyDao = propertyDao;
        this.propertyHolderDao = propertyHolderDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public PropertyHolder addProperty(final int propertyHolderId, final String name, final Object value, final boolean persistent) {

        Validate.notEmpty(name, "Name is empty");
        Validate.notNull(value, "Value is null");

        // reattach the propertyHolder
        PropertyHolder propertyHolder = this.propertyHolderDao.load(propertyHolderId);

        Property property = propertyHolder.getProps().get(name);
        if (property == null) {

            // create the property
            property = Property.Factory.newInstance();
            property.setName(name);
            property.setValue(value);
            property.setPersistent(persistent);

            // associate the propertyHolder
            property.setPropertyHolder(propertyHolder);

            this.propertyDao.create(property);

            // reverse-associate the propertyHolder (after property has received an id)
            propertyHolder.getProps().put(name, property);

        } else {

            property.setValue(value);
        }

        logger.info("added property " + name + " value " + value + " to " + propertyHolder);

        return propertyHolder;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public PropertyHolder removeProperty(final int propertyHolderId, final String name) {

        Validate.notEmpty(name, "Name is empty");

        PropertyHolder propertyHolder = this.propertyHolderDao.load(propertyHolderId);
        Property property = propertyHolder.getProps().get(name);

        if (property != null) {

            this.propertyDao.remove(property.getId());

            propertyHolder.removeProps(name);
        }

        logger.info("removed property " + name + " from " + propertyHolder);

        return propertyHolder;

    }
}
