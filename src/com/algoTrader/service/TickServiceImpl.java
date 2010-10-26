package com.algoTrader.service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.supercsv.exception.SuperCSVException;

import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.Tick;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.sabr.SABRCalibration;
import com.algoTrader.sabr.SABRCalibrationParams;
import com.algoTrader.stockOption.StockOptionUtil;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.EsperService;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.PropertiesUtil;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.util.csv.CsvTickReader;
import com.algoTrader.util.csv.CsvTickWriter;

public abstract class TickServiceImpl extends TickServiceBase {

    private static Logger logger = MyLogger.getLogger(TickServiceImpl.class.getName());

    private static String dataSet = PropertiesUtil.getProperty("strategie.dataSet");
    private static double intrest = PropertiesUtil.getDoubleProperty("strategie.intrest");
    private static double dividend = PropertiesUtil.getDoubleProperty("strategie.dividend");
    private static double beta = PropertiesUtil.getDoubleProperty("strategie.beta");
    private static int strikeDistance = PropertiesUtil.getIntProperty("strategie.strikeDistance");

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd kk:mm");
    private static SimpleDateFormat hourFormat = new SimpleDateFormat("kk:mm:ss");
    private static double MILLISECONDS_PER_YEAR = 31536000000l;
    private static int advanceMinutes = 10;

    private Map<Security, CsvTickWriter> csvWriters = new HashMap<Security, CsvTickWriter>();

    @SuppressWarnings("unchecked")
    protected void handleProcessSecuritiesOnWatchlist() throws SuperCSVException, IOException  {

        List<Security> securities = getSecurityDao().findSecuritiesOnWatchlist();
        for (Security security : securities) {

            // retrieve ticks only between marketOpen & close
            if (DateUtil.compareToTime(security.getMarketOpen()) >= 0 &&
                DateUtil.compareToTime(security.getMarketClose()) <= 0) {

                Tick tick = retrieveTick(security);

                // if we hit a timeout, we get null
                if (tick != null) {

                    try {
                        tick.validate();
                        EsperService.sendEvent(tick);
                    } catch (Exception e) {
                        // do nothing, just ignore invalideTicks
                    }

                    // write the tick to file (even if not valid)
                    CsvTickWriter csvWriter;
                    if (this.csvWriters.containsKey(security)) {
                        csvWriter = this.csvWriters.get(security);
                    } else {
                        csvWriter = new CsvTickWriter(security.getIsin());
                        this.csvWriters.put(security, csvWriter);
                    }
                    csvWriter.write(tick);
                }
            }
        }
    }

    protected void handlePutOnWatchlist(int stockOptionId) throws Exception {

        StockOption stockOption = (StockOption)getStockOptionDao().load(stockOptionId);
        putOnWatchlist(stockOption);
    }

    @SuppressWarnings("unchecked")
    protected void handlePutOnWatchlist(StockOption stockOption) throws Exception {

        if (!stockOption.isOnWatchlist()) {

            putOnExternalWatchlist(stockOption);

            stockOption.setOnWatchlist(true);
            getStockOptionDao().update(stockOption);
            getStockOptionDao().getStockOptionsOnWatchlist(false).add(stockOption);

            logger.info("put stockOption on watchlist " + stockOption.getSymbol());
        }
    }

    protected void handleRemoveFromWatchlist(int stockOptionId) throws Exception {

        StockOption stockOption = (StockOption)getStockOptionDao().load(stockOptionId);
        removeFromWatchlist(stockOption);
    }

    protected void handleRemoveFromWatchlist(StockOption stockOption) throws Exception {

        if (stockOption.isOnWatchlist()) {

            removeFromExternalWatchlist(stockOption);

            stockOption.setOnWatchlist(false);
            getStockOptionDao().update(stockOption);
            getStockOptionDao().getStockOptionsOnWatchlist(false).remove(stockOption);

            logger.info("removed stockOption from watchlist " + stockOption.getSymbol());
        }
    }

    /**
     * must be run with simulation=false (to get correct values for bid, ask and settlement)
     * also recommended to turn of ehache on commandline (to avoid out of memory error)
     */
    protected void handleImportTicks() throws Exception {

        File directory = new File("results/tickdata/" + dataSet);
        for (File file : directory.listFiles()) {

            if (!file.getName().endsWith(".csv")) continue;

            String isin = file.getName().split("\\.")[0];

            Security security = getSecurityDao().findByISIN(isin);
            CsvTickReader reader = new CsvTickReader(isin);

            Tick tick;
            List<Tick> ticks = new ArrayList<Tick>();
            while ((tick = reader.readTick()) != null) {

                if (tick.getLast().equals(new BigDecimal(0)))
                    tick.setLast(null);

                tick.setSecurity(security);
                ticks.add(tick);

            }
            getTickDao().create(ticks);
            System.out.println("imported ticks for: " + isin);
            System.gc();
        }
    }

    @SuppressWarnings("unchecked")
    protected void handleCalculateSabr() throws Exception {

        Security underlaying = getSecurityDao().findByISIN("CH0008616382");
        OptionType type = OptionType.PUT;
        Date startDate = dateFormat.parse("2010.07.19 09:00");
        Date expirationDate = dateFormat.parse("2010.10.15 13:00");
        Date closeHour = hourFormat.parse("17:20:00");

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
                if (underlayingTick == null) {
                    cal.add(Calendar.MINUTE, advanceMinutes);
                    continue;
                }

                BigDecimal underlayingSpot = underlayingTick.getLast();
                double forward = StockOptionUtil.getForward(underlayingSpot.doubleValue(), years, intrest, dividend);
                double atmStrike = RoundUtil.roundToNextN(underlayingSpot, strikeDistance, type).doubleValue();

                List<Tick> ticks = getTickDao().findByDate(date, type);
                List<Double> strikes = new ArrayList<Double>();
                List<Double> volatilities = new ArrayList<Double>();
                double atmVola = 0;
                for (Tick tick : ticks) {

                    try {
                        tick.validateSpread();
                    } catch (Exception ex) {
                        continue;
                    }
                    StockOption stockOption = (StockOption) tick.getSecurity();
                    double strike = stockOption.getStrike().doubleValue();
                    double optionValue = tick.getCurrentValueDouble();

                    try {
                        double volatility = StockOptionUtil.getVolatility(underlayingSpot.doubleValue(), strike, optionValue, years, type);
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
                    System.out.println(dateFormat.format(date) + " " + params.getA() + " " + params.getR() + " " + params.getV());
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
