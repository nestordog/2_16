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
import com.algoTrader.vo.RawTickVO;

public class SqTickServiceImpl extends SqTickServiceBase {

    private static String exactMatch = "//tr/td[.='%1$s']/parent::tr/following-sibling::tr[1]/td[position()=count(//tr/td[.='%1$s']/preceding-sibling::td)+1]/strong";
    private static String partialMatch = "//tr/td[contains(.,'%1$s')]/parent::tr/following-sibling::tr[1]/td[position()=count(//tr/td[contains(.,'%1$s')]/preceding-sibling::td)+1]/strong";

    protected RawTickVO handleRetrieveTick(Security security) throws Exception {

        Document document = SqUtil.getSecurityDocument(security);

        if (XPathAPI.selectSingleNode(document, "//td[contains(.,'Error - Wrong instrument')]") != null) {
            throw new Exception("Wrong Instrument returned for " + security);
        } else if (XPathAPI.selectSingleNode(document, "//div[@id='msgDiv' and contains(.,'verzögert')]") != null) {
            throw new Exception("Delayed quote returned for " + security);
        }

        Tick tick = new TickImpl();

        if (security instanceof StockOption ) {

            // lastDateTime
            String dateValue = SqUtil.getValue(document, String.format(exactMatch, "Datum"));
            String timeValue = SqUtil.getValue(document, String.format(exactMatch, "Zeit"));
            Date lastDateTime = SqUtil.getDate(dateValue + " " + (timeValue != null ? timeValue : "00:00:00"));

            // volume
            String volumeValue = SqUtil.getValue(document, String.format(exactMatch, "Volumen"));
            int volume = SqUtil.getInt(volumeValue);

            // last
            String lastValue = SqUtil.getValue(document, String.format(partialMatch, "Letzter"));
            BigDecimal last = RoundUtil.getBigDecimal(SqUtil.getDouble(lastValue));

            // volBid
            String volBidValue = SqUtil.getValue(document, String.format(exactMatch, "Vol. Geld"));
            int volBid = SqUtil.getInt(volBidValue);

            // volAsk
            String volAskValue = SqUtil.getValue(document, String.format(exactMatch, "Vol. Brief"));
            int volAsk = SqUtil.getInt(volAskValue);

            // check if market is closed
            if (volBid == 0 || volAsk == 0) return null;

            // bid
            String bidValue = SqUtil.getValue(document, String.format(partialMatch, "Geldkurs"));
            BigDecimal bid = RoundUtil.getBigDecimal(SqUtil.getDouble(bidValue));

            // ask
            String askValue = SqUtil.getValue(document, String.format(partialMatch, "Briefkurs"));
            BigDecimal ask = RoundUtil.getBigDecimal(SqUtil.getDouble(askValue));


            // openIntrest
            String openIntrestValue = SqUtil.getValue(document, String.format(exactMatch, "Open Interest"));
            int openIntrest = SqUtil.getInt(openIntrestValue);

            // settlement
            String settlementValue = SqUtil.getValue(document, String.format(exactMatch, "Abrechnungspreis"));
            BigDecimal settlement = RoundUtil.getBigDecimal(SqUtil.getDouble(settlementValue));

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
            String dateValue = SqUtil.getValue(document, String.format(exactMatch, "Datum"));
            String timeValue = SqUtil.getValue(document, String.format(exactMatch, "Zeit"));

            // check if market is closed
            if (timeValue != null && Integer.parseInt(timeValue.replace(":", "")) >= 173000) return null;

            Date lastDateTime = SqUtil.getDate(dateValue + " " + (timeValue != null ? timeValue : "00:00:00"));

            // volume
            String volumeValue = SqUtil.getValue(document, String.format(exactMatch, "Volumen"));
            int volume = SqUtil.getInt(volumeValue);

            // last
            String lastValue = SqUtil.getValue(document, String.format(exactMatch, "Letzter"));
            BigDecimal last = RoundUtil.getBigDecimal(SqUtil.getDouble(lastValue));

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

        return getTickDao().toRawTickVO(tick);
    }

    protected void handlePutOnExternalWatchlist(Security security) throws Exception {
        // do nothing
    }

    protected void handleRemoveFromExternalWatchlist(Security security) throws Exception {
        // do nothing
    }
}
