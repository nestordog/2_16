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
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
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

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.marketData.TickDao;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.OptionDao;
import ch.algotrader.entity.security.OptionFamily;
import ch.algotrader.entity.security.OptionFamilyDao;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityDao;
import ch.algotrader.enumeration.OptionType;
import ch.algotrader.option.OptionSymbol;
import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.util.io.CsvIVolReader;
import ch.algotrader.util.io.CsvTickReader;
import ch.algotrader.vo.IVolVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@Transactional
public class ImportServiceImpl implements ImportService {

    private static final Logger LOGGER = LogManager.getLogger(ImportServiceImpl.class);

    private final CommonConfig commonConfig;

    private final SessionFactory sessionFactory;

    private final SecurityDao securityDao;

    private final TickDao tickDao;

    private final OptionFamilyDao optionFamilyDao;

    private final OptionDao optionDao;

    public ImportServiceImpl(final CommonConfig commonConfig,
            final SessionFactory sessionFactory,
            final SecurityDao securityDao,
            final TickDao tickDao,
            final OptionFamilyDao optionFamilyDao,
            final OptionDao optionDao) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(sessionFactory, "SessionFactory is null");
        Validate.notNull(securityDao, "SecurityDao is null");
        Validate.notNull(tickDao, "TickDao is null");
        Validate.notNull(optionFamilyDao, "OptionFamilyDao is null");
        Validate.notNull(optionDao, "OptionDao is null");

        this.commonConfig = commonConfig;
        this.sessionFactory = sessionFactory;
        this.securityDao = securityDao;
        this.tickDao = tickDao;
        this.optionFamilyDao = optionFamilyDao;
        this.optionDao = optionDao;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void importTicks(final String isin) {

        Validate.notEmpty(isin, "isin is empty");

        File file = new File("files" + File.separator + "tickdata" + File.separator + this.commonConfig.getDataSet() + File.separator + isin + ".csv");

        if (file.exists()) {

            Security security = this.securityDao.findByIsin(isin);
            if (security == null) {
                throw new ImportServiceException("security was not found: " + isin);
            }

            CsvTickReader reader;
            try {
                reader = new CsvTickReader(isin);
            } catch (IOException ex) {
                throw new ImportServiceException(ex);
            }

            // create a set that will eliminate ticks of the same date (not considering milliseconds)
            Comparator<Tick> comp = (t1, t2) -> (int) ((t1.getDateTime().getTime() - t2.getDateTime().getTime()) / 1000);
            Set<Tick> newTicks = new TreeSet<>(comp);

            // fetch all ticks from the file
            Tick tick;
            try {
                while ((tick = reader.readTick()) != null) {

                    if (tick.getLast().equals(new BigDecimal(0))) {
                        tick.setLast(null);
                    }

                    tick.setSecurity(security);
                    newTicks.add(tick);

                }
            } catch (IOException ex) {
                throw new ImportServiceException(ex);
            }

            // eliminate ticks that are already in the DB
            List<Tick> existingTicks = this.tickDao.findBySecurity(security.getId());

            for (Tick tick2 : existingTicks) {
                newTicks.remove(tick2);
            }

            // insert the newTicks into the DB
            this.tickDao.saveAll(newTicks);

            // perform memory release
            Session session = this.sessionFactory.getCurrentSession();
            session.flush();
            session.clear();

            // gc
            System.gc();

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("imported {} ticks for: {}", newTicks.size(), isin);
            }
        } else {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("file does not exist: {}", isin);
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void importIVolTicks(final String optionFamilyId, final String fileName) {

        Validate.notEmpty(optionFamilyId, "Option family id is empty");
        Validate.notEmpty(fileName, "File name is empty");

        OptionFamily family = this.optionFamilyDao.get(Long.parseLong(optionFamilyId));
        Map<String, Option> options = new HashMap<>();

        for (Security security : family.getSecurities()) {
            Option option = (Option) security;
            options.put(option.getSymbol(), option);
        }

        Date date = null;

        File dir = new File("files" + File.separator + "iVol" + File.separator + fileName);

        for (File file : dir.listFiles()) {

            //            String dateString = file.getName().substring(5, 15);
            //            Date fileDate = fileFormat.parse(dateString);
            CsvIVolReader csvReader;
            try {
                csvReader = new CsvIVolReader(fileName + File.separator + file.getName());
            } catch (IOException ex) {
                throw new ImportServiceException(ex);
            }

            IVolVO iVol;
            Set<Tick> ticks = new TreeSet<>(new Comparator<Tick>() {
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

            try {
                while ((iVol = csvReader.readHloc()) != null) {

                    // prevent overlap
                    //                    if (DateUtils.toCalendar(fileDate).get(Calendar.MONTH) != DateUtils.toCalendar(iVol.getDate()).get(Calendar.MONTH)) {
                    //                        continue;
                    //                    }

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

                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("processing {}", iVol.getDate());
                        }

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
                    LocalDate expirationDate = DateTimeLegacy.toLocalDate(expiration);
                    OptionType type = "C".equals(iVol.getType()) ? OptionType.CALL : OptionType.PUT;
                    String symbol = OptionSymbol.getSymbol(family, expirationDate, type, strike, false);
                    String isin = OptionSymbol.getIsin(family, expirationDate, type, strike);
                    String ric = OptionSymbol.getRic(family, expirationDate, type, strike);

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

                        this.optionDao.save(option);
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
            } catch (IOException ex) {
                throw new ImportServiceException(ex);
            }

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("importing {} ticks", ticks.size());
            }

            // divide into chuncks of 10000
            List<Tick> list = new ArrayList<>(ticks);
            for (int i = 0; i < ticks.size(); i = i + 10000) {

                int j = Math.min(i + 10000, ticks.size());
                List<Tick> subList = list.subList(i, j);

                this.tickDao.saveAll(subList);

                // perform memory release
                Session session = this.sessionFactory.getCurrentSession();
                session.flush();
                session.clear();

                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("importing chunk {} - {}", i, j);
                }
            }

            // gc
            System.gc();

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("finished with file {} created {} ticks", file.getName(), ticks.size());
            }
        }

    }

}
