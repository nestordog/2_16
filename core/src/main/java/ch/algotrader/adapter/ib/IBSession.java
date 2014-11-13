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

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;

import com.ib.client.EClientSocket;

import ch.algotrader.enumeration.FeedType;
import ch.algotrader.enumeration.InitializingServiceType;
import ch.algotrader.service.InitializationPriority;
import ch.algotrader.service.InitializingServiceI;
import ch.algotrader.service.MarketDataService;
import ch.algotrader.util.MyLogger;

/**
 * Represents on IB (socket) connection.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@InitializationPriority(value = InitializingServiceType.BROKER_INTERFACE)
public final class IBSession extends EClientSocket implements InitializingServiceI, DisposableBean {

    private static final long serialVersionUID = 6821739991866153788L;

    private static final Logger logger = MyLogger.getLogger(IBSession.class.getName());

    private final int clientId;
    private final String host;
    private final int port;
    private final IBSessionLifecycle sessionLifecycle;
    private MarketDataService marketDataService;

    public IBSession(int clientId, String host, int port, IBSessionLifecycle sessionLifecycle, AbstractIBMessageHandler messageHandler, MarketDataService marketDataService) {

        super(messageHandler);

        Validate.notNull(clientId, "host may not be 0");
        Validate.notNull(host, "host may not be null");
        Validate.notNull(port, "host may not be 0");
        Validate.notNull(sessionLifecycle, "IBSessionLifecycle may not be null");

        this.clientId = clientId;
        this.host = host;
        this.port = port;
        this.sessionLifecycle = sessionLifecycle;

        this.marketDataService = marketDataService;
    }

    @Override
    public void init() {
        connect();
    }

    @Override
    public void destroy() {

        if (isConnected()) {
            eDisconnect();
        }
    }

    public int getClientId() {
        return this.clientId;
    }

    public IBSessionLifecycle getLifecycle() {
        return this.sessionLifecycle;
    }

    /**
     * (re)connects to TWS / IB Gateway
     */
    public void connect() {

        if (isConnected()) {
            eDisconnect();

            sleep();
        }

        while (!connectionAvailable()) {
            sleep();
        }

        eConnect(this.host, this.port, this.clientId);

        if (isConnected()) {
            this.sessionLifecycle.connect();

            // in case there is no 2104 message from the IB Gateway (Market data farm connection is OK)
            // manually invoke initSubscriptions after some time if there is marketDataService
            sleep();
            if (this.sessionLifecycle.logon(true)) {
                if (this.marketDataService != null) {
                    this.marketDataService.initSubscriptions(FeedType.IB);
                }
            }
        }
    }

    /**
     * disconnects from TWS / IB Gateway
     */
    public void disconnect() {

        if (isConnected()) {
            eDisconnect();
            this.sessionLifecycle.disconnect();
        }
    }

    private void sleep() {

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e1) {
            try {
                // during eDisconnect this thread get's interrupted so sleep again
                Thread.sleep(10000);
            } catch (InterruptedException e2) {
                logger.error("problem sleeping", e2);
            }
        }
    }

    private boolean connectionAvailable() {

        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(this.host, this.port), 5000);
            return true;
        } catch (ConnectException e) {
            // do nothing, gateway is down
            logger.info("please start IB Gateway / TWS on port: " + this.port);
            return false;
        } catch (IOException e) {
            logger.error("connection error", e);
            return false;
        } finally {
            try {
                socket.close();
            } catch (IOException ignore) {
            }
        }
    }

}
