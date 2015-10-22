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
package ch.algotrader.dao.property;

import java.util.List;

import ch.algotrader.dao.ReadWriteDao;
import ch.algotrader.entity.property.Property;

/**
 * DAO for {@link ch.algotrader.entity.property.Property} objects.
 *
 * @see ch.algotrader.entity.property.Property
 */
public interface PropertyDao extends ReadWriteDao<Property> {

    /**
     * Finds non-persistent Properties.
     * @return List<Property>
     */
    List<Property> findNonPersistent();

    // spring-dao merge-point
}
