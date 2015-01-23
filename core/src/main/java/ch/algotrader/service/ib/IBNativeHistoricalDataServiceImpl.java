/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.service.ib;

import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import com.ib.client.Contract;
import com.ib.client.TagValue;

import ch.algotrader.adapter.ib.IBIdGenerator;
import ch.algotrader.adapter.ib.IBSession;
import ch.algotrader.adapter.ib.IBUtil;
import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.entity.marketData.BarDao;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityDao;
import ch.algotrader.enumeration.BarType;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.enumeration.TimePeriod;
import ch.algotrader.service.HistoricalDataServiceImpl;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBNativeHistoricalDataServiceImpl extends HistoricalDataServiceImpl implements IBNativeHistoricalDataService {

    private static final Logger logger = Logger.getLogger(IBNativeHistoricalDataServiceImpl.class.getName());
    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd  HH:mm:ss");
    private static final int pacingMillis = 10 * 1000;

    private long lastTimeStamp = 0;

    private final BlockingQueue<Bar> historicalDataQueue;

    private final IBSession iBSession;

    private final IBIdGenerator iBIdGenerator;

    private final SecurityDao securityDao;

    public IBNativeHistoricalDataServiceImpl(final BlockingQueue<Bar> historicalDataQueue,
            final IBSession iBSession,
            final IBIdGenerator iBIdGenerator,
            final SecurityDao securityDao,
            final BarDao barDao) {

        super(barDao);

        Validate.notNull(historicalDataQueue, "HistoricalDataQueue is null");
        Validate.notNull(iBSession, "IBSession is null");
        Validate.notNull(iBIdGenerator, "IBIdGenerator is null");
        Validate.notNull(securityDao, "SecurityDao is null");

        this.historicalDataQueue = historicalDataQueue;
        this.iBSession = iBSession;
        this.iBIdGenerator = iBIdGenerator;
        this.securityDao = securityDao;
    }

    @Override
    public synchronized List<Bar> getHistoricalBars(int securityId, Date endDate, int timePeriodLength, TimePeriod timePeriod, Duration barSize, BarType barType, Map<String, String> properties) {

        Validate.notNull(endDate, "End date is null");
        Validate.notNull(timePeriod, "Time period is null");
        Validate.notNull(barSize, "Bar size is null");
        Validate.notNull(barType, "Bar type is null");

        if (!this.iBSession.getLifecycle().isLoggedOn()) {
            throw new IBNativeHistoricalDataServiceException("cannot download historical data, because IB is not subscribed");
        }

        // make sure queue is empty
        Bar peek = this.historicalDataQueue.peek();
        if (peek != null) {
            this.historicalDataQueue.clear();
            logger.warn("historicalDataQueue was not empty");
        }

        Security security = this.securityDao.get(securityId);
        if (security == null) {
            throw new IBNativeHistoricalDataServiceException("security was not found " + securityId);
        }

        int scale = security.getSecurityFamily().getScale();
        Contract contract = IBUtil.getContract(security);
        int requestId = this.iBIdGenerator.getNextRequestId();
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

        // avoid pacing violations
        long gapMillis = System.currentTimeMillis() - this.lastTimeStamp;
        if (this.lastTimeStamp != 0 && gapMillis < pacingMillis) {
            long waitMillis = pacingMillis - gapMillis;
            logger.debug("waiting " + waitMillis + " millis until next historical data request");
            try {
                Thread.sleep(waitMillis);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IBNativeHistoricalDataServiceException(ex);
            }
        }

        // send the request
        this.iBSession.reqHistoricalData(requestId, contract, dateString, durationString, barSizeString, barTypeString, 1, 1, new ArrayList<TagValue>());

        // read from the queue until a Bar with no dateTime is received
        List<Bar> barList = new ArrayList<Bar>();
        while (true) {

            Bar bar;
            try {
                bar = this.historicalDataQueue.poll(10, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IBNativeHistoricalDataServiceException(ex);
            }

            if (bar == null) {
                throw new IBNativeHistoricalDataServiceException("timeout waiting for historical bars");
            }

            // end of transmission bar does not have a DateTime
            if (bar.getDateTime() == null) {
                break;
            }

            // set & update fields
            bar.setSecurity(security);
            bar.setFeedType(FeedType.IB);
            bar.getOpen().setScale(scale, RoundingMode.HALF_UP);
            bar.getHigh().setScale(scale, RoundingMode.HALF_UP);
            bar.getLow().setScale(scale, RoundingMode.HALF_UP);
            bar.getClose().setScale(scale, RoundingMode.HALF_UP);
            bar.setBarSize(barSize);

            barList.add(bar);
        }

        this.lastTimeStamp = System.currentTimeMillis();

        return barList;

    }
}
