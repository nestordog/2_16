package com.algoTrader.service.ib;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.util.ClassUtils;

import com.algoTrader.enumeration.ConnectionState;

public class IBServiceImpl extends IBServiceBase {

    private Set<IBServiceI> services;

    @Override
    protected void handleInit() {

        for (IBServiceI service : this.getAllIBServices()) {
            service.init();
        }
    }

    @Override
    protected void handleConnect() {

        for (IBServiceI service : this.getAllIBServices()) {
            service.connect();
        }
    }

    @Override
    protected Map<String, ConnectionState> handleGetAllConnectionStates() {

        Map<String, ConnectionState> connectionStates = new HashMap<String, ConnectionState>();
        for (IBServiceI service : this.getAllIBServices()) {
            connectionStates.put(ClassUtils.getShortName(service.getClass().getInterfaces()[0]), service.getConnectionState());
        }
        return connectionStates;
    }

    private Set<IBServiceI> getAllIBServices() {

        if (this.services == null) {

            this.services = new HashSet<IBServiceI>();
            this.services.add(getIBAccountService());
            this.services.add(getIBHistoricalDataService());
            this.services.add(getIBSecurityRetrieverService());
        }

        return this.services;
    }

}
