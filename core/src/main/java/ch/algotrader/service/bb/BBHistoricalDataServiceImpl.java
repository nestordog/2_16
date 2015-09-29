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
package ch.algotrader.service.bb;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;

import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Service;
import com.bloomberglp.blpapi.Session;

import ch.algotrader.adapter.bb.BBAdapter;
import ch.algotrader.adapter.bb.BBConstants;
import ch.algotrader.adapter.bb.BBMessageHandler;
import ch.algotrader.adapter.bb.BBSession;
import ch.algotrader.dao.marketData.BarDao;
import ch.algotrader.dao.security.SecurityDao;
import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.BarType;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.enumeration.InitializingServiceType;
import ch.algotrader.enumeration.TimePeriod;
import ch.algotrader.service.ExternalServiceException;
import ch.algotrader.service.HistoricalDataService;
import ch.algotrader.service.HistoricalDataServiceImpl;
import ch.algotrader.service.InitializationPriority;
import ch.algotrader.service.InitializingServiceI;
import ch.algotrader.service.ServiceException;
import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.util.RoundUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@InitializationPriority(value = InitializingServiceType.BROKER_INTERFACE)
public class BBHistoricalDataServiceImpl extends HistoricalDataServiceImpl implements HistoricalDataService, InitializingServiceI, DisposableBean {

    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private static final Logger LOGGER = LogManager.getLogger(BBHistoricalDataServiceImpl.class);
    private static BBSession session;

    private final BBAdapter bBAdapter;

    private final SecurityDao securityDao;

    public BBHistoricalDataServiceImpl(final BBAdapter bBAdapter,
            final SecurityDao securityDao,
            final BarDao barDao) {

        super(barDao);

        Validate.notNull(bBAdapter, "BBAdapter is null");
        Validate.notNull(securityDao, "SecurityDao is null");

        this.bBAdapter = bBAdapter;
        this.securityDao = securityDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {

        try {
            session = this.bBAdapter.createReferenceDataSession();
            session.startService();
        } catch (IOException ex) {
            throw new ExternalServiceException(ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ServiceException(ex);
        }
    }

    @Override
    public List<Bar> getHistoricalBars(long securityId, Date endDate, int timePeriodLength, TimePeriod timePeriod, final Duration barSize, BarType barType, Map<String, String> properties) {

        Validate.notNull(endDate, "End date is null");
        Validate.notNull(timePeriod, "Time period is null");
        Validate.notNull(barSize, "Bar size is null");
        Validate.notNull(barType, "Bar type is null");

        Security security = this.securityDao.get(securityId);
        if (security == null) {
            throw new ServiceException("security was not found " + securityId);
        }

        if (security.getBbgid() == null) {
            throw new ServiceException("security has no bbgid " + securityId);
        }

        String securityString = "/bbgid/" + security.getBbgid();

        // send the request by using either IntrayBarRequest or HistoricalDataRequest
        try {
            if (barSize.getValue() < Duration.DAY_1.getValue()) {
                sendIntradayBarRequest(endDate, timePeriodLength, timePeriod, barSize, barType, securityString, properties);
            } else {
                sendHistoricalDataRequest(endDate, timePeriodLength, timePeriod, barSize, barType, securityString, properties);
            }
        } catch (IOException ex) {
            throw new ExternalServiceException(ex);
        }
        // instantiate the message handler
        BBHistoricalDataMessageHandler messageHandler = new BBHistoricalDataMessageHandler(security, barSize);

        // process responses
        boolean done = false;
        while (!done) {
            try {
                done = messageHandler.processEvent(session);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new ServiceException(ex);
            }
        }

        return messageHandler.getBarList();

    }

    private void sendIntradayBarRequest(Date endDate, int timePeriodLength, TimePeriod timePeriod, final Duration barSize, BarType barType, final String securityString, Map<String, String> properties) throws IOException {

        int barSizeInt = (int) (barSize.getValue() / 60000);

        String barTypeString;
        switch (barType) {
            case TRADES:
                barTypeString = "TRADE";
                break;
            case BID:
                barTypeString = "BID";
                break;
            case ASK:
                barTypeString = "ASK";
                break;
            case BEST_BID:
                barTypeString = "BEST_BID";
                break;
            case BEST_ASK:
                barTypeString = "BEST_ASK";
                break;
            default:
                throw new IllegalArgumentException("unsupported barType " + barType);
        }

        String startDateString = dateTimeFormat.format(DateTimeLegacy.toGMTDate(getStartDate(endDate, timePeriodLength, timePeriod)));
        String endDateString = dateTimeFormat.format(DateTimeLegacy.toGMTDate(endDate));

        Service service = session.getService();

        Request request = service.createRequest("IntradayBarRequest");
        request.set("security", securityString);
        request.set("eventType", barTypeString);
        request.set("interval", barSizeInt);
        request.set("startDateTime", startDateString);
        request.set("endDateTime", endDateString);

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            request.set(entry.getKey(), entry.getValue());
        }

        // send request
        session.sendRequest(request, null);
    }

    private void sendHistoricalDataRequest(Date endDate, int timePeriodLength, TimePeriod timePeriod, Duration barSize, BarType barType, String securityString, Map<String, String> properties) throws IOException {

        String barSizeString;
        switch (barSize) {
            case DAY_1:
                barSizeString = "DAILY";
                break;
            case WEEK_1:
                barSizeString = "WEEKLY";
                break;
            case MONTH_1:
                barSizeString = "MONTHLY";
                break;
            case MONTH_3:
                barSizeString = "QUARTERLY";
                break;
            case MONTH_6:
                barSizeString = "SEMI_ANNUALLY";
                break;
            case YEAR_1:
                barSizeString = "YEARLY";
                break;
            default:
                throw new IllegalArgumentException("unsupported barSize " + barSize);
        }

        if (!BarType.TRADES.equals(barType)) {
            throw new IllegalArgumentException("unsupported barType " + barType);
        }

        String startDateString = dateFormat.format(DateTimeLegacy.toGMTDate(getStartDate(endDate, timePeriodLength, timePeriod)));
        String endDateString = dateFormat.format(DateTimeLegacy.toGMTDate(endDate));

        Service service = session.getService();

        Request request = service.createRequest("HistoricalDataRequest");

        // add security
        Element securities = request.getElement("securities");
        securities.appendValue(securityString);

        // add fields
        Element fields = request.getElement("fields");
        fields.appendValue("PX_LAST");
        fields.appendValue("OPEN");
        fields.appendValue("HIGH");
        fields.appendValue("LOW");
        fields.appendValue("VOLUME");

        request.set("periodicitySelection", barSizeString);
        request.set("startDate", startDateString);
        request.set("endDate", endDateString);

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            request.set(entry.getKey(), entry.getValue());
        }

        // send request
        session.sendRequest(request, null);
    }

    private Date getStartDate(Date endDate, int timePeriodLength, TimePeriod timePeriod) {

        switch (timePeriod) {
            case DAY:
                return DateUtils.addDays(endDate, -timePeriodLength);
            case WEEK:
                return DateUtils.addWeeks(endDate, -timePeriodLength);
            case MONTH:
                return DateUtils.addMonths(endDate, -timePeriodLength);
            case YEAR:
                return DateUtils.addYears(endDate, -timePeriodLength);
            default:
                throw new IllegalArgumentException("timePeriod is not allowed " + timePeriod);
        }
    }

    @Override
    public void destroy() throws Exception {

        if (session != null && session.isRunning()) {
            session.stop();
        }
    }

    private class BBHistoricalDataMessageHandler extends BBMessageHandler {

        private final Security security;
        private final Duration barSize;
        private final List<Bar> barList;

        public BBHistoricalDataMessageHandler(Security security, Duration barSize) {

            this.security = security;
            this.barSize = barSize;
            this.barList = new ArrayList<>();
        }

        @Override
        protected void processResponseEvent(Event event, Session session) {

            for (Message msg : event) {

                if (msg.hasElement(BBConstants.RESPONSE_ERROR)) {

                    Element errorInfo = msg.getElement(BBConstants.RESPONSE_ERROR);
                    LOGGER.error("request failed {} ({})", errorInfo.getElementAsString(BBConstants.CATEGORY), errorInfo.getElementAsString(BBConstants.MESSAGE));

                    continue;
                }

                if (msg.messageType() == BBConstants.INTRADAY_BAR_RESPONSE) {
                    processIntradayBarResponse(msg);
                } else if (msg.messageType() == BBConstants.HISTORICAL_DATA_RESPONSE) {
                    processHistoricalDataResponse(msg);
                } else {
                    throw new IllegalArgumentException("unknown reponse type: " + msg.messageType());
                }
            }
        }

        private void processIntradayBarResponse(Message msg) {

            Element data = msg.getElement(BBConstants.BAR_DATA).getElement(BBConstants.BAR_TICK_DATA);

            int numBars = data.numValues();
            for (int i = 0; i < numBars; ++i) {

                Element fields = data.getValueAsElement(i);

                Date time = fields.getElementAsDate(BBConstants.TIME2).calendar().getTime();
                double open = fields.getElementAsFloat64(BBConstants.OPEN);
                double high = fields.getElementAsFloat64(BBConstants.HIGH);
                double low = fields.getElementAsFloat64(BBConstants.LOW);
                double close = fields.getElementAsFloat64(BBConstants.CLOSE);
                long volume = fields.getElementAsInt64(BBConstants.VOLUME);

                int scale = this.security.getSecurityFamily().getScale(Broker.BBG.name());

                Bar bar = Bar.Factory.newInstance();
                bar.setDateTime(time);
                bar.setOpen(RoundUtil.getBigDecimal(open, scale));
                bar.setHigh(RoundUtil.getBigDecimal(high, scale));
                bar.setLow(RoundUtil.getBigDecimal(low, scale));
                bar.setClose(RoundUtil.getBigDecimal(close, scale));
                bar.setVol((int) volume);
                bar.setBarSize(this.barSize);
                bar.setFeedType(FeedType.BB.name());
                bar.setSecurity(this.security);

                this.barList.add(bar);
            }
        }

        private void processHistoricalDataResponse(Message msg) {

            Element data = msg.getElement(BBConstants.SECURITY_DATA).getElement(BBConstants.FIELD_DATA);

            int numBars = data.numValues();
            for (int i = 0; i < numBars; ++i) {

                Element bbBar = data.getValueAsElement(i);

                //ignore bars with not PX_LAST
                if (!bbBar.hasElement("PX_LAST")) {
                    continue;
                }

                Date date = bbBar.getElementAsDate(BBConstants.DATE).calendar().getTime();
                double close = bbBar.getElementAsFloat64("PX_LAST");

                // instruments might only have a PX_LAST
                double open = close;
                if (bbBar.hasElement("OPEN")) {
                    open = bbBar.getElementAsFloat64("OPEN");
                }

                double high = close;
                if (bbBar.hasElement("HIGH")) {
                    high = bbBar.getElementAsFloat64("HIGH");
                }

                double low = close;
                if (bbBar.hasElement("LOW")) {
                    low = bbBar.getElementAsFloat64("LOW");
                }

                long volume = 0;
                if (bbBar.hasElement("VOLUME")) {
                    volume = bbBar.getElementAsInt64("VOLUME");
                }

                int scale = this.security.getSecurityFamily().getScale(Broker.BBG.name());

                Bar bar = Bar.Factory.newInstance();
                bar.setDateTime(date);
                bar.setOpen(RoundUtil.getBigDecimal(open, scale));
                bar.setHigh(RoundUtil.getBigDecimal(high, scale));
                bar.setLow(RoundUtil.getBigDecimal(low, scale));
                bar.setClose(RoundUtil.getBigDecimal(close, scale));
                bar.setVol((int) volume);
                bar.setBarSize(this.barSize);
                bar.setFeedType(FeedType.BB.name());
                bar.setSecurity(this.security);

                this.barList.add(bar);
            }
        }

        public List<Bar> getBarList() {

            return this.barList;
        }
    }
}
