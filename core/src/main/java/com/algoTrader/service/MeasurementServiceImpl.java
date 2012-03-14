package com.algoTrader.service;

import java.util.Date;

import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.measurement.Measurement;

public class MeasurementServiceImpl extends MeasurementServiceBase {

    @Override
    protected Measurement handleCreateMeasurement(String strategyName, String type, Date date, double value) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);

        // find out if there is a measurement for specified strategyName, type and date
        Measurement measurement = getMeasurementDao().findMeasurement(strategyName, type, date);

        if (measurement != null) {

            measurement.setValue(value);

            getMeasurementDao().update(measurement);

        } else {

            measurement = Measurement.Factory.newInstance();

            measurement.setStrategy(strategy);
            measurement.setType(type);
            measurement.setDate(date);
            measurement.setValue(value);

            getMeasurementDao().create(measurement);
        }

        return measurement;
    }

    @Override
    protected  void handleDeleteMeasurement(int measurementId) throws Exception {

        getMeasurementDao().remove(measurementId);
    }
}
