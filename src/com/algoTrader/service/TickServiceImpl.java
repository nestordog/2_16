package com.algoTrader.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.supercsv.exception.SuperCSVReflectionException;
import org.w3c.dom.Document;

import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.Tick;
import com.algoTrader.entity.TickImpl;
import com.algoTrader.util.CsvWriter;
import com.algoTrader.util.EsperService;
import com.algoTrader.util.PropertiesUtil;
import com.algoTrader.util.SwissquoteUtil;

public class TickServiceImpl extends TickServiceBase {

    private static int timeout = Integer.parseInt(PropertiesUtil.getProperty("swissquote.timeout"));

    private static Logger logger = Logger.getLogger(TickServiceImpl.class.getName());

    private List securities = new ArrayList();
    private Map csvWriters = new HashMap();


    protected void handleStart(String isin) throws InterruptedException, SuperCSVReflectionException, IOException {

        Security security = getSecurityDao().findByISIN(isin);

        start(security);
    }

    protected void handleStart(Security security) throws SuperCSVReflectionException, IOException, InterruptedException {

        securities.add(security);

        CsvWriter csvWriter = new CsvWriter(security.getIsin());

        csvWriters.put(security, csvWriter);
    }

    protected void handleStart(List isins) throws Exception {

        for (Iterator it = isins.iterator(); it.hasNext(); ) {

            String isin = (String)it.next();
            start(isin);
        }
    }

    protected void handleStartWatchlist() throws Exception {

        List securities = getSecurityDao().findOnWatchlist();

        for (Iterator it = securities.iterator(); it.hasNext(); ) {

            Security security = (Security)it.next();
            start(security);
        }
    }

    protected void handleStop(String isin) throws Exception {

        Security security = getSecurityDao().findByISIN(isin);
        securities.remove(security);
    }

    protected void handleStop(Security security) throws Exception {

        securities.remove(security);
    }

    protected Tick handleRetrieveTick(Security security) throws ParseException, TransformerException, IOException {

        Document document = SwissquoteUtil.getSecurityDocument(security);

        Tick tick = new TickImpl();

        if (security instanceof StockOption ) {

            // date
            String dateValue = SwissquoteUtil.getValue(document, "//table[tr/td='Datum']/tr[2]/td[1]/strong");
            String timeValue = SwissquoteUtil.getValue(document, "//table[tr/td='Datum']/tr[2]/td[2]/strong");
            Date lastDateTime = SwissquoteUtil.getDate(dateValue + " " + (timeValue != null ? timeValue : "00:00:00"));

            // volume
            String volumeValue = SwissquoteUtil.getValue(document, "//table[tr/td='Datum']/tr[4]/td[1]/strong");
            int volume = SwissquoteUtil.getNumber(volumeValue);

            // last
            String lastValue = SwissquoteUtil.getValue(document, "//table[tr/td='Datum']/tr[4]/td[4]/strong");
            BigDecimal last = SwissquoteUtil.getBigDecimal(SwissquoteUtil.getAmount(lastValue));

            // volBid
            String volBidValue = SwissquoteUtil.getValue(document, "//table[tr/td='Datum']/tr[6]/td[1]/strong");
            int volBid = SwissquoteUtil.getNumber(volBidValue);

            // volAsk
            String volAskValue = SwissquoteUtil.getValue(document, "//table[tr/td='Datum']/tr[6]/td[2]/strong");
            int volAsk = SwissquoteUtil.getNumber(volAskValue);

            // bid
            String bidValue = SwissquoteUtil.getValue(document, "//table[tr/td='Datum']/tr[6]/td[3]/strong");
            BigDecimal bid = SwissquoteUtil.getBigDecimal(SwissquoteUtil.getAmount(bidValue));

            // ask
            String askValue = SwissquoteUtil.getValue(document, "//table[tr/td='Datum']/tr[6]/td[4]/strong");
            BigDecimal ask = SwissquoteUtil.getBigDecimal(SwissquoteUtil.getAmount(askValue));


            // openIntrest
            String openIntrestValue = SwissquoteUtil.getValue(document, "//table[tr/td='Datum']/tr[12]/td[1]/strong");
            int openIntrest = SwissquoteUtil.getNumber(openIntrestValue);

            // settlement
            String settlementValue = SwissquoteUtil.getValue(document, "//table[tr/td='Datum']/tr[12]/td[2]/strong");
            BigDecimal settlement = SwissquoteUtil.getBigDecimal(SwissquoteUtil.getAmount(settlementValue));

            tick.setDateTime(new Date());
            tick.setLast(last);
            tick.setLastDateTime(lastDateTime);
            tick.setVolAsk(volAsk);
            tick.setVolBid(volBid);
            tick.setAsk(ask);
            tick.setBid(bid);
            tick.setVol(volume);
            tick.setOpenIntrest(openIntrest);
            tick.setSettlement(settlement);

        } else if (security instanceof Security ) {

            // date
            String dateValue = SwissquoteUtil.getValue(document, "//table[tr/td='Date']/tr[2]/td[1]/strong");
            String timeValue = SwissquoteUtil.getValue(document, "//table[tr/td='Date']/tr[2]/td[2]/strong");
            Date lastDateTime = SwissquoteUtil.getDate(dateValue + " " + (timeValue != null ? timeValue : "00:00:00"));

            // volume
            String volumeValue = SwissquoteUtil.getValue(document, "//table[tr/td='Date']/tr[4]/td[1]/strong");
            int volume = SwissquoteUtil.getNumber(volumeValue);

            // last
            String lastValue = SwissquoteUtil.getValue(document, "//table[tr/td='Date']/tr[4]/td[4]/strong");
            BigDecimal last = SwissquoteUtil.getBigDecimal(SwissquoteUtil.getAmount(lastValue));

            tick.setDateTime(new Date());
            tick.setLast(last);
            tick.setLastDateTime(lastDateTime);
            tick.setVolAsk(0);
            tick.setVolBid(0);
            tick.setAsk(SwissquoteUtil.getBigDecimal(0));
            tick.setBid(SwissquoteUtil.getBigDecimal(0));
            tick.setVol(volume);
            tick.setOpenIntrest(0);
            tick.setSettlement(SwissquoteUtil.getBigDecimal(0));
        }

        tick.setSecurity(security);

        logger.debug(tick);

        return tick;
    }

    protected Tick handleRetrieveTick(String isin) throws IOException, ParseException, TransformerException {

        Security security = getSecurityDao().findByISIN(isin);
        return handleRetrieveTick(security);
    }

    protected void handleRun() throws SuperCSVReflectionException, IOException, InterruptedException {

        while(true) {

            for (Iterator it = securities.iterator(); it.hasNext();) {
                Security security = (Security)it.next();

                Tick tick = retrieveTick(security);

                EsperService.getEPServiceInstance().getEPRuntime().sendEvent(tick);

                CsvWriter csvWriter = (CsvWriter)csvWriters.get(security);
                csvWriter.writeTick(tick);
            }

            Thread.sleep(timeout);
        }
    }
}
