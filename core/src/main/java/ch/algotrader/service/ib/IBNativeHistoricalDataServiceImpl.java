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

import ch.algotrader.adapter.ib.IBDefaultMessageHandler;
import ch.algotrader.adapter.ib.IBIdGenerator;
import ch.algotrader.adapter.ib.IBSession;
import ch.algotrader.adapter.ib.IBUtil;
import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.BarType;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.TimePeriod;
import ch.algotrader.service.HistoricalDataServiceException;
import ch.algotrader.util.MyLogger;
import ch.algotrader.util.RoundUtil;

import com.ib.client.Contract;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBNativeHistoricalDataServiceImpl extends IBNativeHistoricalDataServiceBase implements DisposableBean {

    private static final long serialVersionUID = 8656307573474662794L;
    private static Logger logger = MyLogger.getLogger(IBNativeHistoricalDataServiceImpl.class.getName());
    private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd  HH:mm:ss");
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    private @Value("${ib.historicalDataTimeout}") int historicalDataTimeout;

    private IBSession session;
    private IBDefaultMessageHandler messageHandler;

    private Lock lock = new ReentrantLock();
    private Condition condition = this.lock.newCondition();

    private boolean success;
    private List<Bar> barList;
    private Security security;
    private Duration barSize;
    private int scale;

    private static int sessionId = 2;

    @Override
    protected void handleInit() throws Exception {

        this.messageHandler = new IBHistoricalMessageHandler(sessionId);

        this.session = getIBSessionFactory().getSession(sessionId, this.messageHandler);

        this.success = false;

        this.session.connect();
    }

    @Override
    protected synchronized List<Bar> handleGetHistoricalBars(int securityId, Date endDate, int timePeriodLength, TimePeriod timePeriod, Duration barSize, BarType barType) throws Exception {

        this.security = getSecurityDao().get(securityId);

        if (this.security == null) {
            throw new IBNativeHistoricalDataServiceException("security was not found " + securityId);
        }

        this.barSize = barSize;
        this.scale = this.security.getSecurityFamily().getScale();

        this.barList = new ArrayList<Bar>();
        this.success = false;

        this.lock.lock();

        try {

            Contract contract = IBUtil.getContract(this.security);
            int requestId = IBIdGenerator.getInstance().getNextRequestId();
            String dateString = dateTimeFormat.format(endDate);

            String durationString = timePeriodLength + " ";
            switch (timePeriod) {
                case SEC:
                    durationString += "S";
                    break;
                case DAY:
                    durationString += "D";
                    break;
                case WEEK:
                    durationString += "W";
                    break;
                case MONTH:
                    durationString += "M";
                    break;
                case YEAR:
                    durationString += "Y";
                    break;
                default:
                    throw new IllegalArgumentException("timePeriod is not allowed " + timePeriod);
            }

            String[] barSizeName = barSize.name().split("_");

            String barSizeString = barSizeName[1] + " ";
            if (barSizeName[0].equals("SEC")) {
                barSizeString += "sec";
            } else if (barSizeName[0].equals("MIN")) {
                barSizeString += "min";
            } else if (barSizeName[0].equals("HOUR")) {
                barSizeString += "hour";
            } else if (barSizeName[0].equals("DAY")) {
                barSizeString += "day";
            } else {
                throw new IllegalArgumentException("barSize is not allowed " + barSize);
            }

            if (Integer.parseInt(barSizeName[1]) > 1) {
                barSizeString += "s";
            }

            String barTypeString;
            switch (barType) {
                case TRADES:
                    barTypeString = "TRADES";
                    break;
                case MIDPOINT:
                    barTypeString = "MIDPOINT";
                    break;
                case BID:
                    barTypeString = "BID";
                    break;
                case ASK:
                    barTypeString = "ASK";
                    break;
                case BID_ASK:
                    barTypeString = "BID_ASK";
                    break;
                default:
                    throw new IllegalArgumentException("unsupported barType " + barType);
            }

            this.session.reqHistoricalData(requestId, contract, dateString, durationString, barSizeString, barTypeString, 1, 1);

            while (this.success == false) {
                if (!this.condition.await(this.historicalDataTimeout, TimeUnit.SECONDS)) {
                    this.session.cancelHistoricalData(requestId);
                    continue;
                }
            }

        } finally {
            this.lock.unlock();
        }

        for (Bar bar : this.barList) {
            bar.setSecurity(this.security);
        }

        return this.barList;
    }

    @Override
    public void destroy() throws Exception {

        if (this.session != null) {
            this.session.disconnect();
        }
    }

    private class IBHistoricalMessageHandler extends IBDefaultMessageHandler {

        private IBHistoricalMessageHandler(int clientId) {
            super(clientId);
        }

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
                bar.setOpen(RoundUtil.getBigDecimal(open, IBNativeHistoricalDataServiceImpl.this.scale));
                bar.setHigh(RoundUtil.getBigDecimal(high, IBNativeHistoricalDataServiceImpl.this.scale));
                bar.setLow(RoundUtil.getBigDecimal(low, IBNativeHistoricalDataServiceImpl.this.scale));
                bar.setClose(RoundUtil.getBigDecimal(close, IBNativeHistoricalDataServiceImpl.this.scale));
                bar.setVol(volume);
                bar.setBarSize(IBNativeHistoricalDataServiceImpl.this.barSize);

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

            IBNativeHistoricalDataServiceImpl.this.session.connect();
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
    }
}
