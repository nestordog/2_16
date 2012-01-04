// Esper 4.4.0 QuickFix (to be removed in 4.5.0): replace line 52 with 53
/*
 * *************************************************************************************
 *  Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 *  http://esper.codehaus.org                                                          *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 * *************************************************************************************
 */

package com.espertech.esper.epl.expression;

import com.espertech.esper.epl.agg.AggregationAccessor;
import com.espertech.esper.epl.agg.AggregationMethod;
import com.espertech.esper.epl.agg.AggregationMethodFactory;
import com.espertech.esper.epl.agg.AggregationSpec;
import com.espertech.esper.epl.agg.AggregationSupport;
import com.espertech.esper.epl.core.MethodResolutionService;

public class ExprPlugInAggFunctionNodeFactory implements AggregationMethodFactory
{
    private final AggregationSupport aggregationSupport;
    private final boolean distinct;
    private final Class aggregatedValueType;

    public ExprPlugInAggFunctionNodeFactory(AggregationSupport aggregationSupport, boolean distinct, Class aggregatedValueType)
    {
        this.aggregationSupport = aggregationSupport;
        this.distinct = distinct;
        this.aggregatedValueType = aggregatedValueType;
    }

    public Class getResultType()
    {
        return this.aggregationSupport.getValueType();
    }

    public AggregationSpec getSpec(boolean isMatchRecognize)
    {
        return null;  // defaults apply
    }

    public AggregationAccessor getAccessor()
    {
        return null;  // no accessor
    }

    public AggregationMethod make(MethodResolutionService methodResolutionService, int[] agentInstanceIds, int groupId, int aggregationId) {

        //AggregationMethod method = methodResolutionService.makePlugInAggregator(aggregationSupport.getFunctionName());
        AggregationMethod method = this.aggregationSupport.newAggregator(methodResolutionService);
        if (!this.distinct) {
            return method;
        }
        return methodResolutionService.makeDistinctAggregator(agentInstanceIds, groupId, aggregationId, method, this.aggregatedValueType,false);
    }

    public AggregationMethodFactory getPrototypeAggregator() {
        return this;
    }
}
