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
package ch.algotrader.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;
import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.dao.HibernateInitializer;
import ch.algotrader.dao.SubscriptionDao;
import ch.algotrader.dao.marketData.BarDao;
import ch.algotrader.dao.marketData.TickDao;
import ch.algotrader.dao.security.SecurityDao;
import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.Combination;
import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.util.collection.CollectionUtil;
import ch.algotrader.visitor.InitializationVisitor;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@Transactional(propagation = Propagation.SUPPORTS)
public class ServerLookupServiceImpl implements ServerLookupService {

    private final Map<String, Long> securitySymbolMap = new ConcurrentHashMap<>();
    private final Map<String, Long> securityIsinMap = new ConcurrentHashMap<>();
    private final Map<String, Long> securityBbgidMap = new ConcurrentHashMap<>();
    private final Map<String, Long> securityRicMap = new ConcurrentHashMap<>();
    private final Map<String, Long> securityConidMap = new ConcurrentHashMap<>();
    private final Map<String, Long> securityIdMap = new ConcurrentHashMap<>();

    private final TickDao tickDao;

    private final SecurityDao securityDao;

    private final SubscriptionDao subscriptionDao;

    private final BarDao barDao;

    public ServerLookupServiceImpl(
            final TickDao tickDao,
            final SecurityDao securityDao,
            final SubscriptionDao subscriptionDao,
            final BarDao barDao) {

        Validate.notNull(tickDao, "TickDao is null");
        Validate.notNull(securityDao, "SecurityDao is null");
        Validate.notNull(subscriptionDao, "SubscriptionDao is null");
        Validate.notNull(barDao, "BarDao is null");

        this.tickDao = tickDao;
        this.securityDao = securityDao;
        this.subscriptionDao = subscriptionDao;
        this.barDao = barDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getSecurityIdBySecurityString(final String securityString) {

        Validate.notEmpty(securityString, "Security string is empty");

        // try to find it in the local hashMap cache by symbol, isin, bbgid, ric conid or id
        if (this.securitySymbolMap.containsKey(securityString)) {
            return this.securitySymbolMap.get(securityString);
        }

        if (this.securityIsinMap.containsKey(securityString)) {
            return this.securityIsinMap.get(securityString);
        }

        if (this.securityBbgidMap.containsKey(securityString)) {
            return this.securityBbgidMap.get(securityString);
        }

        if (this.securityRicMap.containsKey(securityString)) {
            return this.securityRicMap.get(securityString);
        }

        if (this.securityConidMap.containsKey(securityString)) {
            return this.securityConidMap.get(securityString);
        }

        if (this.securityIdMap.containsKey(securityString)) {
            return this.securityIdMap.get(securityString);
        }

        // try to find the security by symbol, isin, bbgid, ric conid or id
        Security security = this.securityDao.findBySymbol(securityString);
        if (security != null) {
            this.securitySymbolMap.put(security.getSymbol(), security.getId());
            return security.getId();
        }

        security = this.securityDao.findByIsin(securityString);
        if (security != null) {
            this.securityIsinMap.put(security.getIsin(), security.getId());
            return security.getId();
        }

        security = this.securityDao.findByBbgid(securityString);
        if (security != null) {
            this.securityBbgidMap.put(security.getBbgid(), security.getId());
            return security.getId();
        }

        security = this.securityDao.findByRic(securityString);
        if (security != null) {
            this.securityRicMap.put(security.getRic(), security.getId());
            return security.getId();
        }

        security = this.securityDao.findByConid(securityString);
        if (security != null) {
            this.securityConidMap.put(security.getConid(), security.getId());
            return security.getId();
        }

        if (NumberUtils.isDigits(securityString)) {

            security = this.securityDao.get(Long.parseLong(securityString));
            if (security != null) {
                this.securitySymbolMap.put(Long.toString(security.getId()), security.getId());
                return security.getId();
            }
        }

        throw new ServiceException("Security could not be found: " + securityString);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Subscription> getSubscriptionsByStrategyInclComponentsAndProps(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        List<Subscription> subscriptions = this.subscriptionDao.findByStrategyInclProps(strategyName);

        // initialize components
        for (Subscription subscription : subscriptions) {

            if (subscription.getSecurity() instanceof Combination) {
                Combination combination = (Combination) subscription.getSecurity();
                Hibernate.initialize(combination.getComponents());
            }
        }

        return subscriptions;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Tick> getSubscribedTicksByTimePeriod(final Date startDate, final Date endDate) {

        Validate.notNull(startDate, "Start date is null");
        Validate.notNull(endDate, "End date is null");

        List<Tick> ticks = this.tickDao.findSubscribedByTimePeriod(startDate, endDate);
        for (Tick tick : ticks) {
            tick.getSecurity().accept(InitializationVisitor.INSTANCE, HibernateInitializer.INSTANCE);
        }
        return ticks;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tick getFirstSubscribedTick() {

        return CollectionUtil.getFirstElementOrNull(this.tickDao.findSubscribedByTimePeriod(1, new Date(0), new Date(Long.MAX_VALUE)));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Bar> getSubscribedBarsByTimePeriodAndBarSize(final Date startDate, final Date endDate, final Duration barSize) {

        Validate.notNull(startDate, "Start date is null");
        Validate.notNull(endDate, "End date is null");
        Validate.notNull(barSize, "Bar size is null");

        List<Bar> bars = this.barDao.findSubscribedByTimePeriodAndBarSize(startDate, endDate, barSize);
        for (Bar bar : bars) {
            bar.getSecurity().accept(InitializationVisitor.INSTANCE, HibernateInitializer.INSTANCE);
        }
        return bars;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bar getFirstSubscribedBarByBarSize(final Duration barSize) {

        Validate.notNull(barSize, "Bar size is null");

        return CollectionUtil.getFirstElementOrNull(this.barDao.findSubscribedByTimePeriodAndBarSize(1, new Date(0), new Date(Long.MAX_VALUE), barSize));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initSecurityStrings() {

        for (Security security : this.securityDao.findSubscribedForAutoActivateStrategies()) {

            this.securityIdMap.put(Long.toString(security.getId()), security.getId());

            if (security.getSymbol() != null) {
                this.securitySymbolMap.put(security.getSymbol(), security.getId());
            }

            if (security.getIsin() != null) {
                this.securityIsinMap.put(security.getIsin(), security.getId());
            }

            if (security.getBbgid() != null) {
                this.securityBbgidMap.put(security.getBbgid(), security.getId());
            }

            if (security.getRic() != null) {
                this.securityRicMap.put(security.getRic(), security.getId());
            }

            if (security.getConid() != null) {
                this.securityConidMap.put(security.getConid(), security.getId());
            }
        }

    }


}
