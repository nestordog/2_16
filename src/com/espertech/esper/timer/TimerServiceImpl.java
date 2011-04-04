/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved. * http://esper.codehaus.org * http://www.espertech.com *
 * ---------------------------------------------------------------------------------- * The software in this package is published under the terms of
 * the GPL license * a copy of which has been included with this distribution in the license.txt file. *
 **************************************************************************************/
package com.espertech.esper.timer;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of the internal clocking service interface.
 */
public final class TimerServiceImpl implements TimerService
{
    private final String engineURI;
    private final long msecTimerResolution;
    private TimerCallback timerCallback;
    private ScheduledThreadPoolExecutor timer;
    private EPLTimerTask timerTask;
    private static AtomicInteger NEXT_ID = new AtomicInteger(0);
    private final int id;

    /**
     * Constructor.
     * @param msecTimerResolution is the millisecond resolution or interval the internal timer thread
     * processes schedules
     * @param engineURI engine URI
     */
    public TimerServiceImpl(String engineURI, long msecTimerResolution)
    {
        this.engineURI = engineURI;
        this.msecTimerResolution = msecTimerResolution;
        this.id = NEXT_ID.getAndIncrement();
    }

    /**
     * Returns the timer resolution.
     * @return the millisecond resolution or interval the internal timer thread
     * processes schedules
     */
    public long getMsecTimerResolution()
    {
        return this.msecTimerResolution;
    }

    public void setCallback(TimerCallback timerCallback)
    {
        this.timerCallback = timerCallback;
    }

    public final void startInternalClock()
    {
        if (this.timer != null)
        {
            log.warn(".startInternalClock Internal clock is already started, stop first before starting, operation not completed");
            return;
        }

        if (log.isDebugEnabled())
        {
            log.debug(".startInternalClock Starting internal clock daemon thread, resolution=" + this.msecTimerResolution);
        }

        if (this.timerCallback == null)
        {
            throw new IllegalStateException("Timer callback not set");
        }

        getScheduledThreadPoolExecutorDaemonThread();
        this.timerTask = new EPLTimerTask(this.timerCallback);

        // With no delay start every internal
        ScheduledFuture<?> future = this.timer.scheduleAtFixedRate(this.timerTask, 0, this.msecTimerResolution, TimeUnit.MILLISECONDS);
        this.timerTask.setFuture(future);
    }

    public final void stopInternalClock(boolean warnIfNotStarted)
    {
        if (this.timer == null)
        {
            if (warnIfNotStarted)
            {
                log.warn(".stopInternalClock Internal clock is already stopped, start first before stopping, operation not completed");
            }
            return;
        }

        if (log.isDebugEnabled())
        {
            log.debug(".stopInternalClock Stopping internal clock daemon thread");
        }

        this.timer.shutdown();

        try
        {
            // Sleep for 100 ms to await the internal timer
            Thread.sleep(100);
        }
        catch (InterruptedException e)
        {
            log.info("Timer start wait interval interruped");
        }

        this.timer = null;
    }


    public void enableStats() {
        if (this.timerTask != null) {
            this.timerTask._enableStats = true;
        }
    }

    public void disableStats() {
        if (this.timerTask != null) {
            this.timerTask._enableStats = false;
            // now it is safe to reset stats without any synchronization
            this.timerTask.resetStats();
        }
    }

    public long getMaxDrift() {
        return this.timerTask._maxDrift;
    }

    public long getLastDrift() {
        return this.timerTask._lastDrift;
    }

    public long getTotalDrift() {
        return this.timerTask._totalDrift;
    }

    public long getInvocationCount() {
        return this.timerTask._invocationCount;
    }

       private void getScheduledThreadPoolExecutorDaemonThread() {
        this.timer = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
            // set new thread as daemon thread and name appropriately
            public Thread newThread(Runnable r) {
                String uri = TimerServiceImpl.this.engineURI;
                if (TimerServiceImpl.this.engineURI == null)
                {
                    uri = "default";
                }
                Thread t = new Thread(r, "com.espertech.esper.Timer-" + uri + "-" + TimerServiceImpl.this.id);
                //t.setDaemon(true);
                return t;
            }
        });
        this.timer.setMaximumPoolSize(this.timer.getCorePoolSize());
    }

    private static final Log log = LogFactory.getLog(TimerServiceImpl.class);
}
