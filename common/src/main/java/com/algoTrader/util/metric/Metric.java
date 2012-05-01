package com.algoTrader.util.metric;

import java.util.concurrent.atomic.AtomicLong;

public class Metric {

    private String name;
    private AtomicLong executions;
    private AtomicLong time;

    public Metric(String name) {

        this.name = name;
        this.executions = new AtomicLong();
        this.time = new AtomicLong();
    }

    public void addTime(long time) {
        this.time.addAndGet(time);
        this.executions.addAndGet(1);
    }

    public String getName() {
        return this.name;
    }

    public AtomicLong getExecutions() {
        return this.executions;
    }

    public AtomicLong getTime() {
        return this.time;
    }

}
