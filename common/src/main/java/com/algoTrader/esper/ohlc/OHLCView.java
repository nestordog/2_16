//AlgoTrader: based on TimeBatchView
/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.algoTrader.esper.ohlc;

import java.util.Collections;
import java.util.Iterator;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.collection.ViewUpdatedCollection;
import com.espertech.esper.core.context.util.AgentInstanceViewFactoryChainContext;
import com.espertech.esper.core.service.EPStatementHandleCallback;
import com.espertech.esper.core.service.ExtensionServicesContext;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.schedule.ScheduleHandleCallback;
import com.espertech.esper.schedule.ScheduleSlot;
import com.espertech.esper.util.StopCallback;
import com.espertech.esper.view.CloneableView;
import com.espertech.esper.view.DataWindowView;
import com.espertech.esper.view.StoppableView;
import com.espertech.esper.view.View;
import com.espertech.esper.view.ViewSupport;

/**
 * A data view that aggregates events in a stream to OHLC Bars and releases them at the end of every specified time interval.
 * The view works similar to a time_batch_window
 * The view releases the OHLC Bar after the interval as new data to child views. The prior OHLC Bar if
 * not empty is released as old data to child view. The view doesn't release intervals with no old or new data.
 * It also does not collect old data published by a parent view.
 *
 * For example, we want to calculate the average of IBM stock every hour, for the last hour.
 * The view accepts 2 parameter combinations.
 * (1) A time interval is supplied with a reference point - based on this point the intervals are set.
 * (1) A time interval is supplied but no reference point - the reference point is set when the first event arrives.
 *
 * If there are no events in the current and prior batch, the view will not invoke the update method of child views.
 * In that case also, no next callback is scheduled with the scheduling service until the next event arrives.
 */
public final class OHLCView extends ViewSupport implements CloneableView, StoppableView, StopCallback, DataWindowView {
    // View parameters
    private final OHLCViewFactory ohlcViewFactory;
    private final AgentInstanceViewFactoryChainContext agentInstanceContext;
    private final ExprEvaluator valueExpressionEval;
    private final long msecIntervalSize;
    private final Long initialReferencePoint;
    private final boolean isForceOutput;
    private final boolean isStartEager;
    private final ViewUpdatedCollection viewUpdatedCollection;
    private final ScheduleSlot scheduleSlot;
    private EPStatementHandleCallback handle;

    // Current running parameters
    private Long currentReferencePoint;
    private long currentStartTime;
    private OHLCBar lastBar = null;
    private OHLCBar currentBar = new OHLCBar();
    private boolean isCallbackScheduled;

    /**
     * Constructor.
     * @param ohlcViewFactory for copying this view in a group-by
     * @param agentInstanceContext
     * @param valueExpressionEval
     * @param msecIntervalSize is the number of milliseconds to batch events for
     * @param referencePoint is the reference point onto which to base intervals, or null if there is no such reference point supplied
     * @param forceOutput is true if the batch should produce empty output if there is no value to output following time intervals
     * @param isStartEager is true for start-eager
     * @param viewUpdatedCollection is a collection that the view must update when receiving events
     */
    public OHLCView(OHLCViewFactory ohlcViewFactory,
                         AgentInstanceViewFactoryChainContext agentInstanceContext,
                         ExprEvaluator valueExpressionEval,
                         long msecIntervalSize,
                         Long referencePoint,
                         boolean forceOutput,
                         boolean isStartEager,
                         ViewUpdatedCollection viewUpdatedCollection)
    {
        this.agentInstanceContext = agentInstanceContext;
        this.ohlcViewFactory = ohlcViewFactory;
        this.valueExpressionEval = valueExpressionEval;
        this.msecIntervalSize = msecIntervalSize;
        this.initialReferencePoint = referencePoint;
        this.isStartEager = isStartEager;
        this.viewUpdatedCollection = viewUpdatedCollection;
        this.isForceOutput = forceOutput;

        this.scheduleSlot = agentInstanceContext.getStatementContext().getScheduleBucket().allocateSlot();

        // schedule the first callback
        if (isStartEager)
        {
            if (currentReferencePoint == null)
            {
                currentReferencePoint = agentInstanceContext.getStatementContext().getSchedulingService().getTime();
            }
            scheduleCallback();
            isCallbackScheduled = true;
        }

        agentInstanceContext.getTerminationCallbacks().add(this);
    }

    public View cloneView()
    {
        return ohlcViewFactory.makeView(agentInstanceContext);
    }

    /**
     * Returns the interval size in milliseconds.
     * @return batch size
     */
    public final long getMsecIntervalSize()
    {
        return msecIntervalSize;
    }

    /**
     * Gets the reference point to use to anchor interval start and end dates to.
     * @return is the millisecond reference point.
     */
    public final Long getInitialReferencePoint()
    {
        return initialReferencePoint;
    }

    /**
     * True for force-output.
     * @return indicates force-output
     */
    public boolean isForceOutput()
    {
        return isForceOutput;
    }

    /**
     * True for start-eager.
     * @return indicates start-eager
     */
    public boolean isStartEager()
    {
        return isStartEager;
    }

    public final EventType getEventType()
    {
        return parent.getEventType();
    }

    public final void update(EventBean[] newData, EventBean[] oldData)
    {
        // we don't care about removed data from a prior view
        if ((newData == null) || (newData.length == 0))
        {
            return;
        }

        // If we have an empty window about to be filled for the first time, schedule a callback
        if (currentBar.getOpen() == null)
        {
            if (currentReferencePoint == null)
            {
                currentReferencePoint = initialReferencePoint;
                if (currentReferencePoint == null)
                {
                    currentReferencePoint = agentInstanceContext.getStatementContext().getSchedulingService().getTime();
                }
            }

            // Schedule the next callback if there is none currently scheduled
            if (!isCallbackScheduled)
            {
                scheduleCallback();
                isCallbackScheduled = true;
            }

            this.currentStartTime = this.agentInstanceContext.getStatementContext().getSchedulingService().getTime();
        }

        // update the ohlc with new events
        for (EventBean bean : newData) {
            updateOHLCBar(currentBar, bean);
        }

        // We do not update child views, since we batch the events.
    }

    /**
     * This method updates child views and clears the batch of events.
     * We schedule a new callback at this time if there were events in the batch.
     */
    protected final void sendBatch()
    {
        isCallbackScheduled = false;

        // If there are child views and the batch was filled, fireStatementStopped update method
        if (this.hasViews())
        {
            // Convert to object arrays
            OHLCBar newData = null;
            OHLCBar oldData = null;
            if (currentBar.getOpen() != null)
            {
                newData = currentBar;
            }
            if ((lastBar != null) && (lastBar.getOpen() != null))
            {
                oldData = lastBar;
            }

            // Post new data (current batch) and old data (prior batch)
            if (viewUpdatedCollection != null)
            {
                throw new UnsupportedOperationException("viewUpdatedCollection.update not supported");
            }
            if ((newData != null) || (oldData != null) || (isForceOutput))
            {
                updateChildren(getEventBeans(newData), getEventBeans(oldData));
            }
        }

        // Only if forceOutput is enabled or
        // there have been any events in this or the last interval do we schedule a callback,
        // such as to not waste resources when no events arrive.
        if ((currentBar.getOpen() != null) || ((lastBar != null) && (lastBar.getOpen() != null)) || (isForceOutput))
        {
            scheduleCallback();
            isCallbackScheduled = true;
        }

        lastBar = currentBar;
        currentBar = new OHLCBar();
    }

    /**
     * Returns true if the window is empty, or false if not empty.
     * @return true if empty
     */
    public boolean isEmpty()
    {
        if (lastBar != null)
        {
            if (lastBar.getOpen() == null)
            {
                return true;
            }
        }
        return currentBar.getOpen() == null;
    }

    public final Iterator<EventBean> iterator()
    {
        EventBean bean = this.agentInstanceContext.getStatementContext().getEventAdapterService().adapterForBean(currentBar);
        return Collections.singleton(bean).iterator();
    }

    public final String toString()
    {
        return this.getClass().getName() +
                " msecIntervalSize=" + msecIntervalSize +
                " initialReferencePoint=" + initialReferencePoint;
    }

    private void scheduleCallback()
    {
        long current = agentInstanceContext.getStatementContext().getSchedulingService().getTime();
        long afterMSec = computeWaitMSec(current, this.currentReferencePoint, this.msecIntervalSize);

        ScheduleHandleCallback callback = new ScheduleHandleCallback() {
            public void scheduledTrigger(ExtensionServicesContext extensionServicesContext)
            {
                OHLCView.this.sendBatch();
            }
        };
        handle = new EPStatementHandleCallback(agentInstanceContext.getEpStatementAgentInstanceHandle(), callback);
        agentInstanceContext.getStatementContext().getSchedulingService().add(afterMSec, handle, scheduleSlot);
    }

    /**
     * Given a current time and a reference time and an interval size, compute the amount of
     * milliseconds till the next interval.
     * @param current is the current time
     * @param reference is the reference point
     * @param interval is the interval size
     * @return milliseconds after current time that marks the end of the current interval
     */
    protected static long computeWaitMSec(long current, long reference, long interval)
    {
        // Example:  current c=2300, reference r=1000, interval i=500, solution s=200
        //
        // int n = ((2300 - 1000) / 500) = 2
        // r + (n + 1) * i - c = 200
        //
        // Negative example:  current c=2300, reference r=4200, interval i=500, solution s=400
        // int n = ((2300 - 4200) / 500) = -3
        // r + (n + 1) * i - c = 4200 - 3*500 - 2300 = 400
        //
        long n = (current - reference) / interval;
        if (reference > current)        // References in the future need to deduct one window
        {
            n--;
        }
        long solution = reference + (n + 1) * interval - current;

        if (solution == 0)
        {
            return interval;
        }
        return solution;
    }

    public void stopView() {
        stopSchedule();
        agentInstanceContext.getTerminationCallbacks().remove(this);
    }

    public void stop() {
        stopSchedule();
    }

    public void stopSchedule() {
        if (handle != null) {
            agentInstanceContext.getStatementContext().getSchedulingService().remove(handle, scheduleSlot);
        }
    }

    private void updateOHLCBar(OHLCBar bar, EventBean bean) {

        if (bean == null) {
            return;
        }

        // get the value using the ExprEvaluator
        Double value = (Double) this.valueExpressionEval.evaluate(new EventBean[] { bean }, true, this.agentInstanceContext);

        // open
        if (bar.getOpen() == null) {
            bar.setOpen(value);
        }

        // high
        if (bar.getHigh() == null) {
            bar.setHigh(value);
        } else if (bar.getHigh().compareTo(value) < 0) {
            bar.setHigh(value);
        }

        // low
        if (bar.getLow() == null) {
            bar.setLow(value);
        } else if (bar.getLow().compareTo(value) > 0) {
            bar.setLow(value);
        }

        // close
        bar.setClose(value);
    }

    private EventBean[] getEventBeans(OHLCBar bar) {

        if (bar == null) {
            return null;
        }

        // the the currentReferenceTimeStamp
        long currentReference = computeCurrentReference(this.currentStartTime, this.currentReferencePoint, this.msecIntervalSize);

        // set the time
        bar.setTime(currentReference);

        // wrap it in a EventBean
        EventBean bean = this.agentInstanceContext.getStatementContext().getEventAdapterService().adapterForBean(bar);

        return new EventBean[] { bean };
    }

    private static long computeCurrentReference(long current, long reference, long interval) {

        long n = (current - reference) / interval;
        if (reference > current) // References in the future need to deduct one window
        {
            n--;
        }
        long solution = reference + n * interval;

        if (solution == 0) {
            return interval;
        }
        return solution;
    }

}
