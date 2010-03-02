package com.algoTrader.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.transform.TransformerException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.w3c.tidy.Tidy;

import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.StockOptionImpl;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.Market;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.util.HttpClientUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.util.SwissquoteUtil;
import com.algoTrader.util.TidyUtil;
import com.algoTrader.util.XmlUtil;

public class StockOptionRetrieverServiceImpl extends StockOptionRetrieverServiceBase {

    private static final String optionUrl = "http://www.swissquote.ch/sq_mi/market/derivatives/optionfuture/OptionFuture.action?market=eu&type=option&sector=&group=id&type=option";

    private static Logger logger = MyLogger.getLogger(StockOptionRetrieverServiceImpl.class.getName());

    protected StockOption handleRetrieveStockOption(Security underlaying, Date expiration, BigDecimal strike,
            OptionType type) throws ParseException, TransformerException, IOException {

        Calendar cal = new GregorianCalendar();
        cal.setTime(expiration);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int exp = ((year - 2000) * 100 + month);

        String url = optionUrl + "&underlying=" + underlaying.getIsin() + "&expiration=" + exp + "&strike="
                + strike.longValue();

        GetMethod get = new GetMethod(url);

        HttpClient standardClient = HttpClientUtil.getStandardClient();
        int status = standardClient.executeMethod(get);

        if (status == HttpStatus.SC_NOT_FOUND) {
            logger.warn("invalid option request: underlying=" + underlaying.getIsin() + " expiration=" + exp
                    + " strike=" + strike.longValue());
            return null;
        } else if (status != HttpStatus.SC_OK) {
            logger.warn("invalid option request: underlying=" + underlaying.getIsin() + " expiration=" + exp
                    + " strike=" + strike.longValue());
            return null;
        }

        get.releaseConnection();

        Document listDocument = TidyUtil.parse(get.getResponseBodyAsStream());

        XmlUtil.saveDocumentToFile(listDocument, underlaying.getIsin() + "_" + exp + "_" + strike.longValue() + ".xml", "results/options/");

        StockOption stockOption = new StockOptionImpl();

        String optionUrl = XPathAPI.selectSingleNode(listDocument,
                "//td[contains(a/@class,'list')][" + (OptionType.CALL.equals(type) ? 1 : 2) + "]/a/@href")
                .getNodeValue();
        String param = optionUrl.split("=")[1];
        String isin = param.split("_")[0];
        String market = param.split("_")[1];
        String currency = param.split("_")[2];

        stockOption.setIsin(isin);
        stockOption.setMarket(Market.fromString(market));
        stockOption.setCurrency(Currency.fromString(currency));

        stockOption.setType(type);
        stockOption.setStrike(strike);

        Document optionDocument = SwissquoteUtil.getSecurityDocument(stockOption);

        String dateValue = SwissquoteUtil.getValue(optionDocument, "//table[tr/td='Datum']/tr[10]/td[4]/strong");
        Date expirationDate = new SimpleDateFormat("dd-MM-yyyy kk:mm:ss").parse(dateValue + " 13:00:00");

        String contractSizeValue = SwissquoteUtil
                .getValue(optionDocument, "//table[tr/td='Datum']/tr[10]/td[3]/strong");
        int contractSize = (int) Double.parseDouble(contractSizeValue);

        String symbolValue = XPathAPI.selectSingleNode(optionDocument, "//body/div[1]//h1/text()[2]").getNodeValue();
        String symbol = symbolValue.split("\\(")[0].trim().substring(1);

        stockOption.setExpiration(expirationDate);
        stockOption.setSymbol(symbol);
        stockOption.setContractSize(contractSize);

        stockOption.setUnderlaying(underlaying);

        logger.debug("retrieved option " + stockOption.getSymbol());

        return stockOption;
    }

    protected void handleRetrieveAllStockOptions(Security underlaying) throws ParseException, TransformerException,
            IOException {

        String url = optionUrl + "&underlying=" + underlaying.getIsin() + "&expiration=&strike=";

        GetMethod get = new GetMethod(url);

        HttpClient standardClient = HttpClientUtil.getStandardClient();
        int status = standardClient.executeMethod(get);

        if (status == HttpStatus.SC_NOT_FOUND) {
            logger.warn("invalid option request: underlying=" + underlaying.getIsin());
            return;
        } else if (status != HttpStatus.SC_OK) {
            logger.warn("invalid option request: underlying=" + underlaying.getIsin());
            return;
        }

        get.releaseConnection();

        Document listDocument = TidyUtil.parse(get.getResponseBodyAsStream());

        XmlUtil.saveDocumentToFile(listDocument, underlaying.getIsin() + "_all.xml", "results/options/");

        NodeIterator iterator = XPathAPI.selectNodeIterator(listDocument, "//a[@class='list']/@href");

        Node node;
        while ((node = iterator.nextNode()) != null) {

            StockOption stockOption = new StockOptionImpl();

            String param = node.getNodeValue().split("=")[1];

            String isin = param.split("_")[0];
            String market = param.split("_")[1];
            String currency = param.split("_")[2];

            stockOption.setIsin(isin);
            stockOption.setMarket(Market.fromString(market));
            stockOption.setCurrency(Currency.fromString(currency));

            Document optionDocument = SwissquoteUtil.getSecurityDocument(stockOption);

            String typeValue = SwissquoteUtil.getValue(optionDocument, "//table[tr/td='Datum']/tr[10]/td[1]/strong");
            OptionType type = OptionType.fromString(typeValue.split("\\s")[0].toUpperCase());

            String strikeValue = SwissquoteUtil.getValue(optionDocument, "//table[tr/td='Datum']/tr[10]/td[2]/strong");
            BigDecimal strike = RoundUtil.getBigDecimal(SwissquoteUtil.getAmount(strikeValue));

            String dateValue = SwissquoteUtil.getValue(optionDocument, "//table[tr/td='Datum']/tr[10]/td[4]/strong");
            Date expirationDate = new SimpleDateFormat("dd-MM-yyyy kk:mm:ss").parse(dateValue + " 13:00:00");

            String symbolValue = XPathAPI.selectSingleNode(optionDocument, "//body/div[1]//h1/text()[2]")
                    .getNodeValue();
            String symbol = symbolValue.split("\\(")[0].trim().substring(1);

            String contractSizeValue = SwissquoteUtil.getValue(optionDocument,
                    "//table[tr/td='Datum']/tr[10]/td[3]/strong");
            int contractSize = (int) Double.parseDouble(contractSizeValue);

            stockOption.setType(type);
            stockOption.setStrike(strike);
            stockOption.setExpiration(expirationDate);
            stockOption.setSymbol(symbol);
            stockOption.setContractSize(contractSize);

            stockOption.setUnderlaying(underlaying);

            getSecurityDao().create(stockOption);

            logger.debug("retrieved option " + stockOption.getSymbol());
        }
    }
}
