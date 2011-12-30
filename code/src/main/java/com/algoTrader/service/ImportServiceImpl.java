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
import com.algoTrader.esper.io.CsvIVolReader;
import com.algoTrader.esper.io.CsvTickReader;
import com.algoTrader.stockOption.StockOptionSymbol;
import com.algoTrader.util.MyLogger;
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
    protected void handleImportIVolTicks(String stockOptionFamilyId, String subfolder) throws Exception {

        StockOptionFamily family = getStockOptionFamilyDao().load(Integer.parseInt(stockOptionFamilyId));
        Map<String, StockOption> stockOptions = new HashMap<String, StockOption>();

        for (Security security : family.getSecurities()) {
            StockOption stockOption = (StockOption) security;
            stockOptions.put(stockOption.getSymbol(), stockOption);
        }

        Date date = null;

        File dir = new File("results/iVol/" + subfolder);

        for (File file : dir.listFiles()) {

            //            String dateString = file.getName().substring(5, 15);
            //            Date fileDate = fileFormat.parse(dateString);
            CsvIVolReader csvReader = new CsvIVolReader(subfolder + "/" + file.getName());

            IVolVO iVol;
            List<Tick> ticks = new ArrayList<Tick>();
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
                    stockOption.setUnderlaying(family.getUnderlaying());

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
}
