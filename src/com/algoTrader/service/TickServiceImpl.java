package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.Tick;
import com.algoTrader.entity.TickImpl;
import com.algoTrader.util.CsvWriter;
import com.algoTrader.util.EsperService;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.PropertiesUtil;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.util.SwissquoteUtil;

public class TickServiceImpl extends TickServiceBase {

    private static int timeout = Integer.parseInt(PropertiesUtil.getProperty("retrieval.timeout"));

    private static Logger logger = MyLogger.getLogger(TickServiceImpl.class.getName());

    private static String exactMatch = "//tr/td[.='%1$s']/parent::tr/following-sibling::tr[1]/td[position()=count(//tr/td[.='%1$s']/preceding-sibling::td)+1]/strong";
    private static String partialMatch = "//tr/td[contains(.,'%1$s')]/parent::tr/following-sibling::tr[1]/td[position()=count(//tr/td[contains(.,'%1$s')]/preceding-sibling::td)+1]/strong";

    private Map csvWriters = new HashMap();

    protected Tick handleRetrieveTick(Security security) throws Exception {

        Document document = SwissquoteUtil.getSecurityDocument(security);

        if (XPathAPI.selectSingleNode(document, "//td[contains(.,'Error - Wrong instrument')]") != null) {
            throw new Exception("Wrong Instrument returned for " + security);
        }

        Tick tick = new TickImpl();

        if (security instanceof StockOption ) {

            // lastDateTime
            String dateValue = SwissquoteUtil.getValue(document, String.format(exactMatch, "Datum"));
            String timeValue = SwissquoteUtil.getValue(document, String.format(exactMatch, "Zeit"));
            Date lastDateTime = SwissquoteUtil.getDate(dateValue + " " + (timeValue != null ? timeValue : "00:00:00"));

            // volume
            String volumeValue = SwissquoteUtil.getValue(document, String.format(exactMatch, "Volumen"));
            int volume = SwissquoteUtil.getNumber(volumeValue);

            // last
            String lastValue = SwissquoteUtil.getValue(document, String.format(partialMatch, "Letzter"));
            BigDecimal last = RoundUtil.getBigDecimal(SwissquoteUtil.getAmount(lastValue));

            // volBid
            String volBidValue = SwissquoteUtil.getValue(document, String.format(exactMatch, "Vol. Geld"));
            int volBid = SwissquoteUtil.getNumber(volBidValue);

            // volAsk
            String volAskValue = SwissquoteUtil.getValue(document, String.format(exactMatch, "Vol. Brief"));
            int volAsk = SwissquoteUtil.getNumber(volAskValue);

            // check if market is closed
            if (volBid == 0 || volAsk == 0) return null;

            // bid
            String bidValue = SwissquoteUtil.getValue(document, String.format(partialMatch, "Geldkurs"));
            BigDecimal bid = RoundUtil.getBigDecimal(SwissquoteUtil.getAmount(bidValue));

            // ask
            String askValue = SwissquoteUtil.getValue(document, String.format(partialMatch, "Briefkurs"));
            BigDecimal ask = RoundUtil.getBigDecimal(SwissquoteUtil.getAmount(askValue));


            // openIntrest
            String openIntrestValue = SwissquoteUtil.getValue(document, String.format(exactMatch, "Open Interest"));
            int openIntrest = SwissquoteUtil.getNumber(openIntrestValue);

            // settlement
            String settlementValue = SwissquoteUtil.getValue(document, String.format(exactMatch, "Abrechnungspreis"));
            BigDecimal settlement = RoundUtil.getBigDecimal(SwissquoteUtil.getAmount(settlementValue));

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

            // lastDateTime
            String dateValue = SwissquoteUtil.getValue(document, String.format(exactMatch, "Datum"));
            String timeValue = SwissquoteUtil.getValue(document, String.format(exactMatch, "Zeit"));

            // check if market is closed
            if (timeValue != null && Integer.parseInt(timeValue.replace(":", "")) > 173000) return null;

            Date lastDateTime = SwissquoteUtil.getDate(dateValue + " " + (timeValue != null ? timeValue : "00:00:00"));

            // volume
            String volumeValue = SwissquoteUtil.getValue(document, String.format(exactMatch, "Volumen"));
            int volume = SwissquoteUtil.getNumber(volumeValue);

            // last
            String lastValue = SwissquoteUtil.getValue(document, String.format(exactMatch, "Letzter"));
            BigDecimal last = RoundUtil.getBigDecimal(SwissquoteUtil.getAmount(lastValue));

            tick.setDateTime(new Date());
            tick.setLast(last);
            tick.setLastDateTime(lastDateTime);
            tick.setVolAsk(0);
            tick.setVolBid(0);
            tick.setAsk(RoundUtil.getBigDecimal(0));
            tick.setBid(RoundUtil.getBigDecimal(0));
            tick.setVol(volume);
            tick.setOpenIntrest(0);
            tick.setSettlement(RoundUtil.getBigDecimal(0));
        }

        tick.setSecurity(security);

        return tick;
    }

    /**
     * Not used at the moment (handled by esper timer)
     */
    protected void handleRun() {

        (new Thread("AlgoTraderTickService") {
            public void run() {
                while(true) {
                    try {
                        ServiceLocator.instance().getTickService().processSecuritiesOnWatchlist();
                        Thread.sleep(timeout);
                    } catch (Exception ex) {
                        logger.error("error retrieving ticks ", ex);
                    }
                }
            }
        }).start();
    }

    protected void handleProcessSecuritiesOnWatchlist() throws Exception {

        List securities = getSecurityDao().findSecuritiesOnWatchlist();
        for (Iterator it = securities.iterator(); it.hasNext();) {
            Security security = (Security)it.next();

            Tick tick = retrieveTick(security);

            if (tick != null) {

                if (tick.isValid()) {
                    EsperService.getEPServiceInstance().getEPRuntime().sendEvent(tick);
                }

                // write the tick to file (even if not valid)
                CsvWriter csvWriter;
                if (csvWriters.containsKey(security)) {
                    csvWriter = (CsvWriter)csvWriters.get(security);
                } else {
                    csvWriter = new CsvWriter(security.getIsin());
                    csvWriters.put(security, csvWriter);
                }
                csvWriter.writeTick(tick);
            }
        }
    }
}
