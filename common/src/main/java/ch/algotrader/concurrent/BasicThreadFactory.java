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
package ch.algotrader.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.Validate;

/**
 * Basic thread factory that can create threads with a specific group, name and
 * daemon attribute.
 */
public final class BasicThreadFactory implements ThreadFactory {

    private final String name;
    private final ThreadGroup threadGroup;
    private final Boolean daemon;
    private final AtomicLong count = new AtomicLong(0);

    public BasicThreadFactory(final String name, final ThreadGroup threadGroup, final Boolean daemon) {

        Validate.notEmpty(name, "Session name is null");
        this.name = name;
        this.threadGroup = threadGroup;
        this.daemon = daemon;
    }

    public BasicThreadFactory(final String name) {
        this(name, null, null);
    }

    public BasicThreadFactory(final String name, final boolean daemon) {
        this(name, null, daemon);
    }

    @Override
    public Thread newThread(final Runnable r) {
        Thread thread = new Thread(this.threadGroup, r, this.name + "-" + this.count.incrementAndGet());
        if (this.daemon != null) {
            thread.setDaemon(this.daemon);
        }
        return thread;
    }

}
