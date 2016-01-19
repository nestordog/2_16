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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.CoreConfig;
import ch.algotrader.dao.SubscriptionDao;
import ch.algotrader.dao.security.ForexDao;
import ch.algotrader.dao.security.SecurityDao;
import ch.algotrader.dao.strategy.StrategyDao;
import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.marketData.MarketDataEventVO;
import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.event.dispatch.EventRecipient;
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

    private final SecurityDao securityDao;

    private final StrategyDao strategyDao;

    private final SubscriptionDao subscriptionDao;

    private final ForexDao forexDao;

    private final EngineManager engineManager;

    private final EventDispatcher eventDispatcher;

    private final MarketDataCache marketDataCache;

    private final Map<String, ExternalMarketDataService> externalMarketDataServiceMap;

    private final Boolean normaliseMarketData;

    public MarketDataServiceImpl(final CommonConfig commonConfig,
            final CoreConfig coreConfig,
            final ConfigParams configParams,
            final SecurityDao securityDao,
            final StrategyDao strategyDao,
            final SubscriptionDao subscriptionDao,
            final ForexDao forexDao,
            final EngineManager engineManager,
            final EventDispatcher eventDispatcher,
            final MarketDataCache marketDataCache,
            final Map<String, ExternalMarketDataService> externalMarketDataServiceMap) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(coreConfig, "CoreConfig is null");
        Validate.notNull(configParams, "ConfigParams is null");
        Validate.notNull(securityDao, "SecurityDao is null");
        Validate.notNull(strategyDao, "StrategyDao is null");
        Validate.notNull(subscriptionDao, "SubscriptionDao is null");
        Validate.notNull(forexDao, "ForexDao is null");
        Validate.notNull(engineManager, "EngineManager is null");
        Validate.notNull(eventDispatcher, "EventDispatcher is null");
        Validate.notNull(marketDataCache, "MarketDataCache is null");
        Validate.notNull(externalMarketDataServiceMap, "Map<String, ExternalMarketDataService> is null");

        this.commonConfig = commonConfig;
        this.coreConfig = coreConfig;
        this.securityDao = securityDao;
        this.strategyDao = strategyDao;
        this.subscriptionDao = subscriptionDao;
        this.forexDao = forexDao;
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

    private void subscribe(final Strategy strategy, final Security security, final String feedType) {

        this.eventDispatcher.registerMarketDataSubscription(strategy.getName(), security.getId());

        final MarketDataSubscriptionVO event = new MarketDataSubscriptionVO(strategy.getName(), security.getId(), feedType, true);
        this.eventDispatcher.broadcast(event, EventRecipient.ALL_STRATEGIES);

        if (this.subscriptionDao.findByStrategySecurityAndFeedType(strategy.getName(), security.getId(), feedType) == null) {

            // only external subscribe if nobody was watching the specified security with the specified feedType so far
            if (!this.commonConfig.isSimulation()) {
                List<Subscription> subscriptions = this.subscriptionDao.findBySecurityAndFeedTypeForAutoActivateStrategies(security.getId(), feedType);
                if (subscriptions.size() == 0) {
                    if (!security.getSecurityFamily().isSynthetic()) {
                        getExternalMarketDataService(feedType).subscribe(security);
                    }
                }
            }

            // update links
            Subscription subscription = Subscription.Factory.newInstance(feedType, false, strategy, security);

            this.subscriptionDao.save(subscription);

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
    public void subscribe(final String strategyName, final long securityId, final String feedType) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(feedType, "Feed type is null");

        Strategy strategy = this.strategyDao.findByName(strategyName);
        if (strategy == null) {
            throw new ServiceException("Unknown strategy: " + strategyName);
        }
        Security security = this.securityDao.get(securityId);
        if (security == null) {
            throw new ServiceException("Unknown security: " + securityId);
        }

        subscribe(strategy, security, feedType);

        if (!(security instanceof Forex)) {
            Currency transactionCurrency = security.getSecurityFamily().getCurrency();
            Currency baseCurrency = this.commonConfig.getPortfolioBaseCurrency();
            if (!transactionCurrency.equals(baseCurrency)) {
                Forex forex = this.forexDao.getForex(baseCurrency, transactionCurrency);
                if (forex != null) {
                    subscribe(strategy, forex, this.coreConfig.getDefaultFeedType());
                }
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

    public void unsubscribe(final Strategy strategy, final Security security, final String feedType) {

        this.eventDispatcher.unregisterMarketDataSubscription(strategy.getName(), security.getId());

        final MarketDataSubscriptionVO event = new MarketDataSubscriptionVO(strategy.getName(), security.getId(), feedType, false);
        this.eventDispatcher.broadcast(event, EventRecipient.ALL_STRATEGIES);

        Subscription subscription = this.subscriptionDao.findByStrategySecurityAndFeedType(strategy.getName(), security.getId(), feedType);
        if (subscription != null && !subscription.isPersistent()) {

            this.subscriptionDao.delete(subscription);

            // only external unsubscribe if nobody is watching this security anymore
            if (!this.commonConfig.isSimulation()) {
                List<Subscription> subscriptions = this.subscriptionDao.findBySecurity(security.getId());
                if (subscriptions.isEmpty()) {
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
    public void unsubscribe(final String strategyName, final long securityId, final String feedType) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(feedType, "Feed type is null");

        Strategy strategy = this.strategyDao.findByName(strategyName);
        if (strategy == null) {
            throw new ServiceException("Unknown strategy: " + strategyName);
        }
        Security security = this.securityDao.get(securityId);
        if (security == null) {
            throw new ServiceException("Unknown security: " + securityId);
        }
        unsubscribe(strategy, security, feedType);

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
    public void removeNonPositionSubscriptionsByType(final String strategyName, final Class<? extends Security> type) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(type, "Type is null");

        Collection<Subscription> subscriptions = this.subscriptionDao.findNonPositionSubscriptionsByType(strategyName, type);

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
