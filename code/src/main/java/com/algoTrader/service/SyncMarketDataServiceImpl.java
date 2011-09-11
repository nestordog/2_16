package com.algoTrader.service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.supercsv.exception.SuperCSVException;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.WatchListItem;
import com.algoTrader.entity.WatchListItemImpl;
import com.algoTrader.entity.marketData.Bar;
import com.algoTrader.entity.marketData.MarketDataEvent;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.marketData.TickImpl;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.StockOption;
import com.algoTrader.entity.security.StockOptionFamily;
import com.algoTrader.entity.security.StockOptionImpl;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.esper.io.CsvIVolReader;
import com.algoTrader.esper.io.CsvTickReader;
import com.algoTrader.esper.io.CsvTickWriter;
import com.algoTrader.stockOption.StockOptionSymbol;
import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.vo.BarVO;
import com.algoTrader.vo.IVolVO;
import com.algoTrader.vo.RawTickVO;

public abstract class SyncMarketDataServiceImpl extends SyncMarketDataServiceBase {

    private static final DateFormat fileFormat = new SimpleDateFormat("dd-MM-yyyy");
    private static DecimalFormat decimalFormat = new DecimalFormat("#,##0.0000");

    private static Logger logger = MyLogger.getLogger(SyncMarketDataServiceImpl.class.getName());
    private static String dataSet = ConfigurationUtil.getBaseConfig().getString("dataSource.dataSet");

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

    /**
     * must be run with simulation=false (to get correct values for bid, ask and settlement)
     * also recommended to turn of ehache on commandline (to avoid out of memory error)
     */
    @Override
    protected void handleImportTicks(String isin) throws Exception {

        File file = new File("results/tickdata/" + dataSet + "/" + isin + ".csv");

        if (file.exists()) {

            Security security = getSecurityDao().findByIsin(isin);
            if (security == null) {
                throw new SyncMarketDataServiceException("security was not found: " + isin);
            }

            CsvTickReader reader = new CsvTickReader(isin);

            // create a set that will eliminate ticks of the same date (not considering milliseconds)
            Comparator<Tick> comp = new Comparator<Tick>() {
                @Override
                public int compare(Tick t1, Tick t2) {
                    return (int) ((t1.getDateTime().getTime() - t2.getDateTime().getTime()) / 1000);
                }
            };
            Set<Tick> newTicks = new TreeSet<Tick>(comp);

            // fetch all ticks from the file
            Tick tick;
            while ((tick = reader.readTick()) != null) {

                if (tick.getLast().equals(new BigDecimal(0))) {
                    tick.setLast(null);
                }

                tick.setSecurity(security);
                newTicks.add(tick);

            }

            // eliminate ticks that are already in the DB
            List<Tick> existingTicks = getTickDao().findBySecurity(security.getId());

            for (Tick tick2 : existingTicks) {
                newTicks.remove(tick2);
            }

            // insert the newTicks into the DB
            try {
                getTickDao().create(newTicks);
            } catch (Exception e) {
                logger.error("problem import ticks for " + isin, e);
            }

            // perform memory release
            Session session = getSessionFactory().getCurrentSession();
            session.flush();
            session.clear();

            // gc
            System.gc();

            logger.info("imported " + newTicks.size() + " ticks for: " + isin);
        } else {
            logger.info("file does not exist: " + isin);
        }
    }

    @Override
    protected void handleImportIVolTicks(String stockOptionFamilyId, String symbol) throws Exception {

        StockOptionFamily family = getStockOptionFamilyDao().load(Integer.parseInt(stockOptionFamilyId));
        Set<StockOption> stockOptions = new HashSet<StockOption>();

        for (Security security : family.getSecurities()) {
            StockOption stockOption = (StockOption) security;
            stockOptions.add(stockOption);
        }

        Date date = null;

        File dir = new File("results/iVol/" + symbol);

        for (File file : dir.listFiles()) {

            String dateString = file.getName().substring(5, 15);
            Date fileDate = fileFormat.parse(dateString);
            CsvIVolReader csvReader = new CsvIVolReader(symbol + "/" + file.getName());

            IVolVO iVol;
            List<Tick> ticks = new ArrayList<Tick>();
            while ((iVol = csvReader.readHloc()) != null) {

                // prevent overlap
                if (DateUtils.toCalendar(fileDate).get(Calendar.MONTH) != DateUtils.toCalendar(iVol.getDate()).get(Calendar.MONTH)) {
                    continue;
                }

                if (iVol.getBid() == null || iVol.getAsk() == null) {
                    continue;
                }

                // only consider 3rd Friday
                Calendar expCal = new GregorianCalendar();
                expCal.setMinimalDaysInFirstWeek(2);
                expCal.setFirstDayOfWeek(Calendar.SUNDAY);
                expCal.setTime(iVol.getExpiration());

                if (!(expCal.get(Calendar.WEEK_OF_MONTH) == 3 && expCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)) {
                    continue;
                }

                // for every day create an underlaying-Tick
                if (!iVol.getDate().equals(date)) {

                    date = iVol.getDate();

                    Tick tick = new TickImpl();
                    tick.setDateTime(iVol.getDate());
                    tick.setLast(iVol.getAdjustedStockClosePrice());
                    tick.setBid(new BigDecimal(0));
                    tick.setAsk(new BigDecimal(0));
                    tick.setSecurity(family.getUnderlaying());
                    tick.setSettlement(new BigDecimal(0));

                    ticks.add(tick);
                }

                final StockOption newStockOption = new StockOptionImpl();
                newStockOption.setStrike(iVol.getStrike());
                newStockOption.setExpiration(DateUtils.setHours(DateUtils.addDays(iVol.getExpiration(), -1), 13)); // adjusted expiration date
                newStockOption.setType("C".equals(iVol.getType()) ? OptionType.CALL : OptionType.PUT);

                // check if we have the stockOption already
                StockOption stockOption = CollectionUtils.find(stockOptions, new Predicate<StockOption>() {
                    @Override
                    public boolean evaluate(StockOption stockOption) {
                        return stockOption.getStrike().intValue() == newStockOption.getStrike().intValue()
                                && stockOption.getExpiration().getTime() == newStockOption.getExpiration().getTime()
                                && stockOption.getType().equals(newStockOption.getType());
                    }
                });

                // otherwise create the stockOption
                if (stockOption == null) {

                    stockOption = newStockOption;
                    stockOption.setIsin(StockOptionSymbol.getIsin(family, stockOption.getExpiration(), stockOption.getType(), stockOption.getStrike()));
                    stockOption.setSymbol(StockOptionSymbol.getSymbol(family, stockOption.getExpiration(), stockOption.getType(), stockOption.getStrike()));
                    stockOption.setSecurityFamily(family);
                    stockOption.setUnderlaying(family.getUnderlaying());

                    getStockOptionDao().create(stockOption);
                    stockOptions.add(stockOption);
                }

                // create the tick
                Tick tick = new TickImpl();
                tick.setDateTime(iVol.getDate());
                tick.setBid(iVol.getBid());
                tick.setAsk(iVol.getAsk());
                tick.setVol(iVol.getVolume());
                tick.setOpenIntrest(iVol.getOpenIntrest());
                tick.setSecurity(stockOption);
                tick.setSettlement(new BigDecimal(0));

                ticks.add(tick);
            }

            getTickDao().create(ticks);

            // perform memory release
            Session session = getSessionFactory().getCurrentSession();
            session.flush();
            session.clear();

            // gc
            System.gc();

            logger.info("finished with file " + file.getName() + " created " + ticks.size() + " ticks");
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

    public static class SetAlertValueSubscriber {

        public void update(String strategyName, int securityId, Double value, boolean upper) {

            long startTime = System.currentTimeMillis();
            logger.debug("setAlertValue start");

            ServiceLocator.commonInstance().getSyncMarketDataService().setAlertValue(strategyName, securityId, value, upper);

            logger.debug("setAlertValue end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
        }
    }

    public static class RemoveFromWatchlistSubscriber {

        public void update(String strategyName, int securityId) {

            ServiceLocator.serverInstance().getSyncMarketDataService().removeFromWatchlist(strategyName, securityId);
        }
    }
}
