package com.algoTrader.service.ib;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.algoTrader.entity.Security;
import com.algoTrader.entity.Tick;
import com.algoTrader.entity.TickImpl;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.PropertiesUtil;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.util.csv.CsvTickWriter;
import com.ib.client.Contract;

public class IbHistoricalDataServiceImpl extends IbHistoricalDataServiceBase {

    private static Logger logger = MyLogger.getLogger(IbHistoricalDataServiceImpl.class.getName());

    private static int retrievalTimeout = PropertiesUtil.getIntProperty("ib.retrievalTimeout");

    private static SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd  HH:mm:ss");

    private DefaultClientSocket client;
    private DefaultWrapper wrapper;
    private Lock lock = new ReentrantLock();
    private Condition condition = this.lock.newCondition();

    private Map<Integer, Boolean> requestIdBooleanMap;
    private Map<Integer, Date> requestIdDateMap;

    private CsvTickWriter writer;
    private Security security;

    private static int clientId = 3;

    protected void handleInit() throws Exception {

        this.wrapper = new DefaultWrapper(clientId) {

            @Override
            public void historicalData(int requestId, String dateString, double open, double high, double low, double close, int volume, int count, double WAP, boolean hasGaps) {

                IbHistoricalDataServiceImpl.this.lock.lock();
                try {

                    if (dateString.startsWith("finished")) {

                        IbHistoricalDataServiceImpl.this.requestIdBooleanMap.put(requestId, true);
                        IbHistoricalDataServiceImpl.this.condition.signalAll();

                        return;
                    }

                    Date date = format.parse(dateString);
                    Date requestedDate = IbHistoricalDataServiceImpl.this.requestIdDateMap.get(requestId);

                    // retrieve ticks only between marketOpen & close
                    if (DateUtil.compareTime(date, IbHistoricalDataServiceImpl.this.security.getMarketClose()) > 0
                            || DateUtil.compareTime(date, IbHistoricalDataServiceImpl.this.security.getMarketOpen()) < 0) {

                        return;
                    }

                    // only accept ticks within the last 24h
                    if (requestedDate.getTime() - date.getTime() > 86400000) {

                        return;
                    }

                    BigDecimal last = RoundUtil.getBigDecimal(close);

                    Tick tick = new TickImpl();
                    tick.setDateTime(date);
                    tick.setLast(last);
                    tick.setLastDateTime(date);
                    tick.setVolBid(0);
                    tick.setVolAsk(0);
                    tick.setBid(new BigDecimal(0));
                    tick.setAsk(new BigDecimal(0));
                    tick.setVol(0);
                    tick.setOpenIntrest(0);
                    tick.setSettlement(last);

                    IbHistoricalDataServiceImpl.this.writer.write(tick);

                    String message = " date = " + dateString +
                            " open=" + open +
                            " high=" + high +
                            " low=" + low +
                            " close=" + close +
                            " volume=" + volume +
                            " count=" + count +
                            " WAP=" + WAP +
                            " hasGaps=" + hasGaps;

                    if (hasGaps) {
                        logger.error(message);
                    } else {
                        logger.debug(message);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    IbHistoricalDataServiceImpl.this.lock.unlock();
                }
            }

            public void connectionClosed() {

                super.connectionClosed();

                connect();
            }

            public void error(int requestId, int code, String errorMsg) {

                super.error(requestId, code, errorMsg);

                IbHistoricalDataServiceImpl.this.lock.lock();
                try {
                    IbHistoricalDataServiceImpl.this.requestIdBooleanMap.put(requestId, true);
                    IbHistoricalDataServiceImpl.this.condition.signalAll();
                } finally {
                    IbHistoricalDataServiceImpl.this.lock.unlock();
                }
            }
        };

        this.client = new DefaultClientSocket(this.wrapper);

        connect();
    }

    protected void handleConnect() {

        this.requestIdBooleanMap = new HashMap<Integer, Boolean>();
        this.requestIdDateMap = new HashMap<Integer, Date>();

        this.client.connect(clientId);
    }

    protected void handleRequestHistoricalData(int securityId) throws Exception {

        this.security = getSecurityDao().load(securityId);

        Contract contract = IbUtil.getContract(this.security);

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, 24);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_YEAR, -1);

        Date endDate = cal.getTime();

        cal.add(Calendar.YEAR, -1);
        cal.add(Calendar.DAY_OF_YEAR, 1);

        cal.setTime(format.parse("20100907  00:00:00"));

        this.writer = new CsvTickWriter(this.security.getIsin());

        Date date;
        while ((date = cal.getTime()).compareTo(endDate) < 0) {

            if ((cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) || (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY)) {
                cal.add(Calendar.DAY_OF_YEAR, 1);
                continue;
            }

            this.lock.lock();

            try {

                int requestId = RequestIdManager.getInstance().getNextRequestId();
                this.requestIdBooleanMap.put(requestId, false);
                this.requestIdDateMap.put(requestId, date);

                this.client.reqHistoricalData(requestId, contract, format.format(date), "1 D", "1 min", "TRADES", 1, 1);

                while (!this.requestIdBooleanMap.get(requestId)) {

                    if (!this.condition.await(retrievalTimeout, TimeUnit.SECONDS)) {
                        throw new IbHistoricalDataServiceException("could not get HistoricalData in time");
                    }
                }

                cal.add(Calendar.DAY_OF_YEAR, 1);

                Thread.sleep(10000);

            } finally {
                this.lock.unlock();
            }
        }
    }
}
