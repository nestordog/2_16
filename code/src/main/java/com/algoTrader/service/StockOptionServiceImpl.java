package com.algoTrader.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.math.MathException;
import org.apache.log4j.Logger;

import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.marketData.TickDao;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.StockOption;
import com.algoTrader.entity.security.StockOptionDao;
import com.algoTrader.entity.security.StockOptionFamily;
import com.algoTrader.entity.security.StockOptionImpl;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.sabr.SABRCalibration;
import com.algoTrader.stockOption.StockOptionSymbol;
import com.algoTrader.stockOption.StockOptionUtil;
import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.vo.AtmVolaVO;
import com.algoTrader.vo.SabrVO;
import com.mathworks.toolbox.javabuilder.MWException;

public class StockOptionServiceImpl extends StockOptionServiceBase {

    private static Logger logger = MyLogger.getLogger(StockOptionServiceImpl.class.getName());

    private static double beta = ConfigurationUtil.getBaseConfig().getDouble("sabrBeta");

    private static SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd-kkmmss");
    private static double MILLISECONDS_PER_YEAR = 31536000000l;
    private static int advanceMinutes = 10;
    private static SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy.MM.dd kk:mm:ss");

    protected StockOption handleCreateDummyStockOption(int stockOptionFamilyId, Date expirationDate, BigDecimal underlayingSpot, OptionType type) throws Exception {

        StockOptionFamily family = getStockOptionFamilyDao().load(stockOptionFamilyId);
        Security underlaying = family.getUnderlaying();

        // set third Friday of the month
        Date expiration = DateUtil.getExpirationDate(family.getExpirationType(), expirationDate);

        BigDecimal strike = RoundUtil.roundStockOptionStrikeToNextN(underlayingSpot, family.getStrikeDistance(), type);

        // symbol / isin
        String symbol = StockOptionSymbol.getSymbol(family, expiration, type, strike);
        String isin = StockOptionSymbol.getIsin(family, expiration, type, strike);

        StockOption stockOption = new StockOptionImpl();
        stockOption.setIsin(isin);
        stockOption.setSymbol(symbol);
        stockOption.setStrike(strike);
        stockOption.setExpiration(expiration);
        stockOption.setType(type);
        stockOption.setUnderlaying(underlaying);
        stockOption.setSecurityFamily(family);

        getStockOptionDao().create(stockOption);

        logger.info("created dummy option " + stockOption.getSymbol());

        return stockOption;
    }

    @SuppressWarnings("unchecked")
    protected void handleCalculateSabr(String isin) throws Exception {

        Security underlaying = getSecurityDao().findByIsin(isin);

        List<Date> dates = (List<Date>) getTickDao().findUniqueDates(TickDao.TRANSFORM_NONE);

        for (Date date : dates) {

            List<Date> expirationDates = (List<Date>) getStockOptionDao().findExpirationsByDate(StockOptionDao.TRANSFORM_NONE, date);

            // only consider the next two expiration dates
            if (expirationDates.size() > 2) {
                expirationDates = expirationDates.subList(0, 2);
            }

            for (Date expirationDate : expirationDates) {

                for (OptionType type :  new OptionType[]{OptionType.PUT, OptionType.CALL} ) {

                    SabrVO SABRparams = null;
                    try {
                        SABRparams = calculateSabrForDate(underlaying, type, date, expirationDate);
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

    protected void handleCalculateSabr(String isin, String startDateString, String expirationDateString, String optionType) throws Exception {

        Security underlaying = getSecurityDao().findByIsin(isin);
        OptionType type = OptionType.fromString(optionType);

        Date startDate = inputFormat.parse(startDateString);
        Date expirationDate = inputFormat.parse(expirationDateString);
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

                SabrVO SABRparams = calculateSabrForDate(underlaying, type, cal.getTime(), expirationDate);

                if (SABRparams != null && SABRparams.getAlpha() < 100) {
                    System.out.print(outputFormat.format(cal.getTime()) + " " + SABRparams.getAlpha() + " " + SABRparams.getRho() + " " + SABRparams.getVolVol());
                }

                AtmVolaVO atmVola = calculateAtmVola(underlaying, cal.getTime());
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

    protected SabrVO handleCalculateSabrForDate(Security underlaying, OptionType type, Date date, Date expirationDate) throws MWException {

        StockOptionFamily family = getStockOptionFamilyDao().findByUnderlaying(underlaying.getId());

        double years = (expirationDate.getTime() - date.getTime()) / MILLISECONDS_PER_YEAR;

        Tick underlayingTick = getTickDao().findByDateAndSecurity(date, underlaying.getId());
        if (underlayingTick == null || underlayingTick.getLast() == null) {
            return null;
        }

        BigDecimal underlayingSpot = underlayingTick.getLast();

        double forward = StockOptionUtil.getForward(underlayingSpot.doubleValue(), years, family.getIntrest(), family.getDividend());
        double atmStrike = RoundUtil.roundStockOptionStrikeToNextN(underlayingSpot, family.getStrikeDistance(), type).doubleValue();

        List<Tick> ticks = getTickDao().findBySecurityDateTypeExpiration(underlaying, date, type, expirationDate);
        List<Double> strikes = new ArrayList<Double>();
        List<Double> currentValues = new ArrayList<Double>();
        List<Double> volatilities = new ArrayList<Double>();
        double atmVola = 0;
        for (Tick tick : ticks) {

            StockOption stockOption = (StockOption) tick.getSecurity();

            try {
                stockOption.validateTick(tick);
            } catch (Exception ex) {
                continue;
            }
            double strike = stockOption.getStrike().doubleValue();
            double currentValue = tick.getCurrentValueDouble();

            try {
                double volatility = StockOptionUtil.getVolatility(underlayingSpot.doubleValue(), stockOption.getStrike().doubleValue(), currentValue, years, family.getIntrest(), family.getDividend(), type);
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
        return sabr.calibrate(strikesArray, volatilitiesArray, atmVola, forward, years, beta);

    }

    protected AtmVolaVO handleCalculateAtmVola(Security underlaying, Date date) throws MathException {

        StockOptionFamily family = getStockOptionFamilyDao().findByUnderlaying(underlaying.getId());

        Tick underlayingTick = getTickDao().findByDateAndSecurity(date, underlaying.getId());
        if (underlayingTick == null || underlayingTick.getLast() == null) {
            return null;
        }

        StockOption callOption = getStockOptionDao().findNearestStockOption(underlaying.getId(), date, underlayingTick.getLast(), "CALL");
        StockOption putOption = getStockOptionDao().findNearestStockOption(underlaying.getId(), date, underlayingTick.getLast(), "PUT");

        Tick callTick = getTickDao().findByDateAndSecurity(date, callOption.getId());
        if (callTick == null || callTick.getBid() == null || callTick.getAsk() == null) {
            return null;
        }

        Tick putTick = getTickDao().findByDateAndSecurity(date, putOption.getId());
        if (putTick == null || putTick.getBid() == null || putTick.getAsk() == null) {
            return null;
        }

        double years = (callOption.getExpiration().getTime() - date.getTime()) / MILLISECONDS_PER_YEAR;

        double callVola = StockOptionUtil.getVolatility(underlayingTick.getCurrentValueDouble(), callOption.getStrike().doubleValue(), callTick.getCurrentValueDouble(), years, family.getIntrest(),family.getDividend(), OptionType.CALL);
        double putVola = StockOptionUtil.getVolatility(underlayingTick.getCurrentValueDouble(), putOption.getStrike().doubleValue(), putTick.getCurrentValueDouble(), years, family.getIntrest(),family.getDividend(), OptionType.PUT);

        return new AtmVolaVO(years, callVola, putVola);
    }
}
