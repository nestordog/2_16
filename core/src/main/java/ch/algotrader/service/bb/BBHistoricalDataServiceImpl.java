package ch.algotrader.service.bb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;

import ch.algotrader.adapter.bb.BBMessageHandler;
import ch.algotrader.adapter.bb.BBSession;
import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.BarType;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.TimePeriod;
import ch.algotrader.util.MyLogger;
import ch.algotrader.util.RoundUtil;

import com.bloomberglp.blpapi.Datetime;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.Name;
import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Service;
import com.bloomberglp.blpapi.Session;

public class BBHistoricalDataServiceImpl extends BBHistoricalDataServiceBase implements DisposableBean {

    private static final long serialVersionUID = 1339545758324165650L;

    private static final Name RESPONSE_ERROR = Name.getName("responseError");
    private static final Name CATEGORY = Name.getName("category");
    private static final Name MESSAGE = Name.getName("message");
    private static final Name BAR_DATA = Name.getName("barData");
    private static final Name BAR_TICK_DATA = Name.getName("barTickData");

    private static final Name TIME = Name.getName("time");
    private static final Name OPEN = Name.getName("open");
    private static final Name HIGH = Name.getName("high");
    private static final Name LOW = Name.getName("low");
    private static final Name CLOSE = Name.getName("low");
    private static final Name VOLUME = Name.getName("volume");

    private static Logger logger = MyLogger.getLogger(BBHistoricalDataServiceImpl.class.getName());
    private static BBSession session;

    @Override
    protected void handleInit() throws Exception {

        session = getBBSessionFactory().getReferenceDataSession();
    }

    @Override
    protected List<Bar> handleGetHistoricalBars(int securityId, Date endDate, int timePeriodLength, TimePeriod timePeriod, final Duration barSize, BarType barType) throws Exception {

        Security security = getSecurityDao().get(securityId);

        // send the request
        sendRequest(security, endDate, timePeriodLength, timePeriod, barSize, barType);

        // instantiate the message handler
        BBHistoricalDataMessageHandler messageHandler = new BBHistoricalDataMessageHandler(security, barSize);

        // process responses
        boolean done = false;
        while (!done) {
            done = messageHandler.processEvent(session);
        }

        return messageHandler.getBarList();
    }

    private void sendRequest(Security security, Date endDate, int timePeriodLength, TimePeriod timePeriod, final Duration barSize, BarType barType) throws IOException {

        final String securityString = "/bbgid/" + security.getBbgid();

        if (barSize.getValue() < Duration.MIN_1.getValue() || barSize.getValue() > Duration.DAY_1.getValue()) {
            throw new IllegalArgumentException("barSize hast to be between 1 Min and 1 Day");
        }
        int barSizeInt = (int)(barSize.getValue() / 60000);

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

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDate);

        String endDateString = (new Datetime(calendar)).toString();

        switch (timePeriod) {
            case DAY:
                calendar.add(Calendar.DATE, -timePeriodLength);
                break;
            case WEEK:
                calendar.add(Calendar.WEEK_OF_YEAR, -timePeriodLength);
                break;
            case MONTH:
                calendar.add(Calendar.MONTH, -timePeriodLength);
                break;
            case YEAR:
                calendar.add(Calendar.YEAR, -timePeriodLength);
                break;
            default:
                throw new IllegalArgumentException("timePeriod is not allowed " + timePeriod);
        }

        String startDateString = (new Datetime(calendar)).toString();

        Service service = session.getService();

        Request request = service.createRequest("IntradayBarRequest");
        request.set("security", securityString);
        request.set("eventType", barTypeString);
        request.set("interval", barSizeInt);
        request.set("startDateTime", startDateString);
        request.set("endDateTime", endDateString);

        // send request
        session.sendRequest(request, null);
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
        private final Set<Bar> barList;

        public BBHistoricalDataMessageHandler(Security security, Duration barSize) {

            this.security = security;
            this.barSize = barSize;

            // add dateTime based Comparator to make sure Bars are unique per Date
            this.barList = new TreeSet<Bar>(new Comparator<Bar>() {
                @Override
                public int compare(Bar b0, Bar b1) {
                    return b0.getDateTime().compareTo(b1.getDateTime());
                }});
        }

        @Override
        protected void processResponseEvent(Event event, Session session) {

            for (Message msg : event) {

                if (msg.hasElement(RESPONSE_ERROR)) {

                    Element errorInfo = msg.getElement(RESPONSE_ERROR);
                    logger.error("request failed " + errorInfo.getElementAsString(CATEGORY) + " (" + errorInfo.getElementAsString(MESSAGE) + ")");

                    continue;
                }

                Element data = msg.getElement(BAR_DATA).getElement(BAR_TICK_DATA);

                int numBars = data.numValues();
                for (int i = 0; i < numBars; ++i) {

                    Element bbBar = data.getValueAsElement(i);

                    Date time = bbBar.getElementAsDate(TIME).calendar().getTime();
                    double open = bbBar.getElementAsFloat64(OPEN);
                    double high = bbBar.getElementAsFloat64(HIGH);
                    double low = bbBar.getElementAsFloat64(LOW);
                    double close = bbBar.getElementAsFloat64(CLOSE);
                    long volume = bbBar.getElementAsInt64(VOLUME);

                    // ceil the date according to barSize
                    Date dateTime;
                    switch (this.barSize) {
                        case MIN_1:
                            dateTime= DateUtils.ceiling(time, Calendar.MINUTE);
                            break;
                        case MIN_2:
                            dateTime= DateUtils.ceiling(time, Calendar.MINUTE);
                            break;
                        case MIN_5:
                            dateTime= DateUtils.ceiling(time, Calendar.MINUTE);
                            break;
                        case MIN_15:
                            dateTime= DateUtils.ceiling(time, Calendar.MINUTE);
                            break;
                        case MIN_30:
                            dateTime= DateUtils.ceiling(time, Calendar.MINUTE);
                            break;
                        case HOUR_1:
                            dateTime= DateUtils.ceiling(time, Calendar.HOUR);
                            break;
                        case HOUR_2:
                            dateTime= DateUtils.ceiling(time, Calendar.HOUR);
                            break;
                        case DAY_1:
                            dateTime= DateUtils.ceiling(time, Calendar.DATE);
                            break;
                        default:
                            throw new IllegalArgumentException("barSize is not allowed " + this.barSize);
                    }

                    int scale = this.security.getSecurityFamily().getScale();

                    Bar bar = Bar.Factory.newInstance();
                    bar.setDateTime(dateTime);
                    bar.setOpen(RoundUtil.getBigDecimal(open, scale));
                    bar.setHigh(RoundUtil.getBigDecimal(high, scale));
                    bar.setLow(RoundUtil.getBigDecimal(low, scale));
                    bar.setClose(RoundUtil.getBigDecimal(close, scale));
                    bar.setVol((int) volume);
                    bar.setBarSize(this.barSize);
                    bar.setSecurity(this.security);

                    this.barList.add(bar);
                }
            }
        }

        public List<Bar> getBarList() {

            return new ArrayList<Bar>(this.barList);
        }
    }
}
