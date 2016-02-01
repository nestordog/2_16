/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.trade.ExternalFill;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.TransactionType;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public interface TransactionService {

    /**
     * Records given transaction.
     */
    public void recordTransaction(Transaction transaction);

    /**
     * Creates a Transaction based on a {@link Fill}
     */
    public void createTransaction(Fill fill);

    /**
     * Creates a Transaction based on a {@link ExternalFill}
     */
    public void createTransaction(ExternalFill fill);

    /**
     * Creates a Transaction based on the specified parameters.
     * @param quantity Requested quantity:
     * <ul>
     * <li>BUY: pos</li>
     * <li>SELL: neg</li>
     * <li>EXPIRATION: pos/neg</li>
     * <li>TRANSFER : pos/neg</li>
     * <li>CREDIT: 1</li>
     * <li>INTREST_RECEIVED: 1</li>
     * <li>REFUND : 1</li>
     * <li>DIVIDEND : 1</li>
     * <li>DEBIT: -1</li>
     * <li>INTREST_PAID: -1</li>
     * <li>FEES: -1</li>
     * </ul>
     */
    public void createTransaction(long securityId, String strategyName, String extId, Date dateTime, long quantity, BigDecimal price, BigDecimal executionCommission, BigDecimal clearingCommission,
            BigDecimal fee, Currency currency, TransactionType transactionType, String accountName, String description);

    /**
     * Persists a Transaction to the Database, creates / updates a corresponding Position, updates
     * the corresponding {@link ch.algotrader.entity.strategy.CashBalance   CashBalance} and saves a
     * {@link ch.algotrader.entity.strategy.PortfolioValue PortfolioValue}. In this Transaction
     * closes a Position, a corresponding {@link ch.algotrader.vo.TradePerformanceVO
     * TradePerformanceVO} is calculated.
     */
    public void persistTransaction(Transaction transaction);

    /**
     * Logs aggregated Information of all Fills belonging to one Order.
     */
    public void logFillSummary(List<Fill> fills);

    /**
     * Logs aggregated Information of all Fills belonging to one Order from the given insert stream.
     */
    public void logFillSummary(Map<?, ?>[] insertStream, Map<?, ?>[] removeStream);

    /**
     * Calculates all Cash Balances based on Transactions in the database and makes adjustments if
     * necessary
     */
    public String resetCashBalances();

}
