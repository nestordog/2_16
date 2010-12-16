package com.algoTrader.service;


public class BaseManagementServiceImpl extends BaseManagementServiceBase {

    protected void handleClosePosition(int positionId) throws Exception {

        getPositionService().closePosition(positionId);
    }

    protected void handleSetExitValue(int positionId, double exitValue) throws Exception {

        getPositionService().setExitValue(positionId, exitValue, true);
    }

    protected void handleReconnectIB() throws Exception {

        getIbService().connect();
    }

    protected void handleRunDailyJobs() throws Exception {

        getStockOptionService().setMargins();
        getDispatcherService().getAccountService().processCashTransactions();
    }

    protected void handleReducePosition(int positionId, int quantity) throws Exception {

        getPositionService().reducePosition(positionId, quantity);
    }
}
