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

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.TransactionImpl;
import ch.algotrader.enumeration.QueryType;
import ch.algotrader.hibernate.AbstractDao;
import ch.algotrader.hibernate.EntityConverter;
import ch.algotrader.hibernate.NamedParam;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
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
    public <V> List<V> findTransactionsDesc(int limit, EntityConverter<Transaction, V> converter) {

        Validate.notNull(converter, "EntityConverter is null");

        return find(converter, "Transaction.findTransactionsDesc", limit, QueryType.BY_NAME);
    }

    @Override
    public <V> List<V> findTransactionsByStrategyDesc(int limit, String strategyName, EntityConverter<Transaction, V> converter) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(converter, "EntityConverter is null");

        return find(converter, "Transaction.findTransactionsByStrategyDesc", limit, QueryType.BY_NAME, new NamedParam("strategyName", strategyName));
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
    public List<Transaction> findTradesByMinDateAndMaxDate(Date minDate, Date maxDate) {

        Validate.notNull(minDate, "minDate name is empty");
        Validate.notNull(maxDate, "maxDate is null");

        return find("Transaction.findTradesByMinDateAndMaxDate", QueryType.BY_NAME, new NamedParam("minDate", minDate), new NamedParam("maxDate", maxDate));
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

    @Override
    public BigDecimal findLastIntOrderId(String sessionQualifier) {

        Validate.notEmpty(sessionQualifier, "sessionQualifier is empty");

        return (BigDecimal) findUniqueObject(null, "Transaction.findLastIntOrderId", QueryType.BY_NAME, new NamedParam("sessionQualifier", sessionQualifier));
    }
}
