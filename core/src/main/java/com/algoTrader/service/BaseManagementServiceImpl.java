package com.algoTrader.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.algoTrader.enumeration.ConnectionState;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.service.ib.IBServiceManager;
import com.algoTrader.util.RoundUtil;

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
    protected void handleRecordTransaction(int securityId, String strategyName, String extIdString, String dateTimeString, long quantity, double priceDouble, double commissionDouble,
            String currencyString, String transactionTypeString) throws Exception {

        String extId = !"".equals(extIdString) ? extIdString : null;
        Date dateTime = (new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")).parse(dateTimeString);
        BigDecimal price = RoundUtil.getBigDecimal(priceDouble);
        BigDecimal commission = RoundUtil.getBigDecimal(commissionDouble);
        Currency currency = currencyString != null && !"".equals(currencyString) ? Currency.fromValue(currencyString) : null;
        TransactionType transactionType = TransactionType.fromValue(transactionTypeString);

        getTransactionService().createTransaction(securityId, strategyName, extId, dateTime, quantity, price, commission, currency, transactionType);
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
    protected void handleResetPositionsAndCashBalances() throws Exception {

        getPositionService().resetPositions();

        getCashBalanceService().resetCashBalances();
    }

    @Override
    protected void handleReconnectIB() throws Exception {

        IBServiceManager.connect();
    }

    @Override
    protected Map<String, ConnectionState> handleGetAllConnectionStates() {

        return IBServiceManager.getAllConnectionStates();
    }

    @Override
    protected void handleResetComponentWindow() throws Exception {

        getCombinationService().resetComponentWindow();
    }

    @Override
    protected void handleAddProperty(int propertyHolderId, String name, String value, String type) throws Exception {

        Object obj;
        if ("INT".equals(type)) {
            obj = Integer.parseInt(value);
        } else if ("DOUBLE".equals(type)) {
            obj = Double.parseDouble(value);
        } else if ("MONEY".equals(type)) {
            obj = new BigDecimal(value);
        } else if ("TEXT".equals(type)) {
            obj = value;
        } else if ("DATE".equals(type)) {
            obj = (new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")).parse(value);
        } else if ("BOOLEAN".equals(type)) {
            obj = Boolean.parseBoolean(value);
        } else {
            throw new IllegalArgumentException("unknown type " + type);
        }

        getPropertyService().addProperty(propertyHolderId, name, obj, false);
    }

    @Override
    protected void handleRemoveProperty(int propertyHolderId, String name) throws Exception {

        getPropertyService().removeProperty(propertyHolderId, name);
    }
}
