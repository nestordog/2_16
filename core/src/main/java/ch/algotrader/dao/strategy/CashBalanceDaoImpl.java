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
package ch.algotrader.dao.strategy;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.LockOptions;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import ch.algotrader.dao.AbstractDao;
import ch.algotrader.dao.NamedParam;
import ch.algotrader.entity.strategy.CashBalance;
import ch.algotrader.entity.strategy.CashBalanceImpl;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.QueryType;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@Repository // Required for exception translation
public class CashBalanceDaoImpl extends AbstractDao<CashBalance> implements CashBalanceDao {

    public CashBalanceDaoImpl(final SessionFactory sessionFactory) {

        super(CashBalanceImpl.class, sessionFactory);
    }

    @Override
    public List<CashBalance> findCashBalancesByStrategy(String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        return findCaching("CashBalance.findCashBalancesByStrategy", QueryType.BY_NAME, new NamedParam("strategyName", strategyName));
    }

    @Override
    public CashBalance findByStrategyAndCurrency(Strategy strategy, Currency currency) {

        Validate.notNull(strategy, "Strategy is null");
        Validate.notNull(currency, "Currency is null");

        return findUniqueCaching("CashBalance.findByStrategyAndCurrency", QueryType.BY_NAME, new NamedParam("strategy", strategy), new NamedParam("currency", currency));
    }

    @Override
    public CashBalance findByStrategyAndCurrencyLocked(Strategy strategy, Currency currency) {

        Validate.notNull(strategy, "Strategy is null");
        Validate.notNull(currency, "Currency is null");

        return findUnique(LockOptions.UPGRADE, "CashBalance.findByStrategyAndCurrencyLocked", QueryType.BY_NAME, new NamedParam("strategy", strategy), new NamedParam("currency", currency));
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Currency> findHeldCurrencies() {

        return (List<Currency>) findObjects(null, "CashBalance.findHeldCurrencies", QueryType.BY_NAME);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Currency> findHeldCurrenciesByStrategy(String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        return (List<Currency>) findObjects(null, "CashBalance.findHeldCurrenciesByStrategy", QueryType.BY_NAME, new NamedParam("strategyName", strategyName));
    }

}
