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

import org.apache.log4j.Logger;

import ch.algotrader.util.MyLogger;

import ch.algotrader.ServiceLocator;
import ch.algotrader.enumeration.ConnectionState;
import com.ib.client.EClientSocket;

/**
 * Represents on IB (socket) connection.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public final class IBSession extends EClientSocket {

    private static Logger logger = MyLogger.getLogger(IBSession.class.getName());

    private static boolean simulation = ServiceLocator.instance().getConfiguration().getSimulation();
    private static int port = ServiceLocator.instance().getConfiguration().getInt("ib.port"); //7496;//
    private static String host = ServiceLocator.instance().getConfiguration().getString("ib.host"); // "127.0.0.1";
    private static long connectionTimeout = ServiceLocator.instance().getConfiguration().getInt("ib.connectionTimeout"); //10000;//

    private int clientId;

    /**
     * Constructor based on a {@code clientId} and a {@code IBDefaultMessageHandler MessageHandler}
     */
    public IBSession(int clientId, IBDefaultMessageHandler messageHandler) {

        super(messageHandler);
        this.clientId = clientId;
    }

    /**
     * Returns the associated {@code IBDefaultMessageHandler MessageHandler}
     */
    public IBDefaultMessageHandler getMessageHandler() {

        return (IBDefaultMessageHandler) super.wrapper();
    }

    /**
     * (re)connects to TWS / IB Gateway
     */
    public void connect() {

        if (simulation) {
            return;
        }

        if (isConnected()) {
            eDisconnect();

            sleep();
        }

        this.getMessageHandler().setRequested(false);

        while (!connectionAvailable()) {
            sleep();
        }

        eConnect(host, port, this.clientId);

        if (isConnected()) {
            this.getMessageHandler().setState(ConnectionState.LOGGED_ON);

            // in case there is no 2104 message from the IB Gateway (Market data farm connection is OK)
            // manually invoke initSubscriptions after some time if there is marketDataService
            if (ServiceLocator.instance().containsService("marketDataService")) {
                sleep();
                ServiceLocator.instance().getMarketDataService().initSubscriptions();
            }
        }
    }

    /**
     * disconnects from TWS / IB Gateway
     */
    public void disconnect() {

        if (isConnected()) {
            eDisconnect();
            getMessageHandler().setState(ConnectionState.DISCONNECTED);
        }
    }

    private void sleep() {

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
