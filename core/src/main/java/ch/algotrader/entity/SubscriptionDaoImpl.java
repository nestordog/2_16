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
package ch.algotrader.entity;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import ch.algotrader.enumeration.FeedType;
import ch.algotrader.enumeration.QueryType;
import ch.algotrader.hibernate.AbstractDao;
import ch.algotrader.hibernate.NamedParam;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@Repository // Required for exception translation
public class SubscriptionDaoImpl extends AbstractDao<Subscription> implements SubscriptionDao {

    public SubscriptionDaoImpl(final SessionFactory sessionFactory) {

        super(SubscriptionImpl.class, sessionFactory);
    }

    @Override
    public List<Subscription> findByStrategy(String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        return find("Subscription.findByStrategy", QueryType.BY_NAME, new NamedParam("strategyName", strategyName));
    }

    @Override
    public List<Subscription> findByStrategyInclProps(String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        return find("Subscription.findByStrategyInclProps", QueryType.BY_NAME, new NamedParam("strategyName", strategyName));
    }

    @Override
    public Subscription findByStrategyAndSecurity(String strategyName, long securityId) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        return findUnique("Subscription.findByStrategyAndSecurity", QueryType.BY_NAME, new NamedParam("strategyName", strategyName), new NamedParam("securityId", securityId));
    }

    @Override
    public Subscription findByStrategySecurityAndFeedType(String strategyName, long securityId, FeedType feedType) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(feedType, "FeedType is null");

        return findUnique("Subscription.findByStrategySecurityAndFeedType", QueryType.BY_NAME, new NamedParam("strategyName", strategyName), new NamedParam("securityId", securityId), new NamedParam(
                "feedType", feedType));
    }

    @Override
    public List<Subscription> findBySecurityAndFeedTypeForAutoActivateStrategies(long securityId, FeedType feedType) {

        Validate.notNull(feedType, "FeedType is null");

        return find("Subscription.findBySecurityAndFeedTypeForAutoActivateStrategies", QueryType.BY_NAME, new NamedParam("securityId", securityId), new NamedParam("feedType", feedType));
    }

    @Override
    public List<Subscription> findNonPersistent() {

        return find("Subscription.findNonPersistent", QueryType.BY_NAME);
    }

    @Override
    public List<Subscription> findNonPositionSubscriptions(String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        return find("Subscription.findNonPositionSubscriptions", QueryType.BY_NAME, new NamedParam("strategyName", strategyName));
    }

    @Override
    public List<Subscription> findNonPositionSubscriptionsByType(String strategyName, int type) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        return find("Subscription.findNonPositionSubscriptionsByType", QueryType.BY_NAME, new NamedParam("strategyName", strategyName), new NamedParam("type", type));
    }

}
