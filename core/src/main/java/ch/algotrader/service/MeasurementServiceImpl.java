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

import java.util.Date;

import org.apache.commons.lang.Validate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.dao.strategy.MeasurementDao;
import ch.algotrader.dao.strategy.StrategyDao;
import ch.algotrader.entity.strategy.Measurement;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.esper.EngineManager;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@Transactional
public class MeasurementServiceImpl implements MeasurementService {

    private final MeasurementDao measurementDao;

    private final StrategyDao strategyDao;

    private final EngineManager engineManager;

    public MeasurementServiceImpl(final MeasurementDao measurementDao,
            final StrategyDao strategyDao,
            final EngineManager engineManager) {

        Validate.notNull(measurementDao, "MeasurementDao is null");
        Validate.notNull(strategyDao, "StrategyDao is null");
        Validate.notNull(engineManager, "EngineManager is null");

        this.measurementDao = measurementDao;
        this.strategyDao = strategyDao;
        this.engineManager = engineManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Measurement createMeasurement(final String strategyName, final String name, final Object value) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notEmpty(name, "Name is empty");
        Validate.notNull(value, "Value is null");

        return createMeasurement(strategyName, name, this.engineManager.getCurrentEPTime(), value);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Measurement createMeasurement(final String strategyName, final String name, final Date date, final Object value) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notEmpty(name, "Name is empty");
        Validate.notNull(date, "Date is null");
        Validate.notNull(value, "Value is null");

        Strategy strategy = this.strategyDao.findByName(strategyName);

        // find out if there is a measurement for specified strategyName, type and date
        Measurement measurement = this.measurementDao.findMeasurementByDate(strategyName, name, date);

        if (measurement == null) {

            measurement = Measurement.Factory.newInstance();

            measurement.setStrategy(strategy);
            measurement.setName(name);
            measurement.setDateTime(date);
            measurement.setValue(value);

            this.measurementDao.save(measurement);

        } else {

            measurement.setValue(value);
        }

        return measurement;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteMeasurement(final long measurementId) {

        this.measurementDao.deleteById(measurementId);

    }
}
