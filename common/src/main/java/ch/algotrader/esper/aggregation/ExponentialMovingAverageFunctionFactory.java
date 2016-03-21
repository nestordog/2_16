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

import java.math.BigDecimal;

import com.espertech.esper.client.hook.AggregationFunctionFactory;
import com.espertech.esper.epl.agg.aggregator.AggregationMethod;
import com.espertech.esper.epl.agg.service.AggregationValidationContext;

/**
 * Exponential Moving Average Aggregation Function Factory
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class ExponentialMovingAverageFunctionFactory implements AggregationFunctionFactory {

    private int period;

    @Override
    public void setFunctionName(String functionName) {
        // do nothing
    }

    @Override
    public void validate(AggregationValidationContext validationContext) {
        if (validationContext.getParameterTypes().length != 2) {
            throw new IllegalArgumentException("exponential moving average requires two parameters");
        }

        if (validationContext.getParameterTypes()[0] != BigDecimal.class && validationContext.getParameterTypes()[0] != Double.class && validationContext.getParameterTypes()[0] != double.class) {
            throw new IllegalArgumentException("parameter 0 needs to be of type Double");
        }

        if (validationContext.getParameterTypes()[1] != Integer.class && validationContext.getParameterTypes()[1] != int.class) {
            throw new IllegalArgumentException("parameter 1 needs to be of type Integer");
        }

        if (!validationContext.getIsConstantValue()[1]) {
            throw new IllegalArgumentException("parameter 1 needs to be a constant");
        }

        this.period = (int) validationContext.getConstantValues()[1];

    }

    @Override
    public AggregationMethod newAggregator() {
        return new ExponentialMovingAverageFunction(this.period);
    }

    @Override
    public Class getValueType() {
        return Double.class;
    }

}
