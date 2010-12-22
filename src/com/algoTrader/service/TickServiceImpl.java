package com.algoTrader.service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.supercsv.exception.SuperCSVException;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.Tick;
import com.algoTrader.entity.TickImpl;
import com.algoTrader.stockOption.StockOptionUtil;
import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.util.io.CsvTickReader;
import com.algoTrader.util.io.CsvTickWriter;
import com.algoTrader.vo.RawTickVO;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public abstract class TickServiceImpl extends TickServiceBase {

    private static Logger logger = MyLogger.getLogger(TickServiceImpl.class.getName());
    private static String dataSet = ConfigurationUtil.getBaseConfig().getString("dataSource.dataSet");
    private static boolean simulation = ConfigurationUtil.getBaseConfig().getBoolean("simulation");

    private Map<Security, CsvTickWriter> csvWriters = new HashMap<Security, CsvTickWriter>();
    private Map<Integer, Tick> securities = new HashMap<Integer, Tick>();

    @SuppressWarnings("unchecked")
    protected void handleRetrieveTicks() throws SuperCSVException, IOException  {

        List<Security> securities = getSecurityDao().findSecuritiesOnWatchlist();
        for (Security security : securities) {

            // retrieve ticks only between marketOpen & close
            if (DateUtil.compareToTime(security.getSecurityFamily().getMarketOpen()) >= 0 &&
                DateUtil.compareToTime(security.getSecurityFamily().getMarketClose()) <= 0) {

                RawTickVO rawTick = retrieveTick(security);

                // if we hit a timeout, we get null
                if (rawTick != null) {

                    Tick tick = completeRawTick(rawTick);

                    try {
                        tick.validate();
                    } catch (Exception e) {
                        // do nothing, just ignore invalideTicks
                    }

                    propagateTick(tick);
                    createSimulatedTicks(tick);

                    // write the tick to file (even if not valid)
                    CsvTickWriter csvWriter = this.csvWriters.get(security);
                    if (csvWriter == null) {
                        csvWriter = new CsvTickWriter(security.getIsin());
                        this.csvWriters.put(security, csvWriter);
                    }
                    csvWriter.write(tick);

                    // write the tick to the DB
                    getTickDao().create(tick);
                }
            }
        }
    }

    protected Tick handleCompleteRawTick(RawTickVO rawTick) {

        return getTickDao().rawTickVOToEntity(rawTick);
    }

    @SuppressWarnings("unchecked")
    protected void handleCreateSimulatedTicks(Tick tick) throws Exception {

        if (!simulation)
            return;

        List<StockOption> stockOptions = getStockOptionDao().findStockOptionsOnWatchlist();
        for (StockOption stockOption : stockOptions) {

            Security underlaying = stockOption.getSecurityFamily().getUnderlaying();
            if (tick.getSecurity().getId() == underlaying.getId()) {

                // save the underlyingTick for later
                this.securities.put(tick.getSecurity().getId(), tick);

            } else if (tick.getSecurity().getId() == underlaying.getVolatility().getId()) {

                // only create StockOptionTicks when a volaTick arrives
                Tick underlayingTick = this.securities.get(underlaying.getId());

                if (underlayingTick != null) {

                    double lastDouble = StockOptionUtil.getOptionPrice(stockOption, underlayingTick.getCurrentValueDouble(), tick.getCurrentValueDouble() / 100);
                    BigDecimal last = RoundUtil.getBigDecimal(lastDouble);

                    Tick stockOptionTick = new TickImpl();
                    stockOptionTick.setDateTime(tick.getDateTime());
                    stockOptionTick.setLast(last);
                    stockOptionTick.setLastDateTime(tick.getDateTime());
                    stockOptionTick.setVol(0);
                    stockOptionTick.setVolBid(0);
                    stockOptionTick.setVolAsk(0);
                    stockOptionTick.setBid(new BigDecimal(0));
                    stockOptionTick.setAsk(new BigDecimal(0));
                    stockOptionTick.setOpenIntrest(0);
                    stockOptionTick.setSettlement(new BigDecimal(0));
                    stockOptionTick.setSecurity(stockOption);

                    propagateTick(stockOptionTick);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void handlePropagateTick(Tick tick) {

        logger.debug(tick.getSecurity().getSymbol() + " " + tick);

        getRuleService().sendEvent(StrategyImpl.BASE, tick);

        Collection<Strategy> strategies = tick.getSecurity().getWatchers();
        for (Strategy strategy : strategies) {
            getRuleService().sendEvent(strategy.getName(), tick);
        }
    }

    protected void handlePutOnWatchlist(String strategyName, int securityId) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        Security security = getSecurityDao().load(securityId);
        putOnWatchlist(strategy, security);
    }

    @SuppressWarnings("unchecked")
    protected void handlePutOnWatchlist(Strategy strategy, Security security) throws Exception {

        if (!security.getWatchers().contains(strategy)) {

            // only put on external watchlist if nobody was watching this security so far
            if (security.getWatchers().size() == 0) {
                putOnExternalWatchlist(security);
            }

            // update links
            security.getWatchers().add(strategy);
            getSecurityDao().update(security);
            strategy.getWatchlist().add(security);
            getStrategyDao().update(strategy);

            logger.info("put security on watchlist " + security.getSymbol());
        }
    }

    protected void handleRemoveFromWatchlist(String strategyName, int securityId) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        Security security = getSecurityDao().load(securityId);

        removeFromWatchlist(strategy, security);
    }

    protected void handleRemoveFromWatchlist(Strategy strategy, Security security) throws Exception {

        if (security.getWatchers().contains(strategy)) {

            // update links
            security.getWatchers().remove(strategy);
            getSecurityDao().update(security);
            strategy.getWatchlist().remove(security);
            getStrategyDao().update(strategy);

            // only remove from external watchlist if nobody is watch this security anymore
            if (security.getWatchers().size() == 0) {
                removeFromExternalWatchlist(security);
            }


            logger.info("removed security from watchlist " + security.getSymbol());
        }
    }

    /**
     * must be run with simulation=false (to get correct values for bid, ask and settlement)
     * also recommended to turn of ehache on commandline (to avoid out of memory error)
     */
    protected void handleImportTicks(String isin) throws Exception {

        File file = new File("results/tickdata/" + dataSet + "/" + isin + ".csv");

        if (file.exists()) {

            Security security = getSecurityDao().findByIsin(isin);
            if (security == null) {
                throw new TickServiceException("security was not found: " + isin);
            }

            CsvTickReader reader = new CsvTickReader(isin);

            // the set will eliminate ticks of the same date
            Comparator<Tick> comp = new Comparator<Tick>() {
                public int compare(Tick t1, Tick t2) {
                    int result = t1.getDateTime().compareTo(t2.getDateTime());
                    if (result == 0) {
                        System.currentTimeMillis();
                    }
                    return result;
                }
            };
            Set<Tick> ticks = new TreeSet<Tick>(comp);

            Tick tick;
            while ((tick = reader.readTick()) != null) {

                if (tick.getLast().equals(new BigDecimal(0)))
                    tick.setLast(null);

                tick.setSecurity(security);
                ticks.add(tick);

            }
            try {
                getTickDao().create(ticks);
            } catch (Exception e) {
                logger.error("problem import ticks for " + isin, e);
            }

            logger.info("imported ticks for: " + isin);
        } else {
            logger.info("file does not exist: " + isin);
        }
    }

    public static class RetrieveTickListener implements UpdateListener {

        public void update(EventBean[] newEvents, EventBean[] oldEvents) {

            ServiceLocator.serverInstance().getDispatcherService().getTickService().retrieveTicks();
        }
    }
}
