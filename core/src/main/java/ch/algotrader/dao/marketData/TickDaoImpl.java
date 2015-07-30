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

package ch.algotrader.dao.marketData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;

import ch.algotrader.dao.NamedParam;
import ch.algotrader.dao.SubscriptionDao;
import ch.algotrader.dao.security.SecurityDao;
import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.marketData.TickImpl;
import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.QueryType;
import ch.algotrader.esper.Engine;
import ch.algotrader.hibernate.AbstractDao;
import ch.algotrader.util.collection.Pair;
import ch.algotrader.visitor.TickValidationVisitor;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@Repository // Required for exception translation
public class TickDaoImpl extends AbstractDao<Tick> implements TickDao {

    private final SubscriptionDao subscriptionDao;

    private final SecurityDao securityDao;

    private final Engine serverEngine;

    public TickDaoImpl(final SessionFactory sessionFactory, final SubscriptionDao subscriptionDao, final SecurityDao securityDao, final Engine serverEngine) {

        super(TickImpl.class, sessionFactory);

        Validate.notNull(subscriptionDao);
        Validate.notNull(securityDao);
        Validate.notNull(serverEngine, "Engine is null");

        this.subscriptionDao = subscriptionDao;
        this.securityDao = securityDao;
        this.serverEngine = serverEngine;
    }

    @Override
    public List<Tick> findBySecurity(long securityId) {

        return findCaching("Tick.findBySecurity", QueryType.BY_NAME, new NamedParam("securityId", securityId));
    }

    @Override
    public Tick findBySecurityAndMaxDate(long securityId, Date maxDate) {

        Validate.notNull(maxDate, "maxDate is null");

        return findUniqueCaching("Tick.findBySecurityAndMaxDate", QueryType.BY_NAME, new NamedParam("securityId", securityId), new NamedParam("maxDate", maxDate));
    }

    @Override
    public List<Tick> findTicksBySecurityAndMinDate(long securityId, Date minDate, int intervalDays) {

        Validate.notNull(minDate, "minDate is null");

        return find("Tick.findTicksBySecurityAndMinDate", QueryType.BY_NAME, new NamedParam("securityId", securityId), new NamedParam("minDate", minDate), new NamedParam("intervalDays", intervalDays));
    }

    @Override
    public List<Tick> findTicksBySecurityAndMinDate(int limit, long securityId, Date minDate, int intervalDays) {

        Validate.notNull(minDate, "minDate is null");

        return find("Tick.findTicksBySecurityAndMinDate", limit, QueryType.BY_NAME, new NamedParam("securityId", securityId), new NamedParam("minDate", minDate), new NamedParam("intervalDays",
                intervalDays));
    }

    @Override
    public List<Tick> findTicksBySecurityAndMaxDate(long securityId, Date maxDate, int intervalDays) {

        Validate.notNull(maxDate, "maxDate is null");

        return find("Tick.findTicksBySecurityAndMaxDate", QueryType.BY_NAME, new NamedParam("securityId", securityId), new NamedParam("maxDate", maxDate), new NamedParam("intervalDays", intervalDays));
    }

    @Override
    public List<Tick> findTicksBySecurityAndMaxDate(int limit, long securityId, Date maxDate, int intervalDays) {

        Validate.notNull(maxDate, "maxDate is null");

        return find("Tick.findTicksBySecurityAndMaxDate", limit, QueryType.BY_NAME, new NamedParam("securityId", securityId), new NamedParam("maxDate", maxDate), new NamedParam("intervalDays",
                intervalDays));
    }

    @Override
    public List<Long> findDailyTickIdsBeforeTime(long securityId, Date time) {

        Validate.notNull(time, "Time is null");

        return convertIds(findObjects(null, "Tick.findDailyTickIdsBeforeTime", QueryType.BY_NAME, new NamedParam("securityId", securityId), new NamedParam("time", time)));
    }

    @Override
    public List<Long> findDailyTickIdsAfterTime(long securityId, Date time) {

        Validate.notNull(time, "Time is null");

        return convertIds(findObjects(null, "Tick.findDailyTickIdsAfterTime", QueryType.BY_NAME, new NamedParam("securityId", securityId), new NamedParam("time", time)));
    }

    @Override
    public List<Long> findHourlyTickIdsBeforeMinutesByMinDate(long securityId, int minutes, Date minDate) {

        Validate.notNull(minDate, "minDate is null");

        return convertIds((findObjects(null, "Tick.findHourlyTickIdsBeforeMinutesByMinDate", QueryType.BY_NAME, new NamedParam("securityId", securityId), new NamedParam("minutes", minutes),
                new NamedParam("minDate", minDate))));
    }

    @Override
    public List<Long> findHourlyTickIdsAfterMinutesByMinDate(long securityId, int minutes, Date minDate) {

        Validate.notNull(minDate, "minDate is null");

        return convertIds(findObjects(null, "Tick.findHourlyTickIdsAfterMinutesByMinDate", QueryType.BY_NAME, new NamedParam("securityId", securityId), new NamedParam("minutes", minutes),
                new NamedParam("minDate", minDate)));
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Tick> findByIdsInclSecurityAndUnderlying(List<Long> ids) {

        Validate.notEmpty(ids, "Ids are empty");

        Query query = this.prepareQuery(null, "Tick.findByIdsInclSecurityAndUnderlying", QueryType.BY_NAME);
        query.setParameterList("ids", ids, LongType.INSTANCE);

        return query.list();
    }

    @Override
    public List<Tick> findSubscribedByTimePeriod(Date minDate, Date maxDate) {

        Validate.notNull(minDate, "minDate is null");
        Validate.notNull(maxDate, "maxDate is null");

        return find("Tick.findSubscribedByTimePeriod", QueryType.BY_NAME, new NamedParam("minDate", minDate), new NamedParam("maxDate", maxDate));
    }

    @Override
    public List<Tick> findSubscribedByTimePeriod(int limit, Date minDate, Date maxDate) {

        Validate.notNull(minDate, "minDate is null");
        Validate.notNull(maxDate, "maxDate is null");

        return find("Tick.findSubscribedByTimePeriod", limit, QueryType.BY_NAME, new NamedParam("minDate", minDate), new NamedParam("maxDate", maxDate));
    }

    @SuppressWarnings("unchecked")
    @Override
    public String findTickerIdBySecurity(long securityId) {

        // sometimes Esper returns a Map instead of scalar
        String query = "select tickerId from TickWindow where security.id = " + securityId;
        Object obj = this.serverEngine.executeSingelObjectQuery(query);
        if (obj instanceof Map) {
            return ((Map<String, String>) obj).get("tickerId");
        } else {
            return (String) obj;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Tick> findCurrentTicksByStrategy(String strategyName) {

        Validate.notNull(strategyName, "Strategy name is null");

        List<Subscription> subscriptions = this.subscriptionDao.findByStrategy(strategyName);

        List<Tick> ticks = new ArrayList<>();
        for (Subscription subscription : subscriptions) {
            String query = "select * from TickWindow where security.id = " + subscription.getSecurity().getId();
            Pair<Tick, Object> pair = (Pair<Tick, Object>) this.serverEngine.executeSingelObjectQuery(query);
            if (pair != null) {

                Tick tick = pair.getFirst();
                tick.setDateTime(new Date());

                // refresh the security (associated entities might have been modified
                Security security = this.securityDao.findByIdInitialized(tick.getSecurity().getId());
                tick.setSecurity(security);

                if (security.accept(TickValidationVisitor.INSTANCE, tick)) {
                    ticks.add(tick);
                }
            }
        }

        return ticks;
    }

}
