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
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

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
public class PortfolioValueDaoImpl extends AbstractDao<PortfolioValue> implements PortfolioValueDao {

    public PortfolioValueDaoImpl(final SessionFactory sessionFactory) {

        super(PortfolioValueImpl.class, sessionFactory);
    }

    @Override
    public List<PortfolioValue> findByStrategyAndMinDate(String strategyName, Date minDate) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(minDate, "minDate is null");

        return findCaching("PortfolioValue.findByStrategyAndMinDate", QueryType.BY_NAME, new NamedParam("strategyName", strategyName), new NamedParam("minDate", minDate));
    }

    @Override
    public <V> List<V> findByStrategyAndMinDate(String strategyName, Date minDate, EntityConverter<PortfolioValue, V> converter) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(minDate, "minDate is null");
        Validate.notNull(converter, "Converter is null");

        return find(converter, "PortfolioValue.findByStrategyAndMinDate", QueryType.BY_NAME, new NamedParam("strategyName", strategyName), new NamedParam("minDate", minDate));
    }

}
