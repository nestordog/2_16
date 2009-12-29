package com.algoTrader.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.supercsv.exception.SuperCSVException;
import org.supercsv.exception.SuperCSVReflectionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.w3c.tidy.Tidy;

import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.StockOptionImpl;
import com.algoTrader.entity.Tick;
import com.algoTrader.entity.TickImpl;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.Market;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.util.BigDecimalUtil;
import com.algoTrader.util.CsvWriter;
import com.algoTrader.util.HttpClientUtil;
import com.algoTrader.util.PropertiesUtil;
import com.algoTrader.util.TidyUtil;
import com.algoTrader.util.XmlUtil;


public class TickServiceImpl extends TickServiceBase {

    private static String userId = PropertiesUtil.getProperty("swissquote.userId");
    private static String password = PropertiesUtil.getProperty("swissquote.password");
    private static int timeout = Integer.parseInt(PropertiesUtil.getProperty("swissquote.timeout"));

    private static String tickUrl = "http://www.swissquote.ch/sq_mi/public/market/Detail.action?s=";

    private static final String optionUrl = "http://www.swissquote.ch/sq_mi/market/derivatives/optionfuture/OptionFuture.action?market=eu&type=option&sector=&group=id&type=option";

    private static Logger logger = Logger.getLogger(TickServiceImpl.class.getName());

    private List securities = new ArrayList();
    private Map csvWriters = new HashMap();

    protected void handleRun(Security security) throws SuperCSVException, IOException, InterruptedException {

        securities.add(security);

        CsvWriter csvWriter = new CsvWriter(security.getIsin());

        csvWriters.put(security, csvWriter);

        run();
    }


    protected void handleRun(List lst) throws Exception {


        for (Iterator it = lst.iterator(); it.hasNext(); ) {
            String isin = (String)it.next();
            Security security = getSecurityDao().findByISIN(isin);

            securities.add(security);
            csvWriters.put(security, new CsvWriter(security.getIsin()));
        }

        run();
    }

    protected void handleRun(String isin) throws InterruptedException, SuperCSVReflectionException, IOException {

        Security security = getSecurityDao().findByISIN(isin);
        handleRun(security);
    }

    protected void handleStop(Security security) throws Exception {

        securities.remove(security);
    }


    protected void handleStop(String isin) throws Exception {

        Security security = getSecurityDao().findByISIN(isin);
        securities.remove(security);
    }

    protected Tick handleRetrieveTick(Security security) throws ParseException, TransformerException, IOException {

        Document document = getSecurityDocument(security);

        Tick tick = new TickImpl();

        if (security instanceof StockOption ) {

            // date
            String dateValue = getValue(document, "//table[tr/td='Datum']/tr[2]/td[1]/strong");
            String timeValue = getValue(document, "//table[tr/td='Datum']/tr[2]/td[2]/strong");
            Date lastDateTime = getDate(dateValue + " " + (timeValue != null ? timeValue : "00:00:00"));

            // volume
            String volumeValue = getValue(document, "//table[tr/td='Datum']/tr[4]/td[1]/strong");
            int volume = getNumber(volumeValue);

            // last
            String lastValue = getValue(document, "//table[tr/td='Datum']/tr[4]/td[4]/strong");
            BigDecimal last = BigDecimalUtil.getBigDecimal(getAmount(lastValue));

            // volBid
            String volBidValue = getValue(document, "//table[tr/td='Datum']/tr[6]/td[1]/strong");
            int volBid = getNumber(volBidValue);

            // volAsk
            String volAskValue = getValue(document, "//table[tr/td='Datum']/tr[6]/td[2]/strong");
            int volAsk = getNumber(volAskValue);

            // bid
            String bidValue = getValue(document, "//table[tr/td='Datum']/tr[6]/td[3]/strong");
            BigDecimal bid = BigDecimalUtil.getBigDecimal(getAmount(bidValue));

            // ask
            String askValue = getValue(document, "//table[tr/td='Datum']/tr[6]/td[4]/strong");
            BigDecimal ask = BigDecimalUtil.getBigDecimal(getAmount(askValue));


            // openIntrest
            String openIntrestValue = getValue(document, "//table[tr/td='Datum']/tr[12]/td[1]/strong");
            int openIntrest = getNumber(openIntrestValue);

            // settlement
            String settlementValue = getValue(document, "//table[tr/td='Datum']/tr[12]/td[2]/strong");
            BigDecimal settlement = BigDecimalUtil.getBigDecimal(getAmount(settlementValue));

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
            String dateValue = getValue(document, "//table[tr/td='Date']/tr[2]/td[1]/strong");
            String timeValue = getValue(document, "//table[tr/td='Date']/tr[2]/td[2]/strong");
            Date lastDateTime = getDate(dateValue + " " + (timeValue != null ? timeValue : "00:00:00"));

            // volume
            String volumeValue = getValue(document, "//table[tr/td='Date']/tr[4]/td[1]/strong");
            int volume = getNumber(volumeValue);

            // last
            String lastValue = getValue(document, "//table[tr/td='Date']/tr[4]/td[4]/strong");
            BigDecimal last = BigDecimalUtil.getBigDecimal(getAmount(lastValue));

            tick.setDateTime(new Date());
            tick.setLast(last);
            tick.setLastDateTime(lastDateTime);
            tick.setVolAsk(0);
            tick.setVolBid(0);
            tick.setAsk(BigDecimalUtil.getBigDecimal(0));
            tick.setBid(BigDecimalUtil.getBigDecimal(0));
            tick.setVol(volume);
            tick.setOpenIntrest(0);
            tick.setSettlement(BigDecimalUtil.getBigDecimal(0));
        }

        tick.setSecurity(security);

        logger.debug(tick);

        return tick;
    }


    protected Tick handleRetrieveTick(String isin) throws IOException, ParseException, TransformerException {

        Security security = getSecurityDao().findByISIN(isin);
        return handleRetrieveTick(security);
    }


    protected StockOption handleRetrieveStockOption(Security underlaying, Date expiration, BigDecimal strike, OptionType type) throws ParseException, TransformerException, IOException {

        Calendar cal = new GregorianCalendar();
        cal.setTime(expiration);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int exp = ((year - 2000) * 100 + month);

        String url = optionUrl + "&underlying=" + underlaying.getIsin() +"&expiration=" + exp + "&strike=" + strike.longValue();

        GetMethod get = new GetMethod(url);

        String content;
        try {
            HttpClient standardClient = HttpClientUtil.getStandardClient(true);
            int status = standardClient.executeMethod(get);

            if (status == HttpStatus.SC_NOT_FOUND) {
                logger.warn("invalid option request: underlying=" + underlaying.getIsin() +" expiration=" + exp + " strike=" + strike.longValue());
                return null;
            }else if (status != HttpStatus.SC_OK) {
                logger.warn("invalid option request: underlying=" + underlaying.getIsin() +" expiration=" + exp + " strike=" + strike.longValue());
                return null;
            }

            // get the content
            InputStream in = get.getResponseBodyAsStream();
            StringBuffer out = new StringBuffer();
            byte[] b = new byte[1024];
            for (int n; (n = in.read(b)) != -1;) {
                out.append(new String(b, 0, n));
            }
            in.close();
            content = out.toString();

        } finally {
            get.releaseConnection();
        }

        // parse the Document using Tidy
        Tidy tidy = TidyUtil.getInstance();
        Document listDocument = tidy.parseDOM(new ByteArrayInputStream(content.getBytes()), null);

        // save the file
        XmlUtil.saveDocumentToFile(listDocument, underlaying.getIsin() + "_" + exp + "_" + strike.longValue() + ".xml", "results/options/", false);

        StockOption option = new StockOptionImpl();

        String optionUrl = XPathAPI.selectSingleNode(listDocument, "//td[contains(a/@class,'list')][" + (OptionType.CALL.equals(type) ? 1 : 2) + "]/a/@href").getNodeValue();
        String param = optionUrl.split("=")[1];
        String isin = param.split("_")[0];
        String market = param.split("_")[1];
        String currency = param.split("_")[2];

        option.setIsin(isin);
        option.setMarket(Market.fromString(market));
        option.setCurrency(Currency.fromString(currency));

        option.setType(type);
        option.setStrike(strike);

        Document optionDocument = getSecurityDocument(option);

        String dateValue = getValue(optionDocument, "//table[tr/td='Datum']/tr[10]/td[4]/strong");
        Date expirationDate = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").parse(dateValue + " 13:00:00");

        String contractSizeValue = getValue(optionDocument, "//table[tr/td='Datum']/tr[10]/td[3]/strong");
        int contractSize = (int)Double.parseDouble(contractSizeValue);

        String symbolValue = XPathAPI.selectSingleNode(optionDocument, "//body/div[1]//h1/text()[2]").getNodeValue();
        String symbol = symbolValue.split("\\(")[0].trim().substring(1);

        option.setExpiration(expirationDate);
        option.setSymbol(symbol);
        option.setContractSize(contractSize);

        option.setUnderlaying(underlaying);

        return option;
    }



    protected void handleRetrieveAllStockOptions(Security underlaying) throws ParseException, TransformerException, IOException {

        String url = optionUrl + "&underlying=" + underlaying.getIsin() + "&expiration=&strike=";

        GetMethod get = new GetMethod(url);

        String content;
        try {
            HttpClient standardClient = HttpClientUtil.getStandardClient(true);
            int status = standardClient.executeMethod(get);

            if (status == HttpStatus.SC_NOT_FOUND) {
                logger.warn("invalid option request: underlying=" + underlaying.getIsin());
                return;
            }else if (status != HttpStatus.SC_OK) {
                logger.warn("invalid option request: underlying=" + underlaying.getIsin());
                return;
            }

            // get the content
            InputStream in = get.getResponseBodyAsStream();
            StringBuffer out = new StringBuffer();
            byte[] b = new byte[1024];
            for (int n; (n = in.read(b)) != -1;) {
                out.append(new String(b, 0, n));
            }
            in.close();
            content = out.toString();

        } finally {
            get.releaseConnection();
        }

        // parse the Document using Tidy
        Tidy tidy = TidyUtil.getInstance();
        Document listDocument = tidy.parseDOM(new ByteArrayInputStream(content.getBytes()), null);

        // save the file
        XmlUtil.saveDocumentToFile(listDocument, underlaying.getIsin() + "_all.xml", "results/options/", false);

        NodeIterator iterator = XPathAPI.selectNodeIterator(listDocument, "//a[@class='list']/@href");

        Node node;
        while ((node = iterator.nextNode()) != null) {

            StockOption option = new StockOptionImpl();

            String param = node.getNodeValue().split("=")[1];

            String isin = param.split("_")[0];
            String market = param.split("_")[1];
            String currency = param.split("_")[2];

            option.setIsin(isin);
            option.setMarket(Market.fromString(market));
            option.setCurrency(Currency.fromString(currency));

            Document optionDocument = getSecurityDocument(option);

            String typeValue = getValue(optionDocument, "//table[tr/td='Datum']/tr[10]/td[1]/strong");
            OptionType type = OptionType.fromString(typeValue.split("\\s")[0].toUpperCase());

            String strikeValue = getValue(optionDocument, "//table[tr/td='Datum']/tr[10]/td[2]/strong");
            BigDecimal strike = BigDecimalUtil.getBigDecimal(getAmount(strikeValue));

            String dateValue = getValue(optionDocument, "//table[tr/td='Datum']/tr[10]/td[4]/strong");
            Date expirationDate = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").parse(dateValue + " 13:00:00");

            String symbolValue = XPathAPI.selectSingleNode(optionDocument, "//body/div[1]//h1/text()[2]").getNodeValue();
            String symbol = symbolValue.split("\\(")[0].trim().substring(1);

            String contractSizeValue = getValue(optionDocument, "//table[tr/td='Datum']/tr[10]/td[3]/strong");
            int contractSize = (int)Double.parseDouble(contractSizeValue);

            option.setType(type);
            option.setStrike(strike);
            option.setExpiration(expirationDate);
            option.setSymbol(symbol);
            option.setContractSize(contractSize);

            option.setUnderlaying(underlaying);

            getSecurityDao().create(option);
        }
    }


    private void run() throws SuperCSVReflectionException, IOException, InterruptedException {

        getCepService().runAll();

        while(true) {

            for (Iterator it = securities.iterator(); it.hasNext();) {
                Security security = (Security)it.next();

                Tick tick = retrieveTick(security);

                getCepService().sendEvent(tick);

                CsvWriter csvWriter = (CsvWriter)csvWriters.get(security);
                csvWriter.writeTick(tick);
            }

            Thread.sleep(timeout);
        }
    }


    private Document getSecurityDocument(Security security) throws IOException, HttpException {

        GetMethod get = new GetMethod(tickUrl + security.getIsin() + "_" + security.getMarket() + "_" + security.getCurrency());

        String content;
        try {
            HttpClient standardClient = HttpClientUtil.getSwissquotePremiumClient(userId, password, true);
            int status = standardClient.executeMethod(get);

            if (status == HttpStatus.SC_NOT_FOUND) {
                logger.warn("invalid security: " + security.getIsin());
                return null;
            }else if (status != HttpStatus.SC_OK) {
                logger.error("could not retrieve security: " + security.getIsin() + ", status: " + get.getStatusLine());
                return null;
            }

            // get the content
            InputStream in = get.getResponseBodyAsStream();
            StringBuffer out = new StringBuffer();
            byte[] b = new byte[1024];
            for (int n; (n = in.read(b)) != -1;) {
                out.append(new String(b, 0, n));
            }
            in.close();
            content = out.toString();

        } finally {
            get.releaseConnection();
        }

        // parse the Document using Tidy
        Tidy tidy = TidyUtil.getInstance();
        Document document = tidy.parseDOM(new ByteArrayInputStream(content.getBytes()), null);

        // save the file
        XmlUtil.saveDocumentToFile(document, security.getIsin() + ".xml", "results/swissquote/", false);
        return document;
    }


    private static String getValue(Node document, String expression) throws TransformerException {

        Node node = XPathAPI.selectSingleNode(document, expression);
        if (node == null || node.getFirstChild() == null) return null;

        return node.getFirstChild().getNodeValue();
    }

    private static int getNumber(String inputString) throws ParseException {

        if ("-".equals(inputString)) return 0;

        return NumberFormat.getNumberInstance().parse(inputString).intValue();
    }

    private static double getAmount(String inputString) throws ParseException {

        if (inputString.contains("-")) return 0;

        int index = inputString.indexOf(" ");
        if (index == -1) {
            index = inputString.length();
        }
        return NumberFormat.getNumberInstance().parse(inputString.substring(0,index)).doubleValue();
    }

    private static Date getDate(String date) throws ParseException {
        if (date.startsWith("null")) return null;
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(date);
    }
}
