package com.algoTrader.service.sq;

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
import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math.optimization.fitting.PolynomialFitter;
import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
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
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.service.StockOptionRetrieverServiceImpl;
import com.algoTrader.stockOption.StockOptionUtil;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.util.TidyUtil;
import com.algoTrader.util.XmlUtil;

public class SqStockOptionRetrieverServiceImpl extends SqStockOptionRetrieverServiceBase {

    private static final String optionUrl = "http://www.swissquote.ch/sq_mi/market/derivatives/optionfuture/OptionFuture.action?&type=option";
    private static Logger logger = MyLogger.getLogger(StockOptionRetrieverServiceImpl.class.getName());
    private static String [] markets = new String[] {"eu", "eu", "eu", "eu", "eu", "eu", "eu", "ud"};
    private static String [] groups = new String[] {"sw", "id", "de", "fr", "it", "sk", "xx", null };
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyMM");
    private static SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");

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

        Document listDocument;
        try {
            int status = standardClient.executeMethod(get);

            listDocument = TidyUtil.parse(get.getResponseBodyAsStream());

            XmlUtil.saveDocumentToFile(listDocument, underlaying.getIsin() + "_" + exp + "_" + strike.longValue() + ".xml", "results/options/");

            if (status != HttpStatus.SC_OK) {
                throw new HttpException("invalid option request: underlying=" + underlaying.getIsin() + " expiration=" + exp + " strike=" + strike.longValue());
            }

        } finally {
            get.releaseConnection();
        }

        StockOption stockOption = new StockOptionImpl();

        String optionUrl = SqUtil.getValue(listDocument, "//td[contains(a/@class,'list')][" + (OptionType.CALL.equals(type) ? 1 : 2) + "]/a/@href");
        String param = optionUrl.split("=")[1];
        String isin = param.split("_")[0];
        String market = param.split("_")[1];
        String currency = param.split("_")[2];

        stockOption.setIsin(isin);
        stockOption.setMarket(SqMarketConverter.marketFromString(market));
        stockOption.setCurrency(Currency.fromString(currency));

        stockOption.setType(type);
        stockOption.setStrike(strike);

        Document optionDocument = SqUtil.getSecurityDocument(stockOption);

        String dateValue = SqUtil.getValue(optionDocument, "//table[tr/td='Datum']/tr[10]/td[4]/strong");
        Date expirationDate = new SimpleDateFormat("dd-MM-yyyy kk:mm:ss").parse(dateValue + " 13:00:00");

        String contractSizeValue = SqUtil.getValue(optionDocument, "//table[tr/td='Datum']/tr[10]/td[3]/strong");
        int contractSize = (int) Double.parseDouble(contractSizeValue);

        String symbolValue = SqUtil.getValue(optionDocument, "//body/div[1]//h1/text()[2]");
        String symbol = symbolValue.split("\\(")[0].trim().substring(1);

        stockOption.setExpiration(expirationDate);
        stockOption.setSymbol(symbol);
        stockOption.setContractSize(contractSize);
        stockOption.setMarketOpen(timeFormat.parse("09:00:00"));
        stockOption.setMarketClose(timeFormat.parse("17:00:00"));

        stockOption.setUnderlaying(underlaying);

        logger.debug("retrieved option " + stockOption.getSymbol());

        return stockOption;
    }

    protected void handleRetrieveAllStockOptions(Security underlaying) throws ParseException, TransformerException,
            IOException {

        String url = optionUrl + "&underlying=" + underlaying.getIsin() + "&market=eu&group=id";

        GetMethod get = new GetMethod(url);

        HttpClient standardClient = HttpClientUtil.getStandardClient();

        Document listDocument;
        try {
            int status = standardClient.executeMethod(get);

            listDocument = TidyUtil.parse(get.getResponseBodyAsStream());

            XmlUtil.saveDocumentToFile(listDocument, underlaying.getIsin() + "_all.xml", "results/options/");

            if (status != HttpStatus.SC_OK) {
                throw new HttpException("invalid option request: underlying=" + underlaying.getIsin());
            }

        } finally {
            get.releaseConnection();
        }

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
            stockOption.setMarket(SqMarketConverter.marketFromString(market));
            stockOption.setCurrency(Currency.fromString(currency));

            Document optionDocument = SqUtil.getSecurityDocument(stockOption);

            String typeValue = SqUtil.getValue(optionDocument, "//table[tr/td='Datum']/tr[10]/td[1]/strong");
            OptionType type = OptionType.fromString(typeValue.split("\\s")[0].toUpperCase());

            String strikeValue = SqUtil.getValue(optionDocument, "//table[tr/td='Datum']/tr[10]/td[2]/strong");
            BigDecimal strike = RoundUtil.getBigDecimal(SqUtil.getDouble(strikeValue));

            String dateValue = SqUtil.getValue(optionDocument, "//table[tr/td='Datum']/tr[10]/td[4]/strong");
            Date expirationDate = new SimpleDateFormat("dd-MM-yyyy kk:mm:ss").parse(dateValue + " 13:00:00");

            String symbolValue = SqUtil.getValue(optionDocument, "//body/div[1]//h1/text()[2]");
            String symbol = symbolValue.split("\\(")[0].trim().substring(1);

            String contractSizeValue = SqUtil.getValue(optionDocument, "//table[tr/td='Datum']/tr[10]/td[3]/strong");
            int contractSize = (int) Double.parseDouble(contractSizeValue);

            stockOption.setType(type);
            stockOption.setStrike(strike);
            stockOption.setExpiration(expirationDate);
            stockOption.setSymbol(symbol);
            stockOption.setContractSize(contractSize);
            stockOption.setMarketOpen(timeFormat.parse("09:00:00"));
            stockOption.setMarketClose(timeFormat.parse("17:00:00"));

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

            int status;
            Document listDocument;
            try {
                status = standardClient.executeMethod(get);

                listDocument = TidyUtil.parse(get.getResponseBodyAsStream());

                XmlUtil.saveDocumentToFile(listDocument, market + ((group != null)? "_" + group : "") + "_all.xml", "results/options/");

                if (status != HttpStatus.SC_OK) {
                    throw new HttpException("invalid option request, market=" + market + ((group != null)? ", group=" + group : ""));
                }

            } finally {
                get.releaseConnection();
            }

            NodeIterator underlyingIterator = XPathAPI.selectNodeIterator(listDocument, "//select[@name='underlying']/option");

            Node underlyingNode;
            while ((underlyingNode = underlyingIterator.nextNode()) != null) {

                String title = underlyingNode.getFirstChild().getNodeValue();

                String underlayingIsin = SqUtil.getValue(underlyingNode, "@value");

                String detailUrl = url + "&underlying=" + underlayingIsin;

                get = new GetMethod(detailUrl);

                try {
                    status = standardClient.executeMethod(get);

                    listDocument = TidyUtil.parse(get.getResponseBodyAsStream());

                    XmlUtil.saveDocumentToFile(listDocument, underlayingIsin + "_all.xml", "results/options/");

                    if (status != HttpStatus.SC_OK) {
                        throw new HttpException("invalid option request, isin=" + underlayingIsin);
                    }

                } finally {
                    get.releaseConnection();
                }

                Node underlayingTable = XPathAPI.selectSingleNode(listDocument, "//table[tr/td/strong='Symbol']/tr[2]");

                if (underlayingTable == null) continue;

                String underlayingUrl = SqUtil.getValue(underlayingTable, "td[1]/a/@href");

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

                    String underlayingSymbol = SqUtil.getValue(underlayingTable, "td[1]/a");
                    String underlayingLast = SqUtil.getValue(underlayingTable, "td[3]/strong/a");

                    underlaying = new SecurityImpl();
                    underlaying.setSymbol(underlayingSymbol);
                    underlaying.setIsin(underlayingIsin);
                    underlaying.setMarket(SqMarketConverter.marketFromString(underlayingMarketId));
                    underlaying.setCurrency(Currency.fromString(underlayingCurreny));

                    Tick underlayingTick = new TickImpl();
                    underlayingTick.setDateTime(new Date());
                    underlayingTick.setLast(RoundUtil.getBigDecimal(SqUtil.getDouble(underlayingLast)));
                    underlayingTick.setLastDateTime(new Date());
                    underlayingTick.setSecurity(underlaying);
                    underlayingTick.setSettlement(new BigDecimal(0.0));

                    securities.add(underlaying);
                    ticks.add(underlayingTick);
                }

                // get the contract size
                String optionCode = SqUtil.getValue(listDocument, "//table[tr/@align='CENTER']/tr[@align='LEFT']/td[8]/a/@href").split("=")[1];
                String optionIsin = optionCode.split("_")[0];
                String optionMarketId = optionCode.split("_")[1];
                String optionCurreny = optionCode.split("_")[2];

                StockOption stockOption = new StockOptionImpl();
                stockOption.setIsin(optionIsin);
                stockOption.setMarket(SqMarketConverter.marketFromString(optionMarketId));
                stockOption.setCurrency(Currency.fromString(optionCurreny));

                Document optionDocument = SqUtil.getSecurityDocument(stockOption);
                String contractSize = SqUtil.getValue(optionDocument, "//table[tr/td='Datum']/tr[10]/td[3]/strong");

                NodeIterator optionIterator = XPathAPI.selectNodeIterator(listDocument, "//table[tr/@align='CENTER']/tr[count(td)>10]");

                Node optionNode;
                Date optionExpiration = null;
                while ((optionNode = optionIterator.nextNode()) != null) {

                    String align = SqUtil.getValue(optionNode, "@align");
                    if (align.equals("CENTER")) {
                        String monthUrl = SqUtil.getValue(optionNode, "td/strong/a/@href");
                        String month = monthUrl.split("\\?")[1].split("&")[1].split("=")[1] + "01";
                        optionExpiration = new SimpleDateFormat("yyMMdd").parse(month);

                    } else {
                        String optionStrike = SqUtil.getValue(optionNode, "td[6]/strong/a");

                        String callOptionIsin = SqUtil.getValue(optionNode, "td[5]/a/@href").split("=")[1].split("_")[0];
                        String putOptionIsin = SqUtil.getValue(optionNode, "td[8]/a/@href").split("=")[1].split("_")[0];

                        String callOptionOpenIntrest = SqUtil.getValue(optionNode, "td[1]");
                        String callOptionVol = SqUtil.getValue(optionNode, "td[2]");
                        String callOptionlLast = SqUtil.getValue(optionNode, "td[5]/a/strong");
                        String putOptionLast = SqUtil.getValue(optionNode, "td[8]/a/strong");
                        String putOptionVol = SqUtil.getValue(optionNode, "td[11]");
                        String putOptionOpenIntrest = SqUtil.getValue(optionNode, "td[12]");

                        StockOption callOption = new StockOptionImpl();
                        callOption.setIsin(callOptionIsin);
                        callOption.setMarket(SqMarketConverter.marketFromString(optionMarketId));
                        callOption.setCurrency(Currency.fromString(optionCurreny));
                        callOption.setStrike(RoundUtil.getBigDecimal(SqUtil.getDouble(optionStrike)));
                        callOption.setContractSize(SqUtil.getInt(contractSize));
                        callOption.setType(OptionType.CALL);
                        callOption.setExpiration(optionExpiration);
                        callOption.setUnderlaying(underlaying);
                        securities.add(callOption);

                        Tick callOptionTick = new TickImpl();
                        callOptionTick.setDateTime(new Date());
                        callOptionTick.setBid(new BigDecimal(0));
                        callOptionTick.setAsk(new BigDecimal(0));
                        callOptionTick.setLast(RoundUtil.getBigDecimal(SqUtil.getDouble(callOptionlLast)));
                        callOptionTick.setLastDateTime(new Date());
                        callOptionTick.setVol(SqUtil.getInt(callOptionVol));
                        callOptionTick.setOpenIntrest(SqUtil.getInt(callOptionOpenIntrest));
                        callOptionTick.setSecurity(callOption);
                        callOptionTick.setSettlement(new BigDecimal(0.0));
                        ticks.add(callOptionTick);

                        StockOption putOption = new StockOptionImpl();
                        putOption.setIsin(putOptionIsin);
                        putOption.setMarket(SqMarketConverter.marketFromString(optionMarketId));
                        putOption.setCurrency(Currency.fromString(optionCurreny));
                        putOption.setStrike(RoundUtil.getBigDecimal(SqUtil.getDouble(optionStrike)));
                        putOption.setContractSize(SqUtil.getInt(contractSize));
                        putOption.setType(OptionType.PUT);
                        putOption.setExpiration(optionExpiration);
                        putOption.setUnderlaying(underlaying);
                        securities.add(putOption);

                        Tick putOptionTick = new TickImpl();
                        putOptionTick.setDateTime(new Date());
                        putOptionTick.setBid(new BigDecimal(0));
                        putOptionTick.setAsk(new BigDecimal(0));
                        putOptionTick.setLast(RoundUtil.getBigDecimal(SqUtil.getDouble(putOptionLast)));
                        putOptionTick.setLastDateTime(new Date());
                        putOptionTick.setVol(SqUtil.getInt(putOptionVol));
                        putOptionTick.setOpenIntrest(SqUtil.getInt(putOptionOpenIntrest));
                        putOptionTick.setSecurity(putOption);
                        putOptionTick.setSettlement(new BigDecimal(0.0));
                        ticks.add(putOptionTick);
                    }
                }
                getSecurityDao().create(securities);
                getTickDao().create(ticks);

                System.out.println(title);
            }
            System.out.println("done with " + market + " " + group);
        }
    }

    protected boolean handleVerifyVolatility(StockOption stockOption, TransactionType transactionType) throws HttpException, IOException, TransformerException, ParseException, ConvergenceException, FunctionEvaluationException {

        String isin = stockOption.getUnderlaying().getIsin();
        String expirationString = dateFormat.format(stockOption.getExpiration());
        double years = (stockOption.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / 31536000000.0;

        String url = optionUrl + "&underlying=" + isin + "&market=eu&group=id" + "&expiration=" + expirationString;

        GetMethod get = new GetMethod(url);

        HttpClient standardClient = HttpClientUtil.getStandardClient();

        Document document;
        try {
            int status = standardClient.executeMethod(get);

            document = TidyUtil.parse(get.getResponseBodyAsStream());

            XmlUtil.saveDocumentToFile(document, isin + "_" + expirationString + ".xml", "results/options/");

            if (status != HttpStatus.SC_OK) {
                throw new HttpException("invalid option request: underlying=" + isin + " expiration=" + expirationString);
            }

        } finally {
            get.releaseConnection();
        }

        //FileInputStream in = new FileInputStream("results/options/CH0008616382_1004.xml");
        //Document document = TidyUtil.parse(in);

        String underlayingSpotValue = SqUtil.getValue(document, "//table[tr/td/strong='Symbol']/tr/td/strong/a");
        double underlayingSpot = SqUtil.getDouble(underlayingSpotValue);

        NodeIterator iterator = XPathAPI.selectNodeIterator(document, "//table[tr/@align='CENTER']/tr[count(td)=12]");

        Node node;
        double lastVolatility = Double.MAX_VALUE;
        List<Double> strikes = new ArrayList<Double>();
        List<Double> volatilities = new ArrayList<Double>();
        List<Double> currentValues = new ArrayList<Double>();
        PolynomialFitter fitter = new PolynomialFitter(4, new LevenbergMarquardtOptimizer());

        while ((node = iterator.nextNode()) != null) {

            String strikeValue = SqUtil.getValue(node, "td/strong/a");
            String bidValue = SqUtil.getValue(node, "td[9]");
            String askValue = SqUtil.getValue(node, "td[10]");

            double strike = SqUtil.getDouble(strikeValue);
            double bid = SqUtil.getDouble(bidValue);
            double ask = SqUtil.getDouble(askValue);

            if (bid != 0 && ask != 0) {

                double currentValue = (bid + ask) / 2.0;

                double volatility = StockOptionUtil.getVolatility(underlayingSpot, strike, currentValue, years, stockOption.getType());

                if (volatility > lastVolatility) break;

                fitter.addObservedPoint(1, strike, volatility);

                strikes.add(strike);
                volatilities.add(volatility);
                currentValues.add(currentValue);

                lastVolatility = volatility;
            }
        }

        PolynomialFunction function = fitter.fit();
        SummaryStatistics stats = new SummaryStatistics();

        for (int i = 0; i < strikes.size(); i++) {

            double estimate = function.value(strikes.get(i));
            double difference = Math.abs(volatilities.get(i) - estimate);

            stats.addValue(difference);
        }

        double std = stats.getStandardDeviation();

        int i = strikes.indexOf(stockOption.getStrike().doubleValue());

        double currentValue = currentValues.get(i);
        double estimate = function.value(strikes.get(i));
        double volatility = volatilities.get(i);

        if (TransactionType.BUY.equals(transactionType) && volatility < (estimate - 3.0 * std)) {
            double fairValue = StockOptionUtil.getOptionPrice(stockOption, underlayingSpot, volatility);
            logger.warn("current price (" + currentValue + ") is to high compared to fair-value (" + fairValue + ") in regards to the volatility-curve");
            return false;
        } else if (TransactionType.SELL.equals(transactionType) && volatility > (estimate + 3.0 * std)) {
            double fairValue = StockOptionUtil.getOptionPrice(stockOption, underlayingSpot, volatility);
            logger.warn("current price (" + currentValue + ") is to low compared to fair-value (" + fairValue + ") in regards to the volatility-curve");
            return false;
        } else {
            return true;
        }
    }
}
