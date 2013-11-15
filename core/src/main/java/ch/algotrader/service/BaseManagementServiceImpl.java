/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Status;
import ch.algotrader.enumeration.TransactionType;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.util.RoundUtil;
import ch.algotrader.util.metric.MetricsUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class BaseManagementServiceImpl extends BaseManagementServiceBase {

    @Override
    protected void handleCancelAllOrders() throws Exception {

        getOrderService().cancelAllOrders();
    }

    @Override
    protected void handleRecordTransaction(int securityId, String strategyName, String extIdString, String dateTimeString, long quantity, double priceDouble, double executionCommissionDouble,
            double clearingCommissionDouble, String currencyString, String transactionTypeString, String accountName) throws Exception {

        String extId = !"".equals(extIdString) ? extIdString : null;
        Date dateTime = (new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")).parse(dateTimeString);
        BigDecimal price = RoundUtil.getBigDecimal(priceDouble);
        BigDecimal executionCommission = (executionCommissionDouble != 0) ? RoundUtil.getBigDecimal(executionCommissionDouble) : null;
        BigDecimal clearingCommission = (clearingCommissionDouble != 0) ? RoundUtil.getBigDecimal(clearingCommissionDouble) : null;
        Currency currency = !"".equals(currencyString) ? Currency.fromValue(currencyString) : null;
        TransactionType transactionType = TransactionType.fromValue(transactionTypeString);

        getTransactionService().createTransaction(securityId, strategyName, extId, dateTime, quantity, price, executionCommission, clearingCommission, currency, transactionType, accountName);
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
    protected void handleHedgeForex() throws Exception {

        getForexService().hedgeForex();
    }

    @Override
    protected void handleHedgeDelta(int underlyingId) throws Exception {

        getOptionService().hedgeDelta(underlyingId);
    }

    @Override
    protected void handleRebalancePortfolio() throws Exception {

        getPortfolioPersistenceService().rebalancePortfolio();
    }

    @Override
    protected String handleResetPositionsAndCashBalances() throws Exception {

        return getPositionService().resetPositions() + getCashBalanceService().resetCashBalances();
    }

    @Override
    protected void handleResetComponentWindow() throws Exception {

        getCombinationService().resetComponentWindow();
    }

    @Override
    protected void handleEmptyOpenOrderWindow() throws Exception {

        OrderStatus orderStatus = OrderStatus.Factory.newInstance();
        orderStatus.setStatus(Status.CANCELED);

        EngineLocator.instance().getBaseEngine().sendEvent(orderStatus);
    }

    @Override
    protected void handleLogMetrics() throws Exception {

        MetricsUtil.logMetrics();
        EngineLocator.instance().logStatementMetrics();
    }

    @Override
    protected void handleResetMetrics() throws Exception {

        MetricsUtil.resetMetrics();
        EngineLocator.instance().resetStatementMetrics();
    }
}
