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
package ch.algotrader.service.mysql;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.PositionDao;
import ch.algotrader.entity.PositionImpl;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.TransactionDao;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.strategy.CashBalanceDao;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.esper.Engine;
import ch.algotrader.service.PortfolioService;
import ch.algotrader.service.TransactionPersistenceServiceImpl;
import ch.algotrader.util.HibernateUtil;
import ch.algotrader.vo.CurrencyAmountVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@Transactional
public class MySqlTransactionPersistenceServiceImpl extends TransactionPersistenceServiceImpl {

    private final SessionFactory sessionFactory;

    public MySqlTransactionPersistenceServiceImpl(final CommonConfig commonConfig,
            final PortfolioService portfolioService,
            final SessionFactory sessionFactory,
            final PositionDao positionDao,
            final TransactionDao transactionDao,
            final CashBalanceDao cashBalanceDao,
            final Engine serverEngine) {

        super(commonConfig, portfolioService, positionDao, transactionDao, cashBalanceDao, serverEngine);

        Validate.notNull(sessionFactory, "SessionFactory is null");

        this.sessionFactory = sessionFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ensurePositionAndCashBalance(final Transaction transaction) {

        Validate.notNull(transaction, "Transaction is null");

        Strategy strategy = transaction.getStrategy();
        Security security = transaction.getSecurity();
        Set<Currency> currencySet = new HashSet<Currency>();
        Collection<CurrencyAmountVO> attributions = transaction.getAttributions();
        for (CurrencyAmountVO attribution : attributions) {
            currencySet.add(attribution.getCurrency());
        }

        Session currentSession = this.sessionFactory.getCurrentSession();

        if (security != null) {

            Serializable id = HibernateUtil.getNextId(this.sessionFactory, PositionImpl.class);

            SQLQuery sqlQuery = currentSession.createSQLQuery("INSERT IGNORE INTO position " + "  (id, quantity, cost, realized_p_l, persistent, security_fk, strategy_fk, version) "
                    + "  VALUES (:position_id, 0, 0, 0, 0, :security_id, :strategy_id, 1)");

            sqlQuery.setParameter("position_id", id);
            sqlQuery.setParameter("security_id", security.getId());
            sqlQuery.setParameter("strategy_id", strategy.getId());
            sqlQuery.executeUpdate();
        }

        if (!currencySet.isEmpty()) {

            SQLQuery sqlQuery = currentSession.createSQLQuery("INSERT IGNORE INTO cash_balance " + "(currency, amount, strategy_fk, version) VALUES (:currency, 0, :strategy_id, 1)");
            for (Currency currency : currencySet) {

                sqlQuery.setParameter("currency", currency.name());
                sqlQuery.setParameter("strategy_id", strategy.getId());
                sqlQuery.executeUpdate();
            }
        }
    }
}
