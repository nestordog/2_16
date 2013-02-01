package com.algoTrader.esper.aggregation;

import com.espertech.esper.epl.agg.service.AggregationSupport;
import com.espertech.esper.epl.agg.service.AggregationValidationContext;

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
