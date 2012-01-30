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
import com.algoTrader.entity.WatchListItem;
import com.algoTrader.entity.WatchListItemImpl;
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

        Hibernate.initialize(security.getUnderlaying());
        Hibernate.initialize(security.getSecurityFamily());
        Hibernate.initialize(security.getWatchListItems());
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
    protected void handleInitWatchlist() {

        if (!this.simulation) {

            List<Security> securities = getSecurityDao().findSecuritiesOnActiveWatchlist();

            for (Security security : securities) {
                putOnExternalWatchlist(security);
            }
        }
    }

    @Override
    protected void handlePutOnWatchlist(String strategyName, int securityId) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        Security security = getSecurityDao().load(securityId);

        if (getWatchListItemDao().findByStrategyAndSecurity(strategyName, securityId) == null) {

            // only put on external watchlist if nobody was watching this security so far
            if (security.getWatchListItems().size() == 0) {
                if (!this.simulation) {
                    putOnExternalWatchlist(security);
                }
            }

            // update links
            WatchListItem watchListItem = new WatchListItemImpl();
            watchListItem.setSecurity(security);
            watchListItem.setStrategy(strategy);
            watchListItem.setPersistent(false);
            getWatchListItemDao().create(watchListItem);

            security.getWatchListItems().add(watchListItem);
            getSecurityDao().update(security);

            strategy.getWatchListItems().add(watchListItem);
            getStrategyDao().update(strategy);

            logger.info("put security on watchlist " + security.getSymbol());
        }
    }

    @Override
    protected void handleRemoveFromWatchlist(String strategyName, int securityId) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        Security security = getSecurityDao().load(securityId);

        WatchListItem watchListItem = getWatchListItemDao().findByStrategyAndSecurity(strategyName, securityId);

        if (watchListItem != null && !watchListItem.isPersistent()) {

            // update links
            security.getWatchListItems().remove(watchListItem);
            getSecurityDao().update(security);

            strategy.getWatchListItems().remove(watchListItem);
            getStrategyDao().update(strategy);

            getWatchListItemDao().remove(watchListItem);

            // only remove from external watchlist if nobody is watching this security anymore
            if (security.getWatchListItems().size() == 0) {
                if (!this.simulation) {
                    removeFromExternalWatchlist(security);
                }
            }

            logger.info("removed security from watchlist " + security.getSymbol());
        }
    }

    @Override
    protected void handleRemoveNonPositionWatchListItem(String strategyName) throws Exception {

        List<WatchListItem> watchListItems = getWatchListItemDao().findNonPositionWatchListItem(strategyName);

        for (WatchListItem watchListItem : watchListItems) {
            removeFromWatchlist(watchListItem.getStrategy().getName(), watchListItem.getSecurity().getId());
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void handleRemoveNonPositionWatchListItemByType(String strategyName, Class type) throws Exception {

        int discriminator = HibernateUtil.getDisriminatorValue(getSessionFactory(), type);
        List<WatchListItem> watchListItems = getWatchListItemDao().findNonPositionWatchListItemByType(strategyName, discriminator);

        for (WatchListItem watchListItem : watchListItems) {
            removeFromWatchlist(watchListItem.getStrategy().getName(), watchListItem.getSecurity().getId());
        }
    }

    @Override
    protected void handleSetAlertValue(String strategyName, int securityId, Double value, boolean upper) throws Exception {

        WatchListItem watchListItem = getWatchListItemDao().findByStrategyAndSecurity(strategyName, securityId);

        if (upper) {
            watchListItem.setUpperAlertValue(value);
            logger.info("set upper alert value to " + decimalFormat.format(value) + " for watchListItem " + watchListItem);
        } else {
            watchListItem.setLowerAlertValue(value);
            logger.info("set lower alert value to " + decimalFormat.format(value) + " for watchListItem " + watchListItem);
        }

        getWatchListItemDao().update(watchListItem);
    }

    @Override
    protected void handleRemoveAlertValues(String strategyName, int securityId) throws Exception {

        WatchListItem watchListItem = getWatchListItemDao().findByStrategyAndSecurity(strategyName, securityId);
        if (watchListItem.getUpperAlertValue() != null || watchListItem.getLowerAlertValue() != null) {

            watchListItem.setUpperAlertValue(null);
            watchListItem.setLowerAlertValue(null);

            getWatchListItemDao().update(watchListItem);

            logger.info("removed alert values for watchListItem " + watchListItem);
        }
    }

    @Override
    protected void handleSetAmount(String strategyName, int securityId, Double amount) throws Exception {

        WatchListItem watchListItem = getWatchListItemDao().findByStrategyAndSecurity(strategyName, securityId);

        watchListItem.setAmount(amount);
        logger.info("set amount to " + decimalFormat.format(amount) + " for watchListItem " + watchListItem);

        getWatchListItemDao().update(watchListItem);
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
