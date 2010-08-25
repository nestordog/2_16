package com.algoTrader.service.sq;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;

import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.Tick;
import com.algoTrader.entity.TickImpl;
import com.algoTrader.util.RoundUtil;

public class SQTickServiceImpl extends SQTickServiceBase {

    private static String exactMatch = "//tr/td[.='%1$s']/parent::tr/following-sibling::tr[1]/td[position()=count(//tr/td[.='%1$s']/preceding-sibling::td)+1]/strong";
    private static String partialMatch = "//tr/td[contains(.,'%1$s')]/parent::tr/following-sibling::tr[1]/td[position()=count(//tr/td[contains(.,'%1$s')]/preceding-sibling::td)+1]/strong";

    protected Tick handleRetrieveTick(Security security) throws Exception {

        Document document = SQUtil.getSecurityDocument(security);

        if (XPathAPI.selectSingleNode(document, "//td[contains(.,'Error - Wrong instrument')]") != null) {
            throw new Exception("Wrong Instrument returned for " + security);
        } else if (XPathAPI.selectSingleNode(document, "//div[@id='msgDiv' and contains(.,'verzögert')]") != null) {
            throw new Exception("Delayed quote returned for " + security);
        }

        Tick tick = new TickImpl();

        if (security instanceof StockOption ) {

            // lastDateTime
            String dateValue = SQUtil.getValue(document, String.format(exactMatch, "Datum"));
            String timeValue = SQUtil.getValue(document, String.format(exactMatch, "Zeit"));
            Date lastDateTime = SQUtil.getDate(dateValue + " " + (timeValue != null ? timeValue : "00:00:00"));

            // volume
            String volumeValue = SQUtil.getValue(document, String.format(exactMatch, "Volumen"));
            int volume = SQUtil.getInt(volumeValue);

            // last
            String lastValue = SQUtil.getValue(document, String.format(partialMatch, "Letzter"));
            BigDecimal last = RoundUtil.getBigDecimal(SQUtil.getDouble(lastValue));

            // volBid
            String volBidValue = SQUtil.getValue(document, String.format(exactMatch, "Vol. Geld"));
            int volBid = SQUtil.getInt(volBidValue);

            // volAsk
            String volAskValue = SQUtil.getValue(document, String.format(exactMatch, "Vol. Brief"));
            int volAsk = SQUtil.getInt(volAskValue);

            // check if market is closed
            if (volBid == 0 || volAsk == 0) return null;

            // bid
            String bidValue = SQUtil.getValue(document, String.format(partialMatch, "Geldkurs"));
            BigDecimal bid = RoundUtil.getBigDecimal(SQUtil.getDouble(bidValue));

            // ask
            String askValue = SQUtil.getValue(document, String.format(partialMatch, "Briefkurs"));
            BigDecimal ask = RoundUtil.getBigDecimal(SQUtil.getDouble(askValue));


            // openIntrest
            String openIntrestValue = SQUtil.getValue(document, String.format(exactMatch, "Open Interest"));
            int openIntrest = SQUtil.getInt(openIntrestValue);

            // settlement
            String settlementValue = SQUtil.getValue(document, String.format(exactMatch, "Abrechnungspreis"));
            BigDecimal settlement = RoundUtil.getBigDecimal(SQUtil.getDouble(settlementValue));

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
            String dateValue = SQUtil.getValue(document, String.format(exactMatch, "Datum"));
            String timeValue = SQUtil.getValue(document, String.format(exactMatch, "Zeit"));

            // check if market is closed
            if (timeValue != null && Integer.parseInt(timeValue.replace(":", "")) >= 173000) return null;

            Date lastDateTime = SQUtil.getDate(dateValue + " " + (timeValue != null ? timeValue : "00:00:00"));

            // volume
            String volumeValue = SQUtil.getValue(document, String.format(exactMatch, "Volumen"));
            int volume = SQUtil.getInt(volumeValue);

            // last
            String lastValue = SQUtil.getValue(document, String.format(exactMatch, "Letzter"));
            BigDecimal last = RoundUtil.getBigDecimal(SQUtil.getDouble(lastValue));

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
}
