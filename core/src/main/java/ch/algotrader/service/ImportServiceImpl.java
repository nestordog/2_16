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

import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.OptionFamily;
import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.OptionType;
import ch.algotrader.option.OptionSymbol;
import ch.algotrader.util.MyLogger;
import ch.algotrader.util.io.CsvIVolReader;
import ch.algotrader.util.io.CsvTickReader;
import ch.algotrader.vo.IVolVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class ImportServiceImpl extends ImportServiceBase {

    private static Logger logger = MyLogger.getLogger(ImportServiceImpl.class.getName());

    @Override
    protected void handleImportTicks(String isin) throws Exception {

        File file = new File("files" + File.separator + "tickdata" + File.separator + getCommonConfig().getDataSet() + File.separator + isin + ".csv");

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
    protected void handleImportIVolTicks(String optionFamilyId, String fileName) throws Exception {

        OptionFamily family = getOptionFamilyDao().get(Integer.parseInt(optionFamilyId));
        Map<String, Option> options = new HashMap<String, Option>();

        for (Security security : family.getSecurities()) {
            Option option = (Option) security;
            options.put(option.getSymbol(), option);
        }

        Date date = null;

        File dir = new File("files" + File.separator + "iVol" + File.separator + fileName);

        for (File file : dir.listFiles()) {

            //            String dateString = file.getName().substring(5, 15);
            //            Date fileDate = fileFormat.parse(dateString);
            CsvIVolReader csvReader = new CsvIVolReader(fileName + File.separator + file.getName());

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

                    Tick tick = Tick.Factory.newInstance();
                    tick.setDateTime(iVol.getDate());
                    tick.setLast(iVol.getAdjustedStockClosePrice());
                    tick.setBid(new BigDecimal(0));
                    tick.setAsk(new BigDecimal(0));
                    tick.setSecurity(family.getUnderlying());

                    ticks.add(tick);
                }

                BigDecimal strike = iVol.getStrike();
                Date expiration = DateUtils.setHours(DateUtils.addDays(iVol.getExpiration(), -1), 13); // adjusted expiration date
                OptionType type = "C".equals(iVol.getType()) ? OptionType.CALL : OptionType.PUT;
                String symbol = OptionSymbol.getSymbol(family, expiration, type, strike, false);
                String isin = OptionSymbol.getIsin(family, expiration, type, strike);
                String ric = OptionSymbol.getRic(family, expiration, type, strike);

                // check if we have the option already
                Option option = options.get(symbol);

                // otherwise create the option
                if (option == null) {

                    option = Option.Factory.newInstance();
                    option.setStrike(iVol.getStrike());
                    option.setExpiration(expiration); // adjusted expiration date
                    option.setType(type);
                    option.setSymbol(symbol);
                    option.setIsin(isin);
                    option.setRic(ric);
                    option.setSecurityFamily(family);
                    option.setUnderlying(family.getUnderlying());

                    getOptionDao().create(option);
                    options.put(option.getSymbol(), option);
                }

                // create the tick
                Tick tick = Tick.Factory.newInstance();
                tick.setDateTime(iVol.getDate());
                tick.setBid(iVol.getBid());
                tick.setAsk(iVol.getAsk());
                tick.setVol(iVol.getVolume());
                tick.setSecurity(option);

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
