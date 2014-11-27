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
package ch.algotrader.adapter.fix;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.Validate;

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

    private final String name;
    private final AtomicReference<ConnectionState> connState;

    public DefaultFixSessionLifecycle(final String name) {

        Validate.notNull(name, "Name is null");
        this.name = name;
        this.connState = new AtomicReference<>(ConnectionState.DISCONNECTED);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void create() {

        this.connState.compareAndSet(ConnectionState.DISCONNECTED, ConnectionState.CONNECTED);
    }

    @Override
    public void logon() {

          this.connState.compareAndSet(ConnectionState.CONNECTED, ConnectionState.LOGGED_ON);
    }

    @Override
    public void logoff() {

        this.connState.set(ConnectionState.CONNECTED);
    }

    @Override
    public boolean subscribe() {

        return this.connState.compareAndSet(ConnectionState.LOGGED_ON, ConnectionState.SUBSCRIBED);
    }

    @Override
    public boolean isLoggedOn() {

        return this.connState.get().getValue() >= ConnectionState.LOGGED_ON.getValue();
    }

    @Override
    public boolean isSubscribed() {

        return this.connState.get().getValue() >= ConnectionState.SUBSCRIBED.getValue();
    }

    @Override
    public ConnectionState getConnectionState() {

        return this.connState.get();
    }
}
