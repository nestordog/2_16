package com.algoTrader.service;

import java.util.Set;

import com.algoTrader.service.ib.IbService;

public class CommandServiceImpl extends com.algoTrader.service.CommandServiceBase {

    protected void handleActivate(String ruleName) throws Exception {

        getRuleService().activate(ruleName);
    }

    protected void handleClosePosition(int positionId) throws Exception {

        getStockOptionService().closePosition(positionId);
    }

    protected void handleSetExitValue(int positionId, double exitValue) throws Exception {

        getStockOptionService().setExitValue(positionId, exitValue);
    }

    protected void handleDeactivate(String ruleName) throws Exception {

        getRuleService().deactivate(ruleName);
    }

    @SuppressWarnings("unchecked")
    protected void handleReconnectIB() throws Exception {

        Set<IbService> services = getDispatcherService().getAllIbServices();

        for (IbService service : services) {
            service.connect();
        }
    }

    protected void handleRunDailyJobs() throws Exception {

        getActionService().runDailyJobs();
    }

}
