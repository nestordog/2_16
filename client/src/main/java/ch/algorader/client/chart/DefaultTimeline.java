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
package ch.algorader.client.chart;

import java.io.Serializable;
import java.util.Date;

import org.jfree.chart.axis.Timeline;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
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
