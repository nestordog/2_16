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
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataIntegrityViolationException;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.marketData.MarketDataEvent;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.util.HibernateUtil;
import ch.algotrader.util.MyLogger;
import ch.algotrader.util.io.CsvTickWriter;
import ch.algotrader.util.metric.MetricsUtil;
import ch.algotrader.vo.GenericEventVO;

import com.espertech.esper.collection.Pair;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class MarketDataServiceImpl extends MarketDataServiceBase implements ApplicationContextAware {

    private static Logger logger = MyLogger.getLogger(MarketDataServiceImpl.class.getName());

    private @Value("${simulation}") boolean simulation;
    private @Value("#{T(ch.algotrader.enumeration.FeedType).fromString('${misc.defaultFeedType}')}") FeedType feedType;

    private Map<Security, CsvTickWriter> csvWriters = new HashMap<Security, CsvTickWriter>();
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    protected void handlePersistTick(Tick tick) throws IOException {

        // get the current Date rounded to MINUTES
        Date date = DateUtils.round(new Date(), Calendar.MINUTE);
        tick.setDateTime(date);

        saveCvs(tick);

        // write the tick to the DB (even if not valid)
        getTickDao().create(tick);
    }

    @Override
    protected void handleInitSubscriptions(FeedType feedType) throws Exception  {

        getExternalMarketDataService(feedType).initSubscriptions();
    }

    @Override
    protected void handleSubscribe(String strategyName, int securityId) throws Exception {

        subscribe(strategyName, securityId, this.feedType);
    }

    /**
     * synchronized due to potential mysql innodb deadlocks on concurrent inserts
     * see http://thushw.blogspot.ch/2010/11/mysql-deadlocks-with-concurrent-inserts.html
     */
    @Override
    protected synchronized void handleSubscribe(String strategyName, int securityId, FeedType feedType) throws Exception {

        if (getSubscriptionDao().findByStrategySecurityAndFeedType(strategyName, securityId, feedType) == null) {

            Strategy strategy = getStrategyDao().findByName(strategyName);
            Security security = getSecurityDao().findByIdInclFamilyAndUnderlying(securityId);

            // only external subscribe if nobody was watching the specified security with the specified feedType so far
            if (!this.simulation) {
                List<Subscription> subscriptions = getSubscriptionDao().findBySecurityAndFeedTypeForAutoActivateStrategies(securityId, feedType);
                if (subscriptions.size() == 0) {
                    if (!security.getSecurityFamily().isSynthetic()) {
                        getExternalMarketDataService(feedType).subscribe(security);
                    }
                }
            }

            // update links
            Subscription subscription = Subscription.Factory.newInstance(feedType, false, strategy, security);

            getSubscriptionDao().create(subscription);

            // reverse-associate security (after subscription has received an id)
            security.getSubscriptions().add(subscription);

            logger.info("subscribed security " + security + " with " + feedType);
        }
    }

    @Override
    protected void handleUnsubscribe(String strategyName, int securityId) throws Exception {

        unsubscribe(strategyName, securityId, this.feedType);
    }

    @Override
    protected synchronized void handleUnsubscribe(String strategyName, int securityId, FeedType feedType) throws Exception {

        Subscription subscription = getSubscriptionDao().findByStrategySecurityAndFeedType(strategyName, securityId, feedType);
        if (subscription != null && !subscription.isPersistent()) {

            Security security = getSecurityDao().get(securityId);

            // update links
            security.getSubscriptions().remove(subscription);

            getSubscriptionDao().remove(subscription);

            // only external unsubscribe if nobody is watching this security anymore
            if (!this.simulation) {
                if (security.getSubscriptions().size() == 0) {
                    if (!security.getSecurityFamily().isSynthetic()) {
                        getExternalMarketDataService(feedType).unsubscribe(security);
                    }
                }
            }

            logger.info("unsubscribed security " + security + " with " + feedType);
        }
    }

    @Override
    protected void handleRemoveNonPositionSubscriptions(String strategyName) throws Exception {

        Collection<Subscription> subscriptions = getSubscriptionDao().findNonPositionSubscriptions(strategyName);

        for (Subscription subscription : subscriptions) {
            unsubscribe(subscription.getStrategy().getName(), subscription.getSecurity().getId());
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void handleRemoveNonPositionSubscriptionsByType(String strategyName, Class type) throws Exception {

        int discriminator = HibernateUtil.getDisriminatorValue(getSessionFactory(), type);
        Collection<Subscription> subscriptions = getSubscriptionDao().findNonPositionSubscriptionsByType(strategyName, discriminator);

        for (Subscription subscription : subscriptions) {
            unsubscribe(subscription.getStrategy().getName(), subscription.getSecurity().getId());
        }
    }

    @Override
    protected void handleRequestCurrentTicks(String strategyName) throws Exception {

        Collection<Tick> ticks = getTickDao().findCurrentTicksByStrategy(strategyName);

        for (Tick tick : ticks) {
            EngineLocator.instance().sendEvent(strategyName, tick);
        }
    }

    @Override
    protected void handleLogTickGap(int securityId) {

        Security security = getSecurityDao().get(securityId);

        logger.error(security + " has not received any ticks for " + security.getSecurityFamily().getMaxGap() + " minutes");
    }

    private void saveCvs(Tick tick) throws IOException {

        Security security = tick.getSecurity();

        CsvTickWriter csvWriter;
        synchronized (this.csvWriters) {
            csvWriter = this.csvWriters.get(security);
            if (csvWriter == null) {
                String fileName = security.getIsin() != null ? security.getIsin() : String.valueOf(security.getId());
                csvWriter = new CsvTickWriter(fileName);
                this.csvWriters.put(security, csvWriter);
            }
        }

        synchronized (csvWriter) {
            csvWriter.write(tick);
        }
    }

    /**
     * get the externalMarketDataService defined by MarketDataServiceType
     */
    @SuppressWarnings({ "unchecked" })
    private ExternalMarketDataService getExternalMarketDataService(final FeedType feedType) throws Exception {

        Validate.notNull(feedType, "feedType must not be null");

        Class<ExternalMarketDataService> marketDataServiceClass = (Class<ExternalMarketDataService>) Class.forName(feedType.getValue());

        Map<String, ExternalMarketDataService> externalMarketDataServices = this.applicationContext.getBeansOfType(marketDataServiceClass);

        // select the proxy
        String name = CollectionUtils.find(externalMarketDataServices.keySet(), new Predicate<String>() {
            @Override
            public boolean evaluate(String name) {
                return !name.startsWith("ch.algotrader.service");
            }
        });

        ExternalMarketDataService externalMarketDataService = externalMarketDataServices.get(name);

        Validate.notNull(externalMarketDataService, "externalMarketDataService was not found: " + feedType);

        return externalMarketDataService;
    }

    public static class PropagateMarketDataEventSubscriber {

        public void update(final MarketDataEvent marketDataEvent) {

            // security.toString & marketDataEvent.toString is expensive, so only log if debug is enabled
            if (logger.isTraceEnabled()) {
                logger.trace(marketDataEvent.getSecurityInitialized() + " " + marketDataEvent);
            }

            long startTime = System.nanoTime();

            EngineLocator.instance().sendMarketDataEvent(marketDataEvent);

            MetricsUtil.accountEnd("PropagateMarketDataEventSubscriber.update", startTime);
        }
    }

    public static class PropagateGenericEventSubscriber {

        public void update(final GenericEventVO genericEvent) {

            // security.toString & marketDataEvent.toString is expensive, so only log if debug is enabled
            if (logger.isTraceEnabled()) {
                logger.trace(genericEvent);
            }

            EngineLocator.instance().sendGenericEvent(genericEvent);
        }
    }

    public static class PersistTickSubscriber {

        @SuppressWarnings("rawtypes")
        public void update(Pair<Tick, Object> insertStream, Map removeStream) {

            Tick tick = insertStream.getFirst();
            try {
                ServiceLocator.instance().getMarketDataService().persistTick(tick);

                // catch duplicate entry errors and log them as warn
            } catch (DataIntegrityViolationException e) {
                logger.warn(e.getRootCause().getMessage());
            }
        }
    }
}
