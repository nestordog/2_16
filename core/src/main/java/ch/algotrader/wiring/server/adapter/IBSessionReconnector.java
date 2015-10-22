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
package ch.algotrader.wiring.server.adapter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.Validate;

import ch.algotrader.adapter.ib.IBSession;
import ch.algotrader.concurrent.BasicThreadFactory;
import ch.algotrader.enumeration.ConnectionState;
import ch.algotrader.event.listener.SessionEventListener;
import ch.algotrader.vo.SessionEventVO;

class IBSessionReconnector implements SessionEventListener {

    private final String name;
    private final IBSession session;
    private final ExecutorService executorService;

    public IBSessionReconnector(final String name, final IBSession session) {

        Validate.notEmpty(name, "Session name is null");
        Validate.notNull(session, "IBSession is null");
        this.name = name;
        this.session = session;
        this.executorService = Executors.newSingleThreadExecutor(new BasicThreadFactory("IB-reconnect-thread", true));
    }

    @Override
    public void onChange(final SessionEventVO event) {

        if (this.name.equals(event.getQualifier()) && event.getState() == ConnectionState.DISCONNECTED && !this.session.isTerminated()) {

            this.executorService.execute(() -> {
                try {
                    // Sleep a little in case the session got disconnected due to JVM shutdown
                    Thread.sleep(1000);
                    if (!session.isTerminated()) {
                        session.connect();
                    }
                } catch (InterruptedException ignore) {
                }
            });
        }
    }

    public void destroy() throws Exception {

        this.session.destroy();
        this.executorService.shutdownNow();
    }
}
