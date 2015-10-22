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
package ch.algotrader.esper.view.stat;

import java.util.List;

import com.espertech.esper.client.EventType;
import com.espertech.esper.core.context.util.AgentInstanceViewFactoryChainContext;
import com.espertech.esper.core.service.StatementContext;
import com.espertech.esper.epl.expression.core.ExprNode;
import com.espertech.esper.epl.expression.core.ExprNodeUtility;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.view.View;
import com.espertech.esper.view.ViewFactory;
import com.espertech.esper.view.ViewFactoryContext;
import com.espertech.esper.view.ViewFactorySupport;
import com.espertech.esper.view.ViewParameterException;
import com.espertech.esper.view.stat.StatViewAdditionalProps;

/**
 * Factory for {@link GeometricalAverageView} instances.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class GeometricalAverageViewFactory implements ViewFactory {

    protected final static String NAME = "Geometrical average";

    private List<ExprNode> viewParameters;
    private int streamNumber;

    /**
     * Property name of data field.
     */
    protected ExprNode fieldExpression;
    protected StatViewAdditionalProps additionalProps;

    protected EventType eventType;

    @Override
    public void setViewParameters(ViewFactoryContext viewFactoryContext, List<ExprNode> expressionParameters) throws ViewParameterException {
        this.viewParameters = expressionParameters;
        this.streamNumber = viewFactoryContext.getStreamNum();
    }

    @Override
    public void attach(EventType parentEventType, StatementContext statementContext, ViewFactory optionalParentFactory, List<ViewFactory> parentViewFactories) throws ViewParameterException {
        ExprNode[] validated = ViewFactorySupport.validate(getViewName(), parentEventType, statementContext, this.viewParameters, true);
        if (validated.length < 1) {
            throw new ViewParameterException(getViewParamMessage());
        }
        if (!JavaClassHelper.isNumeric(validated[0].getExprEvaluator().getType())) {
            throw new ViewParameterException(getViewParamMessage());
        }
        this.fieldExpression = validated[0];

        this.additionalProps = StatViewAdditionalProps.make(validated, 1, parentEventType);
        this.eventType = GeometricalAverageView.createEventType(statementContext, this.additionalProps, this.streamNumber);
    }

    @Override
    public View makeView(AgentInstanceViewFactoryChainContext agentInstanceViewFactoryContext) {
        return new GeometricalAverageView(this, agentInstanceViewFactoryContext);
    }

    @Override
    public EventType getEventType() {
        return this.eventType;
    }

    @Override
    public boolean canReuse(View view) {
        if (!(view instanceof GeometricalAverageView)) {
            return false;
        }
        if (this.additionalProps != null) {
            return false;
        }

        GeometricalAverageView other = (GeometricalAverageView) view;
        if (!ExprNodeUtility.deepEquals(other.getFieldExpression(), this.fieldExpression)) {
            return false;
        }

        return true;
    }

    @Override
    public String getViewName() {
        return NAME;
    }

    private String getViewParamMessage() {
        return getViewName() + " view require a single expression returning a numeric value as a parameter";
    }

    public void setFieldExpression(ExprNode fieldExpression) {
        this.fieldExpression = fieldExpression;
    }

    public void setAdditionalProps(StatViewAdditionalProps additionalProps) {
        this.additionalProps = additionalProps;
    }

    public StatViewAdditionalProps getAdditionalProps() {
        return this.additionalProps;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }
}
