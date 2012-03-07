package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.algoTrader.entity.marketData.Bar;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.marketData.TickImpl;
import com.algoTrader.entity.security.Security;
import com.algoTrader.enumeration.BarType;
import com.algoTrader.enumeration.Period;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.io.CsvTickWriter;

public abstract class HistoricalDataServiceImpl extends HistoricalDataServiceBase {

    private static Logger logger = MyLogger.getLogger(HistoricalDataServiceImpl.class.getName());

    @Override
    protected void handleDownload1MinTicks(int[] securityIds, BarType[] barTypes, Date startDate, Date endDate) throws Exception {

        for (int securityId : securityIds) {

            Security security = getSecurityDao().load(securityId);

            CsvTickWriter writer = new CsvTickWriter(security.getIsin());

            download1MinTicksForSecurity(security, barTypes, startDate, endDate, writer);

            writer.close();
        }
    }

    private void download1MinTicksForSecurity(Security security, BarType[] barTypes, Date startDate, Date endDate, CsvTickWriter writer) throws Exception {

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(startDate);

        Date date;
        while ((date = cal.getTime()).compareTo(endDate) <= 0) {

            if ((cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) && (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)) {

                download1MinTicksForDate(security, date, barTypes, writer);
            }
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private void download1MinTicksForDate(Security security, Date date, BarType[] barTypes, CsvTickWriter writer) throws Exception {

        TreeMap<Date, Tick> timeTickMap = new TreeMap<Date, Tick>();

        // run all barTypes and get the ticks
        for (BarType barType : barTypes) {

            List<Bar> bars = getHistoricalBars(security.getId(), date, 1, Period.DAY, 1, Period.MINUTE, barType);

            // filter Bars by date
            for (Iterator<Bar> it = bars.iterator(); it.hasNext();) {

                Bar bar = it.next();

                // retrieve bars only between marketOpen & close
                if (DateUtil.compareTime(bar.getDateTime(), security.getSecurityFamily().getMarketClose()) > 0
                        || DateUtil.compareTime(bar.getDateTime(), security.getSecurityFamily().getMarketOpen()) < 0) {

                    it.remove();
                    continue;
                }

                // only accept bars within the last 24h
                if (date.getTime() - bar.getDateTime().getTime() > 86400000) {

                    it.remove();
                    continue;
                }
            }

            // to make sure we don't get a pacing error
            Thread.sleep(10000);

            for (Bar bar : bars) {

                Tick tick = timeTickMap.get(bar.getDateTime());

                if (tick == null) {

                    tick = new TickImpl();
                    tick.setSecurity(security);
                    tick.setLast(new BigDecimal(0));
                    tick.setDateTime(bar.getDateTime());
                    tick.setVol(0);
                    tick.setVolBid(0);
                    tick.setVolAsk(0);
                    tick.setBid(new BigDecimal(0));
                    tick.setAsk(new BigDecimal(0));
                    tick.setOpenIntrest(0);
                    tick.setSettlement(new BigDecimal(0));

                    timeTickMap.put(bar.getDateTime(), tick);
                }

                if (BarType.TRADES.equals(barType)) {
                    Entry<Date, Tick> entry = timeTickMap.lowerEntry(bar.getDateTime());
                    if (entry != null) {
                        Tick lastTick = entry.getValue();
                        if (bar.getVol() > 0) {
                            tick.setVol(lastTick.getVol() + bar.getVol());
                            tick.setLastDateTime(bar.getDateTime());
                        } else {
                            tick.setVol(lastTick.getVol());
                            tick.setLastDateTime(lastTick.getLastDateTime());
                        }
                    } else {
                        if (bar.getVol() > 0) {
                            tick.setVol(bar.getVol());
                            tick.setLastDateTime(bar.getDateTime());
                        }
                    }
                    BigDecimal last = bar.getClose();
                    tick.setLast(last);
                } else if (BarType.BID.equals(barType)) {
                    BigDecimal bid = bar.getClose();
                    tick.setBid(bid);
                } else if (BarType.ASK.equals(barType)) {
                    BigDecimal ask = bar.getClose();
                    tick.setAsk(ask);
                } else if (BarType.BID_ASK.equals(barType)) {
                    BigDecimal bid = bar.getOpen();
                    tick.setBid(bid);
                    BigDecimal ask = bar.getClose();
                    tick.setAsk(ask);
                }
            }
        }

        // write the ticks to the csvWriter
        for (Map.Entry<Date, Tick> entry : timeTickMap.entrySet()) {

            Tick tick = entry.getValue();
            writer.write(tick);
        }

        logger.debug("wrote " + timeTickMap.entrySet().size() + " ticks for: " + security.getSymbol() + " on date: " + date);
    }

}
