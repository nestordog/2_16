/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
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
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.TransactionType;
import ch.algotrader.vo.PositionMutationVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface TransactionService {

    /**
     * Creates a Transaction based on a {@link Fill}
     */
    public void createTransaction(Fill fill);

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
    public void createTransaction(int securityId, String strategyName, String extId, Date dateTime, long quantity, BigDecimal price, BigDecimal executionCommission, BigDecimal clearingCommission,
            BigDecimal fee, Currency currency, TransactionType transactionType, String accountName, String description);

    /**
     * Persists a Transaction to the Database, creates / updates a corresponding Position, updates
     * the corresponding {@link ch.algotrader.entity.strategy.CashBalance   CashBalance} and saves a
     * {@link ch.algotrader.entity.strategy.PortfolioValue PortfolioValue}. In this Transaction
     * closes a Position, a corresponding {@link ch.algotrader.vo.TradePerformanceVO
     * TradePerformanceVO} is calculated.
     */
    public PositionMutationVO persistTransaction(Transaction transaction);

    /**
     * Propagates a Fill to the corresponding Strategy.
     */
    public void propagateFill(Fill fill);

    /**
     * Logs aggregated Information of all Fills belonging to one Order.
     */
    public void logFillSummary(List<Fill> fills);

    /**
     * Creates Rebalance Transactions so that Net-Liquidation-Values of all strategies are in line
     * with the defined Strategy-Allocation.
     */
    public void rebalancePortfolio();

    /**
     * Saves current Portfolio Values for all Strategies marked as {@code autoActivate}
     */
    public void savePortfolioValues();

    /**
     * Saves current Portfolio Values as a consequence for a performance relevant Transaction. See
     * {@link Transaction#isPerformanceRelevant}. If there have been
     * PortfolioValues created since this Transaction, they are recreated (including PortfolioValues
     * of Base).
     */
    public void savePortfolioValue(Transaction transaction);

    /**
     * Restores all PortfolioValues of the specified Strategy after the {@code fromDate} up to and
     * including the {@code toDate}.
     */
    public void restorePortfolioValues(Strategy strategy, Date fromDate, Date toDate);

}
