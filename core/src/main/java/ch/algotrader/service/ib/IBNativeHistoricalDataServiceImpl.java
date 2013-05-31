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
package ch.algotrader.service.ib;

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

import ch.algotrader.adapter.ib.IBClient;
import ch.algotrader.adapter.ib.IBDefaultMessageHandler;
import ch.algotrader.adapter.ib.IBIdGenerator;
import ch.algotrader.adapter.ib.IBUtil;
import ch.algotrader.util.MyLogger;
import ch.algotrader.util.RoundUtil;

import com.algoTrader.entity.marketData.Bar;
import com.algoTrader.entity.security.Security;
import com.algoTrader.enumeration.BarType;
import com.algoTrader.enumeration.TimePeriod;
import com.algoTrader.service.HistoricalDataServiceException;
import com.algoTrader.service.ib.IBNativeHistoricalDataServiceBase;
import com.ib.client.Contract;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBNativeHistoricalDataServiceImpl extends IBNativeHistoricalDataServiceBase implements DisposableBean {

    private static final long serialVersionUID = 8656307573474662794L;
    private static Logger logger = MyLogger.getLogger(IBNativeHistoricalDataServiceImpl.class.getName());
    private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd  HH:mm:ss");
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    private @Value("${simulation}") boolean simulation;
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

        this.messageHandler = new IBDefaultMessageHandler(clientId) {

            @Override
            public void historicalData(int requestId, String dateString, double open, double high, double low, double close, int volume, int count, double WAP,
                    boolean hasGaps) {

                IBNativeHistoricalDataServiceImpl.this.lock.lock();
                try {

                    if (dateString.startsWith("finished")) {

                        IBNativeHistoricalDataServiceImpl.this.success = true;
                        IBNativeHistoricalDataServiceImpl.this.condition.signalAll();

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

                    IBNativeHistoricalDataServiceImpl.this.barList.add(bar);

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
                    IBNativeHistoricalDataServiceImpl.this.lock.unlock();
                }
            }

            @Override
            public void connectionClosed() {

                super.connectionClosed();

                IBNativeHistoricalDataServiceImpl.this.client.connect();
            }

            @Override
            public void error(int requestId, int code, String errorMsg) {

                // 165 Historical data farm is connected
                // 162 Historical market data Service error message.
                // 2105 A historical data farm is disconnected.
                // 2107 A historical data farm connection has become inactive
                // but should be available upon demand.
                if (code == 162 || code == 165 || code == 2105 || code == 2106 || code == 2107) {

                    IBNativeHistoricalDataServiceImpl.this.lock.lock();
                    try {

                        if (code == 2105 || code == 2107) {

                            super.error(requestId, code, errorMsg);
                            IBNativeHistoricalDataServiceImpl.this.success = false;
                        }

                        if (code == 162) {

                            logger.warn("HMDS query returned no data");
                            IBNativeHistoricalDataServiceImpl.this.success = true;
                        }

                        IBNativeHistoricalDataServiceImpl.this.condition.signalAll();
                    } finally {
                        IBNativeHistoricalDataServiceImpl.this.lock.unlock();
                    }
                } else {
                    super.error(requestId, code, errorMsg);
                }
            }
        };

        this.client = getIBClientFactory().getClient(clientId, this.messageHandler);

        this.success = false;

        this.client.connect();
    }

    @Override
    protected synchronized List<Bar> handleGetHistoricalBars(int securityId, Date endDate, int timePeriodLength, TimePeriod timePeriod, int barSize,
            TimePeriod barSizePeriod, BarType barType) throws Exception {

        this.barList = new ArrayList<Bar>();
        this.success = false;

        Security security = getSecurityDao().get(securityId);

        this.lock.lock();

        try {

            Contract contract = IBUtil.getContract(security);
            int requestId = IBIdGenerator.getInstance().getNextRequestId();
            String dateString = dateTimeFormat.format(endDate);

            String durationString = timePeriodLength + " ";
            switch (timePeriod) {
                case MSEC:
                    throw new IllegalArgumentException("MILLISECOND durationPeriod is not allowed");
                case SEC:
                    durationString += "S";
                    break;
                case MIN:
                    throw new IllegalArgumentException("MINUTE durationPeriod is not allowed");
                case HOUR:
                    throw new IllegalArgumentException("HOUR durationPeriod is not allowed");
                case DAY:
                    durationString += "D";
                    break;
                case WEEK:
                    durationString += "W";
                    break;
                case MONTH:
                    throw new IllegalArgumentException("MONTH durationPeriod is not allowed");
                case YEAR:
                    durationString += "Y";
                    break;
            }

            String barSizeString = barSize + " ";
            switch (barSizePeriod) {
                case MSEC:
                    throw new IllegalArgumentException("MILLISECOND barSize is not allowed");
                case SEC:
                    barSizeString += "sec";
                    break;
                case MIN:
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
                case MONTH:
                    throw new IllegalArgumentException("MONTH barSize is not allowed");
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
    public void destroy() throws Exception {

        if (this.client != null) {
            this.client.disconnect();
        }
    }
}
