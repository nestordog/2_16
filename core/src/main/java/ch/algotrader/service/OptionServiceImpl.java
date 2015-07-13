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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.math.util.MathUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.CoreConfig;
import ch.algotrader.dao.PositionDao;
import ch.algotrader.dao.SubscriptionDao;
import ch.algotrader.dao.security.FutureFamilyDao;
import ch.algotrader.dao.security.OptionDao;
import ch.algotrader.dao.security.OptionFamilyDao;
import ch.algotrader.dao.security.SecurityDao;
import ch.algotrader.dao.strategy.StrategyDao;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.marketData.MarketDataEvent;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.FutureFamily;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.OptionFamily;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.enumeration.OptionType;
import ch.algotrader.enumeration.Side;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.esper.callback.TickCallback;
import ch.algotrader.option.OptionSymbol;
import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.util.DateUtil;
import ch.algotrader.util.RoundUtil;
import ch.algotrader.util.collection.CollectionUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@Transactional
public class OptionServiceImpl implements OptionService {

    private static final Logger LOGGER = LogManager.getLogger(OptionServiceImpl.class);

    private final CommonConfig commonConfig;

    private final CoreConfig coreConfig;

    private final MarketDataService marketDataService;

    private final FutureService futureService;

    private final OrderService orderService;

    private final LocalLookupService localLookupService;

    private final SecurityDao securityDao;

    private final OptionFamilyDao optionFamilyDao;

    private final OptionDao optionDao;

    private final PositionDao positionDao;

    private final SubscriptionDao subscriptionDao;

    private final FutureFamilyDao futureFamilyDao;

    private final StrategyDao strategyDao;

    private final EngineManager engineManager;

    private final Engine serverEngine;

    public OptionServiceImpl(
            final CommonConfig commonConfig,
            final CoreConfig coreConfig,
            final MarketDataService marketDataService,
            final FutureService futureService,
            final OrderService orderService,
            final LocalLookupService localLookupService,
            final SecurityDao securityDao,
            final OptionFamilyDao optionFamilyDao,
            final OptionDao optionDao,
            final PositionDao positionDao,
            final SubscriptionDao subscriptionDao,
            final FutureFamilyDao futureFamilyDao,
            final StrategyDao strategyDao,
            final EngineManager engineManager,
            final Engine serverEngine) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(coreConfig, "CoreConfig is null");
        Validate.notNull(marketDataService, "MarketDataService is null");
        Validate.notNull(futureService, "FutureService is null");
        Validate.notNull(orderService, "OrderService is null");
        Validate.notNull(localLookupService, "LocalLookupService is null");
        Validate.notNull(securityDao, "SecurityDao is null");
        Validate.notNull(optionFamilyDao, "OptionFamilyDao is null");
        Validate.notNull(optionDao, "OptionDao is null");
        Validate.notNull(positionDao, "PositionDao is null");
        Validate.notNull(subscriptionDao, "SubscriptionDao is null");
        Validate.notNull(futureFamilyDao, "FutureFamilyDao is null");
        Validate.notNull(strategyDao, "StrategyDao is null");
        Validate.notNull(engineManager, "EngineManager is null");
        Validate.notNull(serverEngine, "Engine is null");

        this.commonConfig = commonConfig;
        this.coreConfig = coreConfig;
        this.marketDataService = marketDataService;
        this.futureService = futureService;
        this.orderService = orderService;
        this.localLookupService = localLookupService;
        this.securityDao = securityDao;
        this.optionFamilyDao = optionFamilyDao;
        this.optionDao = optionDao;
        this.positionDao = positionDao;
        this.subscriptionDao = subscriptionDao;
        this.futureFamilyDao = futureFamilyDao;
        this.strategyDao = strategyDao;
        this.engineManager = engineManager;
        this.serverEngine = serverEngine;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hedgeDelta(final long underlyingId) {

        List<Position> positions = this.positionDao.findOpenPositionsByUnderlying(underlyingId);

        // get the deltaAdjustedMarketValue
        double deltaAdjustedMarketValue = 0;
        for (Position position : positions) {
            MarketDataEvent marketDataEvent = this.localLookupService.getCurrentMarketDataEvent(position.getSecurity().getId());
            MarketDataEvent underlyingMarketDataEvent = this.localLookupService.getCurrentMarketDataEvent(position.getSecurity().getUnderlying().getId());

            deltaAdjustedMarketValue += position.getMarketValue(marketDataEvent) * position.getSecurity().getLeverage(marketDataEvent, underlyingMarketDataEvent);
        }

        final Security underlying = this.securityDao.get(underlyingId);
        final Strategy server = this.strategyDao.findServer();

        Subscription underlyingSubscription = this.subscriptionDao.findByStrategyAndSecurity(StrategyImpl.SERVER, underlying.getId());
        if (!underlyingSubscription.hasProperty("hedgingFamily")) {
            throw new IllegalStateException("no hedgingFamily defined for security " + underlying);
        }

        final FutureFamily futureFamily = this.futureFamilyDao.load(underlyingSubscription.getIntProperty("hedgingFamily"));

        Date targetDate = DateUtils.addMilliseconds(this.engineManager.getCurrentEPTime(), this.coreConfig.getDeltaHedgeMinTimeToExpiration());
        final Future future = this.futureService.getFutureByMinExpiration(futureFamily.getId(), targetDate);
        final double deltaAdjustedMarketValuePerContract = deltaAdjustedMarketValue / futureFamily.getContractSize();

        this.serverEngine.addFirstTickCallback(Collections.singleton((Security) future), new TickCallback() {
            @Override
            public void onFirstTick(String strategyName, List<Tick> ticks) throws Exception {

                // round to the number of contracts
                int qty = (int) MathUtils.round(deltaAdjustedMarketValuePerContract / ticks.get(0).getCurrentValueDouble(), 0);

                if (qty != 0) {
                    // create the order
                    Order order = OptionServiceImpl.this.orderService.createOrderByOrderPreference(OptionServiceImpl.this.coreConfig.getDeltaHedgeOrderPreference());
                    order.setStrategy(server);
                    order.setSecurity(future);
                    order.setQuantity(Math.abs(qty));
                    order.setSide(qty > 0 ? Side.SELL : Side.BUY);

                    OptionServiceImpl.this.orderService.sendOrder(order);
                } else {

                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("no delta hedge necessary on {}", underlying);
                    }
                }
            }
        });

        // make sure the future is subscriped
        this.marketDataService.subscribe(server.getName(), future.getId());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Option createOTCOption(final long optionFamilyId, final Date expirationDate, final BigDecimal strike, final OptionType type) {

        Validate.notNull(expirationDate, "Expiration date is null");
        Validate.notNull(strike, "Strike is null");
        Validate.notNull(type, "Type is null");

        OptionFamily family = this.optionFamilyDao.get(optionFamilyId);
        Security underlying = family.getUnderlying();

        // symbol / isin
        String symbol = OptionSymbol.getSymbol(family, DateTimeLegacy.toLocalDate(expirationDate), type, strike, true);

        Option option = Option.Factory.newInstance();
        option.setSymbol(symbol);
        option.setStrike(strike);
        option.setExpiration(expirationDate);
        option.setType(type);
        option.setUnderlying(underlying);
        option.setSecurityFamily(family);

        this.optionDao.save(option);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("created OTC option {}", option);
        }

        return option;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Option createDummyOption(final long optionFamilyId, final Date targetExpirationDate, final BigDecimal targetStrike, final OptionType type) {

        Validate.notNull(targetExpirationDate, "Target expiration date is null");
        Validate.notNull(targetStrike, "Target strike is null");
        Validate.notNull(type, "Type is null");

        OptionFamily family = this.optionFamilyDao.get(optionFamilyId);
        Security underlying = family.getUnderlying();

        // get next expiration date after targetExpirationDate according to expirationType
        Date expiration = DateUtil.getExpirationDate(family.getExpirationType(), targetExpirationDate);
        LocalDate expirationDate = DateTimeLegacy.toLocalDate(expiration);

        // get nearest strike according to strikeDistance
        BigDecimal strike = roundOptionStrikeToNextN(targetStrike, new BigDecimal(family.getStrikeDistance()), type);

        // symbol / isin
        String symbol = OptionSymbol.getSymbol(family, expirationDate, type, strike, false);
        String isin = OptionSymbol.getIsin(family, expirationDate, type, strike);
        String ric = OptionSymbol.getRic(family, expirationDate, type, strike);

        Option option = Option.Factory.newInstance();
        option.setSymbol(symbol);
        option.setIsin(isin);
        option.setRic(ric);
        option.setStrike(strike);
        option.setExpiration(expiration);
        option.setType(type);
        option.setUnderlying(underlying);
        option.setSecurityFamily(family);

        this.optionDao.save(option);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("created dummy option {}", option);
        }

        return option;

    }

    private BigDecimal roundOptionStrikeToNextN(BigDecimal spot, BigDecimal n, OptionType type) {

        if (OptionType.CALL.equals(type)) {
            // increase by strikeOffset and round to upper n
            return RoundUtil.roundToNextN(spot, n, BigDecimal.ROUND_CEILING);
        } else {
            // reduce by strikeOffset and round to lower n
            return RoundUtil.roundToNextN(spot, n, BigDecimal.ROUND_FLOOR);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Option getOptionByMinExpirationAndMinStrikeDistance(final long underlyingId, final Date targetExpirationDate, final BigDecimal underlyingSpot, final OptionType optionType) {

        Validate.notNull(targetExpirationDate, "Target expiration date is null");
        Validate.notNull(underlyingSpot, "Underlying spot is null");
        Validate.notNull(optionType, "Option type is null");

        Option option = CollectionUtil.getSingleElementOrNull(this.optionDao.findByMinExpirationAndMinStrikeDistance(1, underlyingId, targetExpirationDate, underlyingSpot, optionType));

        // if no stock option was found, create it if simulating options
        if (this.commonConfig.isSimulation() && this.coreConfig.isSimulateOptions()) {

            OptionFamily family = this.optionFamilyDao.findByUnderlying(underlyingId);
            if ((option == null) || Math.abs(option.getStrike().doubleValue() - underlyingSpot.doubleValue()) > family.getStrikeDistance()) {

                option = createDummyOption(family.getId(), targetExpirationDate, underlyingSpot, optionType);
            }
        }

        if (option == null) {
            throw new ServiceException("no option available for expiration " + targetExpirationDate + " strike " + underlyingSpot + " type " + optionType);
        } else {
            return option;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Option getOptionByMinExpirationAndStrikeLimit(final long underlyingId, final Date targetExpirationDate, final BigDecimal underlyingSpot, final OptionType optionType) {

        Validate.notNull(targetExpirationDate, "Target expiration date is null");
        Validate.notNull(underlyingSpot, "Underlying spot is null");
        Validate.notNull(optionType, "Option type is null");

        OptionFamily family = this.optionFamilyDao.findByUnderlying(underlyingId);

        Option option = CollectionUtil.getSingleElementOrNull(this.optionDao.findByMinExpirationAndStrikeLimit(1, underlyingId, targetExpirationDate, underlyingSpot, optionType));

        // if no future was found, create it if simulating options
        if (this.commonConfig.isSimulation() && this.coreConfig.isSimulateOptions()) {
            if ((option == null) || Math.abs(option.getStrike().doubleValue() - underlyingSpot.doubleValue()) > family.getStrikeDistance()) {

                option = createDummyOption(family.getId(), targetExpirationDate, underlyingSpot, optionType);
            }
        }

        if (option == null) {
            throw new ServiceException("no option available for expiration " + targetExpirationDate + " strike " + underlyingSpot + " type " + optionType);
        } else {
            return option;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Option getOptionByMinExpirationAndMinStrikeDistanceWithTicks(final long underlyingId, final Date targetExpirationDate, final BigDecimal underlyingSpot, final OptionType optionType,
            final Date date) {

        Validate.notNull(targetExpirationDate, "Target expiration date is null");
        Validate.notNull(underlyingSpot, "Underlying spot is null");
        Validate.notNull(optionType, "Option type is null");
        Validate.notNull(date, "Date is null");

        return CollectionUtil.getSingleElementOrNull(this.optionDao.findByMinExpirationAndMinStrikeDistanceWithTicks(1, underlyingId, targetExpirationDate, underlyingSpot, optionType, date));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Option getOptionByMinExpirationAndStrikeLimitWithTicks(final long underlyingId, final Date targetExpirationDate, final BigDecimal underlyingSpot, final OptionType optionType, final Date date) {

        Validate.notNull(targetExpirationDate, "Target expiration date is null");
        Validate.notNull(underlyingSpot, "Underlying spot is null");
        Validate.notNull(optionType, "Option type is null");
        Validate.notNull(date, "Date is null");

        return CollectionUtil.getSingleElementOrNull(this.optionDao.findByMinExpirationAndStrikeLimitWithTicks(1, underlyingId, targetExpirationDate, underlyingSpot, optionType, date));

    }

}
