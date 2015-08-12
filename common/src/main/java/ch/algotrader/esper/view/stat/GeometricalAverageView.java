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
package ch.algotrader.esper.view.stat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.collection.SingleEventIterator;
import com.espertech.esper.core.context.util.AgentInstanceViewFactoryChainContext;
import com.espertech.esper.core.service.StatementContext;
import com.espertech.esper.epl.expression.core.ExprEvaluator;
import com.espertech.esper.epl.expression.core.ExprNode;
import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.metrics.instrumentation.InstrumentationHelper;
import com.espertech.esper.view.CloneableView;
import com.espertech.esper.view.DerivedValueView;
import com.espertech.esper.view.View;
import com.espertech.esper.view.ViewFactory;
import com.espertech.esper.view.ViewSupport;
import com.espertech.esper.view.stat.StatViewAdditionalProps;

/**
 * View for computing geometrical average.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class GeometricalAverageView extends ViewSupport implements CloneableView, DerivedValueView {

    private final GeometricalAverageViewFactory viewFactory;
    protected final AgentInstanceViewFactoryChainContext agentInstanceContext;
    private final ExprEvaluator fieldExpressionEvaluator;
    protected final GeometricalAverageBean baseStatisticsBean = new GeometricalAverageBean();

    private EventBean lastNewEvent;
    private EventBean[] eventsPerStream = new EventBean[1];
    protected Object[] lastValuesEventNew;

    /**
     * Constructor requires the name of the field to use in the parent view to compute the statistics.
     * compute the statistics on.
     */
    public GeometricalAverageView(GeometricalAverageViewFactory viewFactory, AgentInstanceViewFactoryChainContext agentInstanceContext) {
        this.viewFactory = viewFactory;
        this.agentInstanceContext = agentInstanceContext;
        this.fieldExpressionEvaluator = viewFactory.fieldExpression.getExprEvaluator();
    }

    @Override
    public View cloneView() {
        return this.viewFactory.makeView(this.agentInstanceContext);
    }

    /**
     * Returns field name of the field to report statistics on.
     * @return field name
     */
    public final ExprNode getFieldExpression() {
        return this.viewFactory.fieldExpression;
    }

    @Override
    public void update(EventBean[] newData, EventBean[] oldData) {
        if (InstrumentationHelper.ENABLED) {
            InstrumentationHelper.get().qViewProcessIRStream(this, GeometricalAverageViewFactory.NAME, newData, oldData);
        }

        // If we have child views, keep a reference to the old values, so we can update them as old data event.
        EventBean oldDataMap = null;
        if (this.lastNewEvent == null) {
            if (this.hasViews()) {
                oldDataMap = populateMap(this.baseStatisticsBean, this.agentInstanceContext.getStatementContext().getEventAdapterService(), this.viewFactory.eventType,
                        this.viewFactory.additionalProps, this.lastValuesEventNew);
            }
        }

        // add data points to the bean
        if (newData != null) {
            for (EventBean element : newData) {
                this.eventsPerStream[0] = element;
                Number pointnum = (Number) this.fieldExpressionEvaluator.evaluate(this.eventsPerStream, true, this.agentInstanceContext);
                if (pointnum != null) {
                    double point = pointnum.doubleValue();
                    this.baseStatisticsBean.addPoint(point, 0);
                }
            }

            if ((this.viewFactory.additionalProps != null) && (newData.length != 0)) {
                if (this.lastValuesEventNew == null) {
                    this.lastValuesEventNew = new Object[this.viewFactory.additionalProps.getAdditionalExpr().length];
                }
                for (int val = 0; val < this.viewFactory.additionalProps.getAdditionalExpr().length; val++) {
                    this.lastValuesEventNew[val] = this.viewFactory.additionalProps.getAdditionalExpr()[val].evaluate(this.eventsPerStream, true, this.agentInstanceContext);
                }
            }
        }

        // remove data points from the bean
        if (oldData != null) {
            for (EventBean element : oldData) {
                this.eventsPerStream[0] = element;
                Number pointnum = (Number) this.fieldExpressionEvaluator.evaluate(this.eventsPerStream, true, this.agentInstanceContext);
                if (pointnum != null) {
                    double point = pointnum.doubleValue();
                    this.baseStatisticsBean.removePoint(point, 0);
                }
            }
        }

        // If there are child view, call update method
        if (this.hasViews()) {
            EventBean newDataMap = populateMap(this.baseStatisticsBean, this.agentInstanceContext.getStatementContext().getEventAdapterService(), this.viewFactory.eventType,
                    this.viewFactory.additionalProps, this.lastValuesEventNew);

            EventBean[] oldEvents;
            EventBean[] newEvents = new EventBean[] { newDataMap };
            if (this.lastNewEvent == null) {
                oldEvents = new EventBean[] { oldDataMap };
            } else {
                oldEvents = new EventBean[] { this.lastNewEvent };
            }

            if (InstrumentationHelper.ENABLED) {
                InstrumentationHelper.get().qViewIndicate(this, GeometricalAverageViewFactory.NAME, newEvents, oldEvents);
            }
            updateChildren(newEvents, oldEvents);
            if (InstrumentationHelper.ENABLED) {
                InstrumentationHelper.get().aViewIndicate();
            }

            this.lastNewEvent = newDataMap;
        }

        if (InstrumentationHelper.ENABLED) {
            InstrumentationHelper.get().aViewProcessIRStream();
        }
    }

    @Override
    public final EventType getEventType() {
        return this.viewFactory.eventType;
    }

    @Override
    public final Iterator<EventBean> iterator() {
        return new SingleEventIterator(populateMap(this.baseStatisticsBean, this.agentInstanceContext.getStatementContext().getEventAdapterService(), this.viewFactory.eventType,
                this.viewFactory.additionalProps, this.lastValuesEventNew));
    }

    @Override
    public final String toString() {
        return this.getClass().getName() + " fieldExpression=" + this.viewFactory.fieldExpression;
    }

    public static EventBean populateMap(GeometricalAverageBean baseStatisticsBean, EventAdapterService eventAdapterService, EventType eventType, StatViewAdditionalProps additionalProps,
            Object[] lastNewValues) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("geomaverage", baseStatisticsBean.getGeomAvgX());
        if (additionalProps != null) {
            additionalProps.addProperties(result, lastNewValues);
        }
        return eventAdapterService.adapterForTypedMap(result, eventType);
    }

    /**
     * Creates the event type for this view.
     * @param statementContext is the event adapter service
     * @return event type of view
     */
    public static EventType createEventType(StatementContext statementContext, StatViewAdditionalProps additionalProps, int streamNum) {
        Map<String, Object> eventTypeMap = new HashMap<String, Object>();
        eventTypeMap.put("geomaverage", double.class);
        String outputEventTypeName = statementContext.getStatementId() + "_statview_" + streamNum;
        return statementContext.getEventAdapterService().createAnonymousMapType(outputEventTypeName, eventTypeMap);
    }

    public GeometricalAverageBean getBaseStatisticsBean() {
        return this.baseStatisticsBean;
    }

    public Object[] getLastValuesEventNew() {
        return this.lastValuesEventNew;
    }

    public void setLastValuesEventNew(Object[] lastValuesEventNew) {
        this.lastValuesEventNew = lastValuesEventNew;
    }

    @Override
    public ViewFactory getViewFactory() {
        return this.viewFactory;
    }
}
