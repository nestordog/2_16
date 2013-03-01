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
package com.algoTrader.esper.aggregation;

import java.util.ArrayList;
import java.util.List;

import com.espertech.esper.epl.agg.service.AggregationSupport;
import com.espertech.esper.epl.agg.service.AggregationValidationContext;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class VariableLengthLinestFunction extends AggregationSupport {

    private List<Double> xs = new ArrayList<Double>();
    private List<Double> ys = new ArrayList<Double>();
    private int requestedLength = 0;

    @Override
    public void validate(AggregationValidationContext validationContext) {
        // not implemented yet
    }

    @Override
    public void enter(Object obj) {
        Object[] params = (Object[]) obj;
        this.xs.add((Double) params[0]);
        this.ys.add((Double) params[1]);
        this.requestedLength = (Integer) params[2];
    }

    @Override
    public void leave(Object obj) {
        // not implemented yet
    }

    @Override
    public Class<Double> getValueType() {
        return Double.class;
    }

    @Override
    public Object getValue() {

        int size = this.xs.size();

        if (size < 2) {
            return null;
        }

        List<Double> subX = this.xs.subList(Math.max(0, size - this.requestedLength), size);
        List<Double> subY = this.ys.subList(Math.max(0, size - this.requestedLength), size);

        // get all x and y

        int n = subX.size();
        double sumX = 0;
        double sumY = 0;
        double sumXSq = 0;
        double sumXY = 0;
        for (int i = 0; i < subX.size(); i++) {
            double x = subX.get(i);
            double y = subY.get(i);

            sumX += x;
            sumY += y;
            sumXSq += x * x;
            sumXY += x * y;
        }

        double ssx = sumXSq - sumX * sumX / n;
        if (ssx == 0) {
            return Double.NaN;
        }

        double sp = sumXY - sumX * sumY / n;
        double result = sp / ssx;

        return result;
    }

    @Override
    public void clear() {
        // not implemented yet
    }
}
