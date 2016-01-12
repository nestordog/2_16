/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.service.ib;

import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ib.client.Contract;
import com.ib.client.TagValue;

import ch.algotrader.adapter.IdGenerator;
import ch.algotrader.adapter.ib.IBPendingRequests;
import ch.algotrader.adapter.ib.IBSession;
import ch.algotrader.adapter.ib.IBUtil;
import ch.algotrader.concurrent.Promise;
import ch.algotrader.concurrent.PromiseImpl;
import ch.algotrader.config.IBConfig;
import ch.algotrader.dao.marketData.BarDao;
import ch.algotrader.dao.security.SecurityDao;
import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.enumeration.MarketDataEventType;
import ch.algotrader.enumeration.TimePeriod;
import ch.algotrader.service.HistoricalDataService;
import ch.algotrader.service.HistoricalDataServiceImpl;
import ch.algotrader.service.ServiceException;
import ch.algotrader.util.DateTimeLegacy;

/**
 * See <a href="http://www.interactivebrokers.com/php/apiUsersGuide/apiguide/api/historical_data_limitations.htm">Historical Data Limitations</a> for further details.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class IBNativeHistoricalDataServiceImpl extends HistoricalDataServiceImpl implements HistoricalDataService {

    private static final Logger LOGGER = LogManager.getLogger(IBNativeHistoricalDataServiceImpl.class);
    private static final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyyMMdd  HH:mm:ss");
    private static final int pacingMillis = 10 * 1000;

    private long lastTimeStamp = 0;

    private final IBSession iBSession;
    private final IBConfig iBConfig;
    private final IBPendingRequests pendingRequests;
    private final IdGenerator requestIdGenerator;
    private final SecurityDao securityDao;

    public IBNativeHistoricalDataServiceImpl(
            final IBSession iBSession,
            final IBConfig iBConfig,
            final IBPendingRequests pendingRequests,
            final IdGenerator requestIdGenerator,
            final SecurityDao securityDao,
            final BarDao barDao) {

        super(barDao);

        Validate.notNull(iBSession, "IBSession is null");
        Validate.notNull(iBConfig, "IBConfig is null");
        Validate.notNull(pendingRequests, "IBPendingRequests is null");
        Validate.notNull(requestIdGenerator, "IdGenerator is null");
        Validate.notNull(securityDao, "SecurityDao is null");

        this.iBSession = iBSession;
        this.iBConfig = iBConfig;
        this.pendingRequests = pendingRequests;
        this.requestIdGenerator = requestIdGenerator;
        this.securityDao = securityDao;
    }

    @Override
    public List<Bar> getHistoricalBars(long securityId, Date endDate, int timePeriodLength, TimePeriod timePeriod, Duration barSize, MarketDataEventType marketDataEventType, Map<String, String> properties) {

        Validate.notNull(endDate, "End date is null");
        Validate.notNull(timePeriod, "Time period is null");
        Validate.notNull(barSize, "Bar size is null");
        Validate.notNull(marketDataEventType, "Bar type is null");

        if (!this.iBSession.isLoggedOn()) {
            throw new ServiceException("IB session is not logged on");
        }

        Security security = this.securityDao.get(securityId);
        if (security == null) {
            throw new ServiceException("security was not found " + securityId);
        }

        int scale = security.getSecurityFamily().getScale(Broker.IB.name());
        Contract contract = IBUtil.getContract(security);
        int requestId = (int) this.requestIdGenerator.generateId();
        String dateString = dateTimeFormat.format(DateTimeLegacy.toLocalDateTime(endDate));

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
        switch (barSizeName[0]) {
            case "SEC":
                barSizeString += "sec";
                break;
            case "MIN":
                barSizeString += "min";
                break;
            case "HOUR":
                barSizeString += "hour";
                break;
            case "DAY":
                barSizeString += "day";
                break;
            default:
                throw new IllegalArgumentException("barSize is not allowed " + barSize);
        }

        if (Integer.parseInt(barSizeName[1]) > 1) {
            barSizeString += "s";
        }

        String marketDataEventTypeString;
        switch (marketDataEventType) {
            case TRADES:
                marketDataEventTypeString = "TRADES";
                break;
            case MIDPOINT:
                marketDataEventTypeString = "MIDPOINT";
                break;
            case BID:
                marketDataEventTypeString = "BID";
                break;
            case ASK:
                marketDataEventTypeString = "ASK";
                break;
            case BID_ASK:
                marketDataEventTypeString = "BID_ASK";
                break;
            default:
                throw new IllegalArgumentException("unsupported marketDataEventType " + marketDataEventType);
        }

        // avoid pacing violations
        long gapMillis = System.currentTimeMillis() - this.lastTimeStamp;
        if (this.lastTimeStamp != 0 && gapMillis < pacingMillis) {
            long waitMillis = pacingMillis - gapMillis;
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("waiting {} millis until next historical data request", waitMillis);
            }
            try {
                Thread.sleep(waitMillis);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new ServiceException(ex);
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Request historic data; request id = {}; conId = {}; date = {}; duration = {}; bar size = {}",
                    requestId, contract.m_conId, dateString, durationString, barSizeString);
        }

        PromiseImpl<List<Bar>> promise = new PromiseImpl<>(null);
        this.pendingRequests.addHistoricDataRequest(requestId, promise);
        this.iBSession.reqHistoricalData(requestId, contract, dateString, durationString, barSizeString, marketDataEventTypeString, this.iBConfig.useRTH() ? 1 : 0, 1, Collections.<TagValue>emptyList());
        List<Bar> bars = getBarsBlocking(promise);

        // set & update fields
        for (Bar bar: bars) {
            bar.setSecurity(security);
            bar.setFeedType(FeedType.IB.name());
            bar.getOpen().setScale(scale, RoundingMode.HALF_UP);
            bar.getHigh().setScale(scale, RoundingMode.HALF_UP);
            bar.getLow().setScale(scale, RoundingMode.HALF_UP);
            bar.getClose().setScale(scale, RoundingMode.HALF_UP);
            bar.setBarSize(barSize);
        }
        this.lastTimeStamp = System.currentTimeMillis();
        return bars;
    }

    @Override
    public List<Tick> getHistoricalTicks(long securityId, Date endDate, int timePeriodLength, TimePeriod timePeriod, MarketDataEventType marketDataEventType, Map<String, String> properties) {

        throw new UnsupportedOperationException("historical ticks not supported for IB");
    }

    private List<Bar> getBarsBlocking(final Promise<List<Bar>> promise) {
        try {
            return promise.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ServiceException(ex);
        } catch (ExecutionException ex) {
            throw IBNativeSupport.rethrow(ex.getCause());
        }
    }

}
