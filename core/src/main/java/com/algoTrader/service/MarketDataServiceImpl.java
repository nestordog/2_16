package com.algoTrader.service;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.Subscription;
import com.algoTrader.entity.SubscriptionImpl;
import com.algoTrader.entity.marketData.Bar;
import com.algoTrader.entity.marketData.MarketDataEvent;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.security.Security;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.HibernateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.io.CsvTickWriter;
import com.algoTrader.vo.BarVO;
import com.algoTrader.vo.RawTickVO;
import com.espertech.esper.collection.Pair;

public abstract class MarketDataServiceImpl extends MarketDataServiceBase {

    private static final long serialVersionUID = 2871084846072648536L;
    private static Logger logger = MyLogger.getLogger(MarketDataServiceImpl.class.getName());
    private static DecimalFormat decimalFormat = new DecimalFormat("#,##0.0000");

    private @Value("${simulation}") boolean simulation;

    private Map<Security, CsvTickWriter> csvWriters = new HashMap<Security, CsvTickWriter>();

    @Override
    protected Tick handleCompleteRawTick(RawTickVO rawTick) {

        return getTickDao().rawTickVOToEntity(rawTick);
    }

    @Override
    protected Bar handleCompleteBar(BarVO barVO) {

        return getBarDao().barVOToEntity(barVO);
    }

    @Override
    protected void handlePropagateMarketDataEvent(MarketDataEvent marketDataEvent) {

        Security security = marketDataEvent.getSecurity();

        // marketDataEvent.toString is expensive, so only log if debug is anabled
        if (!logger.getParent().getLevel().isGreaterOrEqual(Level.DEBUG)) {
            logger.trace(marketDataEvent.getSecurity().getSymbol() + " " + marketDataEvent);
        }

        // lock or merge the security
        if (!HibernateUtil.lock(this.getSessionFactory(), security)) {
            security = (Security) HibernateUtil.merge(this.getSessionFactory(), security);
        }

        Hibernate.initialize(security.getUnderlying());
        Hibernate.initialize(security.getSecurityFamily());
        Hibernate.initialize(security.getSubscriptions());
        Hibernate.initialize(security.getPositions());

        getRuleService().sendMarketDataEvent(marketDataEvent);
    }

    @Override
    protected void handlePersistTick(Tick tick) throws IOException {

        Security security = tick.getSecurity();

        // get the current Date rounded to MINUTES
        Date date = DateUtils.round(DateUtil.getCurrentEPTime(), Calendar.MINUTE);
        tick.setDateTime(date);

        // write the tick to file
        CsvTickWriter csvWriter = this.csvWriters.get(security);
        if (csvWriter == null) {
            csvWriter = new CsvTickWriter(security.getIsin());
            this.csvWriters.put(security, csvWriter);
        }
        csvWriter.write(tick);

        // write the tick to the DB (even if not valid)
        getTickDao().create(tick);
    }

    @Override
    protected void handleInitSubscriptions() {

        if (!this.simulation) {

            List<Security> securities = getSecurityDao().findSubscribedForAutoActivateStrategiesInclFamily();

            for (Security security : securities) {
                if (!security.getSecurityFamily().isSynthetic()) {
                    externalSubscribe(security);
                }
            }
        }
    }

    @Override
    protected void handleSubscribe(String strategyName, int securityId) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        Security security = getSecurityDao().load(securityId);

        if (getSubscriptionDao().findByStrategyAndSecurity(strategyName, securityId) == null) {

            // only external subscribe if nobody was watching this security so far
            List<Subscription> subscriptions = getSubscriptionDao().findBySecurityForAutoActivateStrategies(security.getId());
            if (subscriptions.size() == 0) {
                if (!this.simulation && !security.getSecurityFamily().isSynthetic()) {
                    externalSubscribe(security);
                }
            }

            // update links
            Subscription subscription = new SubscriptionImpl();
            subscription.setSecurity(security);
            subscription.setStrategy(strategy);
            subscription.setPersistent(false);
            getSubscriptionDao().create(subscription);

            security.getSubscriptions().add(subscription);
            getSecurityDao().update(security);

            strategy.getSubscriptions().add(subscription);
            getStrategyDao().update(strategy);

            logger.info("subscribed security " + security.getSymbol());
        }
    }

    @Override
    protected void handleUnsubscribe(String strategyName, int securityId) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        Security security = getSecurityDao().load(securityId);

        Subscription subscription = getSubscriptionDao().findByStrategyAndSecurity(strategyName, securityId);

        if (subscription != null && !subscription.isPersistent()) {

            // update links
            security.getSubscriptions().remove(subscription);
            getSecurityDao().update(security);

            strategy.getSubscriptions().remove(subscription);
            getStrategyDao().update(strategy);

            getSubscriptionDao().remove(subscription);

            // only external unsubscribe if nobody is watching this security anymore
            if (security.getSubscriptions().size() == 0) {
                if (!this.simulation && !security.getSecurityFamily().isSynthetic()) {
                    externalUnsubscribe(security);
                }
            }

            logger.info("unsubscribed security " + security.getSymbol());
        }
    }

    @Override
    protected void handleRemoveNonPositionSubscriptions(String strategyName) throws Exception {

        List<Subscription> subscriptions = getSubscriptionDao().findNonPositionSubscriptions(strategyName);

        for (Subscription subscription : subscriptions) {
            unsubscribe(subscription.getStrategy().getName(), subscription.getSecurity().getId());
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void handleRemoveNonPositionSubscriptionsByType(String strategyName, Class type) throws Exception {

        int discriminator = HibernateUtil.getDisriminatorValue(getSessionFactory(), type);
        List<Subscription> subscriptions = getSubscriptionDao().findNonPositionSubscriptionsByType(strategyName, discriminator);

        for (Subscription subscription : subscriptions) {
            unsubscribe(subscription.getStrategy().getName(), subscription.getSecurity().getId());
        }
    }

    @Override
    protected void handleSetAlertValue(String strategyName, int securityId, Double value, boolean upper) throws Exception {

        Subscription subscription = getSubscriptionDao().findByStrategyAndSecurity(strategyName, securityId);

        if (upper) {
            subscription.setUpperAlertValue(value);
            logger.info("set upper alert value to " + decimalFormat.format(value) + " for subscription " + subscription);
        } else {
            subscription.setLowerAlertValue(value);
            logger.info("set lower alert value to " + decimalFormat.format(value) + " for subscription " + subscription);
        }

        getSubscriptionDao().update(subscription);
    }

    @Override
    protected void handleRemoveAlertValues(String strategyName, int securityId) throws Exception {

        Subscription subscription = getSubscriptionDao().findByStrategyAndSecurity(strategyName, securityId);
        if (subscription.getUpperAlertValue() != null || subscription.getLowerAlertValue() != null) {

            subscription.setUpperAlertValue(null);
            subscription.setLowerAlertValue(null);

            getSubscriptionDao().update(subscription);

            logger.info("removed alert values for subscription " + subscription);
        }
    }

    @Override
    protected void handleSetAmount(String strategyName, int securityId, Double amount) throws Exception {

        Subscription subscription = getSubscriptionDao().findByStrategyAndSecurity(strategyName, securityId);

        subscription.setAmount(amount);
        logger.info("set amount to " + decimalFormat.format(amount) + " for subscription " + subscription);

        getSubscriptionDao().update(subscription);
    }

    public static class PropagateMarketDataEventSubscriber {

        public void update(MarketDataEvent marketDataEvent) {

            ServiceLocator.instance().getMarketDataService().propagateMarketDataEvent(marketDataEvent);
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
