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
package ch.algotrader.adapter.ib;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.DisposableBean;

import ch.algotrader.enumeration.ConnectionState;
import ch.algotrader.event.listener.SessionEventListener;
import ch.algotrader.vo.SessionEventVO;

public final class IBSessionEventListener implements SessionEventListener, DisposableBean {

    private final String name;
    private final IBSession session;
    private final ExecutorService executorService;
    private final AtomicLong count = new AtomicLong(0);

    public IBSessionEventListener(final String name, final IBSession session) {

        Validate.notEmpty(name, "Session name is null");
        Validate.notNull(session, "IBSession is null");
        this.name = name;
        this.session = session;
        this.executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable r) {
                return new Thread(null, r, "IB-reconnect-thread-" + count.incrementAndGet());
            }
        });
    }

    @Override
    public void onChange(final SessionEventVO event) {

        if (this.name.equals(event.getQualifier()) && event.getState() == ConnectionState.DISCONNECTED) {

            this.executorService.execute(session::connect);
        }
    }

    @Override
    public void destroy() throws Exception {

        this.session.destroy();
        this.executorService.shutdownNow();
    }
}
