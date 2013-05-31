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

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.algoTrader.enumeration.ConnectionState;

/**
 * Factory class for IBClients (IB Connections).
 * This class an its public methods are available through JMX.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@ManagedResource(objectName = "ch.algorader.ib:name=IBClientFactory")
public class IBClientFactory {

    private @Value("${simulation}") boolean simulation;
    private @Value("${ib.defaultClientId}") int defaultClientId;

    private IBClient defaultClient;
    private Map<Integer, IBClient> clients = new HashMap<Integer, IBClient>();

    /**
     * Gets the DefaultClient, usually clientId = 0.
     */
    public IBClient getDefaultClient() {

        if (this.simulation) {
            return null;
        }

        if (this.defaultClient == null) {

            this.defaultClient = new IBClient(this.defaultClientId, new IBEsperMessageHandler(this.defaultClientId));

            this.clients.put(this.defaultClientId, this.defaultClient);

            this.defaultClient.connect();
        }

        return this.defaultClient;
    }

    /**
     * Gets a new IBClient based on a {@code clientId} and a {@code IBDefaultMessageHandler MessageHandler}
     */
    public IBClient getClient(int clientId, IBDefaultMessageHandler messageHandler) {

        IBClient client = new IBClient(clientId, messageHandler);
        this.clients.put(clientId, client);

        return client;
    }

    /**
     * (re)connects all IBClients
     */
    @ManagedOperation
    @ManagedOperationParameters({})
    public void connect() {

        for (IBClient client : this.clients.values()) {
            client.connect();
        }
    }

    /**
     * disconnects all IBClients
     */
    @ManagedOperation
    @ManagedOperationParameters({})
    public void disconnect() {

        for (IBClient client : this.clients.values()) {
            client.disconnect();
        }
    }

    /**
     * Sets the Log Level on all IBClients
     */
    @ManagedOperation
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "logLevel", description = "<html> <head> </head> <body> <p> logLevel: </p> <ul>     <li> 1 (SYSTEM) </li> <li> 2 (ERROR) </li> <li> 3 (WARNING) </li> <li> 4 (INFORMATION) </li> <li> 5 (DETAIL) </li> </ul> </body> </html>") })
    public void setLogLevel(int logLevel) {

        for (IBClient client : this.clients.values()) {
            client.setServerLogLevel(logLevel);
        }
    }

    /**
     * Returns a Map with {@code clientId} and {@code connectionState} of all IB Clients
     */
    @ManagedAttribute
    public Map<Integer, ConnectionState> getConnectionStates() {

        Map<Integer, ConnectionState> connectionStates = new HashMap<Integer, ConnectionState>();
        for (Map.Entry<Integer, IBClient> entry : this.clients.entrySet()) {
            connectionStates.put(entry.getKey(), entry.getValue().getMessageHandler().getState());
        }
        return connectionStates;
    }
}
