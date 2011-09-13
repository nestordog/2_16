package com.algoTrader.service;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.supercsv.exception.SuperCSVException;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.WatchListItem;
import com.algoTrader.entity.WatchListItemImpl;
import com.algoTrader.entity.marketData.Bar;
import com.algoTrader.entity.marketData.MarketDataEvent;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.security.Security;
import com.algoTrader.esper.io.CsvTickWriter;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.vo.BarVO;
import com.algoTrader.vo.RawTickVO;

public abstract class SyncMarketDataServiceImpl extends SyncMarketDataServiceBase {

    private static Logger logger = MyLogger.getLogger(SyncMarketDataServiceImpl.class.getName());

    private Map<Security, CsvTickWriter> csvWriters = new HashMap<Security, CsvTickWriter>();

    @Override
    protected void handleProcessTick(int securityId) throws SuperCSVException, IOException {

        Security security = getSecurityDao().load(securityId);

        // retrieve ticks only between marketOpen & close
        if (DateUtil.compareToTime(security.getSecurityFamily().getMarketOpen()) >= 0
                && DateUtil.compareToTime(security.getSecurityFamily().getMarketClose()) <= 0) {

            RawTickVO rawTick = retrieveTick(security);

            // if we hit a timeout, we get null
            if (rawTick != null) {

                Tick tick = completeRawTick(rawTick);

                if (tick.isSpreadValid()) {

                    // only valid ticks get send into esper
                    getRuleService().sendEvent(StrategyImpl.BASE, tick);
                }

                // write the tick to file (even if not valid)
                CsvTickWriter csvWriter = this.csvWriters.get(security);
                if (csvWriter == null) {
                    csvWriter = new CsvTickWriter(security.getIsin());
                    this.csvWriters.put(security, csvWriter);
                }
                csvWriter.write(tick);

                // write the tick to the DB (even if not valid)
                getTickDao().create(tick);
            }
        }
    }

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

        // marketDataEvent.toString is expensive, so only log if debug is anabled
        if (!logger.getParent().getLevel().isGreaterOrEqual(Level.INFO)) {
            logger.debug(marketDataEvent.getSecurity().getSymbol() + " " + marketDataEvent);
        }

        Collection<WatchListItem> watchListItems = marketDataEvent.getSecurity().getWatchListItems();
        for (WatchListItem watchListItem : watchListItems) {
            getRuleService().sendEvent(watchListItem.getStrategy().getName(), marketDataEvent);
        }
    }

    @Override
    protected void handlePutOnWatchlist(String strategyName, int securityId) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        Security security = getSecurityDao().load(securityId);
        putOnWatchlist(strategy, security);
    }

    @Override
    protected void handlePutOnWatchlist(Strategy strategy, Security security) throws Exception {

        if (getWatchListItemDao().findByStrategyAndSecurity(strategy.getName(), security.getId()) == null) {

            // only put on external watchlist if nobody was watching this security so far
            if (security.getWatchListItems().size() == 0) {
                putOnExternalWatchlist(security);
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

        removeFromWatchlist(strategy, security);
    }

    @Override
    protected void handleRemoveFromWatchlist(Strategy strategy, Security security) throws Exception {

        WatchListItem watchListItem = getWatchListItemDao().findByStrategyAndSecurity(strategy.getName(), security.getId());

        if (watchListItem != null && !watchListItem.isPersistent()) {

            // update links
            security.getWatchListItems().remove(watchListItem);
            getSecurityDao().update(security);

            strategy.getWatchListItems().remove(watchListItem);
            getStrategyDao().update(strategy);

            getWatchListItemDao().remove(watchListItem);

            // only remove from external watchlist if nobody is watching this security anymore
            if (security.getWatchListItems().size() == 0) {
                removeFromExternalWatchlist(security);
            }

            logger.info("removed security from watchlist " + security.getSymbol());
        }
    }

    public static class RetrieveTickSubscriber {

        public void update(int securityId) {

            ServiceLocator.serverInstance().getSyncMarketDataService().processTick(securityId);
        }
    }

    public static class PropagateTickSubscriber {

        public void update(MarketDataEvent marketDataEvent) {

            ServiceLocator.serverInstance().getSyncMarketDataService().propagateMarketDataEvent(marketDataEvent);
        }
    }


    public static class RemoveFromWatchlistSubscriber {

        public void update(String strategyName, int securityId) {

            ServiceLocator.serverInstance().getSyncMarketDataService().removeFromWatchlist(strategyName, securityId);
        }
    }
}
