/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.service;

import ch.algotrader.entity.property.PropertyHolder;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public interface PropertyService {

    /**
     * Adds a Property with the specified {@code name} and {@code value} assigned to the specified
     * PropertyHolder. If {@code persistent} is set to {@code false}, the Property will be removed
     * when resetting the database before a simulation run.
     */
    public PropertyHolder addProperty(long propertyHolderId, String name, Object value, boolean persistent);

    /**
     * Removes the specified Property from the specified PropertyHolder.
     */
    public PropertyHolder removeProperty(long propertyHolderid, String name);

}
