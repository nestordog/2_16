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
package ch.algotrader.adapter.ib;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;

import ch.algotrader.enumeration.FeedType;
import ch.algotrader.service.InitializingServiceI;
import ch.algotrader.service.MarketDataService;
import ch.algotrader.util.MyLogger;
import ch.algotrader.util.spring.Configuration;

import com.ib.client.EClientSocket;

/**
 * Represents on IB (socket) connection.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public final class IBSession extends EClientSocket implements InitializingServiceI, DisposableBean {

    private static final long serialVersionUID = 6821739991866153788L;

    private static final Logger logger = MyLogger.getLogger(IBSession.class.getName());

    private int clientId;
    private final Configuration configuration;
    private final IBSessionLifecycle sessionLifecycle;
    private MarketDataService marketDataService;

    public IBSession(int clientId, Configuration configuration, IBSessionLifecycle sessionLifecycle, AbstractIBMessageHandler messageHandler, MarketDataService marketDataService) {

        super(messageHandler);

        Validate.notNull(configuration, "Configuration may not be null");
        Validate.notNull(sessionLifecycle, "IBSessionLifecycle may not be null");

        this.clientId = clientId;
        this.configuration = configuration;
        this.sessionLifecycle = sessionLifecycle;
        this.marketDataService = marketDataService;
    }

    @Override
    public void init() {
        connect();
    }

    @Override
    public void destroy() {
        disconnect();
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

        int port = this.configuration.getInt("ib.port"); //4001
        String host = this.configuration.getString("ib.host"); // "127.0.0.1

        eConnect(host, port, this.clientId);

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

        long connectionTimeout = this.configuration.getInt("ib.connectionTimeout"); //10000;//

        try {
            Thread.sleep(connectionTimeout);
        } catch (InterruptedException e1) {
            try {
                // during eDisconnect this thread get's interrupted so sleep again
                Thread.sleep(connectionTimeout);
            } catch (InterruptedException e2) {
                logger.error("problem sleeping", e2);
            }
        }
    }

    private synchronized boolean connectionAvailable() {

        try {

            int port = this.configuration.getInt("ib.port"); //7496;//
            String host = this.configuration.getString("ib.host"); // "127.0.0.1";

            Socket socket = new Socket(host, port);
            socket.close();
            return true;
        } catch (ConnectException e) {
            // do nothing, gateway is down
            return false;
        } catch (IOException e) {
            logger.error("connection error", e);
            return false;
        }
    }
}
