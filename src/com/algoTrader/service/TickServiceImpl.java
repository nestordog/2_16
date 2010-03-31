package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;

import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.Tick;
import com.algoTrader.entity.TickImpl;
import com.algoTrader.util.EsperService;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.util.SwissquoteUtil;
import com.algoTrader.util.TickCsvWriter;

public class TickServiceImpl extends TickServiceBase {

    private static String exactMatch = "//tr/td[.='%1$s']/parent::tr/following-sibling::tr[1]/td[position()=count(//tr/td[.='%1$s']/preceding-sibling::td)+1]/strong";
    private static String partialMatch = "//tr/td[contains(.,'%1$s')]/parent::tr/following-sibling::tr[1]/td[position()=count(//tr/td[contains(.,'%1$s')]/preceding-sibling::td)+1]/strong";

    private Map<Security, TickCsvWriter> csvWriters = new HashMap<Security, TickCsvWriter>();

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
            if (timeValue != null && Integer.parseInt(timeValue.replace(":", "")) >= 173000) return null;

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

    @SuppressWarnings("unchecked")
    protected void handleProcessSecuritiesOnWatchlist() throws Exception {

        List<Security> securities = getSecurityDao().findSecuritiesOnWatchlist();
        for (Security security : securities) {

            Tick tick = retrieveTick(security);

            if (tick != null) {

                if (tick.isValid()) {
                    EsperService.sendEvent(tick);
                }

                // write the tick to file (even if not valid)
                TickCsvWriter csvWriter;
                if (csvWriters.containsKey(security)) {
                    csvWriter = (TickCsvWriter)csvWriters.get(security);
                } else {
                    csvWriter = new TickCsvWriter(security.getIsin());
                    csvWriters.put(security, csvWriter);
                }
                csvWriter.write(tick);
            }
        }
    }
}
