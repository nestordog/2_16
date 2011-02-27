package com.algoTrader.service.ib;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.util.ClassUtils;

import com.algoTrader.enumeration.ConnectionState;

public class IbServiceImpl extends IbServiceBase {

    private Set<IIbService> services;

    protected void handleInit() {

        for (IIbService service : this.getAllIbServices()) {
            service.init();
        }
    }

    protected void handleConnect() {

        for (IIbService service : this.getAllIbServices()) {
            service.connect();
        }
    }

    protected Map<String, ConnectionState> handleGetAllConnectionStates() {

        Map<String, ConnectionState> connectionStates = new HashMap<String, ConnectionState>();
        for (IIbService service : this.getAllIbServices()) {
            connectionStates.put(ClassUtils.getShortName(service.getClass().getInterfaces()[0]), service.getConnectionState());
        }
        return connectionStates;
    }

    private Set<IIbService> getAllIbServices() {

        if (this.services == null) {

            this.services = new HashSet<IIbService>();
            this.services.add(getIbTickService());
            this.services.add(getIbTransactionService());
            this.services.add(getIbAccountService());
            this.services.add(getIbHistoricalDataService());
            // this.services.add(getIbStockOptionRetrieverService());
        }

        return this.services;
    }

}
