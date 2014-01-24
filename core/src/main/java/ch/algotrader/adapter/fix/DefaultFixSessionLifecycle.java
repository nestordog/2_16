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
package ch.algotrader.adapter.fix;

import java.util.concurrent.atomic.AtomicReference;

import quickfix.SessionID;
import ch.algotrader.enumeration.ConnectionState;

/**
 * Default implementation of {@link FixSessionLifecycle} that keeps track of
 * FIX connection state state.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class DefaultFixSessionLifecycle implements FixSessionLifecycle {

    private final AtomicReference<ConnectionState> connState;

    public DefaultFixSessionLifecycle() {

        this.connState = new AtomicReference<ConnectionState>(ConnectionState.DISCONNECTED);
    }

    @Override
    public void created(SessionID sessionID) {

        this.connState.compareAndSet(ConnectionState.DISCONNECTED, ConnectionState.CONNECTED);
    }

    @Override
    public void loggedOn(SessionID sessionID) {

          this.connState.compareAndSet(ConnectionState.CONNECTED, ConnectionState.LOGGED_ON);
    }

    @Override
    public void loggedOff(SessionID sessionID) {

        this.connState.set(ConnectionState.CONNECTED);
    }

    @Override
    public ConnectionState getConnState() {

        return connState.get();
    }

    @Override
    public boolean triggerSubscribe() {

        return this.connState.compareAndSet(ConnectionState.LOGGED_ON, ConnectionState.SUBSCRIBED);
    }
}
