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

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.dao.strategy.MeasurementDao;
import ch.algotrader.dao.strategy.MeasurementDaoImpl;
import ch.algotrader.hibernate.InMemoryDBTest;

/**
* Unit tests for {@link MeasurementDaoImpl}.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*
* @version $Revision$ $Date$
*/
public class MeasurementDaoTest extends InMemoryDBTest {

    private MeasurementDao dao;

    private Strategy strategy;

    public MeasurementDaoTest() throws IOException {

        super();
    }

    @Override
    @Before
    public void setup() throws Exception {

        super.setup();

        this.dao = new MeasurementDaoImpl(this.sessionFactory);

        this.strategy = new StrategyImpl();
        this.strategy.setName("Strategy1");
    }

    @Test
    public void testFindMeasurementByDate() {

        Calendar cal1 = Calendar.getInstance();

        Measurement measurement1 = new MeasurementImpl();
        measurement1.setName("Measurement");
        measurement1.setDateTime(cal1.getTime());
        measurement1.setStrategy(this.strategy);

        this.session.save(this.strategy);
        this.session.save(measurement1);
        this.session.flush();

        Measurement measurement2 = this.dao.findMeasurementByDate("Dummy", "Measurement", cal1.getTime());

        Assert.assertNull(measurement2);

        Measurement measurement3 = this.dao.findMeasurementByDate("Strategy1", "Dummy", cal1.getTime());

        Assert.assertNull(measurement3);

        Calendar dummy = Calendar.getInstance();
        dummy.add(Calendar.HOUR_OF_DAY, 1);

        Measurement measurement4 = this.dao.findMeasurementByDate("Strategy1", "Measurement", dummy.getTime());

        Assert.assertNull(measurement4);

        Measurement measurement5 = this.dao.findMeasurementByDate("Strategy1", "Measurement", cal1.getTime());

        Assert.assertNotNull(measurement5);

        Assert.assertSame(this.strategy, measurement5.getStrategy());
        Assert.assertSame(measurement1, measurement5);
    }

    @Test
    public void testFindMeasurementsByMaxDate() {

        Calendar cal1 = Calendar.getInstance();

        Measurement measurement1 = new MeasurementImpl();
        measurement1.setName("Measurement");
        measurement1.setDateTime(cal1.getTime());
        measurement1.setStrategy(this.strategy);

        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.HOUR_OF_DAY, 1);

        Measurement measurement2 = new MeasurementImpl();
        measurement2.setName("Measurement");
        measurement2.setDateTime(cal2.getTime());
        measurement2.setStrategy(this.strategy);

        this.session.save(this.strategy);
        this.session.save(measurement1);
        this.session.save(measurement2);
        this.session.flush();

        List<Measurement> measurements1 = this.dao.findMeasurementsByMaxDate("Dummy", "Measurement", cal1.getTime());

        Assert.assertEquals(0, measurements1.size());

        List<Measurement> measurements2 = this.dao.findMeasurementsByMaxDate("Strategy1", "Dummy", cal1.getTime());

        Assert.assertEquals(0, measurements2.size());

        Calendar before = Calendar.getInstance();
        before.add(Calendar.HOUR_OF_DAY, -1);

        List<Measurement> measurements3 = this.dao.findMeasurementsByMaxDate("Strategy1", "Measurement", before.getTime());

        Assert.assertEquals(0, measurements3.size());

        List<Measurement> measurements4 = this.dao.findMeasurementsByMaxDate("Strategy1", "Measurement", cal1.getTime());

        Assert.assertEquals(1, measurements4.size());

        Assert.assertSame(this.strategy, measurements4.get(0).getStrategy());
        Assert.assertSame(measurement1, measurements4.get(0));

        List<Measurement> measurements5 = this.dao.findMeasurementsByMaxDate("Strategy1", "Measurement", cal2.getTime());

        Assert.assertEquals(2, measurements5.size());

        Assert.assertSame(this.strategy, measurements5.get(0).getStrategy());
        Assert.assertSame(measurement2, measurements5.get(0));
        Assert.assertSame(this.strategy, measurements5.get(1).getStrategy());
        Assert.assertSame(measurement1, measurements5.get(1));
    }

    @Test
    public void testFindMeasurementsByMaxDateByLimit() {

        Calendar cal1 = Calendar.getInstance();

        Measurement measurement1 = new MeasurementImpl();
        measurement1.setName("Measurement");
        measurement1.setDateTime(cal1.getTime());
        measurement1.setStrategy(this.strategy);

        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.HOUR_OF_DAY, 1);

        Measurement measurement2 = new MeasurementImpl();
        measurement2.setName("Measurement");
        measurement2.setDateTime(cal2.getTime());
        measurement2.setStrategy(this.strategy);

        this.session.save(this.strategy);
        this.session.save(measurement1);
        this.session.save(measurement2);
        this.session.flush();

        List<Measurement> measurements1 = this.dao.findMeasurementsByMaxDate(2, "Dummy", "Measurement", cal1.getTime());

        Assert.assertEquals(0, measurements1.size());

        List<Measurement> measurements2 = this.dao.findMeasurementsByMaxDate(2, "Strategy1", "Dummy", cal1.getTime());

        Assert.assertEquals(0, measurements2.size());

        Calendar before = Calendar.getInstance();
        before.add(Calendar.HOUR_OF_DAY, -1);

        List<Measurement> measurements3 = this.dao.findMeasurementsByMaxDate(2, "Strategy1", "Measurement", before.getTime());

        Assert.assertEquals(0, measurements3.size());

        List<Measurement> measurements4 = this.dao.findMeasurementsByMaxDate(2, "Strategy1", "Measurement", cal1.getTime());

        Assert.assertEquals(1, measurements4.size());

        Assert.assertSame(this.strategy, measurements4.get(0).getStrategy());
        Assert.assertSame(measurement1, measurements4.get(0));

        List<Measurement> measurements5 = this.dao.findMeasurementsByMaxDate(2, "Strategy1", "Measurement", cal2.getTime());

        Assert.assertEquals(2, measurements5.size());

        Assert.assertSame(this.strategy, measurements5.get(0).getStrategy());
        Assert.assertSame(measurement2, measurements5.get(0));
        Assert.assertSame(this.strategy, measurements5.get(1).getStrategy());
        Assert.assertSame(measurement1, measurements5.get(1));

        List<Measurement> measurements6 = this.dao.findMeasurementsByMaxDate(1, "Strategy1", "Measurement", cal2.getTime());

        Assert.assertEquals(1, measurements6.size());

        List<Measurement> measurements7 = this.dao.findMeasurementsByMaxDate(3, "Strategy1", "Measurement", cal2.getTime());

        Assert.assertEquals(2, measurements7.size());
    }

    @Test
    public void testFindAllMeasurementsByMaxDate() {

        Calendar cal1 = Calendar.getInstance();

        Measurement measurement1 = new MeasurementImpl();
        measurement1.setName("Measurement");
        measurement1.setDateTime(cal1.getTime());
        measurement1.setStrategy(this.strategy);

        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.HOUR_OF_DAY, 1);

        Measurement measurement2 = new MeasurementImpl();
        measurement2.setName("Measurement");
        measurement2.setDateTime(cal2.getTime());
        measurement2.setStrategy(this.strategy);

        this.session.save(this.strategy);
        this.session.save(measurement1);
        this.session.save(measurement2);
        this.session.flush();

        List<Measurement> measurements1 = this.dao.findAllMeasurementsByMaxDate("Dummy", cal1.getTime());

        Assert.assertEquals(0, measurements1.size());

        Calendar before = Calendar.getInstance();
        before.add(Calendar.HOUR_OF_DAY, -1);

        List<Measurement> measurements2 = this.dao.findAllMeasurementsByMaxDate("Strategy1", before.getTime());

        Assert.assertEquals(0, measurements2.size());

        List<Measurement> measurements3 = this.dao.findAllMeasurementsByMaxDate("Strategy1", cal1.getTime());

        Assert.assertEquals(1, measurements3.size());

        Assert.assertSame(this.strategy, measurements3.get(0).getStrategy());
        Assert.assertSame(measurement1, measurements3.get(0));

        List<Measurement> measurements4 = this.dao.findAllMeasurementsByMaxDate("Strategy1", cal2.getTime());

        Assert.assertEquals(2, measurements4.size());

        Assert.assertSame(this.strategy, measurements4.get(0).getStrategy());
        Assert.assertSame(measurement2, measurements4.get(0));
        Assert.assertSame(this.strategy, measurements4.get(1).getStrategy());
        Assert.assertSame(measurement1, measurements4.get(1));
    }

    @Test
    public void testFindMeasurementsByMinDate() {

        Calendar cal1 = Calendar.getInstance();

        Measurement measurement1 = new MeasurementImpl();
        measurement1.setName("Measurement");
        measurement1.setDateTime(cal1.getTime());
        measurement1.setStrategy(this.strategy);

        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.HOUR_OF_DAY, 1);

        Measurement measurement2 = new MeasurementImpl();
        measurement2.setName("Measurement");
        measurement2.setDateTime(cal2.getTime());
        measurement2.setStrategy(this.strategy);

        this.session.save(this.strategy);
        this.session.save(measurement1);
        this.session.save(measurement2);
        this.session.flush();

        List<Measurement> measurements1 = this.dao.findMeasurementsByMinDate("Dummy", "Measurement", cal1.getTime());

        Assert.assertEquals(0, measurements1.size());

        List<Measurement> measurements2 = this.dao.findMeasurementsByMinDate("Strategy1", "Dummy", cal1.getTime());

        Assert.assertEquals(0, measurements2.size());

        Calendar after = Calendar.getInstance();
        after.add(Calendar.HOUR_OF_DAY, 2);

        List<Measurement> measurements3 = this.dao.findMeasurementsByMinDate("Strategy1", "Measurement", after.getTime());

        Assert.assertEquals(0, measurements3.size());

        List<Measurement> measurements4 = this.dao.findMeasurementsByMinDate("Strategy1", "Measurement", cal2.getTime());

        Assert.assertEquals(1, measurements4.size());

        Assert.assertSame(this.strategy, measurements4.get(0).getStrategy());
        Assert.assertSame(measurement2, measurements4.get(0));

        List<Measurement> measurements5 = this.dao.findMeasurementsByMinDate("Strategy1", "Measurement", cal1.getTime());

        Assert.assertEquals(2, measurements5.size());

        Assert.assertSame(this.strategy, measurements5.get(0).getStrategy());
        Assert.assertSame(measurement1, measurements5.get(0));
        Assert.assertSame(this.strategy, measurements5.get(1).getStrategy());
        Assert.assertSame(measurement2, measurements5.get(1));
    }

    @Test
    public void testFindMeasurementsByMinDateByLimit() {

        Calendar cal1 = Calendar.getInstance();

        Measurement measurement1 = new MeasurementImpl();
        measurement1.setName("Measurement");
        measurement1.setDateTime(cal1.getTime());
        measurement1.setStrategy(this.strategy);

        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.HOUR_OF_DAY, 1);

        Measurement measurement2 = new MeasurementImpl();
        measurement2.setName("Measurement");
        measurement2.setDateTime(cal2.getTime());
        measurement2.setStrategy(this.strategy);

        this.session.save(this.strategy);
        this.session.save(measurement1);
        this.session.save(measurement2);
        this.session.flush();

        List<Measurement> measurements1 = this.dao.findMeasurementsByMinDate(2, "Dummy", "Measurement", cal1.getTime());

        Assert.assertEquals(0, measurements1.size());

        List<Measurement> measurements2 = this.dao.findMeasurementsByMinDate(2, "Strategy1", "Dummy", cal1.getTime());

        Assert.assertEquals(0, measurements2.size());

        Calendar after = Calendar.getInstance();
        after.add(Calendar.HOUR_OF_DAY, 2);

        List<Measurement> measurements3 = this.dao.findMeasurementsByMinDate(2, "Strategy1", "Measurement", after.getTime());

        Assert.assertEquals(0, measurements3.size());

        List<Measurement> measurements4 = this.dao.findMeasurementsByMinDate(2, "Strategy1", "Measurement", cal2.getTime());

        Assert.assertEquals(1, measurements4.size());

        Assert.assertSame(this.strategy, measurements4.get(0).getStrategy());
        Assert.assertSame(measurement2, measurements4.get(0));

        List<Measurement> measurements5 = this.dao.findMeasurementsByMinDate(2, "Strategy1", "Measurement", cal1.getTime());

        Assert.assertEquals(2, measurements5.size());

        Assert.assertSame(this.strategy, measurements5.get(0).getStrategy());
        Assert.assertSame(measurement1, measurements5.get(0));
        Assert.assertSame(this.strategy, measurements5.get(1).getStrategy());
        Assert.assertSame(measurement2, measurements5.get(1));

        List<Measurement> measurements6 = this.dao.findMeasurementsByMinDate(1, "Strategy1", "Measurement", cal1.getTime());

        Assert.assertEquals(1, measurements6.size());

        List<Measurement> measurements7 = this.dao.findMeasurementsByMinDate(3, "Strategy1", "Measurement", cal1.getTime());

        Assert.assertEquals(2, measurements7.size());
    }

    @Test
    public void testFindAllMeasurementsByMinDate() {

        Calendar cal1 = Calendar.getInstance();

        Measurement measurement1 = new MeasurementImpl();
        measurement1.setName("Measurement");
        measurement1.setDateTime(cal1.getTime());
        measurement1.setStrategy(this.strategy);

        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.HOUR_OF_DAY, 1);

        Measurement measurement2 = new MeasurementImpl();
        measurement2.setName("Measurement");
        measurement2.setDateTime(cal2.getTime());
        measurement2.setStrategy(this.strategy);

        this.session.save(this.strategy);
        this.session.save(measurement1);
        this.session.save(measurement2);
        this.session.flush();

        List<Measurement> measurements1 = this.dao.findAllMeasurementsByMinDate("Dummy", cal1.getTime());

        Assert.assertEquals(0, measurements1.size());

        Calendar after = Calendar.getInstance();
        after.add(Calendar.HOUR_OF_DAY, 2);

        List<Measurement> measurements2 = this.dao.findAllMeasurementsByMinDate("Strategy1", after.getTime());

        Assert.assertEquals(0, measurements2.size());

        List<Measurement> measurements3 = this.dao.findAllMeasurementsByMinDate("Strategy1", cal2.getTime());

        Assert.assertEquals(1, measurements3.size());

        Assert.assertSame(this.strategy, measurements3.get(0).getStrategy());
        Assert.assertSame(measurement2, measurements3.get(0));

        List<Measurement> measurements4 = this.dao.findAllMeasurementsByMinDate("Strategy1", cal1.getTime());

        Assert.assertEquals(2, measurements4.size());

        Assert.assertSame(this.strategy, measurements4.get(0).getStrategy());
        Assert.assertSame(measurement1, measurements4.get(0));
        Assert.assertSame(this.strategy, measurements4.get(1).getStrategy());
        Assert.assertSame(measurement2, measurements4.get(1));
    }

}
