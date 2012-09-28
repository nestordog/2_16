package com.algoTrader.service.ib;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.enumeration.ConnectionState;
import com.algoTrader.util.MyLogger;
import com.ib.client.EClientSocket;

public final class IBClient extends EClientSocket {

    private static Logger logger = MyLogger.getLogger(IBClient.class.getName());

    private static boolean simulation = ServiceLocator.instance().getConfiguration().getSimulation();
    private static int port = ServiceLocator.instance().getConfiguration().getInt("ib.port"); //7496;//
    private static String host = ServiceLocator.instance().getConfiguration().getString("ib.host"); // "127.0.0.1";
    private static long connectionTimeout = ServiceLocator.instance().getConfiguration().getInt("ib.connectionTimeout"); //10000;//

    private int clientId;

    public IBClient(int clientId, IBDefaultMessageHandler messageHandler) {

        super(messageHandler);
        this.clientId = clientId;
    }

    public IBDefaultMessageHandler getMessageHandler() {

        return (IBDefaultMessageHandler) super.wrapper();
    }

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
