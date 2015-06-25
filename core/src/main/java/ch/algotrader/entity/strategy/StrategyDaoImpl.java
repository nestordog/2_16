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
package ch.algotrader.entity.strategy;

import java.util.Date;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import ch.algotrader.enumeration.QueryType;
import ch.algotrader.hibernate.AbstractDao;
import ch.algotrader.hibernate.NamedParam;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@Repository // Required for exception translation
public class StrategyDaoImpl extends AbstractDao<Strategy> implements StrategyDao {

    public StrategyDaoImpl(final SessionFactory sessionFactory) {
        super(StrategyImpl.class, sessionFactory);
    }

    @Override
    public Strategy findServer() {

        return findUniqueCaching("Strategy.findServer", QueryType.BY_NAME);
    }

    @Override
    public Strategy findByName(final String name) {

        Validate.notEmpty(name, "Strategy name is empty");

        return findUniqueCaching("Strategy.findByName", QueryType.BY_NAME, new NamedParam("name", name));
    }

    @Override
    public Set<Strategy> findAutoActivateStrategies() {

        return findAsSetCaching("Strategy.findAutoActivateStrategies", QueryType.BY_NAME);
    }

    @Override
    public Date findCurrentDBTime() {

        return Date.class.cast(findUniqueObject(null, "Strategy.findCurrentDBTime", QueryType.BY_NAME));
    }

}
