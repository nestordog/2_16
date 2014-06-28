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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import ch.algotrader.adapter.ib.IBUtil;
import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.BarType;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.enumeration.TimePeriod;
import ch.algotrader.util.MyLogger;

import com.ib.client.Contract;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBNativeHistoricalDataServiceImpl extends IBNativeHistoricalDataServiceBase {

    private static final Logger logger = MyLogger.getLogger(IBNativeHistoricalDataServiceImpl.class.getName());
    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd  HH:mm:ss");
    private static final int pacingMillis = 10 * 1000;

    private BlockingQueue<Bar> historicalDataQueue;
    private long lastTimeStamp = 0;

    public void setHistoricalDataQueue(BlockingQueue<Bar> historicalDataQueue) {
        this.historicalDataQueue = historicalDataQueue;
    }

    @Override
    protected synchronized List<Bar> handleGetHistoricalBars(int securityId, Date endDate, int timePeriodLength, TimePeriod timePeriod, Duration barSize, BarType barType) throws Exception {

        if (!getIBSession().getLifecycle().isSubscribed()) {
            throw new IBNativeHistoricalDataServiceException("cannot download historical data, because IB is not subscribed");
        }

        // make sure queue is empty
        if (this.historicalDataQueue.peek() != null) {
            this.historicalDataQueue.clear();
            throw new IllegalStateException("historicalDataQueue is not empty");
        }

        Security security = getSecurityDao().get(securityId);
        if (security == null) {
            throw new IBNativeHistoricalDataServiceException("security was not found " + securityId);
        }

        int scale = security.getSecurityFamily().getScale();
        Contract contract = IBUtil.getContract(security);
        int requestId = getIBIdGenerator().getNextRequestId();
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
            Thread.sleep(waitMillis);
        }

        // send the request
        getIBSession().reqHistoricalData(requestId, contract, dateString, durationString, barSizeString, barTypeString, 1, 1);

        // read from the queue until a Bar with no dateTime is received
        List<Bar> barList = new ArrayList<Bar>();
        while (true) {

            Bar bar = this.historicalDataQueue.poll(10, TimeUnit.SECONDS);

            if (bar == null) {
                throw new IllegalStateException("timeout waiting for historical bars");
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
