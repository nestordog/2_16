/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.entity.strategy;

import org.junit.Before;
import org.junit.Test;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.EntityTest;
import ch.algotrader.entity.strategy.MeasurementDao;

/**
 * @author <a href="mailto:amasood@algotrader.ch">Ahmad Masood</a>
 *
 * @version $Revision$ $Date$
 */
public class MeasurementTest extends EntityTest {

    private MeasurementDao measurementDao;

    @Before
    public void before() {

        this.measurementDao = ServiceLocator.instance().getService("measurementDao", MeasurementDao.class);
    }

    @Test
    public void testFindAllMeasurementsByMaxDate() {

        this.measurementDao.findAllMeasurementsByMaxDate(null, null);
    }

    @Test
    public void testFindAllMeasurementsByMinDate() {

        this.measurementDao.findAllMeasurementsByMinDate(null, null);
    }

    @Test
    public void testFindMeasurementByDate() {

        this.measurementDao.findMeasurementByDate(null, null, null);
    }

    @Test
    public void testFindMeasurementsByMaxDate() {

        this.measurementDao.findMeasurementsByMaxDate(null, null, null);
    }

    @Test
    public void testFindMeasurementsByMinDate() {

        this.measurementDao.findMeasurementsByMinDate(null, null, null);
    }

}
