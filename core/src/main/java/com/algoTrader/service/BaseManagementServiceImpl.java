package com.algoTrader.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Transformer;

import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.trade.OrderStatus;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.MarketChannel;
import com.algoTrader.enumeration.Status;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.esper.EsperManager;
import com.algoTrader.util.RoundUtil;

public class BaseManagementServiceImpl extends BaseManagementServiceBase {

    @Override
    protected Collection<String> handleGetMarketChannels() throws Exception {

        return CollectionUtils.collect(getOrderService().getMarketChannels(), new Transformer<MarketChannel, String>() {
            @Override
            public String transform(MarketChannel marketChannel) {
                return marketChannel.toString();
            }
        });
    }

    @Override
    protected String handleGetDefaultMarketChannel() throws Exception {
        return getOrderService().getDefaultMarketChannel().toString();
    }

    @Override
    protected void handleSetDefaultMarketChannel(String marketChannel) throws Exception {

        getOrderService().setDefaultMarketChannel(MarketChannel.fromString(marketChannel));
    }

    @Override
    protected void handleClosePosition(int positionId, boolean unsubscribe) throws Exception {

        getPositionService().closePosition(positionId, unsubscribe);
    }

    @Override
    protected void handleReducePosition(int positionId, int quantity) throws Exception {

        getPositionService().reducePosition(positionId, quantity);
    }

    @Override
    protected void handleCancelAllOrders() throws Exception {

        getOrderService().cancelAllOrders();
    }

    @Override
    protected void handleSetExitValue(int positionId, double exitValue) throws Exception {

        getPositionService().setExitValue(positionId, exitValue, true);
    }

    @Override
    protected void handleRecordTransaction(int securityId, String strategyName, String extIdString, String dateTimeString, long quantity, double priceDouble, double executionCommissionDouble,
            double clearingCommissionDouble, String currencyString, String transactionTypeString, String marketChannelString) throws Exception {

        String extId = !"".equals(extIdString) ? extIdString : null;
        Date dateTime = (new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")).parse(dateTimeString);
        BigDecimal price = RoundUtil.getBigDecimal(priceDouble);
        BigDecimal executionCommission = (executionCommissionDouble != 0) ? RoundUtil.getBigDecimal(executionCommissionDouble) : null;
        BigDecimal clearingCommission = (clearingCommissionDouble != 0) ? RoundUtil.getBigDecimal(clearingCommissionDouble) : null;
        Currency currency = !"".equals(currencyString) ? Currency.fromValue(currencyString) : null;
        TransactionType transactionType = TransactionType.fromValue(transactionTypeString);
        MarketChannel marketChannel = !"".equals(marketChannelString) ? MarketChannel.fromValue(marketChannelString) : null;

        getTransactionService().createTransaction(securityId, strategyName, extId, dateTime, quantity, price, executionCommission, clearingCommission, currency, transactionType, marketChannel);
    }

    @Override
    protected void handleSetMargins() throws Exception {

        getPositionService().setMargins();
    }

    @Override
    protected void handleReconcile() throws Exception {

        getReconciliationService().reconcile();
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
    protected void handleResetComponentWindow() throws Exception {

        getCombinationService().resetComponentWindow();
    }

    @Override
    protected void handleEmptyOpenOrderWindow() throws Exception {

        OrderStatus orderStatus = OrderStatus.Factory.newInstance();
        orderStatus.setStatus(Status.CANCELED);

        EsperManager.sendEvent(StrategyImpl.BASE, orderStatus);
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

    @Override
    protected void handleSetComponentQuantity(int combinationId, int securityId, long quantity) throws Exception {

        getCombinationService().setComponentQuantity(combinationId, securityId, quantity);
    }

    @Override
    protected void handleRemoveComponent(int combinationId, final int securityId) {

        getCombinationService().removeComponent(combinationId, securityId);
    }
}
