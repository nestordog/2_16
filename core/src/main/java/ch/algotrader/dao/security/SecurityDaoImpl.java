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
package ch.algotrader.dao.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import ch.algotrader.dao.AbstractDao;
import ch.algotrader.dao.HibernateInitializer;
import ch.algotrader.dao.NamedParam;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityImpl;
import ch.algotrader.enumeration.QueryType;
import ch.algotrader.visitor.InitializationVisitor;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@Repository // Required for exception translation
public class SecurityDaoImpl extends AbstractDao<Security> implements SecurityDao {

    public SecurityDaoImpl(final SessionFactory sessionFactory) {

        super(SecurityImpl.class, sessionFactory);
    }

    @Override
    public Security findByIdInitialized(long id) {

        Security security = get(id);

        // initialize the security
        if (security != null) {
            security.accept(InitializationVisitor.INSTANCE, HibernateInitializer.INSTANCE);
        }

        return security;
    }

    @Override
    public List<Security> findByIds(Collection<Long> ids) {

        Validate.notEmpty(ids, "Ids are empty");

        return this.find("Security.findByIds", QueryType.BY_NAME, new NamedParam("ids", ids));
    }

    @Override
    public Security findBySymbol(String symbol) {

        Validate.notEmpty(symbol, "Symbol is empty");

        return findUniqueCaching("Security.findBySymbol", QueryType.BY_NAME, new NamedParam("symbol", symbol));
    }

    @Override
    public Security findByIsin(String isin) {

        Validate.notEmpty(isin, "isin is empty");

        return findUniqueCaching("Security.findByIsin", QueryType.BY_NAME, new NamedParam("isin", isin));
    }

    @Override
    public Security findByBbgid(String bbgid) {

        Validate.notEmpty(bbgid, "bbgid is empty");

        return findUniqueCaching("Security.findByBbgid", QueryType.BY_NAME, new NamedParam("bbgid", bbgid));
    }

    @Override
    public Security findByRic(String ric) {

        Validate.notEmpty(ric, "ric is empty");

        return findUniqueCaching("Security.findByRic", QueryType.BY_NAME, new NamedParam("ric", ric));
    }

    @Override
    public Security findByConid(String conid) {

        Validate.notEmpty(conid, "conid is empty");

        return findUniqueCaching("Security.findByConid", QueryType.BY_NAME, new NamedParam("conid", conid));
    }

    @Override
    public Security findByTtid(String ttid) {

        Validate.notEmpty(ttid, "ttid is empty");

        return findUniqueCaching("Security.findByTtid", QueryType.BY_NAME, new NamedParam("ttid", ttid));
    }

    @Override
    public Security findByIdInclFamilyAndUnderlying(long id) {

        return findUniqueCaching("Security.findByIdInclFamilyAndUnderlying", QueryType.BY_NAME, new NamedParam("id", id));
    }

    @Override
    public Security findByIdInclFamilyUnderlyingExchangeAndBrokerParameters(long id) {

        return findUnique("Security.findByIdInclFamilyUnderlyingExchangeAndBrokerParameters", QueryType.BY_NAME, new NamedParam("id", id));
    }

    @Override
    public List<Security> findSubscribedForAutoActivateStrategies() {

        return findCaching("Security.findSubscribedForAutoActivateStrategies", QueryType.BY_NAME);
    }

    @Override
    public List<Security> findSubscribedByFeedTypeForAutoActivateStrategiesInclFamily(String feedType) {

        Validate.notNull(feedType, "Feed type is null");

        return findCaching("Security.findSubscribedByFeedTypeForAutoActivateStrategiesInclFamily", QueryType.BY_NAME, new NamedParam("feedType", feedType));
    }

    @Override
    public List<Security> findSubscribedByFeedTypeAndStrategyInclFamily(String feedType, String strategyName) {

        Validate.notNull(feedType, "Feed type is null");
        Validate.notEmpty(strategyName, "Strategy name is empty");

        return findCaching("Security.findSubscribedByFeedTypeAndStrategyInclFamily", QueryType.BY_NAME, new NamedParam("feedType", feedType), new NamedParam("strategyName", strategyName));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> findSubscribedAndFeedTypeForAutoActivateStrategies() {

        return (List<Map<String, Object>>) findObjects(null, "Security.findSubscribedAndFeedTypeForAutoActivateStrategies", QueryType.BY_NAME);
    }

    @Override
    public Long findSecurityIdByIsin(String isin) {

        Validate.notEmpty(isin, "isin is empty");

        return convertId(findUniqueObject(null, "Security.findSecurityIdByIsin", QueryType.BY_NAME, new NamedParam("isin", isin)));
    }

}
