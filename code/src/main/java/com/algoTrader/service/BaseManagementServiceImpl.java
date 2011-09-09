package com.algoTrader.service;

import java.util.Map;

import com.algoTrader.enumeration.ConnectionState;

public class BaseManagementServiceImpl extends BaseManagementServiceBase {

    @Override
    protected void handleClosePosition(int positionId) throws Exception {

        getPositionService().closePosition(positionId);
    }

    @Override
    protected void handleSetExitValue(int positionId, double exitValue) throws Exception {

        getPositionService().setExitValue(positionId, exitValue, true);
    }

    @Override
    protected void handleReconnectIB() throws Exception {

        getIBService().connect();
    }

    @Override
    protected void handleSetMargins() throws Exception {

        getPositionService().setMargins();
    }

    @Override
    protected void handleProcessCashTransactions() throws Exception {

        getAccountService().processCashTransactions();
    }

    @Override
    protected void handleReducePosition(int positionId, int quantity) throws Exception {

        getPositionService().reducePosition(positionId, quantity);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Map<String, ConnectionState> handleGetAllConnectionStates() {

        return getIBService().getAllConnectionStates();
    }

    @Override
    protected void handleEqualizeForex() throws Exception {

        getForexService().equalizeForex();
    }

    @Override
    protected void handlePutOnWatchlist(String strategyName, int securityid) throws Exception {

        getSyncMarketDataService().putOnWatchlist(strategyName, securityid);
    }

    @Override
    protected void handleRemoveFromWatchlist(String strategyName, int securityid) throws Exception {

        getSyncMarketDataService().removeFromWatchlist(strategyName, securityid);
    }

}
