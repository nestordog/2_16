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

package ch.algotrader.dao;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.TransactionImpl;
import ch.algotrader.enumeration.QueryType;
import ch.algotrader.util.DateTimeLegacy;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
@Repository // Required for exception translation
public class TransactionDaoImpl extends AbstractDao<Transaction> implements TransactionDao {

    public TransactionDaoImpl(final SessionFactory sessionFactory) {

        super(TransactionImpl.class, sessionFactory);
    }

    @Override
    public List<Transaction> findByStrategy(String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        return findCaching("Transaction.findByStrategy", QueryType.BY_NAME, new NamedParam("strategyName", strategyName));
    }

    @Override
    public List<Transaction> findDailyTransactions() {

        LocalDate today = LocalDate.now();
        return find("Transaction.findDailyTransactions", QueryType.BY_NAME, new NamedParam("curdate", DateTimeLegacy.toLocalDate(today)));
    }

    @Override
    public List<Transaction> findDailyTransactionsByStrategy(String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        LocalDate today = LocalDate.now();
        return find("Transaction.findDailyTransactionsByStrategy", QueryType.BY_NAME,
                new NamedParam("curdate", DateTimeLegacy.toLocalDate(today)), new NamedParam("strategyName", strategyName));
    }

    @Override
    public List<Transaction> findAllTradesInclSecurity() {

        return findCaching("Transaction.findAllTradesInclSecurity", QueryType.BY_NAME);
    }

    @Override
    public List<Transaction> findCashflowsByStrategyAndMinDate(String strategyName, Date minDate) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(minDate, "minDate is null");

        return findCaching("Transaction.findCashflowsByStrategyAndMinDate", QueryType.BY_NAME, new NamedParam("strategyName", strategyName), new NamedParam("minDate", minDate));
    }

    @Override
    public List<Transaction> findByMaxDate(Date maxDate) {

        Validate.notNull(maxDate, "maxDate is null");

        return find("Transaction.findByMaxDate", QueryType.BY_NAME, new NamedParam("maxDate", maxDate));
    }

    @Override
    public List<Transaction> findByStrategyAndMaxDate(String strategyName, Date maxDate) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(maxDate, "maxDate is null");

        return find("Transaction.findByStrategyAndMaxDate", QueryType.BY_NAME, new NamedParam("strategyName", strategyName), new NamedParam("maxDate", maxDate));
    }
}
