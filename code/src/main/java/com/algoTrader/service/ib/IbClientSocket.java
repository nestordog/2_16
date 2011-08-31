package com.algoTrader.service.ib;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.algoTrader.enumeration.ConnectionState;
import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.MyLogger;
import com.ib.client.EClientSocket;

public class IbClientSocket extends EClientSocket {

    private static final long connectionTimeout = ConfigurationUtil.getBaseConfig().getInt("ib.connectionTimeout");
    private static int port = ConfigurationUtil.getBaseConfig().getInt("ib.port");

    private static Logger logger = MyLogger.getLogger(IbClientSocket.class.getName());

    private IbWrapper wrapper;

    public IbClientSocket(IbWrapper wrapper) {

        super(wrapper);
        this.wrapper = wrapper;
    }

    public void connect(int clientId) {

        if (isConnected()) {
            eDisconnect();

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

        this.wrapper.setRequested(false);

        while (!connectionAvailable()) {

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

        eConnect(null, port, clientId);

        if (isConnected()) {
            this.wrapper.setState(ConnectionState.CONNECTED);
            logger.debug("connectionState: " + this.wrapper.getState());
        }
    }

    public void disconnect() {

        if (isConnected()) {
            eDisconnect();
        }
    }

    private static synchronized boolean connectionAvailable() {
        try {
            Socket socket = new Socket("127.0.0.1", port);
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
