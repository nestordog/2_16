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
package ch.algotrader.esper.aggregation;

import com.espertech.esper.epl.agg.aggregator.AggregationMethod;

/**
 * Exponential Moving Average Aggregation Function
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class ExponentialMovingAverageFunction implements AggregationMethod {

    private final int period;

    private double ema;
    private long numDataPoints;

    public ExponentialMovingAverageFunction(int period) {
        this.period = period;
    }

    @Override
    public void clear() {
        this.ema = 0;
        this.numDataPoints = 0;
    }

    @Override
    public void enter(Object object) {
        if (object == null) {
            return;
        }
        this.numDataPoints++;

        Object[] array = (Object[]) object;
        double value = ((Number) array[0]).doubleValue();
        if (this.numDataPoints == 1) {
            this.ema = value;
        } else if (this.numDataPoints <= this.period) {
            this.ema = ((this.ema * (this.numDataPoints - 1)) + value) / this.numDataPoints;
        } else {
            double exponent = 2.0 / (this.period + 1.0);
            this.ema = value * exponent + this.ema * (1.0 - exponent);
        }

    }

    @Override
    public void leave(Object object) {
        // no action
    }

    @Override
    public Object getValue() {
        if (this.numDataPoints == 0) {
            return null;
        }

        return this.ema;
    }

    @Override
    public Class getValueType() {
        return Double.class;
    }
}
