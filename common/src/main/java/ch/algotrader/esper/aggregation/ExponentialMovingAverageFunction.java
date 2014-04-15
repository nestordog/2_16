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
package ch.algotrader.esper.aggregation;

import com.espertech.esper.epl.agg.service.AggregationSupport;
import com.espertech.esper.epl.agg.service.AggregationValidationContext;

/**
 * The ExponentialMovingAverageFunction provides an exponential moving average over a defined period of time.
 * To use the AggregateFunction the following configuration has to be added to the esper configuration :
 * <pre>
 * &lt;plugin-aggregation-function name="ema" factory-class="ch.algotrader.esper.aggregation.ExponentialMovingAverageFunction"/&gt;
 * </pre>
 * The AggregationFunction can then be used in an esper statement like this:
 * <pre>
 * select ema(value, 10)
 * from Event;
 * </pre>
 * <i>Note: ExponentialMovingAverage is also available through the GenericTALibFunction.</i>
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class ExponentialMovingAverageFunction extends AggregationSupport {

    private int n;
    private double emaValue;

    public ExponentialMovingAverageFunction() {
        super();
        this.n = 0;
        this.emaValue = 0.0;
    }

    @Override
    public void validate(AggregationValidationContext validationContext) {
        if ((validationContext.getParameterTypes().length == 2)
                && ((validationContext.getParameterTypes()[0] == double.class) || (validationContext.getParameterTypes()[0] == Double.class))
                && ((validationContext.getParameterTypes()[1] == int.class) || (validationContext.getParameterTypes()[1] == Integer.class))) {
        } else {
            throw new IllegalArgumentException("EMA aggregation requires a double parameter");
        }
    }

    @Override
    public void enter(Object obj) {

        Object[] params = (Object[]) obj;
        double value = (Double) params[0];
        int periods = (Integer) params[1];
        double exponent = 2.0 / (periods + 1.0);

        this.n++;
        if (this.n == 1) {
            this.emaValue = value;
        } else {
            this.emaValue = value * exponent + (1 - exponent) * this.emaValue;
        }
    }

    @Override
    public void leave(Object value) {
        throw new IllegalArgumentException("leave not allowed");
    }

    @Override
    public Class<Double> getValueType() {
        return Double.class;
    }

    @Override
    public Object getValue() {
        return this.emaValue;
    }

    @Override
    public void clear() {
        throw new IllegalArgumentException("clear not allowed");
    }
}
