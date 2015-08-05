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
package ch.algotrader.dao.security;

import ch.algotrader.dao.ReadWriteDao;
import ch.algotrader.entity.security.SecurityReference;

/**
 * DAO for {@link ch.algotrader.entity.security.SecurityReference} objects.
 *
 * @see ch.algotrader.entity.security.SecurityReference
 */
public interface SecurityReferenceDao extends ReadWriteDao<SecurityReference> {

    /**
     * Finds a SecurityReference by owner security and reference {@code name}
     * @param ownerSecurityId the ID of the owner security
     * @param name the name of the reference
     * @return SecurityReference the security reference for the specified arguments or null if not found
     */
    SecurityReference findByOwnerAndName(long ownerSecurityId, String name);
}
