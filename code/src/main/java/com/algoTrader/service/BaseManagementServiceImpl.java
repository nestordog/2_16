package com.algoTrader.service;

import java.util.Map;

import com.algoTrader.enumeration.ConnectionState;

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

    protected void handleSetMargins() throws Exception {

        getPositionService().setMargins();
    }

    protected void handleProcessCashTransactions() throws Exception {

        getAccountService().processCashTransactions();
    }

    protected void handleReducePosition(int positionId, int quantity) throws Exception {

        getPositionService().reducePosition(positionId, quantity);
    }

    @SuppressWarnings("unchecked")
    protected Map<String, ConnectionState> handleGetAllConnectionStates() {

        return getIbService().getAllConnectionStates();
    }

    @Override
    protected void handleEqualizeForex() throws Exception {

        getForexService().equalizeForex();
    }

    @Override
    protected void handlePutOnWatchlist(String strategyName, int securityid) throws Exception {

        getMarketDataService().putOnWatchlist(strategyName, securityid);
    }

    @Override
    protected void handleRemoveFromWatchlist(String strategyName, int securityid) throws Exception {

        getMarketDataService().removeFromWatchlist(strategyName, securityid);
    }

}
