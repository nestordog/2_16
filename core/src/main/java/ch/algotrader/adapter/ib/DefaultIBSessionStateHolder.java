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

import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.enumeration.ConnectionState;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.service.MarketDataService;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class DefaultIBSessionStateHolder implements IBSessionStateHolder {

    private static final Logger logger = LogManager.getLogger(DefaultIBSessionStateHolder.class.getName());

    private final MarketDataService marketDataService;
    private final AtomicReference<ConnectionState> connState;

    public DefaultIBSessionStateHolder(final MarketDataService marketDataService) {

        this.marketDataService = marketDataService;
        this.connState = new AtomicReference<>(ConnectionState.DISCONNECTED);
    }

    @Override
    public void onConnect() {

        compareAndSet(ConnectionState.DISCONNECTED, ConnectionState.CONNECTED);
    }

    @Override
    public void onDisconnect() {

        this.connState.set(ConnectionState.DISCONNECTED);
        logger.debug("change state to " + ConnectionState.DISCONNECTED);
    }

    /**
     * logon with subscriptions either maintained or not-maintained
     * returns true if a re-subscription is necessary
     */
    @Override
    public void onLogon(boolean maintained) {

        if (onLogon0(maintained) && this.marketDataService.isSupportedFeed(FeedType.IB)) {
            this.marketDataService.initSubscriptions(FeedType.IB);
        }
    }

    private synchronized boolean onLogon0(boolean maintained) {

        if (maintained) {

            // if we have previously been IDLE no need to re-subscribe
            if (compareAndSet(ConnectionState.IDLE, ConnectionState.SUBSCRIBED)) {
                return false;
            }

            // if we have been CONNECTED we need to resubscribe
            return compareAndSet(ConnectionState.CONNECTED, ConnectionState.LOGGED_ON);

        } else {

            // re-subscribe if we have been previously CONNECTED or IDLE
            return (compareAndSet(ConnectionState.IDLE, ConnectionState.LOGGED_ON) || compareAndSet(ConnectionState.CONNECTED, ConnectionState.LOGGED_ON));
        }
    }

    @Override
    public synchronized void onLogoff() {

        compareAndSet(ConnectionState.LOGGED_ON, ConnectionState.CONNECTED);
        compareAndSet(ConnectionState.SUBSCRIBED, ConnectionState.IDLE);
    }

    /**
     * returns true if there is a need to subscribe
     */
    @Override
    public boolean subscribe() {

        return compareAndSet(ConnectionState.LOGGED_ON, ConnectionState.SUBSCRIBED);
    }

    @Override
    public ConnectionState getConnectionState() {

        return this.connState.get();
    }

    @Override
    public boolean isConnected() {

        return this.connState.get().getValue() >= ConnectionState.CONNECTED.getValue();
    }

    @Override
    public boolean isLoggedOn() {

        return this.connState.get().getValue() >= ConnectionState.LOGGED_ON.getValue();
    }

    @Override
    public boolean isSubscribed() {

        return this.connState.get().getValue() >= ConnectionState.SUBSCRIBED.getValue();
    }

    private boolean compareAndSet(ConnectionState before, ConnectionState after) {

        boolean success = this.connState.compareAndSet(before, after);

        if (success) {
            logger.debug("change state from " + before + " to " + after);
        }

        return success;
    }
}

