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
package ch.algotrader.entity.strategy;

import org.junit.Before;
import org.junit.Test;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.EntityTest;

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
