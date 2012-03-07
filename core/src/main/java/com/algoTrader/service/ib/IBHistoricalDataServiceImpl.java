package com.algoTrader.service.ib;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.entity.marketData.Bar;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.marketData.TickImpl;
import com.algoTrader.entity.security.Security;
import com.algoTrader.enumeration.BarType;
import com.algoTrader.enumeration.ConnectionState;
import com.algoTrader.enumeration.Period;
import com.algoTrader.service.HistoricalDataServiceException;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.util.io.CsvTickWriter;
import com.ib.client.Contract;

public class IBHistoricalDataServiceImpl extends IBHistoricalDataServiceBase implements DisposableBean {

    private static final long serialVersionUID = 8656307573474662794L;
    private static Logger logger = MyLogger.getLogger(IBHistoricalDataServiceImpl.class.getName());
    private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd  HH:mm:ss");
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    private @Value("${simulation}") boolean simulation;
    private @Value("#{'${marketChannel}' == 'IB'}") boolean ibEnabled;
    private @Value("${ib.historicalDataServiceEnabled}") boolean historicalDataServiceEnabled;

    private IBClient client;
    private IBDefaultAdapter wrapper;
    private Lock lock = new ReentrantLock();
    private Condition condition = this.lock.newCondition();

    private boolean success;
    private List<Bar> barList;

    private static int clientId = 2;

    @Override
    protected void handleInit() throws Exception {

        if (!this.ibEnabled || this.simulation || !this.historicalDataServiceEnabled) {
            return;
        }

        this.wrapper = new IBDefaultAdapter(clientId) {

            @Override
            public void historicalData(int requestId, String dateString, double open, double high, double low, double close, int volume, int count, double WAP,
                    boolean hasGaps) {

                IBHistoricalDataServiceImpl.this.lock.lock();
                try {

                    if (dateString.startsWith("finished")) {

                        IBHistoricalDataServiceImpl.this.success = true;
                        IBHistoricalDataServiceImpl.this.condition.signalAll();

                        return;
                    }

                    Date date = null;
                    try {
                        date = dateTimeFormat.parse(dateString);
                    } catch (Exception e) {
                        date = dateFormat.parse(dateString);
                    }

                    Bar bar = Bar.Factory.newInstance();
                    bar.setDateTime(date);
                    bar.setOpen(RoundUtil.getBigDecimal(open));
                    bar.setHigh(RoundUtil.getBigDecimal(high));
                    bar.setLow(RoundUtil.getBigDecimal(low));
                    bar.setClose(RoundUtil.getBigDecimal(close));
                    bar.setVol(volume);

                    IBHistoricalDataServiceImpl.this.barList.add(bar);

                    if (hasGaps) {

                        // @formatter:off
                        String message = "bar with gaps " + dateString +
                                " open=" + open +
                                " high=" + high +
                                " low=" + low +
                                " close=" + close +
                                " volume=" + volume +
                                " count=" + count +
                                " WAP=" + WAP +
                                " hasGaps=" + hasGaps;
                        // @formatter:on

                        logger.error(message, new HistoricalDataServiceException(message));
                    }

                } catch (Exception e) {
                    throw new HistoricalDataServiceException(e);
                } finally {
                    IBHistoricalDataServiceImpl.this.lock.unlock();
                }
            }

            @Override
            public void connectionClosed() {

                super.connectionClosed();

                connect();
            }

            @Override
            public void error(int requestId, int code, String errorMsg) {

                // 165 Historical data farm is connected
                // 162 Historical market data Service error message.
                // 2105 A historical data farm is disconnected.
                // 2107 A historical data farm connection has become inactive
                // but should be available upon demand.
                if (code == 162 || code == 165 || code == 2105 || code == 2106 || code == 2107) {

                    IBHistoricalDataServiceImpl.this.lock.lock();
                    try {

                        if (code == 2105 || code == 2107) {

                            super.error(requestId, code, errorMsg);
                            IBHistoricalDataServiceImpl.this.success = false;
                        }

                        if (code == 162) {

                            logger.warn("HMDS query returned no data");
                            IBHistoricalDataServiceImpl.this.success = true;
                        }

                        IBHistoricalDataServiceImpl.this.condition.signalAll();
                    } finally {
                        IBHistoricalDataServiceImpl.this.lock.unlock();
                    }
                } else {
                    super.error(requestId, code, errorMsg);
                }
            }
        };

        this.client = new IBClient(clientId, this.wrapper);

        connect();
    }

    @Override
    protected void handleConnect() {

        if (!this.ibEnabled || this.simulation || !this.historicalDataServiceEnabled) {
            return;
        }

        this.success = false;

        this.client.connect();
    }

    @Override
    protected synchronized List<Bar> handleGetHistoricalBars(int securityId, Date endDate, int duration, Period durationPeriod, int barSize,
            Period barSizePeriod, BarType barType) throws Exception {

        this.barList = new ArrayList<Bar>();
        this.success = false;

        Security security = getSecurityDao().load(securityId);

        this.lock.lock();

        try {

            Contract contract = IBUtil.getContract(security);
            int requestId = RequestIDGenerator.singleton().getNextRequestId();
            String dateString = dateTimeFormat.format(endDate);

            String durationString = duration + " ";
            switch (durationPeriod) {
                case MILLISECOND:
                    throw new IllegalArgumentException("MILLISECOND durationPeriod is not allowed");
                case SECOND:
                    durationString += "S";
                    break;
                case MINUTE:
                    throw new IllegalArgumentException("MINUTE durationPeriod is not allowed");
                case HOUR:
                    throw new IllegalArgumentException("HOUR durationPeriod is not allowed");
                case DAY:
                    durationString += "D";
                    break;
                case WEEK:
                    durationString += "W";
                    break;
                case YEAR:
                    durationString += "Y";
                    break;
            }

            String barSizeString = barSize + " ";
            switch (barSizePeriod) {
                case MILLISECOND:
                    throw new IllegalArgumentException("MILLISECOND barSize is not allowed");
                case SECOND:
                    barSizeString += "sec";
                    break;
                case MINUTE:
                    barSizeString += "min";
                    break;
                case HOUR:
                    barSizeString += "hour";
                    break;
                case DAY:
                    barSizeString += "day";
                    break;
                case WEEK:
                    throw new IllegalArgumentException("WEEK barSize is not allowed");
                case YEAR:
                    throw new IllegalArgumentException("YEAR barSize is not allowed");
            }

            if (barSize > 1) {
                barSizeString += "s";
            }

            this.client.reqHistoricalData(requestId, contract, dateString, durationString, barSizeString, barType.getValue(), 1, 1);

            while (this.success == false) {
                this.condition.await();
            }

        } finally {
            this.lock.unlock();
        }

        for (Bar bar : this.barList) {
            bar.setSecurity(security);
        }

        return this.barList;
    }

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
            for (Iterator<Bar> it = this.barList.iterator(); it.hasNext();) {

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

    @Override
    protected ConnectionState handleGetConnectionState() {

        if (this.wrapper == null) {
            return ConnectionState.DISCONNECTED;
        } else {
            return this.wrapper.getState();
        }
    }

    @Override
    public void destroy() throws Exception {

        if (this.client != null) {
            this.client.disconnect();
        }
    }
}
