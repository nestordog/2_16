package com.algoTrader.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.StockOptionFamily;
import com.algoTrader.entity.Tick;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.sabr.SABRCalibration;
import com.algoTrader.sabr.SABRCalibrationParams;
import com.algoTrader.stockOption.StockOptionUtil;
import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.RoundUtil;

public abstract class StockOptionRetrieverServiceImpl extends StockOptionRetrieverServiceBase {

    private static double beta = ConfigurationUtil.getBaseConfig().getDouble("sabrBeta");

    private static SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd-kkmmss");
    private static double MILLISECONDS_PER_YEAR = 31536000000l;
    private static int advanceMinutes = 10;
    private static SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy.MM.dd kk:mm:ss");

    @SuppressWarnings("unchecked")
    protected void handleCalculateSabr(String isin, String startDateString, String expirationDateString, String optionType) throws Exception {

        Security underlaying = getSecurityDao().findByIsin(isin);
        StockOptionFamily family = getStockOptionFamilyDao().findByUnderlaying(underlaying.getId());

        OptionType type = OptionType.fromString(optionType);
        Date startDate = inputFormat.parse(startDateString);
        Date expirationDate = inputFormat.parse(expirationDateString);
        Date closeHour = (new SimpleDateFormat("kkmmss")).parse("172000");

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(startDate);

        SABRCalibration sabr = SABRCalibration.getInstance();

        while (cal.getTime().compareTo(expirationDate) < 0) {

            if ((cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                cal.add(Calendar.DAY_OF_YEAR, 1);
                continue;
            }

            Date date = cal.getTime();

            while (DateUtil.compareTime(cal.getTime(), closeHour) <= 0) {

                date = cal.getTime();

                double years = (expirationDate.getTime() - date.getTime()) / MILLISECONDS_PER_YEAR;

                Tick underlayingTick = getTickDao().findByDateAndSecurity(date, underlaying);
                if (underlayingTick == null || underlayingTick.getLast() == null) {
                    cal.add(Calendar.MINUTE, advanceMinutes);
                    continue;
                }

                BigDecimal underlayingSpot = underlayingTick.getLast();
                double forward = StockOptionUtil.getForward(underlayingSpot.doubleValue(), years, family.getIntrest(), family.getDividend());
                double atmStrike = RoundUtil.roundToNextN(underlayingSpot, family.getStrikeDistance(), type).doubleValue();

                List<Tick> ticks = getTickDao().findByDateTypeAndExpiration(date, type, expirationDate);
                List<Double> strikes = new ArrayList<Double>();
                List<Double> volatilities = new ArrayList<Double>();
                double atmVola = 0;
                for (Tick tick : ticks) {

                    StockOption stockOption = (StockOption) tick.getSecurity();

                    try {
                        stockOption.validateTickSpread(tick);
                    } catch (Exception ex) {
                        continue;
                    }
                    double strike = stockOption.getStrike().doubleValue();
                    double currentValue = tick.getCurrentValueDouble();

                    try {
                        double volatility = StockOptionUtil.getVolatility(underlayingSpot.doubleValue(), stockOption.getStrike().doubleValue(), currentValue, years, family.getIntrest(), family.getDividend(), type);
                        strikes.add(strike);
                        volatilities.add(volatility);

                        if (atmStrike == strike) {
                            atmVola = volatility;
                        }
                    } catch (Exception e) {
                        // do nothing
                    }
                }

                if (strikes.size() < 10 || atmVola == 0) {
                    cal.add(Calendar.MINUTE, advanceMinutes);
                    continue;
                }

                Double[] strikesArray = strikes.toArray(new Double[0]);
                Double[] volatilitiesArray = volatilities.toArray(new Double[0]);

                SABRCalibrationParams params = sabr.calibrate(strikesArray, volatilitiesArray, atmVola, forward, years, beta);

                if (params.getA() < 100) {
                    System.out.println(outputFormat.format(date) + " " + params.getA() + " " + params.getR() + " " + params.getV());
                }

                cal.add(Calendar.MINUTE, advanceMinutes);
            }

            cal.add(Calendar.DAY_OF_YEAR, 1);
            cal.set(Calendar.HOUR_OF_DAY, 9);
            cal.set(Calendar.MINUTE, 00);
        }

        sabr.dispose();
    }
}
