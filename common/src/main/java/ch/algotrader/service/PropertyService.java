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

import ch.algotrader.entity.property.PropertyHolder;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface PropertyService {

    /**
     * Adds a Property with the specified {@code name} and {@code value} assigned to the specified
     * PropertyHolder. If {@code persistent} is set to {@code false}, the Property will be removed
     * when resetting the database before a simulation run.
     */
    public PropertyHolder addProperty(int propertyHolderId, String name, Object value, boolean persistent);

    /**
     * Removes the specified Property from the specified PropertyHolder.
     */
    public PropertyHolder removeProperty(int propertyHolderid, String name);

}
