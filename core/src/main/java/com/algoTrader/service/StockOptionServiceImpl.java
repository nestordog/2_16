/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package com.algoTrader.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.multimap.MultiHashMap;
import org.apache.commons.math.MathException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.security.ImpliedVolatility;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.StockOption;
import com.algoTrader.entity.security.StockOptionFamily;
import com.algoTrader.entity.security.StockOptionImpl;
import com.algoTrader.enumeration.Duration;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.sabr.SABRCalibration;
import com.algoTrader.stockOption.StockOptionSymbol;
import com.algoTrader.stockOption.StockOptionUtil;
import com.algoTrader.util.CollectionUtil;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.vo.ATMVolVO;
import com.algoTrader.vo.SABRSmileVO;
import com.algoTrader.vo.SABRSurfaceVO;
import com.mathworks.toolbox.javabuilder.MWException;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class StockOptionServiceImpl extends StockOptionServiceBase {

    private static Logger logger = MyLogger.getLogger(StockOptionServiceImpl.class.getName());
    private static int advanceMinutes = 10;
    private static SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy.MM.dd kk:mm:ss");

    private @Value("${misc.sabrBeta}") double beta;

    @Override
    protected StockOption handleCreateDummyStockOption(int stockOptionFamilyId, Date expirationDate, BigDecimal targetStrike, OptionType type) throws Exception {

        StockOptionFamily family = getStockOptionFamilyDao().get(stockOptionFamilyId);
        Security underlying = family.getUnderlying();

        // set third Friday of the month
        Date expiration = DateUtil.getExpirationDate(family.getExpirationType(), expirationDate);

        BigDecimal strike = RoundUtil.roundStockOptionStrikeToNextN(targetStrike, family.getStrikeDistance(), type);

        // symbol / isin
        String symbol = StockOptionSymbol.getSymbol(family, expiration, type, strike);
        String isin = StockOptionSymbol.getIsin(family, expiration, type, strike);
        String ric = StockOptionSymbol.getRic(family, expiration, type, strike);

        StockOption stockOption = new StockOptionImpl();
        stockOption.setSymbol(symbol);
        stockOption.setIsin(isin);
        stockOption.setRic(ric);
        stockOption.setStrike(strike);
        stockOption.setExpiration(expiration);
        stockOption.setType(type);
        stockOption.setUnderlying(underlying);
        stockOption.setSecurityFamily(family);

        getStockOptionDao().create(stockOption);

        logger.info("created dummy option " + stockOption);

        return stockOption;
    }

    @Override
    protected void handlePrintSABRByOptionPrice(String isin, Date expirationDate, OptionType optionType, Date startDate) throws Exception {

        Security underlying = getSecurityDao().findByIsin(isin);

        Date closeHour = (new SimpleDateFormat("kkmmss")).parse("172000");

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(startDate);

        SABRCalibration.getInstance();

        while (cal.getTime().compareTo(expirationDate) < 0) {

            if ((cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                cal.add(Calendar.DAY_OF_YEAR, 1);
                continue;
            }

            while (DateUtil.compareTime(cal.getTime(), closeHour) <= 0) {

                System.out.print(outputFormat.format(cal.getTime()));

                SABRSmileVO SABRparams = calibrateSABRSmileByOptionPrice(underlying, optionType, cal.getTime(), expirationDate);

                if (SABRparams != null && SABRparams.getAlpha() < 100) {
                    System.out.print(outputFormat.format(cal.getTime()) + " " + SABRparams.getAlpha() + " " + SABRparams.getRho() + " "
                            + SABRparams.getVolVol());
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

        SABRCalibration.getInstance().dispose();
    }

    @Override
    protected void handlePrintSABRByIVol(String isin, Duration duration, Date startDate, Date endDate) throws Exception {

        Security underlying = getSecurityDao().findByIsin(isin);

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(startDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);

        SABRCalibration.getInstance();

        while (cal.getTime().compareTo(endDate) < 0) {

            if ((cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                cal.add(Calendar.DAY_OF_YEAR, 1);
                continue;
            }

            SABRSmileVO SABRparams = calibrateSABRSmileByIVol(underlying, duration, cal.getTime());

            if (SABRparams != null) {
                System.out.println(outputFormat.format(cal.getTime()) + " " + SABRparams.getAlpha() + " " + SABRparams.getRho() + " " + SABRparams.getVolVol());
            }

            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        SABRCalibration.getInstance().dispose();
    }

    @Override
    protected SABRSmileVO handleCalibrateSABRSmileByOptionPrice(Security underlying, OptionType optionType, Date expirationDate, Date date) throws MWException {

        StockOptionFamily family = getStockOptionFamilyDao().findByUnderlying(underlying.getId());

        double years = (expirationDate.getTime() - date.getTime()) / Duration.YEAR_1.getValue();

        Tick underlyingTick = getTickDao().findByDateAndSecurity(date, underlying.getId());
        if (underlyingTick == null || underlyingTick.getLast() == null) {
            return null;
        }

        BigDecimal underlyingSpot = underlyingTick.getLast();

        double forward = StockOptionUtil.getForward(underlyingSpot.doubleValue(), years, family.getIntrest(), family.getDividend());
        double atmStrike = RoundUtil.roundStockOptionStrikeToNextN(underlyingSpot, family.getStrikeDistance(), optionType).doubleValue();

        List<Tick> ticks = getTickDao().findStockOptionTicksBySecurityDateTypeAndExpirationInclSecurity(underlying, date, optionType, expirationDate);
        List<Double> strikes = new ArrayList<Double>();
        List<Double> currentValues = new ArrayList<Double>();
        List<Double> volatilities = new ArrayList<Double>();
        double atmVola = 0;
        for (Tick tick : ticks) {

            StockOption stockOption = (StockOption) tick.getSecurity();

            if (!tick.isSpreadValid()) {
                continue;
            }

            double strike = stockOption.getStrike().doubleValue();
            double currentValue = tick.getCurrentValueDouble();

            try {
                double volatility = StockOptionUtil.getImpliedVolatility(underlyingSpot.doubleValue(), stockOption.getStrike().doubleValue(), currentValue, years, family.getIntrest(),
                        family.getDividend(), optionType);

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

        return SABRCalibration.getInstance().calibrate(strikesArray, volatilitiesArray, atmVola, forward, years, this.beta);
    }

    @Override
    protected SABRSmileVO handleCalibrateSABRSmileByIVol(Security underlying, Duration duration, Date date) throws MWException {

        Tick underlyingTick = getTickDao().findByDateAndSecurity(date, underlying.getId());
        if (underlyingTick == null || underlyingTick.getLast() == null) {
            return null;
        }

        double underlyingSpot = underlyingTick.getLast().doubleValue();

        List<Tick> ticks = getTickDao().findImpliedVolatilityTicksBySecurityDateAndDuration(underlying, date, duration);
        if (ticks.size() < 3) {
            return null;
        }

        return internalCalibrateSABRByIVol(underlying, duration, ticks, underlyingSpot);
    }

    @Override
    protected SABRSurfaceVO handleCalibrateSABRSurfaceByIVol(Security underlying, Date date) throws MWException {

        Tick underlyingTick = getTickDao().findByDateAndSecurity(date, underlying.getId());
        if (underlyingTick == null || underlyingTick.getLast() == null) {
            return null;
        }

        double underlyingSpot = underlyingTick.getLast().doubleValue();

        List<Tick> allTicks = getTickDao().findImpliedVolatilityTicksBySecurityDate(underlying, date);

        // group by duration
        MultiMap<Duration, Tick> durationMap = new MultiHashMap<Duration, Tick>();
        for (Tick tick : allTicks) {
            ImpliedVolatility impliedVolatility = (ImpliedVolatility) tick.getSecurity();
            durationMap.put(impliedVolatility.getDuration(), tick);
        }

        // process each duration
        SABRSurfaceVO surface = new SABRSurfaceVO();
        for (Duration duration : durationMap.keySet()) {

            Collection<Tick> ticksPerDuration = durationMap.get(duration);

            SABRSmileVO sabr = internalCalibrateSABRByIVol(underlying, duration, ticksPerDuration, underlyingSpot);
            surface.getSmiles().add(sabr);
        }

        return surface;
    }

    private SABRSmileVO internalCalibrateSABRByIVol(Security underlying, Duration duration, Collection<Tick> ticks, double underlyingSpot) throws MWException {

        StockOptionFamily family = getStockOptionFamilyDao().findByUnderlying(underlying.getId());

        double years = duration.getValue() / Duration.YEAR_1.getValue();

        double forward = StockOptionUtil.getForward(underlyingSpot, years, family.getIntrest(), family.getDividend());

        List<Double> strikes = new ArrayList<Double>();
        List<Double> volatilities = new ArrayList<Double>();
        double atmVola = 0;
        for (Tick tick : ticks) {

            ImpliedVolatility impliedVola = (ImpliedVolatility) tick.getSecurity();

            double strike;
            if (OptionType.CALL.equals(impliedVola.getType())) {
                strike = underlyingSpot * (1.0 - impliedVola.getMoneyness());
            } else {
                strike = underlyingSpot * (1.0 + impliedVola.getMoneyness());
            }

            double volatility = tick.getCurrentValueDouble();

            if (impliedVola.getMoneyness() == 0.0) {
                atmVola = volatility;
            }

            strikes.add(strike);
            volatilities.add(volatility);
        }

        Double[] strikesArray = strikes.toArray(new Double[0]);
        Double[] volatilitiesArray = volatilities.toArray(new Double[0]);

        return SABRCalibration.getInstance().calibrate(strikesArray, volatilitiesArray, atmVola, forward, years, this.beta);
    }

    @Override
    protected ATMVolVO handleCalculateATMVol(Security underlying, Date date) throws MathException {

        StockOptionFamily family = getStockOptionFamilyDao().findByUnderlying(underlying.getId());

        Tick underlyingTick = getTickDao().findByDateAndSecurity(date, underlying.getId());
        if (underlyingTick == null || underlyingTick.getLast() == null) {
            return null;
        }

        List<StockOption> callOptions = getStockOptionDao().findByMinExpirationAndStrikeLimit(1, 1, underlying.getId(), date, underlyingTick.getLast(), OptionType.CALL);
        List<StockOption> putOptions = getStockOptionDao().findByMinExpirationAndStrikeLimit(1, 1, underlying.getId(), date, underlyingTick.getLast(), OptionType.PUT);

        StockOption callOption = CollectionUtil.getFirstElementOrNull(callOptions);
        StockOption putOption = CollectionUtil.getFirstElementOrNull(putOptions);

        Tick callTick = getTickDao().findByDateAndSecurity(date, callOption.getId());
        if (callTick == null || callTick.getBid() == null || callTick.getAsk() == null) {
            return null;
        }

        Tick putTick = getTickDao().findByDateAndSecurity(date, putOption.getId());
        if (putTick == null || putTick.getBid() == null || putTick.getAsk() == null) {
            return null;
        }

        double years = (callOption.getExpiration().getTime() - date.getTime()) / Duration.YEAR_1.getValue();

        double callVola = StockOptionUtil.getImpliedVolatility(underlyingTick.getCurrentValueDouble(), callOption.getStrike().doubleValue(),
                callTick.getCurrentValueDouble(), years, family.getIntrest(), family.getDividend(), OptionType.CALL);
        double putVola = StockOptionUtil.getImpliedVolatility(underlyingTick.getCurrentValueDouble(), putOption.getStrike().doubleValue(),
                putTick.getCurrentValueDouble(), years, family.getIntrest(), family.getDividend(), OptionType.PUT);

        return new ATMVolVO(years, callVola, putVola);
    }
}
