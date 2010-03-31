package com.algoTrader.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

import com.algoTrader.entity.Security;
import com.algoTrader.entity.SecurityImpl;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.StockOptionImpl;
import com.algoTrader.entity.Tick;
import com.algoTrader.entity.TickImpl;
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

    private static final String optionUrl = "http://www.swissquote.ch/sq_mi/market/derivatives/optionfuture/OptionFuture.action?&type=option";

    private static Logger logger = MyLogger.getLogger(StockOptionRetrieverServiceImpl.class.getName());

    private static String [] markets = new String[] {"ud", " eu", "eu", "eu", "eu", "eu", "eu", "eu"};
    private static String [] groups = new String[] {null, "sw", "id", "de", "fr", "it", "sk", "xx" };

    protected StockOption handleRetrieveStockOption(Security underlaying, Date expiration, BigDecimal strike,
            OptionType type) throws ParseException, TransformerException, IOException {

        Calendar cal = new GregorianCalendar();
        cal.setTime(expiration);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int exp = ((year - 2000) * 100 + month);

        String url = optionUrl + "&underlying=" + underlaying.getIsin() + "&expiration=" + exp + "&strike=" + strike.longValue();

        GetMethod get = new GetMethod(url);

        HttpClient standardClient = HttpClientUtil.getStandardClient();
        int status = standardClient.executeMethod(get);

        if (status != HttpStatus.SC_OK) {
            throw new HttpException("invalid option request: underlying=" + underlaying.getIsin() + " expiration=" + exp + " strike=" + strike.longValue());
        }

        Document listDocument = TidyUtil.parse(get.getResponseBodyAsStream());
        get.releaseConnection();

        XmlUtil.saveDocumentToFile(listDocument, underlaying.getIsin() + "_" + exp + "_" + strike.longValue() + ".xml", "results/options/");

        StockOption stockOption = new StockOptionImpl();

        String optionUrl = XPathAPI.selectSingleNode(listDocument, "//td[contains(a/@class,'list')][" + (OptionType.CALL.equals(type) ? 1 : 2) + "]/a/@href").getNodeValue();
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

        String contractSizeValue = SwissquoteUtil.getValue(optionDocument, "//table[tr/td='Datum']/tr[10]/td[3]/strong");
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

        String url = optionUrl + "&underlying=" + underlaying.getIsin() + "&market=eu&group=id";

        GetMethod get = new GetMethod(url);

        HttpClient standardClient = HttpClientUtil.getStandardClient();
        int status = standardClient.executeMethod(get);

        if (status != HttpStatus.SC_OK) {
            throw new HttpException("invalid option request: underlying=" + underlaying.getIsin());
        }

        Document listDocument = TidyUtil.parse(get.getResponseBodyAsStream());
        get.releaseConnection();
        XmlUtil.saveDocumentToFile(listDocument, underlaying.getIsin() + "_all.xml", "results/options/");

        NodeIterator iterator = XPathAPI.selectNodeIterator(listDocument, "//a[@class='list']/@href");

        Node node;
        while ((node = iterator.nextNode()) != null) {

            StockOption stockOption = new StockOptionImpl();

            String param = node.getNodeValue().split("=")[1];

            String isin = param.split("_")[0];

            if (getSecurityDao().findByISIN(isin) != null) continue;

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

            String symbolValue = XPathAPI.selectSingleNode(optionDocument, "//body/div[1]//h1/text()[2]").getNodeValue();
            String symbol = symbolValue.split("\\(")[0].trim().substring(1);

            String contractSizeValue = SwissquoteUtil.getValue(optionDocument, "//table[tr/td='Datum']/tr[10]/td[3]/strong");
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

    protected void handleRetrieveAllStockOptions() throws Exception {


        for (int i = 0; i < markets.length; i++) {

            String market = markets[i];
            String group = groups[i];

            String url = optionUrl + "&market=" + market + ((group != null)? "&group=" + group : "");

            GetMethod get = new GetMethod(url);

            HttpClient standardClient = HttpClientUtil.getStandardClient();
            int status = standardClient.executeMethod(get);

            if (status != HttpStatus.SC_OK) {
                throw new HttpException("invalid option request, market=" + market + ((group != null)? ", group=" + group : ""));
            }

            Document listDocument = TidyUtil.parse(get.getResponseBodyAsStream());
            get.releaseConnection();
            XmlUtil.saveDocumentToFile(listDocument, market + ((group != null)? "_" + group : "") + "_all.xml", "results/options/");

            NodeIterator underlyingIterator = XPathAPI.selectNodeIterator(listDocument, "//select[@name='underlying']/option");

            Node underlyingNode;
            while ((underlyingNode = underlyingIterator.nextNode()) != null) {

                String title = underlyingNode.getFirstChild().getNodeValue();

                String underlayingIsin = XPathAPI.selectSingleNode(underlyingNode, "@value").getNodeValue();

                String detailUrl = url + "&underlying=" + underlayingIsin;

                get = new GetMethod(detailUrl);
                status = standardClient.executeMethod(get);

                if (status != HttpStatus.SC_OK) {
                    throw new HttpException("invalid option request, isin=" + underlayingIsin);
                }

                listDocument = TidyUtil.parse(get.getResponseBodyAsStream());
                get.releaseConnection();
                XmlUtil.saveDocumentToFile(listDocument, underlayingIsin + "_all.xml", "results/options/");

                Node underlayingTable = XPathAPI.selectSingleNode(listDocument, "//table[tr/td/strong='Symbol']/tr[2]");

                if (underlayingTable == null) continue;

                String underlayingUrl = XPathAPI.selectSingleNode(underlayingTable, "td[1]/a/@href").getNodeValue();

                String underlayingMarketId = null;
                String underlayingCurreny = null;

                String queryString = underlayingUrl.split("\\?")[1];
                if (queryString.startsWith("s=")) {
                    queryString = underlayingUrl.split("=")[1];
                    underlayingIsin = queryString.split("_")[0];
                    underlayingMarketId = queryString.split("_")[1];
                    underlayingCurreny = queryString.split("_")[2];

                } else if (underlayingIsin.startsWith("CH")){
                    underlayingMarketId = "M9";
                    underlayingCurreny = "CHF";

                } else if (underlayingIsin.startsWith("DE")){
                    underlayingMarketId = "13";
                    underlayingCurreny = "EUR";
                } else if (underlayingIsin.startsWith("US")){
                    continue;
                } else {
                    throw new RuntimeException("unrecognized isin");
                }

                List<Security> securities = new ArrayList<Security>();
                List<Tick> ticks = new ArrayList<Tick>();

                Security underlaying = getSecurityDao().findByISIN(underlayingIsin);
                if (underlaying == null) {

                    String underlayingSymbol = XPathAPI.selectSingleNode(underlayingTable, "td[1]/a").getFirstChild().getNodeValue();
                    String underlayingLast = XPathAPI.selectSingleNode(underlayingTable, "td[3]/strong/a").getFirstChild().getNodeValue();

                    underlaying = new SecurityImpl();
                    underlaying.setSymbol(underlayingSymbol);
                    underlaying.setIsin(underlayingIsin);
                    underlaying.setMarket(Market.fromString(underlayingMarketId));
                    underlaying.setCurrency(Currency.fromString(underlayingCurreny));

                    Tick underlayingTick = new TickImpl();
                    underlayingTick.setDateTime(new Date());
                    underlayingTick.setLast(RoundUtil.getBigDecimal(SwissquoteUtil.getAmount(underlayingLast)));
                    underlayingTick.setLastDateTime(new Date());
                    underlayingTick.setSecurity(underlaying);
                    underlayingTick.setSettlement(new BigDecimal(0.0));

                    securities.add(underlaying);
                    ticks.add(underlayingTick);
                }

                // get the contract size
                String optionCode = XPathAPI.selectSingleNode(listDocument, "//table[tr/@align='CENTER']/tr[@align='LEFT']/td[8]/a/@href").getNodeValue().split("=")[1];
                String optionIsin = optionCode.split("_")[0];
                String optionMarketId = optionCode.split("_")[1];
                String optionCurreny = optionCode.split("_")[2];

                StockOption stockOption = new StockOptionImpl();
                stockOption.setIsin(optionIsin);
                stockOption.setMarket(Market.fromString(optionMarketId));
                stockOption.setCurrency(Currency.fromString(optionCurreny));

                Document optionDocument = SwissquoteUtil.getSecurityDocument(stockOption);
                String contractSize = SwissquoteUtil.getValue(optionDocument, "//table[tr/td='Datum']/tr[10]/td[3]/strong");

                NodeIterator optionIterator = XPathAPI.selectNodeIterator(listDocument, "//table[tr/@align='CENTER']/tr[count(td)>10]");

                Node optionNode;
                Date optionExpiration = null;
                while ((optionNode = optionIterator.nextNode()) != null) {

                    String align = XPathAPI.selectSingleNode(optionNode, "@align").getNodeValue();
                    if (align.equals("CENTER")) {
                        String monthUrl = XPathAPI.selectSingleNode(optionNode, "td/strong/a/@href").getNodeValue();
                        String month = monthUrl.split("\\?")[1].split("&")[1].split("=")[1] + "01";
                        optionExpiration = new SimpleDateFormat("yyMMdd").parse(month);

                    } else {
                        optionCode = XPathAPI.selectSingleNode(optionNode, "td[8]/a/@href").getNodeValue().split("=")[1];
                        optionIsin = optionCode.split("_")[0];

                        String optionStrike = XPathAPI.selectSingleNode(optionNode, "td[6]/strong/a").getFirstChild().getNodeValue();
                        String optionLast = XPathAPI.selectSingleNode(optionNode, "td[8]/a/strong").getFirstChild().getNodeValue();
                        String optionVol = XPathAPI.selectSingleNode(optionNode, "td[11]").getFirstChild().getNodeValue();
                        String optionOpenIntrest = XPathAPI.selectSingleNode(optionNode, "td[12]").getFirstChild().getNodeValue();

                        stockOption = new StockOptionImpl();
                        stockOption.setIsin(optionIsin);
                        stockOption.setMarket(Market.fromString(optionMarketId));
                        stockOption.setCurrency(Currency.fromString(optionCurreny));
                        stockOption.setStrike(RoundUtil.getBigDecimal(SwissquoteUtil.getAmount(optionStrike)));
                        stockOption.setContractSize(SwissquoteUtil.getNumber(contractSize));
                        stockOption.setType(OptionType.PUT);
                        stockOption.setExpiration(optionExpiration);
                        stockOption.setUnderlaying(underlaying);
                        securities.add(stockOption);

                        Tick optionTick = new TickImpl();
                        optionTick.setDateTime(new Date());
                        optionTick.setLast(RoundUtil.getBigDecimal(SwissquoteUtil.getAmount(optionLast)));
                        optionTick.setLastDateTime(new Date());
                        optionTick.setVol(SwissquoteUtil.getNumber(optionVol));
                        optionTick.setOpenIntrest(SwissquoteUtil.getNumber(optionOpenIntrest));
                        optionTick.setSecurity(stockOption);
                        optionTick.setSettlement(new BigDecimal(0.0));
                        ticks.add(optionTick);
                    }
                }
                getSecurityDao().create(securities);
                getTickDao().create(ticks);

                System.out.println(title);
            }
            System.out.println("done with " + market + " " + group);
        }
    }
}
