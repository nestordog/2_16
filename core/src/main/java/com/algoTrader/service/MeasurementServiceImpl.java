package com.algoTrader.service;

import java.util.Date;

import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.strategy.Measurement;

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
    protected  void handleDeleteMeasurement(int measurementId) throws Exception {

        getMeasurementDao().remove(measurementId);
    }
}
