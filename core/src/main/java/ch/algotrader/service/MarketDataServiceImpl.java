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
package ch.algotrader.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ch.algotrader.config.ConfigParams;
import ch.algotrader.entity.security.SecurityFamily;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.event.dispatch.EventRecipient;
import ch.algotrader.util.HibernateUtil;
import ch.algotrader.visitor.TickValidationVisitor;
import ch.algotrader.vo.marketData.MarketDataSubscriptionVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
@Transactional(propagation = Propagation.SUPPORTS)
public class MarketDataServiceImpl implements MarketDataService {

    private static final Logger LOGGER = LogManager.getLogger(MarketDataServiceImpl.class);

    private final CommonConfig commonConfig;

    private final CoreConfig coreConfig;

    private final SessionFactory sessionFactory;

    private final TickDao tickDao;

    private final SecurityDao securityDao;

    private final StrategyDao strategyDao;

    private final SubscriptionDao subscriptionDao;

    private final EngineManager engineManager;

    private final EventDispatcher eventDispatcher;

    private final MarketDataCache marketDataCache;

    private final Map<String, ExternalMarketDataService> externalMarketDataServiceMap;

    private final Boolean normaliseMarketData;

    public MarketDataServiceImpl(final CommonConfig commonConfig,
            final CoreConfig coreConfig,
            final ConfigParams configParams,
            final SessionFactory sessionFactory,
            final TickDao tickDao,
            final SecurityDao securityDao,
            final StrategyDao strategyDao,
            final SubscriptionDao subscriptionDao,
            final EngineManager engineManager,
            final EventDispatcher eventDispatcher,
            final MarketDataCache marketDataCache,
            final Map<String, ExternalMarketDataService> externalMarketDataServiceMap) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(coreConfig, "CoreConfig is null");
        Validate.notNull(configParams, "ConfigParams is null");
        Validate.notNull(sessionFactory, "SessionFactory is null");
        Validate.notNull(tickDao, "TickDao is null");
        Validate.notNull(securityDao, "SecurityDao is null");
        Validate.notNull(strategyDao, "StrategyDao is null");
        Validate.notNull(subscriptionDao, "SubscriptionDao is null");
        Validate.notNull(engineManager, "EngineManager is null");
        Validate.notNull(eventDispatcher, "EventDispatcher is null");
        Validate.notNull(marketDataCache, "MarketDataCache is null");
        Validate.notNull(externalMarketDataServiceMap, "Map<String, ExternalMarketDataService> is null");

        this.commonConfig = commonConfig;
        this.coreConfig = coreConfig;
        this.sessionFactory = sessionFactory;
        this.tickDao = tickDao;
        this.securityDao = securityDao;
        this.strategyDao = strategyDao;
        this.subscriptionDao = subscriptionDao;
        this.engineManager = engineManager;
        this.eventDispatcher = eventDispatcher;
        this.marketDataCache = marketDataCache;
        this.externalMarketDataServiceMap = new ConcurrentHashMap<>(externalMarketDataServiceMap);
        this.normaliseMarketData = configParams.getBoolean("misc.normaliseMarketData", false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void persistTick(final Tick tick) {

        Validate.notNull(tick, "Tick is null");
        // write the tick to the DB (even if not valid)
        this.tickDao.save(tick);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initSubscriptions(final String feedType) {

        Validate.notNull(feedType, "Feed type is null");

        ExternalMarketDataService externalMarketDataService = getExternalMarketDataService(feedType);
        if (externalMarketDataService.initSubscriptionReady()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Initializing subscriptions for data feed {}", feedType);
            }
            Set<Security> securities = new LinkedHashSet<>();
            if (this.commonConfig.isEmbedded()) {
                for (Engine engine : this.engineManager.getEngines()) {
                    String strategyName = engine.getStrategyName();
                    List<Security> strategySubscribed = this.securityDao.findSubscribedByFeedTypeAndStrategyInclFamily(feedType, strategyName);
                    for (Security security : strategySubscribed) {

                        this.eventDispatcher.registerMarketDataSubscription(strategyName, security.getId());
                        securities.add(security);
                    }
                }
            } else {
                Engine serverEngine = this.engineManager.getServerEngine();
                String strategyName = serverEngine.getStrategyName();
                List<Security> serverSubscribed = this.securityDao.findSubscribedByFeedTypeAndStrategyInclFamily(feedType, strategyName);
                for (Security security : serverSubscribed) {

                    this.eventDispatcher.registerMarketDataSubscription(strategyName, security.getId());

                    final MarketDataSubscriptionVO event = new MarketDataSubscriptionVO(strategyName, security.getId(), feedType, true);
                    this.eventDispatcher.broadcast(event, EventRecipient.ALL_STRATEGIES);

                    securities.add(security);
                }
                securities.addAll(securityDao.findSubscribedByFeedTypeForAutoActivateStrategiesInclFamily(feedType));
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
    public void subscribe(final String strategyName, final long securityId, final String feedType) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(feedType, "Feed type is null");

        this.eventDispatcher.registerMarketDataSubscription(strategyName, securityId);

        final MarketDataSubscriptionVO event = new MarketDataSubscriptionVO(strategyName, securityId, feedType, true);
        this.eventDispatcher.broadcast(event, EventRecipient.ALL_STRATEGIES);

        if (this.subscriptionDao.findByStrategySecurityAndFeedType(strategyName, securityId, feedType) == null) {

            Strategy strategy = this.strategyDao.findByName(strategyName);
            if (strategy == null) {
                throw new ServiceException("Unknown strategy: " + strategyName);
            }
            Security security = this.securityDao.get(securityId);
            if (security == null) {
                throw new ServiceException("Unknown security: " + securityId);
            }

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
    public void unsubscribe(final String strategyName, final long securityId, final String feedType) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(feedType, "Feed type is null");

        this.eventDispatcher.unregisterMarketDataSubscription(strategyName, securityId);

        final MarketDataSubscriptionVO event = new MarketDataSubscriptionVO(strategyName, securityId, feedType, false);
        this.eventDispatcher.broadcast(event, EventRecipient.ALL_STRATEGIES);

        Subscription subscription = this.subscriptionDao.findByStrategySecurityAndFeedType(strategyName, securityId, feedType);
        if (subscription != null && !subscription.isPersistent()) {

            Security security = this.securityDao.get(securityId);

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
    public void removeNonPositionSubscriptionsByType(final String strategyName, final Class<?> type) {

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

        Map<Long, MarketDataEventVO> currentMarketDataEvents = this.marketDataCache.getCurrentMarketDataEvents();
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

        Security security = this.securityDao.get(securityId);

        LOGGER.error("{} has not received any ticks for {} minutes", security, security.getSecurityFamily().getMaxGap());

    }

    private ExternalMarketDataService getExternalMarketDataService(final String feedType) {

        Validate.notNull(feedType, "String is null");

        ExternalMarketDataService externalMarketDataService = this.externalMarketDataServiceMap.get(feedType);
        if (externalMarketDataService == null) {
            throw new ServiceException("No external market data service found for feed type " + feedType);
        }
        return externalMarketDataService;
    }

    @Override
    public Set<String> getSupportedFeeds() {
        return new HashSet<>(this.externalMarketDataServiceMap.keySet());
    }

    @Override
    public boolean isSupportedFeed(String feedType) {

        return this.externalMarketDataServiceMap.containsKey(feedType);
    }

    @Override
    public boolean isTickValid(final TickVO tick) {

        if (tick == null) {
            return false;
        }
        Security security = this.securityDao.get(tick.getSecurityId());
        if (security == null) {
            return false;
        }
        return security.accept(TickValidationVisitor.INSTANCE, tick);
    }

    @Override
    public TickVO normaliseTick(TickVO tick) {
        if(normaliseMarketData && tick != null){

            Security security = this.securityDao.get(tick.getSecurityId());
            if(security != null){
                SecurityFamily securityFamily = security.getSecurityFamily();
                BigDecimal multiplier = BigDecimal.valueOf(securityFamily.getPriceMultiplier(tick.getFeedType()));

                BigDecimal last = tick.getLast() != null ? tick.getLast().multiply(multiplier) : tick.getLast();
                BigDecimal bid = tick.getBid() != null ? tick.getBid().multiply(multiplier) : tick.getBid();
                BigDecimal ask = tick.getAsk() != null ? tick.getAsk().multiply(multiplier) : tick.getAsk();

                return new TickVO(tick.getId(),
                    tick.getDateTime(),
                    tick.getFeedType(),
                    tick.getSecurityId(),
                    last,
                    tick.getLastDateTime(),
                    bid,
                    ask,
                    tick.getVolBid(),
                    tick.getVolAsk(),
                    tick.getVol());
            }
        }
        return tick;
    }
}
