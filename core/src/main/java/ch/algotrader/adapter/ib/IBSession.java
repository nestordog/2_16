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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.ib.client.EClientSocket;

import ch.algotrader.enumeration.FeedType;
import ch.algotrader.enumeration.InitializingServiceType;
import ch.algotrader.service.InitializationPriority;
import ch.algotrader.service.InitializingServiceI;
import ch.algotrader.service.MarketDataService;

/**
 * Represents on IB (socket) connection.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@InitializationPriority(value = InitializingServiceType.BROKER_INTERFACE)
public final class IBSession extends EClientSocket implements InitializingServiceI, DisposableBean, ApplicationContextAware {

    private static final long serialVersionUID = 6821739991866153788L;

    private static final Logger logger = LogManager.getLogger(IBSession.class.getName());

    private final int clientId;
    private final String host;
    private final int port;
    private final IBSessionLifecycle fixSessionStateHolder;
    private final MarketDataService marketDataService;
    private ApplicationContext applicationContext;

    public IBSession(int clientId, String host, int port, IBSessionLifecycle fixSessionStateHolder, AbstractIBMessageHandler messageHandler, MarketDataService marketDataService) {

        super(messageHandler);

        Validate.notNull(clientId, "host may not be 0");
        Validate.notNull(host, "host may not be null");
        Validate.notNull(port, "host may not be 0");
        Validate.notNull(fixSessionStateHolder, "IBSessionLifecycle may not be null");

        this.clientId = clientId;
        this.host = host;
        this.port = port;
        this.fixSessionStateHolder = fixSessionStateHolder;

        this.marketDataService = marketDataService;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
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
        return this.fixSessionStateHolder;
    }

    /**
     * (re)connects to TWS / IB Gateway
     */
    public void connect() {

        if (isConnected()) {
            eDisconnect();

            sleep();
        }

        waitAndConnect();

        if (isConnected()) {
            this.fixSessionStateHolder.connect();

            // in case there is no 2104 message from the IB Gateway (Market data farm connection is OK)
            // manually invoke initSubscriptions after some time if there is marketDataService
            if (this.fixSessionStateHolder.logon(true)) {
                if (this.applicationContext.containsBean("iBNativeMarketDataService")) {
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
            this.fixSessionStateHolder.disconnect();
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

    private void waitAndConnect() {

        for (;;) {

            Socket socket = new Socket();
            try {
                socket.connect(new InetSocketAddress(this.host, this.port), 5000);
                eConnect(socket, this.clientId);
                return;
            } catch (ConnectException e) {
                // do nothing, gateway is down
                logger.info("please start IB Gateway / TWS on port: " + this.port);
            } catch (IOException e) {
                logger.error("connection error", e);
            }
            try {
                socket.close();
            } catch (IOException ignore) {
            }

            sleep();
        }

    }

}
