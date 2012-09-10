package com.algoTrader.service.ib;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.algoTrader.enumeration.ConnectionState;

@ManagedResource(objectName = "com.algoTrader.ib:name=IBClientFactory")
public class IBClientFactory {

    private @Value("${simulation}") boolean simulation;
    private @Value("${ib.defaultClientId}") int defaultClientId;

    private IBClient defaultClient;
    private Map<Integer, IBClient> clients = new HashMap<Integer, IBClient>();

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

    public IBClient getClient(int clientId, IBDefaultMessageHandler messageHandler) {

        IBClient client = new IBClient(clientId, messageHandler);
        this.clients.put(clientId, client);

        return client;
    }

    @ManagedOperation
    @ManagedOperationParameters({})
    public void connect() {

        for (IBClient client : this.clients.values()) {
            client.connect();
        }
    }

    @ManagedOperation
    @ManagedOperationParameters({})
    public void disconnect() {

        for (IBClient client : this.clients.values()) {
            client.disconnect();
        }
    }

    @ManagedOperation
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "logLevel", description = "<html> <head> </head> <body> <p> logLevel: </p> <ul>     <li> 1 (SYSTEM) </li> <li> 2 (ERROR) </li> <li> 3 (WARNING) </li> <li> 4 (INFORMATION) </li> <li> 5 (DETAIL) </li> </ul> </body> </html>") })
    public void setLogLevel(int logLevel) {

        for (IBClient client : this.clients.values()) {
            client.setServerLogLevel(logLevel);
        }
    }

    @ManagedAttribute
    public Map<Integer, ConnectionState> getConnectionStates() {

        Map<Integer, ConnectionState> connectionStates = new HashMap<Integer, ConnectionState>();
        for (Map.Entry<Integer, IBClient> entry : this.clients.entrySet()) {
            connectionStates.put(entry.getKey(), entry.getValue().getMessageHandler().getState());
        }
        return connectionStates;
    }
}
