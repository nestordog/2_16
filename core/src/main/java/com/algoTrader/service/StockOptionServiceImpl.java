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

import org.apache.commons.math.MathException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.security.ImpliedVolatility;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.StockOption;
import com.algoTrader.entity.security.StockOptionFamily;
import com.algoTrader.entity.security.StockOptionImpl;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.sabr.SABRCalibration;
import com.algoTrader.stockOption.StockOptionSymbol;
import com.algoTrader.stockOption.StockOptionUtil;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.vo.AtmVolaVO;
import com.algoTrader.vo.SabrVO;
import com.mathworks.toolbox.javabuilder.MWException;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class StockOptionServiceImpl extends StockOptionServiceBase {

    private static Logger logger = MyLogger.getLogger(StockOptionServiceImpl.class.getName());
    private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd-kkmmss");
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    private static double MILLISECONDS_PER_YEAR = 31536000000l;
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
    protected void handleCalculateSabr(String isin) throws Exception {

        Security underlying = getSecurityDao().findByIsin(isin);

        Collection<Date> dates = getTickDao().findUniqueDates();

        for (Date date : dates) {

            List<Date> expirationDates = getStockOptionDao().findExpirationsByUnderlyingAndDate(underlying.getId(), date);

            // only consider the next two expiration dates
            if (expirationDates.size() > 2) {
                expirationDates = expirationDates.subList(0, 2);
            }

            for (Date expirationDate : expirationDates) {

                for (OptionType type : new OptionType[] { OptionType.PUT, OptionType.CALL }) {

                    SabrVO SABRparams = null;
                    try {
                        SABRparams = calculateSabrForDate(underlying, type, date, expirationDate);
                    } catch (Exception e) {
                        continue;
                    }

                    if (SABRparams != null && SABRparams.getAlpha() < 100) {

                        System.out.print(outputFormat.format(date) + " " + outputFormat.format(expirationDate) + " " + type + " ");
                        System.out.println(SABRparams.getAlpha() + " " + SABRparams.getRho() + " " + SABRparams.getVolVol());
                    }
                }
            }
        }
        SABRCalibration.getInstance().dispose();
    }

    @Override
    protected void handleCalculateSabr(String isin, String startDateString, String expirationDateString, String optionType) throws Exception {

        Security underlying = getSecurityDao().findByIsin(isin);
        OptionType type = OptionType.fromString(optionType);

        Date startDate = dateTimeFormat.parse(startDateString);
        Date expirationDate = dateTimeFormat.parse(expirationDateString);
        Date closeHour = (new SimpleDateFormat("kkmmss")).parse("172000");

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(startDate);

        while (cal.getTime().compareTo(expirationDate) < 0) {

            if ((cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                cal.add(Calendar.DAY_OF_YEAR, 1);
                continue;
            }

            while (DateUtil.compareTime(cal.getTime(), closeHour) <= 0) {

                System.out.print(outputFormat.format(cal.getTime()));

                SabrVO SABRparams = calculateSabrForDate(underlying, type, cal.getTime(), expirationDate);

                if (SABRparams != null && SABRparams.getAlpha() < 100) {
                    System.out.print(outputFormat.format(cal.getTime()) + " " + SABRparams.getAlpha() + " " + SABRparams.getRho() + " "
                            + SABRparams.getVolVol());
                }

                AtmVolaVO atmVola = calculateAtmVola(underlying, cal.getTime());
                if (atmVola != null) {
                    System.out.print(" " + atmVola.getYears() + " " + atmVola.getCallVola() + " " + atmVola.getPutVola());
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
    protected SabrVO handleCalculateSabrForDate(Security underlying, OptionType type, Date date, Date expirationDate) throws MWException {

        StockOptionFamily family = getStockOptionFamilyDao().findByUnderlying(underlying.getId());

        double years = (expirationDate.getTime() - date.getTime()) / MILLISECONDS_PER_YEAR;

        Tick underlyingTick = getTickDao().findByDateAndSecurity(date, underlying.getId());
        if (underlyingTick == null || underlyingTick.getLast() == null) {
            return null;
        }

        BigDecimal underlyingSpot = underlyingTick.getLast();

        double forward = StockOptionUtil.getForward(underlyingSpot.doubleValue(), years, family.getIntrest(), family.getDividend());
        double atmStrike = RoundUtil.roundStockOptionStrikeToNextN(underlyingSpot, family.getStrikeDistance(), type).doubleValue();

        List<Tick> ticks = getTickDao().findBySecurityDateTypeAndExpirationInclSecurity(underlying, date, type, expirationDate);
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
                double volatility = StockOptionUtil.getImpliedVolatility(underlyingSpot.doubleValue(), stockOption.getStrike().doubleValue(), currentValue, years,
                        family.getIntrest(), family.getDividend(), type);
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

        SABRCalibration sabr = SABRCalibration.getInstance();
        return sabr.calibrate(strikesArray, volatilitiesArray, atmVola, forward, years, this.beta);

    }

    @Override
    protected AtmVolaVO handleCalculateAtmVola(Security underlying, Date date) throws MathException {

        StockOptionFamily family = getStockOptionFamilyDao().findByUnderlying(underlying.getId());

        Tick underlyingTick = getTickDao().findByDateAndSecurity(date, underlying.getId());
        if (underlyingTick == null || underlyingTick.getLast() == null) {
            return null;
        }

        List<StockOption> callOptions = getStockOptionDao().findByMinExpirationAndStrikeLimit(1, 1, underlying.getId(), date, underlyingTick.getLast(), OptionType.CALL);
        List<StockOption> putOptions = getStockOptionDao().findByMinExpirationAndStrikeLimit(1, 1, underlying.getId(), date, underlyingTick.getLast(), OptionType.PUT);

        StockOption callOption = null;
        if (!callOptions.isEmpty()) {
            callOption = callOptions.get(0);
        }

        StockOption putOption = null;
        if (!putOptions.isEmpty()) {
            putOption = putOptions.get(0);
        }

        Tick callTick = getTickDao().findByDateAndSecurity(date, callOption.getId());
        if (callTick == null || callTick.getBid() == null || callTick.getAsk() == null) {
            return null;
        }

        Tick putTick = getTickDao().findByDateAndSecurity(date, putOption.getId());
        if (putTick == null || putTick.getBid() == null || putTick.getAsk() == null) {
            return null;
        }

        double years = (callOption.getExpiration().getTime() - date.getTime()) / MILLISECONDS_PER_YEAR;

        double callVola = StockOptionUtil.getImpliedVolatility(underlyingTick.getCurrentValueDouble(), callOption.getStrike().doubleValue(),
                callTick.getCurrentValueDouble(), years, family.getIntrest(), family.getDividend(), OptionType.CALL);
        double putVola = StockOptionUtil.getImpliedVolatility(underlyingTick.getCurrentValueDouble(), putOption.getStrike().doubleValue(),
                putTick.getCurrentValueDouble(), years, family.getIntrest(), family.getDividend(), OptionType.PUT);

        return new AtmVolaVO(years, callVola, putVola);
    }

    @Override
    protected void handleCalculateSabrByIVol(String isin, String startDateString, String endDateString, String durationString) throws Exception {

        Security underlying = getSecurityDao().findByIsin(isin);

        Date startDate = dateFormat.parse(startDateString);
        Date endDate = dateFormat.parse(endDateString);
        int duration = Integer.parseInt(durationString);

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(startDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);

        while (cal.getTime().compareTo(endDate) < 0) {

            if ((cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                cal.add(Calendar.DAY_OF_YEAR, 1);
                continue;
            }

            SabrVO SABRparams = handleCalculateSabrForDateByIVol(underlying, cal.getTime(), duration);

            if (SABRparams != null) {
                System.out.println(outputFormat.format(cal.getTime()) + " " + SABRparams.getAlpha() + " " + SABRparams.getRho() + " " + SABRparams.getVolVol());
            }

            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        SABRCalibration.getInstance().dispose();

    }

    @Override
    protected SabrVO handleCalculateSabrForDateByIVol(Security underlying, Date date, int duration) throws MWException {

        StockOptionFamily family = getStockOptionFamilyDao().findByUnderlying(underlying.getId());

        double years = duration / 12.0;

        Tick underlyingTick = getTickDao().findByDateAndSecurity(date, underlying.getId());
        if (underlyingTick == null || underlyingTick.getLast() == null) {
            return null;
        }

        double underlyingSpot = underlyingTick.getLast().doubleValue();

        double forward = StockOptionUtil.getForward(underlyingSpot, years, family.getIntrest(), family.getDividend());

        List<Tick> ticks = getTickDao().findBySecurityDateAndDuration(underlying, date, duration);
        if (ticks.size() < 3) {
            return null;
        }

        List<Double> strikes = new ArrayList<Double>();
        List<Double> volatilities = new ArrayList<Double>();
        double atmVola = 0;
        for (Tick tick : ticks) {

            ImpliedVolatility impliedVola = (ImpliedVolatility) tick.getSecurity();

            double strike = underlyingSpot / (2.0 - impliedVola.getMoneyness() / 100.0);
            double volatility = tick.getCurrentValueDouble();

            if (impliedVola.getMoneyness() == 100) {
                atmVola = volatility;
            }

            strikes.add(strike);
            volatilities.add(volatility);
        }

        Double[] strikesArray = strikes.toArray(new Double[0]);
        Double[] volatilitiesArray = volatilities.toArray(new Double[0]);

        SABRCalibration sabr = SABRCalibration.getInstance();
        return sabr.calibrate(strikesArray, volatilitiesArray, atmVola, forward, years, this.beta);
    }
}
