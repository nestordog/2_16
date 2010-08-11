package com.algoTrader.util;

import com.espertech.esper.epl.agg.AggregationSupport;

public class ExponentialMovingAverageFunction extends AggregationSupport {

    private int n;
    private double emaValue;

    public ExponentialMovingAverageFunction() {
        super();
        this.n = 0;
        this.emaValue = 0.0;
    }

    @SuppressWarnings("unchecked")
    public void validate(Class childNodeType) {
        if (childNodeType != double.class && childNodeType != Double.class) {
            throw new IllegalArgumentException("EMA aggregation requires a double parameter");
        }
    }

    public void enter(Object obj) {

        Object[] params = (Object[]) obj;
        double value = (Double) params[0];
        int periods = (Integer) params[1];
        double exponent = 2.0 / (periods + 1.0);

        this.n++;
        if (this.n == 1) {
            this.emaValue = value;
        } else if (this.n <= periods) {
            this.emaValue = (this.emaValue * (this.n - 1) + value) / this.n;
        } else {
            this.emaValue = value * exponent + (1 - exponent) * this.emaValue;
        }
    }

    public void leave(Object value) {
        throw new IllegalArgumentException("leave not allowed");
    }

    public Class<Double> getValueType() {
        return Double.class;
    }

    public Object getValue() {
        return this.emaValue;
    }

    public void clear() {
        throw new IllegalArgumentException("clear not allowed");
    }
}
