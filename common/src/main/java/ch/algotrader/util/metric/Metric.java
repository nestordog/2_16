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
package ch.algotrader.util.metric;

import java.util.concurrent.atomic.AtomicLong;

/**
 * POJO used for metrics logging
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class Metric {

    private final String name;
    private final AtomicLong executions;
    private final AtomicLong time;

    public Metric(String name) {

        this.name = name;
        this.executions = new AtomicLong();
        this.time = new AtomicLong();
    }

    /**
     * adds the specified time to the this Metric and increases {@code executions} by 1.
     */
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
