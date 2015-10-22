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
package ch.algotrader.dao.strategy;

import java.util.Date;
import java.util.List;

import ch.algotrader.dao.ReadWriteDao;
import ch.algotrader.entity.strategy.Measurement;

/**
 * DAO for {@link ch.algotrader.entity.strategy.Measurement} objects.
 *
 * @see ch.algotrader.entity.strategy.Measurement
 */
public interface MeasurementDao extends ReadWriteDao<Measurement> {

    /**
     * Finds a Measurement of the specified Date with the specified name
     * @param strategyName
     * @param name
     * @param dateTime
     * @return Measurement
     */
    Measurement findMeasurementByDate(String strategyName, String name, Date dateTime);

    /**
     * Finds all Measurements before the specified Date with the specified name
     * @param strategyName
     * @param name
     * @param maxDateTime
     * @return List<Measurement>
     */
    List<Measurement> findMeasurementsByMaxDate(String strategyName, String name, Date maxDateTime);

    /**
     * <p>
     * Does the same thing as {@link #findMeasurementsByMaxDate(String, String, Date)} with an
     * additional argument called <code>limit</code>. The <code>limit</code>
     * argument allows you to specify the page number when you are paging the results.
     * </p>
     * @param limit
     * @param strategyName
     * @param name
     * @param maxDateTime
     * @return List<Measurement>
     */
    List<Measurement> findMeasurementsByMaxDate(int limit, String strategyName, String name, Date maxDateTime);

    /**
     * Finds all Measurements before the specified Date
     * @param strategyName
     * @param maxDateTime
     * @return List<Measurement>
     */
    List<Measurement> findAllMeasurementsByMaxDate(String strategyName, Date maxDateTime);

    /**
     * Finds all Measurements after the specified Date with the specified name
     * @param strategyName
     * @param name
     * @param minDateTime
     * @return List<Measurement>
     */
    List<Measurement> findMeasurementsByMinDate(String strategyName, String name, Date minDateTime);

    /**
     * <p>
     * Does the same thing as {@link #findMeasurementsByMinDate(String, String, Date)} with an
     * additional argument called <code>limit</code>. The <code>limit</code>
     * argument allows you to specify the page number when you are paging the results.
     * </p>
     * @param limit
     * @param strategyName
     * @param name
     * @param minDateTime
     * @return List<Measurement>
     */
    List<Measurement> findMeasurementsByMinDate(int limit, String strategyName, String name, Date minDateTime);

    /**
     * Finds all Measurements after the specified Date
     * @param strategyName
     * @param minDateTime
     * @return List<Measurement>
     */
    List<Measurement> findAllMeasurementsByMinDate(String strategyName, Date minDateTime);

    // spring-dao merge-point
}
