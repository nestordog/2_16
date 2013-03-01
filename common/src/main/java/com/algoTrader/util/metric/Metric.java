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
package com.algoTrader.util.metric;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
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
