package com.algoTrader.service.ib;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.entity.marketData.Bar;
import com.algoTrader.entity.security.Security;
import com.algoTrader.enumeration.BarType;
import com.algoTrader.enumeration.ConnectionState;
import com.algoTrader.enumeration.Period;
import com.algoTrader.service.HistoricalDataServiceException;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.ib.client.Contract;

public class IBHistoricalDataServiceImpl extends IBHistoricalDataServiceBase implements DisposableBean {

    private static final long serialVersionUID = 8656307573474662794L;
    private static Logger logger = MyLogger.getLogger(IBHistoricalDataServiceImpl.class.getName());
    private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd  HH:mm:ss");
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    private @Value("${simulation}") boolean simulation;
    private @Value("#{'${marketChannel}' == 'IB'}") boolean ibEnabled;
    private @Value("${ib.historicalDataServiceEnabled}") boolean historicalDataServiceEnabled;
    private @Value("${ib.historicalDataTimeout}") int historicalDataTimeout;

    private IBClient client;
    private IBDefaultMessageHandler messageHandler;
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

        this.messageHandler = new IBDefaultMessageHandler(clientId) {

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

        this.client = new IBClient(clientId, this.messageHandler);

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

        Security security = getSecurityDao().get(securityId);

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
                if (!this.condition.await(this.historicalDataTimeout, TimeUnit.SECONDS)) {
                    this.client.cancelHistoricalData(requestId);
                    continue;
                }
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
    protected ConnectionState handleGetConnectionState() {

        if (this.messageHandler == null) {
            return ConnectionState.DISCONNECTED;
        } else {
            return this.messageHandler.getState();
        }
    }

    @Override
    public void destroy() throws Exception {

        if (this.client != null) {
            this.client.disconnect();
        }
    }
}
