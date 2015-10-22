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
package ch.algotrader.dao.security;

import java.util.Date;
import java.util.List;

import ch.algotrader.dao.ReadWriteDao;
import ch.algotrader.entity.security.EasyToBorrow;

/**
 * DAO for {@link ch.algotrader.entity.security.EasyToBorrow} objects.
 *
 * @see ch.algotrader.entity.security.EasyToBorrow
 */
public interface EasyToBorrowDao extends ReadWriteDao<EasyToBorrow> {

    /**
     * Finds the EasyToBorrow for the specified {@code date} and {@code broker}
     * @param date
     * @param broker
     * @return List<EasyToBorrow>
     */
    List<EasyToBorrow> findByDateAndBroker(Date date, String broker);

    // spring-dao merge-point
}
