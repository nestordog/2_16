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

import java.util.Collection;

import ch.algotrader.entity.Transaction;
import ch.algotrader.vo.PositionMutationVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface TransactionPersistenceService {

     /**
     * Makes sure there is a {@link ch.algotrader.entity.Position} record for the given strategy
     * and security and {@link ch.algotrader.entity.strategy.CashBalance}s records
     * for the given strategy and currencies required to sa.
     */
    void ensurePositionAndCashBalance(Transaction transaction);

    /**
     * Saves the given {@link ch.algotrader.entity.Transaction}, updates the corresponding
     * {@link ch.algotrader.entity.Position}, updates the corresponding
     * {@link ch.algotrader.entity.strategy.CashBalance} and saves a
     * {@link ch.algotrader.entity.strategy.PortfolioValue}. If this transaction
     * closes a position, a corresponding {@link ch.algotrader.vo.TradePerformanceVO} is calculated.
     */
    PositionMutationVO saveTransaction(Transaction transaction);

    /**
     * Saves given {@link ch.algotrader.entity.Transaction}s, updates
     * the corresponding {@link ch.algotrader.entity.Position}s, updates the corresponding
     * {@link ch.algotrader.entity.strategy.CashBalance}s and saves a
     * {@link ch.algotrader.entity.strategy.PortfolioValue}s.
     */
    void saveTransactions(Collection<Transaction> transactions);


    /**
     * Calculates all Cash Balances based on Transactions in the database and makes adjustments if
     * necessary
     */
    public String resetCashBalances();

}
