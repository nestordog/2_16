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
package ch.algotrader.service.h2;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.PositionDao;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.TransactionDao;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.strategy.CashBalance;
import ch.algotrader.entity.strategy.CashBalanceDao;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.esper.Engine;
import ch.algotrader.service.PortfolioService;
import ch.algotrader.service.TransactionPersistenceServiceImpl;
import ch.algotrader.util.spring.HibernateSession;
import ch.algotrader.vo.CurrencyAmountVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@HibernateSession
public class H2TransactionPersistenceServiceImpl extends TransactionPersistenceServiceImpl {

    private final SessionFactory sessionFactory;

    private final PositionDao positionDao;

    private final CashBalanceDao cashBalanceDao;

    public H2TransactionPersistenceServiceImpl(final CommonConfig commonConfig,
            final PortfolioService portfolioService,
            final SessionFactory sessionFactory,
            final PositionDao positionDao,
            final TransactionDao transactionDao,
            final CashBalanceDao cashBalanceDao,
            final Engine serverEngine) {

        super(commonConfig, portfolioService, positionDao, transactionDao, cashBalanceDao, serverEngine);

        Validate.notNull(sessionFactory, "SessionFactory is null");
        Validate.notNull(positionDao, "PositionDao is null");
        Validate.notNull(cashBalanceDao, "CashBalanceDao is null");

        this.sessionFactory = sessionFactory;
        this.positionDao = positionDao;
        this.cashBalanceDao = cashBalanceDao;
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

            Position position = this.positionDao.findBySecurityAndStrategy(security.getId(), strategy.getName());
            if (position == null) {
                position = Position.Factory.newInstance(0, 0, 0, false, strategy, security);
                currentSession.save(position);
            }
        }

        if (!currencySet.isEmpty()) {

            for (Currency currency : currencySet) {

                CashBalance cashBalance = this.cashBalanceDao.findByStrategyAndCurrency(strategy, currency);
                if (cashBalance == null) {
                    cashBalance = CashBalance.Factory.newInstance(currency, new BigDecimal(0.0), strategy);
                    currentSession.save(cashBalance);
                }
            }
        }
    }
}
