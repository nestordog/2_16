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
package ch.algotrader.dao;

import java.util.Date;
import java.util.List;

import ch.algotrader.entity.Transaction;
import ch.algotrader.hibernate.EntityConverter;
import ch.algotrader.hibernate.ReadWriteDao;

/**
 * DAO for {@link ch.algotrader.entity.Transaction} objects.
 *
 * @see ch.algotrader.entity.Transaction
 */
public interface TransactionDao extends ReadWriteDao<Transaction> {

    /**
     * @param strategyName
     * @return List<Transaction>
     */
    List<Transaction> findByStrategy(String strategyName);

    /**
     * Finds Transactions in descending {@code dateTime} order.
     * The <code>limit</code> argument allows you to specify the limit when you are paging the results.
     * @param limit
     * @param converter
     * @return List<V>
     */
    <V> List<V> findTransactionsDesc(int limit, EntityConverter<Transaction, V> converter);

    /**
     * Finds Transactions of a specific Strategy in descending {@code dateTime} order.
     * The <code>limit</code> argument allows you to specify the limit when you are paging the results.
     * @param limit
     * @param strategyName
     * @param converter
     * @return List<V>
     */
    <V> List<V> findTransactionsByStrategyDesc(int limit, String strategyName, EntityConverter<Transaction, V> converter);

    /**
     * Finds all Trades (either  {@code BUY}, {@code SELL}, {@code EXPIRATION} or {@code TRANSFER})
     * with their corresponding Security fetched.
     * @return List<Transaction>
     */
    List<Transaction> findAllTradesInclSecurity();

    /**
     * Finds all non-trade Transactions of a particular Strategy after the defined {@code minDate}
     * @param strategyName
     * @param minDate
     * @return List<Transaction>
     */
    List<Transaction> findCashflowsByStrategyAndMinDate(String strategyName, Date minDate);

    /**
     * Finds all Transaction before the specified {@code maxDate}
     * @param maxDate
     * @return List<Transaction>
     */
    List<Transaction> findByMaxDate(Date maxDate);

    /**
     * Finds all Transaction of a specified Strategy before the specified {@code maxDate}
     * @param strategyName
     * @param maxDate
     * @return List<Transaction>
     */
    List<Transaction> findByStrategyAndMaxDate(String strategyName, Date maxDate);

    // spring-dao merge-point
}
