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
package ch.algotrader.service;

import java.util.Date;

import ch.algotrader.entity.strategy.Measurement;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.service.MeasurementServiceBase;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class MeasurementServiceImpl extends MeasurementServiceBase {

    @Override
    protected Measurement handleCreateMeasurement(String strategyName, String name, Date date, Object value) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);

        // find out if there is a measurement for specified strategyName, type and date
        Measurement measurement = getMeasurementDao().findMeasurementByDate(strategyName, name, date);

        if (measurement == null) {

            measurement = Measurement.Factory.newInstance();

            measurement.setStrategy(strategy);
            measurement.setName(name);
            measurement.setDate(date);
            measurement.setValue(value);

            getMeasurementDao().create(measurement);

        } else {

            measurement.setValue(value);
        }

        return measurement;
    }

    @Override
    protected Measurement handleCreateMeasurement(String strategyName, String name, Object value) throws Exception {

        return createMeasurement(strategyName, name, new Date(), value);
    }

    @Override
    protected  void handleDeleteMeasurement(int measurementId) throws Exception {

        getMeasurementDao().remove(measurementId);
    }
}
