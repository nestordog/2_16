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
package ch.algotrader.entity.security;

import java.util.List;

import ch.algotrader.hibernate.ReadWriteDao;

/**
 * DAO for {@link ch.algotrader.entity.security.Stock} objects.
 *
 * @see ch.algotrader.entity.security.Stock
 */
public interface StockDao extends ReadWriteDao<Stock> {

    /**
     *
     * @param code
     * @return List<Stock>
     */
    List<Stock> findBySectory(String code);

    /**
     *
     * @param code
     * @return List<Stock>
     */
    List<Stock> findByIndustryGroup(String code);

    /**
     *
     * @param code
     * @return List<Stock>
     */
    List<Stock> findByIndustry(String code);

    /**
     *
     * @param code
     * @return List<Stock>
     */
    List<Stock> findBySubIndustry(String code);

    /**
     * Finds all Stocks of the specified {@link SecurityFamily}
     * @param securityFamilyId
     * @return List<Stock>
     */
    List<Stock> findStocksBySecurityFamily(long securityFamilyId);

    // spring-dao merge-point
}