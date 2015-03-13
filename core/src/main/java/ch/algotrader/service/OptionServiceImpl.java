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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.multimap.MultiHashMap;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.math.MathException;
import org.apache.commons.math.util.MathUtils;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.CoreConfig;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.PositionDao;
import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.SubscriptionDao;
import ch.algotrader.entity.marketData.MarketDataEvent;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.marketData.TickDao;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.FutureFamily;
import ch.algotrader.entity.security.FutureFamilyDao;
import ch.algotrader.entity.security.ImpliedVolatility;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.OptionDao;
import ch.algotrader.entity.security.OptionFamily;
import ch.algotrader.entity.security.OptionFamilyDao;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityDao;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyDao;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.OptionType;
import ch.algotrader.enumeration.Side;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.esper.callback.TickCallback;
import ch.algotrader.option.OptionSymbol;
import ch.algotrader.option.OptionUtil;
import ch.algotrader.option.SABR;
import ch.algotrader.option.SABRException;
import ch.algotrader.util.DateUtil;
import ch.algotrader.util.RoundUtil;
import ch.algotrader.util.collection.CollectionUtil;
import ch.algotrader.vo.ATMVolVO;
import ch.algotrader.vo.SABRSmileVO;
import ch.algotrader.vo.SABRSurfaceVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@Transactional
public class OptionServiceImpl implements OptionService {

    private static Logger logger = Logger.getLogger(OptionServiceImpl.class.getName());
    private static int advanceMinutes = 10;
    private static SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy.MM.dd kk:mm:ss");

    private final CommonConfig commonConfig;

    private final CoreConfig coreConfig;

    private final MarketDataService marketDataService;

    private final FutureService futureService;

    private final OrderService orderService;

    private final LocalLookupService localLookupService;

    private final SecurityDao securityDao;

    private final TickDao tickDao;

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
            final TickDao tickDao,
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
        Validate.notNull(tickDao, "TickDao is null");
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
        this.tickDao = tickDao;
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
    public void hedgeDelta(final int underlyingId) {

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

                    logger.info("no delta hedge necessary on " + underlying);
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
    public Option createOTCOption(final int optionFamilyId, final Date expirationDate, final BigDecimal strike, final OptionType type) {

        Validate.notNull(expirationDate, "Expiration date is null");
        Validate.notNull(strike, "Strike is null");
        Validate.notNull(type, "Type is null");

        OptionFamily family = this.optionFamilyDao.get(optionFamilyId);
        Security underlying = family.getUnderlying();

        // symbol / isin
        String symbol = OptionSymbol.getSymbol(family, expirationDate, type, strike, true);

        Option option = Option.Factory.newInstance();
        option.setSymbol(symbol);
        option.setStrike(strike);
        option.setExpiration(expirationDate);
        option.setType(type);
        option.setUnderlying(underlying);
        option.setSecurityFamily(family);

        this.optionDao.save(option);

        logger.info("created OTC option " + option);

        return option;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Option createDummyOption(final int optionFamilyId, final Date targetExpirationDate, final BigDecimal targetStrike, final OptionType type) {

        Validate.notNull(targetExpirationDate, "Target expiration date is null");
        Validate.notNull(targetStrike, "Target strike is null");
        Validate.notNull(type, "Type is null");

        OptionFamily family = this.optionFamilyDao.get(optionFamilyId);
        Security underlying = family.getUnderlying();

        // get next expiration date after targetExpirationDate according to expirationType
        Date expirationDate = DateUtil.getExpirationDate(family.getExpirationType(), targetExpirationDate);

        // get nearest strike according to strikeDistance
        BigDecimal strike = roundOptionStrikeToNextN(targetStrike, family.getStrikeDistance(), type);

        // symbol / isin
        String symbol = OptionSymbol.getSymbol(family, expirationDate, type, strike, false);
        String isin = OptionSymbol.getIsin(family, expirationDate, type, strike);
        String ric = OptionSymbol.getRic(family, expirationDate, type, strike);

        Option option = Option.Factory.newInstance();
        option.setSymbol(symbol);
        option.setIsin(isin);
        option.setRic(ric);
        option.setStrike(strike);
        option.setExpiration(expirationDate);
        option.setType(type);
        option.setUnderlying(underlying);
        option.setSecurityFamily(family);

        this.optionDao.save(option);

        logger.info("created dummy option " + option);

        return option;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printSABRSmileByOptionPrice(final String isin, final Date expirationDate, final OptionType optionType, final Date startDate) {

        Validate.notEmpty(isin, "isin is empty");
        Validate.notNull(expirationDate, "Expiration date is null");
        Validate.notNull(optionType, "Option type is null");
        Validate.notNull(startDate, "Start date is null");

        Security underlying = this.securityDao.findByIsin(isin);

        Date closeHour;
        try {
            closeHour = (new SimpleDateFormat("kkmmss")).parse("172000");
        } catch (ParseException ex) {
            throw new OptionServiceException(ex);
        }

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(startDate);

        while (cal.getTime().compareTo(expirationDate) < 0) {

            if ((cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                cal.add(Calendar.DAY_OF_YEAR, 1);
                continue;
            }

            while (DateUtil.compareTime(cal.getTime(), closeHour) <= 0) {

                System.out.print(outputFormat.format(cal.getTime()));

                SABRSmileVO SABRparams = calibrateSABRSmileByOptionPrice(underlying.getId(), optionType, cal.getTime(), expirationDate);

                if (SABRparams != null && SABRparams.getAlpha() < 100) {
                    System.out.print(outputFormat.format(cal.getTime()) + " " + SABRparams.getAlpha() + " " + SABRparams.getRho() + " " + SABRparams.getVolVol());
                }

                ATMVolVO atmVola = calculateATMVol(underlying, cal.getTime());
                if (atmVola != null) {
                    System.out.print(" " + atmVola.getYears() + " " + atmVola.getCallVol() + " " + atmVola.getPutVol());
                }

                System.out.println();

                cal.add(Calendar.MINUTE, advanceMinutes);
            }

            cal.add(Calendar.DAY_OF_YEAR, 1);
            cal.set(Calendar.HOUR_OF_DAY, 9);
            cal.set(Calendar.MINUTE, 00);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printSABRSmileByIVol(final String isin, final Duration duration, final Date startDate, final Date endDate) {

        Validate.notEmpty(isin, "isin is empty");
        Validate.notNull(duration, "Duration is null");
        Validate.notNull(startDate, "Start date is null");
        Validate.notNull(endDate, "End date is null");

        Security underlying = this.securityDao.findByIsin(isin);

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(startDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);

        while (cal.getTime().compareTo(endDate) < 0) {

            if ((cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                cal.add(Calendar.DAY_OF_YEAR, 1);
                continue;
            }

            SABRSmileVO SABRparams = calibrateSABRSmileByIVol(underlying.getId(), duration, cal.getTime());

            if (SABRparams != null) {
                System.out.println(outputFormat.format(cal.getTime()) + " " + SABRparams.getAlpha() + " " + SABRparams.getRho() + " " + SABRparams.getVolVol());
            }

            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SABRSmileVO calibrateSABRSmileByOptionPrice(final int underlyingId, final OptionType type, final Date expirationDate, final Date date) {

        Validate.notNull(type, "Type is null");
        Validate.notNull(expirationDate, "Expiration date is null");
        Validate.notNull(date, "Date is null");

        OptionFamily family = this.optionFamilyDao.findByUnderlying(underlyingId);

        double years = (expirationDate.getTime() - date.getTime()) / (double) Duration.YEAR_1.getValue();

        Tick underlyingTick = this.tickDao.findBySecurityAndMaxDate(underlyingId, date);
        if (underlyingTick == null || underlyingTick.getLast() == null) {
            return null;
        }

        BigDecimal underlyingSpot = underlyingTick.getLast();

        double forward = OptionUtil.getForward(underlyingSpot.doubleValue(), years, family.getIntrest(), family.getDividend());
        double atmStrike = roundOptionStrikeToNextN(underlyingSpot, family.getStrikeDistance(), type).doubleValue();

        List<Tick> ticks = this.tickDao.findOptionTicksBySecurityDateTypeAndExpirationInclSecurity(underlyingId, date, type, expirationDate);
        List<Double> strikes = new ArrayList<Double>();
        List<Double> currentValues = new ArrayList<Double>();
        List<Double> volatilities = new ArrayList<Double>();
        double atmVola = 0;
        for (Tick tick : ticks) {

            Option option = (Option) tick.getSecurity();

            double strike = option.getStrike().doubleValue();
            double currentValue = tick.getCurrentValueDouble();

            try {
                double volatility = OptionUtil.getImpliedVolatility(underlyingSpot.doubleValue(), option.getStrike().doubleValue(), currentValue, years, family.getIntrest(), family.getDividend(),
                        type);

                strikes.add(strike);
                currentValues.add(currentValue);
                volatilities.add(volatility);

                if (atmStrike == strike) {
                    atmVola = volatility;
                }
            } catch (Exception e) {
                // do nothing
            }
        }

        if (strikes.size() < 10 || atmVola == 0) {
            return null;
        }

        Double[] strikesArray = strikes.toArray(new Double[0]);
        Double[] volatilitiesArray = volatilities.toArray(new Double[0]);

        try {
            return SABR.calibrate(strikesArray, volatilitiesArray, atmVola, forward, years);
        } catch (SABRException ex) {
            throw new OptionServiceException(ex);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SABRSmileVO calibrateSABRSmileByIVol(final int underlyingId, final Duration duration, final Date date) {

        Validate.notNull(duration, "Duration is null");
        Validate.notNull(date, "Date is null");

        Tick underlyingTick = this.tickDao.findBySecurityAndMaxDate(underlyingId, date);
        if (underlyingTick == null || underlyingTick.getLast() == null) {
            return null;
        }

        double underlyingSpot = underlyingTick.getLast().doubleValue();

        List<Tick> ticks = this.tickDao.findImpliedVolatilityTicksBySecurityDateAndDuration(underlyingId, date, duration);
        if (ticks.size() < 3) {
            return null;
        }

        return internalCalibrateSABRByIVol(underlyingId, duration, ticks, underlyingSpot);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SABRSurfaceVO calibrateSABRSurfaceByIVol(final int underlyingId, final Date date) {

        Validate.notNull(date, "Date is null");
        Tick underlyingTick = this.tickDao.findBySecurityAndMaxDate(underlyingId, date);
        if (underlyingTick == null || underlyingTick.getLast() == null) {
            return null;
        }

        double underlyingSpot = underlyingTick.getLast().doubleValue();

        List<Tick> allTicks = this.tickDao.findImpliedVolatilityTicksBySecurityAndDate(underlyingId, date);

        // group by duration
        MultiMap<Duration, Tick> durationMap = new MultiHashMap<Duration, Tick>();
        for (Tick tick : allTicks) {
            ImpliedVolatility impliedVolatility = (ImpliedVolatility) tick.getSecurity();
            durationMap.put(impliedVolatility.getDuration(), tick);
        }

        // sort durations ascending
        TreeSet<Duration> durations = new TreeSet<Duration>(new Comparator<Duration>() {
            @Override
            public int compare(Duration d1, Duration d2) {
                return ((d1.getValue() == d2.getValue()) ? 0 : (d1.getValue() < d2.getValue()) ? -1 : 1);
            }
        });
        durations.addAll(durationMap.keySet());

        // process each duration
        SABRSurfaceVO surface = new SABRSurfaceVO();
        for (Duration duration : durations) {

            Collection<Tick> ticksPerDuration = durationMap.get(duration);

            SABRSmileVO sabr;
            sabr = internalCalibrateSABRByIVol(underlyingId, duration, ticksPerDuration, underlyingSpot);

            surface.getSmiles().add(sabr);
        }

        return surface;

    }

    private SABRSmileVO internalCalibrateSABRByIVol(int underlyingId, Duration duration, Collection<Tick> ticks, double underlyingSpot) {

        // we need the OptionFamily because the IVol Family does not have intrest and dividend
        OptionFamily family = this.optionFamilyDao.findByUnderlying(underlyingId);

        double years = (double) duration.getValue() / Duration.YEAR_1.getValue();

        double forward = OptionUtil.getForward(underlyingSpot, years, family.getIntrest(), family.getDividend());

        List<Double> strikes = new ArrayList<Double>();
        List<Double> volatilities = new ArrayList<Double>();
        double atmVola = 0;
        for (Tick tick : ticks) {

            ImpliedVolatility impliedVola = (ImpliedVolatility) tick.getSecurity();

            double volatility = tick.getCurrentValueDouble();
            double strike = 0;
            if (impliedVola.getDelta() != null) {

                if (impliedVola.getDelta() == 0.5) {
                    atmVola = volatility;
                    strike = forward;
                } else {
                    strike = OptionUtil.getStrikeByDelta(impliedVola.getDelta(), volatility, years, forward, family.getIntrest(), impliedVola.getType());
                }

            } else if (impliedVola.getMoneyness() != null) {

                if (impliedVola.getMoneyness() == 0.0) {
                    atmVola = volatility;
                    strike = forward;
                } else {
                    if (OptionType.CALL.equals(impliedVola.getType())) {
                        strike = underlyingSpot * (1.0 - impliedVola.getMoneyness());
                    } else {
                        strike = underlyingSpot * (1.0 + impliedVola.getMoneyness());
                    }
                }
            } else {
                throw new IllegalArgumentException("either moneyness or delta is needed for SABR calibration");
            }

            strikes.add(strike);
            volatilities.add(volatility);
        }

        Double[] strikesArray = strikes.toArray(new Double[0]);
        Double[] volatilitiesArray = volatilities.toArray(new Double[0]);

        try {
            return SABR.calibrate(strikesArray, volatilitiesArray, atmVola, forward, years);
        } catch (SABRException ex) {
            throw new OptionServiceException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ATMVolVO calculateATMVol(final Security underlying, final Date date) {

        Validate.notNull(underlying, "Underlying is null");
        Validate.notNull(date, "Date is null");

        OptionFamily family = this.optionFamilyDao.findByUnderlying(underlying.getId());

        Tick underlyingTick = this.tickDao.findBySecurityAndMaxDate(underlying.getId(), date);
        if (underlyingTick == null || underlyingTick.getLast() == null) {
            return null;
        }

        List<Option> callOptions = this.optionDao.findByMinExpirationAndStrikeLimit(1, underlying.getId(), date, underlyingTick.getLast(), OptionType.CALL);
        List<Option> putOptions = this.optionDao.findByMinExpirationAndStrikeLimit(1, underlying.getId(), date, underlyingTick.getLast(), OptionType.PUT);

        Option callOption = CollectionUtil.getFirstElementOrNull(callOptions);
        Option putOption = CollectionUtil.getFirstElementOrNull(putOptions);

        Tick callTick = this.tickDao.findBySecurityAndMaxDate(callOption.getId(), date);
        if (callTick == null || callTick.getBid() == null || callTick.getAsk() == null) {
            return null;
        }

        Tick putTick = this.tickDao.findBySecurityAndMaxDate(putOption.getId(), date);
        if (putTick == null || putTick.getBid() == null || putTick.getAsk() == null) {
            return null;
        }

        double years = (callOption.getExpiration().getTime() - date.getTime()) / (double) Duration.YEAR_1.getValue();

        double callVola, putVola;
        try {
            callVola = OptionUtil.getImpliedVolatility(underlyingTick.getCurrentValueDouble(), callOption.getStrike().doubleValue(), callTick.getCurrentValueDouble(), years, family.getIntrest(),
                    family.getDividend(), OptionType.CALL);
            putVola = OptionUtil.getImpliedVolatility(underlyingTick.getCurrentValueDouble(), putOption.getStrike().doubleValue(), putTick.getCurrentValueDouble(), years, family.getIntrest(),
                    family.getDividend(), OptionType.PUT);
        } catch (MathException ex) {
            throw new OptionServiceException(ex);
        }

        return new ATMVolVO(years, callVola, putVola);

    }

    private BigDecimal roundOptionStrikeToNextN(BigDecimal spot, double n, OptionType type) {

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
    public Option getOptionByMinExpirationAndMinStrikeDistance(final int underlyingId, final Date targetExpirationDate, final BigDecimal underlyingSpot, final OptionType optionType) {

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
            throw new LookupServiceException("no option available for expiration " + targetExpirationDate + " strike " + underlyingSpot + " type " + optionType);
        } else {
            return option;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Option getOptionByMinExpirationAndStrikeLimit(final int underlyingId, final Date targetExpirationDate, final BigDecimal underlyingSpot, final OptionType optionType) {

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
            throw new LookupServiceException("no option available for expiration " + targetExpirationDate + " strike " + underlyingSpot + " type " + optionType);
        } else {
            return option;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Option getOptionByMinExpirationAndMinStrikeDistanceWithTicks(final int underlyingId, final Date targetExpirationDate, final BigDecimal underlyingSpot, final OptionType optionType,
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
    public Option getOptionByMinExpirationAndStrikeLimitWithTicks(final int underlyingId, final Date targetExpirationDate, final BigDecimal underlyingSpot, final OptionType optionType, final Date date) {

        Validate.notNull(targetExpirationDate, "Target expiration date is null");
        Validate.notNull(underlyingSpot, "Underlying spot is null");
        Validate.notNull(optionType, "Option type is null");
        Validate.notNull(date, "Date is null");

        return CollectionUtil.getSingleElementOrNull(this.optionDao.findByMinExpirationAndStrikeLimitWithTicks(1, underlyingId, targetExpirationDate, underlyingSpot, optionType, date));

    }

}
