package com.algoTrader.service;

import java.util.Map;

import com.algoTrader.enumeration.ConnectionState;

public class BaseManagementServiceImpl extends BaseManagementServiceBase {

    @Override
    protected void handleClosePosition(int positionId, boolean unsubscribe) throws Exception {

        getPositionService().closePosition(positionId, unsubscribe);
    }

    @Override
    protected void handleCloseCombination(int combinationId, String strategyName) throws Exception {

        getCombinationService().closeCombination(combinationId, strategyName);
    }

    @Override
    protected void handleReducePosition(int positionId, int quantity) throws Exception {

        getPositionService().reducePosition(positionId, quantity);
    }

    @Override
    protected void handleSetExitValue(int positionId, double exitValue) throws Exception {

        getPositionService().setExitValue(positionId, exitValue, true);
    }

    @Override
    protected void handleSetMargins() throws Exception {

        getPositionService().setMargins();
    }

    @Override
    protected void handleReconcile() throws Exception {

        getAccountService().reconcile();
    }

    @Override
    protected void handleEqualizeForex() throws Exception {

        getForexService().equalizeForex();
    }

    @Override
    protected void handleRebalancePortfolio() throws Exception {

        getAccountService().rebalancePortfolio();
    }

    @Override
    protected void handleResetCashBalances() throws Exception {

        getCashBalanceService().resetCashBalances();
    }

    @Override
    protected void handleReconnectIB() throws Exception {

        getIBService().connect();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Map<String, ConnectionState> handleGetAllConnectionStates() {

        return getIBService().getAllConnectionStates();
    }
}
