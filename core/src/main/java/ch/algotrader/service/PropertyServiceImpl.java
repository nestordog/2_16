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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.dao.property.PropertyDao;
import ch.algotrader.dao.property.PropertyHolderDao;
import ch.algotrader.entity.property.Property;
import ch.algotrader.entity.property.PropertyHolder;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@Transactional
public class PropertyServiceImpl implements PropertyService {

    private static final Logger LOGGER = LogManager.getLogger(PropertyServiceImpl.class);

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
    public PropertyHolder addProperty(final long propertyHolderId, final String name, final Object value, final boolean persistent) {

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

            this.propertyDao.save(property);

            // reverse-associate the propertyHolder (after property has received an id)
            propertyHolder.getProps().put(name, property);

        } else {

            property.setValue(value);
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("added property {} value {} to {}", name, value, propertyHolder);
        }

        return propertyHolder;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public PropertyHolder removeProperty(final long propertyHolderId, final String name) {

        Validate.notEmpty(name, "Name is empty");

        PropertyHolder propertyHolder = this.propertyHolderDao.load(propertyHolderId);
        Property property = propertyHolder.getProps().get(name);

        if (property != null) {

            propertyHolder.removeProps(name);

            this.propertyDao.deleteById(property.getId());
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("removed property {} from {}", name, propertyHolder);
        }

        return propertyHolder;

    }
}
