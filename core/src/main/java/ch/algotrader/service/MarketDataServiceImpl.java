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

import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.cache.CacheManager;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.CoreConfig;
import ch.algotrader.dao.SubscriptionDao;
import ch.algotrader.dao.marketData.TickDao;
import ch.algotrader.dao.security.SecurityDao;
import ch.algotrader.dao.strategy.StrategyDao;
import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.marketData.MarketDataEventVO;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityImpl;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.util.HibernateUtil;
import ch.algotrader.util.io.CsvTickWriter;
import ch.algotrader.visitor.TickValidationVisitor;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@Transactional(propagation = Propagation.SUPPORTS)
public class MarketDataServiceImpl implements MarketDataService, ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOGGER = LogManager.getLogger(MarketDataServiceImpl.class);

    private final Map<Security, CsvTickWriter> csvWriters = new HashMap<>();

    private final CommonConfig commonConfig;

    private final CoreConfig coreConfig;

    private final SessionFactory sessionFactory;

    private final TickDao tickDao;

    private final SecurityDao securityDao;

    private final StrategyDao strategyDao;

    private final SubscriptionDao subscriptionDao;

    private final EngineManager engineManager;

    private final EventDispatcher eventDispatcher;

    private final LocalLookupService localLookupService;

    private final CacheManager cacheManager;

    private final AtomicBoolean initialized;

    private final Map<FeedType, ExternalMarketDataService> externalMarketDataServiceMap;

    public MarketDataServiceImpl(final CommonConfig commonConfig,
            final CoreConfig coreConfig,
            final SessionFactory sessionFactory,
            final TickDao tickDao,
            final SecurityDao securityDao,
            final StrategyDao strategyDao,
            final SubscriptionDao subscriptionDao,
            final EngineManager engineManager,
            final EventDispatcher eventDispatcher,
            final LocalLookupService localLookupService,
            final CacheManager cacheManager) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(coreConfig, "CoreConfig is null");
        Validate.notNull(sessionFactory, "SessionFactory is null");
        Validate.notNull(tickDao, "TickDao is null");
        Validate.notNull(securityDao, "SecurityDao is null");
        Validate.notNull(strategyDao, "StrategyDao is null");
        Validate.notNull(subscriptionDao, "SubscriptionDao is null");
        Validate.notNull(engineManager, "EngineManager is null");
        Validate.notNull(eventDispatcher, "EventDispatcher is null");
        Validate.notNull(localLookupService, "LocalLookupService is null");
        Validate.notNull(cacheManager, "CacheManager is null");

        this.commonConfig = commonConfig;
        this.coreConfig = coreConfig;
        this.sessionFactory = sessionFactory;
        this.tickDao = tickDao;
        this.securityDao = securityDao;
        this.strategyDao = strategyDao;
        this.subscriptionDao = subscriptionDao;
        this.engineManager = engineManager;
        this.eventDispatcher = eventDispatcher;
        this.localLookupService = localLookupService;
        this.cacheManager = cacheManager;
        this.initialized = new AtomicBoolean(false);
        this.externalMarketDataServiceMap = new ConcurrentHashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void persistTick(final Tick tick) {

        Validate.notNull(tick, "Tick is null");

        // get the current Date rounded to MINUTES
        Date date = DateUtils.round(new Date(), Calendar.MINUTE);
        tick.setDateTime(date);

        try {
            saveCvs(tick);
        } catch (IOException ex) {
            throw new ServiceException(ex);
        }

        // write the tick to the DB (even if not valid)
        this.tickDao.save(tick);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initSubscriptions(final FeedType feedType) {

        Validate.notNull(feedType, "Feed type is null");

        ExternalMarketDataService externalMarketDataService = getExternalMarketDataService(feedType);
        if (externalMarketDataService.initSubscriptions()) {
            final Set<Security> securities = new LinkedHashSet<>();
            for (final Engine engine : this.engineManager.getEngines()) {
                securities.addAll(this.securityDao.findSubscribedByFeedTypeAndStrategyInclFamily(feedType, engine.getStrategyName()));
            }

            for (Security security : securities) {
                if (!security.getSecurityFamily().isSynthetic()) {
                    externalMarketDataService.subscribe(security);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void subscribe(final String strategyName, final long securityId) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        subscribe(strategyName, securityId, this.coreConfig.getDefaultFeedType());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void subscribe(final String strategyName, final long securityId, final FeedType feedType) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(feedType, "Feed type is null");

        eventDispatcher.registerMarketDataSubscription(strategyName, securityId);

        if (this.subscriptionDao.findByStrategySecurityAndFeedType(strategyName, securityId, feedType) == null) {

            Strategy strategy = this.strategyDao.findByName(strategyName);
            Security security = this.cacheManager.get(SecurityImpl.class, securityId);

            // only external subscribe if nobody was watching the specified security with the specified feedType so far
            if (!this.commonConfig.isSimulation()) {
                List<Subscription> subscriptions = this.subscriptionDao.findBySecurityAndFeedTypeForAutoActivateStrategies(securityId, feedType);
                if (subscriptions.size() == 0) {
                    if (!security.getSecurityFamily().isSynthetic()) {
                        getExternalMarketDataService(feedType).subscribe(security);
                    }
                }
            }

            // update links
            Subscription subscription = Subscription.Factory.newInstance(feedType, false, strategy, security);

            this.subscriptionDao.save(subscription);

            // reverse-associate security (after subscription has received an id)
            security.getSubscriptions().add(subscription);

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("subscribed security {} with {}", security, feedType);
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void unsubscribe(final String strategyName, final long securityId) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        unsubscribe(strategyName, securityId, this.coreConfig.getDefaultFeedType());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void unsubscribe(final String strategyName, final long securityId, final FeedType feedType) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(feedType, "Feed type is null");

        eventDispatcher.unregisterMarketDataSubscription(strategyName, securityId);

        Subscription subscription = this.subscriptionDao.findByStrategySecurityAndFeedType(strategyName, securityId, feedType);
        if (subscription != null && !subscription.isPersistent()) {

            Security security = this.cacheManager.get(SecurityImpl.class, securityId);

            // update links
            security.getSubscriptions().remove(subscription);

            this.subscriptionDao.delete(subscription);

            // only external unsubscribe if nobody is watching this security anymore
            if (!this.commonConfig.isSimulation()) {
                if (security.getSubscriptions().size() == 0) {
                    if (!security.getSecurityFamily().isSynthetic()) {
                        getExternalMarketDataService(feedType).unsubscribe(security);
                    }
                }
            }

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("unsubscribed security {} with {}", security, feedType);
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void removeNonPositionSubscriptions(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        Collection<Subscription> subscriptions = this.subscriptionDao.findNonPositionSubscriptions(strategyName);

        for (Subscription subscription : subscriptions) {
            unsubscribe(subscription.getStrategy().getName(), subscription.getSecurity().getId());
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void removeNonPositionSubscriptionsByType(final String strategyName, final Class type) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(type, "Type is null");

        int discriminator = HibernateUtil.getDisriminatorValue(this.sessionFactory, type);
        Collection<Subscription> subscriptions = this.subscriptionDao.findNonPositionSubscriptionsByType(strategyName, discriminator);

        for (Subscription subscription : subscriptions) {
            unsubscribe(subscription.getStrategy().getName(), subscription.getSecurity().getId());
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestCurrentTicks(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        Map<Long, MarketDataEventVO> currentMarketDataEvents = localLookupService.getCurrentMarketDataEvents();
        List<Subscription> subscriptions = this.subscriptionDao.findByStrategy(strategyName);
        for (Subscription subscription: subscriptions) {
            long securityId = subscription.getSecurity().getId();
            MarketDataEventVO marketDataEventVO = currentMarketDataEvents.get(securityId);
            if (marketDataEventVO != null) {
                this.eventDispatcher.sendEvent(strategyName, marketDataEventVO);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logTickGap(final long securityId) {

        Security security = this.cacheManager.get(SecurityImpl.class, securityId);

        LOGGER.error("{} has not received any ticks for {} minutes", security, security.getSecurityFamily().getMaxGap());

    }

    private void saveCvs(Tick tick) throws IOException {

        Security security = tick.getSecurity();

        CsvTickWriter csvWriter;
        synchronized (this.csvWriters) {
            csvWriter = this.csvWriters.get(security);
            if (csvWriter == null) {
                String fileName = security.getIsin() != null ? security.getIsin() : security.getSymbol() != null ? security.getSymbol() : String.valueOf(security.getId());
                csvWriter = new CsvTickWriter(fileName);
                this.csvWriters.put(security, csvWriter);
            }
        }

        synchronized (csvWriter) {
            csvWriter.write(tick);
        }
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {

        if (this.initialized.compareAndSet(false, true)) {

            ApplicationContext applicationContext = event.getApplicationContext();
            this.externalMarketDataServiceMap.clear();
            Map<String, ExternalMarketDataService> map = applicationContext.getBeansOfType(ExternalMarketDataService.class);
            for (Map.Entry<String, ExternalMarketDataService> entry: map.entrySet()) {

                ExternalMarketDataService externalMarketDataService = entry.getValue();
                this.externalMarketDataServiceMap.put(externalMarketDataService.getFeedType(), externalMarketDataService);
            }
        }
    }

    private ExternalMarketDataService getExternalMarketDataService(final FeedType feedType) {

        Validate.notNull(feedType, "FeedType is null");

        ExternalMarketDataService externalMarketDataService = this.externalMarketDataServiceMap.get(feedType);
        if (externalMarketDataService == null) {
            throw new ServiceException("No ExternalMarketDataService found for feed type " + feedType);
        }
        return externalMarketDataService;
    }

    @Override
    public Set<FeedType> getSupportedFeeds() {
        return new HashSet<>(this.externalMarketDataServiceMap.keySet());
    }

    @Override
    public boolean isSupportedFeed(FeedType feedType) {

        return this.externalMarketDataServiceMap.containsKey(feedType);
    }

    @Override
    public boolean isTickValid(final TickVO tick) {

        if (tick == null) {
            return false;
        }
        Security security = this.cacheManager.get(SecurityImpl.class, tick.getSecurityId());
        if (security == null) {
            return false;
        }
        return security.accept(TickValidationVisitor.INSTANCE, tick);
    }

}
