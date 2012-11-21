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
import com.algoTrader.util.metric.MetricsUtil;

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
    protected void handleCancelAllOrders() throws Exception {

        getOrderService().cancelAllOrders();
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
    protected void handleTransferPosition(int positionId, String targetStrategyName) throws Exception {

        getPositionService().transferPosition(positionId, targetStrategyName);
    }

    @Override
    protected void handleSetMargins() throws Exception {

        getPositionService().setMargins();
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
    protected void handleLogMetrics() throws Exception {

        MetricsUtil.logMetrics();
        EsperManager.logStatementMetrics();
    }

    @Override
    protected void handleResetMetrics() throws Exception {

        MetricsUtil.resetMetrics();
        EsperManager.resetStatementMetrics();
    }
}
