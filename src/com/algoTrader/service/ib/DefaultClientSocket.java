package com.algoTrader.service.ib;

import com.algoTrader.util.PropertiesUtil;
import com.ib.client.EClientSocket;

public class DefaultClientSocket extends EClientSocket {

    private static final long reconnectTimeout = PropertiesUtil.getIntProperty("ib.reconnectTimeout");
    private static int port = PropertiesUtil.getIntProperty("ib.port");

    private DefaultWrapper wrapper;

    public DefaultClientSocket(DefaultWrapper wrapper) {

        super(wrapper);
        this.wrapper = wrapper;
    }

    public void connect(int clientId) {

        this.wrapper.setRequested(false);

        while (!isConnected()) {
            eConnect(null, port, clientId);

            if (!isConnected()) {
                try {
                    Thread.sleep(reconnectTimeout);
                } catch (InterruptedException e) {
                    // no nothing
                }
            }
        }
    }
}
