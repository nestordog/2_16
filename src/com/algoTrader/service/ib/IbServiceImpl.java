package com.algoTrader.service.ib;

import java.util.HashSet;
import java.util.Set;

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

    private Set<IIbService> getAllIbServices() {

        if (this.services == null) {

            this.services = new HashSet<IIbService>();
            this.services.add(getIbTickService());
            this.services.add(getIbTransactionService());
            this.services.add(getIbAccountService());
            // this.services.add(getIbStockOptionRetrieverService());
        }

        return this.services;
    }

}
