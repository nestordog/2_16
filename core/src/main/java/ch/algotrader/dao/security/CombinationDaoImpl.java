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

package ch.algotrader.dao.security;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import ch.algotrader.dao.AbstractDao;
import ch.algotrader.dao.NamedParam;
import ch.algotrader.entity.security.Combination;
import ch.algotrader.entity.security.CombinationImpl;
import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.QueryType;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
@Repository // Required for exception translation
public class CombinationDaoImpl extends AbstractDao<Combination> implements CombinationDao {

    public CombinationDaoImpl(final SessionFactory sessionFactory) {

        super(CombinationImpl.class, sessionFactory);
    }

    @Override
    public List<Combination> findSubscribedByStrategy(String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        return findCaching("Combination.findSubscribedByStrategy", QueryType.BY_NAME, new NamedParam("strategyName", strategyName));
    }

    @Override
    public List<Combination> findSubscribedByStrategyAndUnderlying(String strategyName, long underlyingId) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        return findCaching("Combination.findSubscribedByStrategyAndUnderlying", QueryType.BY_NAME, new NamedParam("strategyName", strategyName), new NamedParam("underlyingId", underlyingId));
    }

    @Override
    public List<Combination> findSubscribedByStrategyAndComponent(String strategyName, long securityId) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        return findCaching("Combination.findSubscribedByStrategyAndComponent", QueryType.BY_NAME, new NamedParam("strategyName", strategyName), new NamedParam("securityId", securityId));
    }

    @Override
    public List<Combination> findSubscribedByStrategyAndComponentType(String strategyName, Class<? extends Security> type) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        return findCaching("Combination.findSubscribedByStrategyAndComponentType", QueryType.BY_NAME, new NamedParam("strategyName", strategyName), new NamedParam("type", type.getSimpleName()));
    }

    @Override
    public List<Combination> findSubscribedByStrategyAndComponentTypeWithZeroQty(String strategyName, Class<? extends Security> type) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        return findCaching("Combination.findSubscribedByStrategyAndComponentTypeWithZeroQty", QueryType.BY_NAME, new NamedParam("strategyName", strategyName), new NamedParam("type", type.getSimpleName()));
    }

    @Override
    public List<Combination> findNonPersistent() {

        return findCaching("Combination.findNonPersistent", QueryType.BY_NAME);
    }

}
