//AlgoTrader: based on TimeBatchViewFactory
/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this /***********************************************************************************
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
package ch.algotrader.esper.ohlc;

import java.util.List;

import com.espertech.esper.client.EventType;
import com.espertech.esper.core.context.util.AgentInstanceViewFactoryChainContext;
import com.espertech.esper.core.service.StatementContext;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.view.DataWindowBatchingViewFactory;
import com.espertech.esper.view.DataWindowViewFactory;
import com.espertech.esper.view.DataWindowViewWithPrevious;
import com.espertech.esper.view.View;
import com.espertech.esper.view.ViewFactory;
import com.espertech.esper.view.ViewFactoryContext;
import com.espertech.esper.view.ViewFactorySupport;
import com.espertech.esper.view.ViewParameterException;
import com.espertech.esper.view.ViewServiceHelper;
import com.espertech.esper.view.window.IStreamRelativeAccess;
import com.espertech.esper.view.window.RelativeAccessByEventNIndexMap;
import com.espertech.esper.view.window.TimeBatchViewFactoryParams;

/**
 * Factory for {@link OHLCView}.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class OHLCViewFactory extends TimeBatchViewFactoryParams implements DataWindowViewFactory, DataWindowViewWithPrevious, DataWindowBatchingViewFactory
{
    /**
     * The reference point, or null if none supplied.
     */
    protected Long optionalReferencePoint;

    private ExprEvaluator valueExpressionEval;

    private List<ExprNode> exprNodes;

    @Override
    public void setViewParameters(ViewFactoryContext viewFactoryContext, List<ExprNode> expressionParameters) throws ViewParameterException
    {
        List<Object> viewParameters = ViewFactorySupport.validateAndEvaluate("OHLC view", viewFactoryContext.getStatementContext(), expressionParameters.subList(1, expressionParameters.size()));
        String errorMessage = "OHLC view requires one single numeric or time period parameter and a numeric expression supplying datapoints, and an optional long-typed reference point in msec, and an optional list of control keywords as a string parameter (please see the documentation)";
        if ((viewParameters.size() < 1) || (viewParameters.size() > 3))
        {
            throw new ViewParameterException(errorMessage);
        }

        processExpiry(viewParameters.get(0), errorMessage, "OHLC view requires a size of at least 1 msec");

        if ((viewParameters.size() == 2) && (viewParameters.get(1) instanceof String))
        {
            processKeywords(viewParameters.get(1), errorMessage);
        }
        else
        {
            if (viewParameters.size() >= 2)
            {
                Object paramRef = viewParameters.get(1);
                if ((!(paramRef instanceof Number)) || (JavaClassHelper.isFloatingPointNumber((Number)paramRef)))
                {
                    throw new ViewParameterException("OHLC view requires a Long-typed reference point in msec as a second parameter");
                }
                this.optionalReferencePoint = ((Number) paramRef).longValue();
            }

            if (viewParameters.size() == 3)
            {
                processKeywords(viewParameters.get(2), errorMessage);
            }
        }
        this.exprNodes = expressionParameters;
    }

    @Override
    public void attach(EventType parentEventType, StatementContext statementContext, ViewFactory optionalParentFactory, List<ViewFactory> parentViewFactories) throws ViewParameterException
    {
        ExprNode[] validated = ViewFactorySupport.validate("OHLC View", parentEventType, statementContext, this.exprNodes, true);
        String errorMessage = "OHLC view require an expression returning a numeric value as a parameter";
        if (!JavaClassHelper.isNumeric(validated[0].getExprEvaluator().getType()))
        {
            throw new ViewParameterException(errorMessage);
        }
        this.valueExpressionEval = validated[0].getExprEvaluator();
        this.eventType = statementContext.getEventAdapterService().addBeanType(OHLCBar.class.getName(), OHLCBar.class, false, false, false);
    }

    @Override
    public Object makePreviousGetter() {
        return new RelativeAccessByEventNIndexMap();
    }

    @Override
    public View makeView(AgentInstanceViewFactoryChainContext agentInstanceViewFactoryContext)
    {
        IStreamRelativeAccess relativeAccessByEvent = ViewServiceHelper.getOptPreviousExprRelativeAccess(agentInstanceViewFactoryContext);
        if (agentInstanceViewFactoryContext.isRemoveStream())
        {
            throw new UnsupportedOperationException("remove not supported");
        }
        else
        {
            return new OHLCView(this, agentInstanceViewFactoryContext, this.valueExpressionEval, this.millisecondsBeforeExpiry, this.optionalReferencePoint, this.isForceUpdate, this.isStartEager, relativeAccessByEvent);
        }
    }

    @Override
    public EventType getEventType()
    {
        return this.eventType;
    }

    @Override
    public boolean canReuse(View view)
    {
        if (!(view instanceof OHLCView))
        {
            return false;
        }

        OHLCView myView = (OHLCView) view;
        if (myView.getMsecIntervalSize() != this.millisecondsBeforeExpiry)
        {
            return false;
        }

        if ((myView.getInitialReferencePoint() != null) && (this.optionalReferencePoint != null))
        {
            if (!myView.getInitialReferencePoint().equals(this.optionalReferencePoint.longValue()))
            {
                return false;
            }
        }
        if ( ((myView.getInitialReferencePoint() == null) && (this.optionalReferencePoint != null)) ||
             ((myView.getInitialReferencePoint() != null) && (this.optionalReferencePoint == null)) )
        {
            return false;
        }

        if (myView.isForceOutput() != this.isForceUpdate)
        {
            return false;
        }

        if (myView.isStartEager())  // since it's already started
        {
            return false;
        }

        return myView.isEmpty();
    }

    @Override
    public String getViewName()
    {
        return "OHLC View";
    }
}
