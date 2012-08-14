package com.algoTrader.service;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.marketData.TickImpl;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.StockOption;
import com.algoTrader.entity.security.StockOptionFamily;
import com.algoTrader.entity.security.StockOptionImpl;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.stockOption.StockOptionSymbol;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.io.CsvIVolReader;
import com.algoTrader.util.io.CsvTickReader;
import com.algoTrader.vo.IVolVO;

public class ImportServiceImpl extends ImportServiceBase {

    private static Logger logger = MyLogger.getLogger(ImportServiceImpl.class.getName());

    private @Value("${dataSource.dataSet}") String dataSet;

    /**
     * must be run with simulation=false (to get correct values for bid, ask and settlement)
     * also recommended to turn of ehache on commandline (to avoid out of memory error)
     */
    @Override
    protected void handleImportTicks(String isin) throws Exception {

        File file = new File("results/tickdata/" + this.dataSet + "/" + isin + ".csv");

        if (file.exists()) {

            Security security = getSecurityDao().findByIsin(isin);
            if (security == null) {
                throw new ImportServiceException("security was not found: " + isin);
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
    protected void handleImportIVolTicks(String stockOptionFamilyId, String fileName) throws Exception {

        StockOptionFamily family = getStockOptionFamilyDao().get(Integer.parseInt(stockOptionFamilyId));
        Map<String, StockOption> stockOptions = new HashMap<String, StockOption>();

        for (Security security : family.getSecurities()) {
            StockOption stockOption = (StockOption) security;
            stockOptions.put(stockOption.getSymbol(), stockOption);
        }

        Date date = null;

        File dir = new File("results/iVol/" + fileName);

        for (File file : dir.listFiles()) {

            //            String dateString = file.getName().substring(5, 15);
            //            Date fileDate = fileFormat.parse(dateString);
            CsvIVolReader csvReader = new CsvIVolReader(fileName + "/" + file.getName());

            IVolVO iVol;
            Set<Tick> ticks = new TreeSet<Tick>(new Comparator<Tick>() {
                @Override
                public int compare(Tick t1, Tick t2) {
                    if (t1.getSecurity().getId() > t2.getSecurity().getId()) {
                        return 1;
                    } else if (t1.getSecurity().getId() < t2.getSecurity().getId()) {
                        return -1;
                    } else if (t1.getDateTime().getTime() > t2.getDateTime().getTime()) {
                        return 1;
                    } else if (t1.getDateTime().getTime() < t2.getDateTime().getTime()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
            while ((iVol = csvReader.readHloc()) != null) {

                // prevent overlap
//                if (DateUtils.toCalendar(fileDate).get(Calendar.MONTH) != DateUtils.toCalendar(iVol.getDate()).get(Calendar.MONTH)) {
//                    continue;
//                }

                if (iVol.getBid() == null || iVol.getAsk() == null) {
                    continue;
                }

                // only consider 3rd Friday
                Calendar expCal = new GregorianCalendar();
                expCal.setMinimalDaysInFirstWeek(2);
                expCal.setFirstDayOfWeek(Calendar.SUNDAY);
                expCal.setTime(iVol.getExpiration());

                //                if (!(expCal.get(Calendar.WEEK_OF_MONTH) == 3 && expCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)) {
                //                    continue;
                //                }

                // for every day create an underlying-Tick
                if (!iVol.getDate().equals(date)) {

                    logger.info("processing " + iVol.getDate());

                    date = iVol.getDate();

                    Tick tick = new TickImpl();
                    tick.setDateTime(iVol.getDate());
                    tick.setLast(iVol.getAdjustedStockClosePrice());
                    tick.setBid(new BigDecimal(0));
                    tick.setAsk(new BigDecimal(0));
                    tick.setSecurity(family.getUnderlying());
                    tick.setSettlement(new BigDecimal(0));

                    ticks.add(tick);
                }

                BigDecimal strike = iVol.getStrike();
                Date expiration = DateUtils.setHours(DateUtils.addDays(iVol.getExpiration(), -1), 13); // adjusted expiration date
                OptionType type = "C".equals(iVol.getType()) ? OptionType.CALL : OptionType.PUT;
                String symbol = StockOptionSymbol.getSymbol(family, expiration, type, strike);
                String isin = StockOptionSymbol.getIsin(family, expiration, type, strike);

                // check if we have the stockOption already
                StockOption stockOption = stockOptions.get(symbol);

                // otherwise create the stockOption
                if (stockOption == null) {

                    stockOption = new StockOptionImpl();
                    stockOption.setStrike(iVol.getStrike());
                    stockOption.setExpiration(expiration); // adjusted expiration date
                    stockOption.setType(type);
                    stockOption.setSymbol(symbol);
                    stockOption.setIsin(isin);
                    stockOption.setSecurityFamily(family);
                    stockOption.setUnderlying(family.getUnderlying());

                    getStockOptionDao().create(stockOption);
                    stockOptions.put(stockOption.getSymbol(), stockOption);
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

            logger.info("importing " + ticks.size() + " ticks");

            // divide into chuncks of 10000
            List<Tick> list = new ArrayList<Tick>(ticks);
            for (int i = 0; i < ticks.size(); i = i + 10000) {

                int j = Math.min(i + 10000, ticks.size());
                List<Tick> subList = list.subList(i, j);

                getTickDao().create(subList);

                // perform memory release
                Session session = getSessionFactory().getCurrentSession();
                session.flush();
                session.clear();

                logger.info("importing chunk " + i + " - " + j);
            }

            // gc
            System.gc();

            logger.info("finished with file " + file.getName() + " created " + ticks.size() + " ticks");
        }
    }
}
