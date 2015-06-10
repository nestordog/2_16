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

import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.CoreConfig;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.FutureDao;
import ch.algotrader.entity.security.FutureFamily;
import ch.algotrader.entity.security.FutureFamilyDao;
import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.future.FutureSymbol;
import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.util.DateUtil;
import ch.algotrader.util.collection.CollectionUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@Transactional
public class FutureServiceImpl implements FutureService {

    private static final Logger LOGGER = LogManager.getLogger(FutureServiceImpl.class);

    private final CommonConfig commonConfig;

    private final CoreConfig coreConfig;

    private final FutureFamilyDao futureFamilyDao;

    private final FutureDao futureDao;

    private final EngineManager engineManager;

    public FutureServiceImpl(
            final CommonConfig commonConfig,
            final CoreConfig coreConfig,
            final FutureFamilyDao futureFamilyDao,
            final FutureDao futureDao,
            final EngineManager engineManager) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(coreConfig, "CoreConfig is null");
        Validate.notNull(futureFamilyDao, "FutureFamilyDao is null");
        Validate.notNull(futureDao, "FutureDao is null");
        Validate.notNull(engineManager, "EngineManager is null");

        this.commonConfig = commonConfig;
        this.coreConfig = coreConfig;
        this.futureFamilyDao = futureFamilyDao;
        this.futureDao = futureDao;
        this.engineManager = engineManager;
     }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void createDummyFutures(final long futureFamilyId) {

        FutureFamily family = this.futureFamilyDao.get(futureFamilyId);
        Security underlying = family.getUnderlying();

        Collection<Future> futures = this.futureDao.findByMinExpiration(family.getId(), this.engineManager.getCurrentEPTime());

        // create the missing part of the futures chain
        for (int i = futures.size() + 1; i <= family.getLength(); i++) {

            int duration = i * (int) (family.getExpirationDistance().getValue() / Duration.MONTH_1.getValue());

            Date expiration = DateUtil.getExpirationDateNMonths(family.getExpirationType(), this.engineManager.getCurrentEPTime(), duration);
            LocalDate expirationDate = DateTimeLegacy.toLocalDate(expiration);

            String symbol = FutureSymbol.getSymbol(family, expirationDate);
            String isin = FutureSymbol.getIsin(family, expirationDate);
            String ric = FutureSymbol.getRic(family, expirationDate);

            Future future = Future.Factory.newInstance();
            future.setSymbol(symbol);
            future.setIsin(isin);
            future.setRic(ric);
            future.setExpiration(expiration);
            future.setUnderlying(underlying);
            future.setSecurityFamily(family);

            this.futureDao.save(future);

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("created future {}", future);
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Future getFutureByMinExpiration(final long futureFamilyId, final Date targetExpirationDate) {

        Validate.notNull(targetExpirationDate, "Target expiration date is null");

        Future future = CollectionUtil.getSingleElementOrNull(this.futureDao.findByMinExpiration(1, futureFamilyId, targetExpirationDate));

        // if no future was found, create the missing part of the future-chain
        if (this.commonConfig.isSimulation() && future == null && (this.coreConfig.isSimulateFuturesByUnderlying() || this.coreConfig.isSimulateFuturesByGenericFutures())) {

            createDummyFutures(futureFamilyId);

            future = CollectionUtil.getSingleElementOrNull(this.futureDao.findByMinExpiration(1, futureFamilyId, targetExpirationDate));
        }

        if (future == null) {
            throw new LookupServiceException("no future available for expiration " + targetExpirationDate);
        } else {
            return future;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Future getFutureByExpiration(final long futureFamilyId, final Date expirationDate) {

        Validate.notNull(expirationDate, "Expiration date is null");

        Future future = this.futureDao.findByExpirationInclSecurityFamily(futureFamilyId, expirationDate);

        // if no future was found, create the missing part of the future-chain
        if (this.commonConfig.isSimulation() && future == null && (this.coreConfig.isSimulateFuturesByUnderlying() || this.coreConfig.isSimulateFuturesByGenericFutures())) {

            createDummyFutures(futureFamilyId);
            future = this.futureDao.findByExpirationInclSecurityFamily(futureFamilyId, expirationDate);
        }

        if (future == null) {
            throw new LookupServiceException("no future available targetExpiration " + expirationDate);
        } else {

            return future;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Future getFutureByDuration(final long futureFamilyId, final Date targetDate, final int duration) {

        Validate.notNull(targetDate, "Target date is null");

        FutureFamily futureFamily = this.futureFamilyDao.get(futureFamilyId);

        Date expirationDate = DateUtil.getExpirationDateNMonths(futureFamily.getExpirationType(), targetDate, duration);
        Future future = this.futureDao.findByExpirationInclSecurityFamily(futureFamilyId, expirationDate);

        // if no future was found, create the missing part of the future-chain
        if (this.commonConfig.isSimulation() && future == null && (this.coreConfig.isSimulateFuturesByUnderlying() || this.coreConfig.isSimulateFuturesByGenericFutures())) {

            createDummyFutures(futureFamily.getId());
            future = this.futureDao.findByExpirationInclSecurityFamily(futureFamilyId, expirationDate);
        }

        if (future == null) {
            throw new LookupServiceException("no future available targetExpiration " + targetDate + " and duration " + duration);
        } else {
            return future;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Future> getFuturesByMinExpiration(final long futureFamilyId, final Date minExpirationDate) {

        Validate.notNull(minExpirationDate, "Min expiration date is null");

        return this.futureDao.findByMinExpiration(futureFamilyId, minExpirationDate);

    }

}
