package com.algoTrader.client.chart;

import java.io.Serializable;
import java.util.Date;

import org.jfree.chart.axis.Timeline;

public class DefaultTimeline implements Timeline, Serializable {

    private static final long serialVersionUID = -9169910551943900344L;

    @Override
    public long toTimelineValue(long millisecond) {
        return millisecond;
    }

    @Override
    public long toTimelineValue(Date date) {
        return date.getTime();
    }

    @Override
    public long toMillisecond(long value) {
        return value;
    }

    @Override
    public boolean containsDomainValue(long millisecond) {
        return true;
    }

    @Override
    public boolean containsDomainValue(Date date) {
        return true;
    }

    @Override
    public boolean containsDomainRange(long from, long to) {
        return true;
    }

    @Override
    public boolean containsDomainRange(Date from, Date to) {
        return true;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (object == this) {
            return true;
        }
        if (object instanceof DefaultTimeline) {
            return true;
        }
        return false;
    }
}
