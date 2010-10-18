/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view.stat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.collection.SingleEventIterator;
import com.espertech.esper.core.StatementContext;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.view.CloneableView;
import com.espertech.esper.view.View;
import com.espertech.esper.view.ViewFieldEnum;
import com.espertech.esper.view.ViewSupport;

/**
 * View for computing statistics, which the view exposes via fields representing
 * the sum, count, standard deviation for sample and for population and
 * variance.
 */
public final class UnivariateStatisticsView extends ViewSupport implements CloneableView {
    private final StatementContext statementContext;
    private final EventType eventType;
    private final ExprNode fieldExpression;
    private final BaseStatisticsBean baseStatisticsBean = new BaseStatisticsBean();
    private EventBean lastNewEvent;
    private EventBean[] eventsPerStream = new EventBean[1];

    /**
     * Constructor requires the name of the field to use in the parent view to
     * compute the statistics.
     *
     * @param fieldExpression
     *            is the expression to use to get numeric data points for this
     *            view to compute the statistics on.
     * @param statementContext
     *            contains required view services
     */
    public UnivariateStatisticsView(StatementContext statementContext, ExprNode fieldExpression) {
        this.statementContext = statementContext;
        this.fieldExpression = fieldExpression;
        this.eventType = createEventType(statementContext);
    }

    public View cloneView(StatementContext statementContext) {
        return new UnivariateStatisticsView(statementContext, this.fieldExpression);
    }

    /**
     * Returns field name of the field to report statistics on.
     *
     * @return field name
     */
    public final ExprNode getFieldExpression() {
        return this.fieldExpression;
    }

    public final void update(EventBean[] newData, EventBean[] oldData) {
        // If we have child views, keep a reference to the old values, so we can
        // update them as old data event.
        EventBean oldDataMap = null;
        if (this.lastNewEvent == null) {
            if (this.hasViews()) {
                oldDataMap = populateMap(this.baseStatisticsBean, this.statementContext.getEventAdapterService(), this.eventType);
            }
        }

        // add data points to the bean
        if (newData != null) {
            for (EventBean element : newData) {
                this.eventsPerStream[0] = element;
                double point = ((Number) this.fieldExpression.evaluate(this.eventsPerStream, true, this.statementContext)).doubleValue();
                this.baseStatisticsBean.addPoint(point, 0);
            }
        }

        // remove data points from the bean
        if (oldData != null) {
            for (EventBean element : oldData) {
                this.eventsPerStream[0] = element;
                double point = ((Number) this.fieldExpression.evaluate(this.eventsPerStream, true, this.statementContext)).doubleValue();
                this.baseStatisticsBean.removePoint(point, 0);
            }
        }

        // If there are child view, call update method
        if (this.hasViews()) {
            EventBean newDataMap = populateMap(this.baseStatisticsBean, this.statementContext.getEventAdapterService(), this.eventType);

            if (this.lastNewEvent == null) {
                updateChildren(new EventBean[] { newDataMap }, new EventBean[] { oldDataMap });
            } else {
                updateChildren(new EventBean[] { newDataMap }, new EventBean[] { this.lastNewEvent });
            }

            this.lastNewEvent = newDataMap;
        }
    }

    public final EventType getEventType() {
        return this.eventType;
    }

    public final Iterator<EventBean> iterator() {
        return new SingleEventIterator(populateMap(this.baseStatisticsBean, this.statementContext.getEventAdapterService(), this.eventType));
    }

    public final String toString() {
        return this.getClass().getName() + " fieldExpression=" + this.fieldExpression;
    }

    private static EventBean populateMap(BaseStatisticsBean baseStatisticsBean, EventAdapterService eventAdapterService, EventType eventType) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put(ViewFieldEnum.UNIVARIATE_STATISTICS__DATAPOINTS.getName(), baseStatisticsBean.getN());
        result.put(ViewFieldEnum.UNIVARIATE_STATISTICS__TOTAL.getName(), baseStatisticsBean.getXSum());
        result.put(ViewFieldEnum.UNIVARIATE_STATISTICS__STDDEV.getName(), baseStatisticsBean.getXStandardDeviationSample());
        result.put(ViewFieldEnum.UNIVARIATE_STATISTICS__STDDEVPA.getName(), baseStatisticsBean.getXStandardDeviationPop());
        result.put(ViewFieldEnum.UNIVARIATE_STATISTICS__VARIANCE.getName(), baseStatisticsBean.getXVariance());
        result.put(ViewFieldEnum.UNIVARIATE_STATISTICS__AVERAGE.getName(), baseStatisticsBean.getXAverage());
        result.put(ViewFieldEnum.UNIVARIATE_STATISTICS__GEOMAVERAGE.getName(), baseStatisticsBean.getProdX());
        return eventAdapterService.adaptorForTypedMap(result, eventType);
    }

    /**
     * Creates the event type for this view.
     *
     * @param statementContext
     *            is the event adapter service
     * @return event type of view
     */
    protected static EventType createEventType(StatementContext statementContext) {
        Map<String, Object> eventTypeMap = new HashMap<String, Object>();
        eventTypeMap.put(ViewFieldEnum.UNIVARIATE_STATISTICS__DATAPOINTS.getName(), long.class);
        eventTypeMap.put(ViewFieldEnum.UNIVARIATE_STATISTICS__TOTAL.getName(), double.class);
        eventTypeMap.put(ViewFieldEnum.UNIVARIATE_STATISTICS__STDDEV.getName(), double.class);
        eventTypeMap.put(ViewFieldEnum.UNIVARIATE_STATISTICS__STDDEVPA.getName(), double.class);
        eventTypeMap.put(ViewFieldEnum.UNIVARIATE_STATISTICS__VARIANCE.getName(), double.class);
        eventTypeMap.put(ViewFieldEnum.UNIVARIATE_STATISTICS__AVERAGE.getName(), double.class);
        eventTypeMap.put(ViewFieldEnum.UNIVARIATE_STATISTICS__GEOMAVERAGE.getName(), double.class);
        return statementContext.getEventAdapterService().createAnonymousMapType(eventTypeMap);
    }
}
