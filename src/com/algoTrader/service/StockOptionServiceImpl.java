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
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.util.MathUtils;
import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.w3c.tidy.Tidy;

import com.algoTrader.criteria.StockOptionCriteria;
import com.algoTrader.entity.Account;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.StockOptionImpl;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.TransactionImpl;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.Market;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.HttpClientUtil;
import com.algoTrader.util.PropertiesUtil;
import com.algoTrader.util.StockOptionUtil;
import com.algoTrader.util.SwissquoteUtil;
import com.algoTrader.util.TidyUtil;
import com.algoTrader.util.XmlUtil;

public class StockOptionServiceImpl extends com.algoTrader.service.StockOptionServiceBase {

    private static final String optionUrl = "http://www.swissquote.ch/sq_mi/market/derivatives/optionfuture/OptionFuture.action?market=eu&type=option&sector=&group=id&type=option";

    private static Market market = Market.fromString(PropertiesUtil.getProperty("simulation.market"));
    private static Currency currency = Currency.fromString(PropertiesUtil.getProperty("simulation.currency"));
    private static OptionType optionType = OptionType.fromString(PropertiesUtil.getProperty("simulation.optionType"));
    private static int contractSize = Integer.parseInt(PropertiesUtil.getProperty("simulation.contractSize"));

    private static Logger logger = Logger.getLogger(StockOptionServiceImpl.class.getName());

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

        String content;
        try {
            HttpClient standardClient = HttpClientUtil.getStandardClient(true);
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
        XmlUtil.saveDocumentToFile(listDocument, underlaying.getIsin() + "_" + exp + "_" + strike.longValue() + ".xml",
                "results/options/", false);

        StockOption option = new StockOptionImpl();

        String optionUrl = XPathAPI.selectSingleNode(listDocument,
                "//td[contains(a/@class,'list')][" + (OptionType.CALL.equals(type) ? 1 : 2) + "]/a/@href")
                .getNodeValue();
        String param = optionUrl.split("=")[1];
        String isin = param.split("_")[0];
        String market = param.split("_")[1];
        String currency = param.split("_")[2];

        option.setIsin(isin);
        option.setMarket(Market.fromString(market));
        option.setCurrency(Currency.fromString(currency));

        option.setType(type);
        option.setStrike(strike);

        Document optionDocument = SwissquoteUtil.getSecurityDocument(option);

        String dateValue = SwissquoteUtil.getValue(optionDocument, "//table[tr/td='Datum']/tr[10]/td[4]/strong");
        Date expirationDate = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").parse(dateValue + " 13:00:00");

        String contractSizeValue = SwissquoteUtil
                .getValue(optionDocument, "//table[tr/td='Datum']/tr[10]/td[3]/strong");
        int contractSize = (int) Double.parseDouble(contractSizeValue);

        String symbolValue = XPathAPI.selectSingleNode(optionDocument, "//body/div[1]//h1/text()[2]").getNodeValue();
        String symbol = symbolValue.split("\\(")[0].trim().substring(1);

        option.setExpiration(expirationDate);
        option.setSymbol(symbol);
        option.setContractSize(contractSize);

        option.setUnderlaying(underlaying);

        return option;
    }

    protected void handleRetrieveAllStockOptions(Security underlaying) throws ParseException, TransformerException,
            IOException {

        String url = optionUrl + "&underlying=" + underlaying.getIsin() + "&expiration=&strike=";

        GetMethod get = new GetMethod(url);

        String content;
        try {
            HttpClient standardClient = HttpClientUtil.getStandardClient(true);
            int status = standardClient.executeMethod(get);

            if (status == HttpStatus.SC_NOT_FOUND) {
                logger.warn("invalid option request: underlying=" + underlaying.getIsin());
                return;
            } else if (status != HttpStatus.SC_OK) {
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

            Document optionDocument = SwissquoteUtil.getSecurityDocument(option);

            String typeValue = SwissquoteUtil.getValue(optionDocument, "//table[tr/td='Datum']/tr[10]/td[1]/strong");
            OptionType type = OptionType.fromString(typeValue.split("\\s")[0].toUpperCase());

            String strikeValue = SwissquoteUtil.getValue(optionDocument, "//table[tr/td='Datum']/tr[10]/td[2]/strong");
            BigDecimal strike = SwissquoteUtil.getBigDecimal(SwissquoteUtil.getAmount(strikeValue));

            String dateValue = SwissquoteUtil.getValue(optionDocument, "//table[tr/td='Datum']/tr[10]/td[4]/strong");
            Date expirationDate = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").parse(dateValue + " 13:00:00");

            String symbolValue = XPathAPI.selectSingleNode(optionDocument, "//body/div[1]//h1/text()[2]")
                    .getNodeValue();
            String symbol = symbolValue.split("\\(")[0].trim().substring(1);

            String contractSizeValue = SwissquoteUtil.getValue(optionDocument,
                    "//table[tr/td='Datum']/tr[10]/td[3]/strong");
            int contractSize = (int) Double.parseDouble(contractSizeValue);

            option.setType(type);
            option.setStrike(strike);
            option.setExpiration(expirationDate);
            option.setSymbol(symbol);
            option.setContractSize(contractSize);

            option.setUnderlaying(underlaying);

            getSecurityDao().create(option);
        }
    }

    protected void handleExpireStockOptions() throws Exception {

        List list = getPositionDao().findExpiredPositions();

        for (Iterator it = list.iterator(); it.hasNext();) {

            Position position = (Position) it.next();

            // StockOption
            StockOption option = (StockOptionImpl) position.getSecurity();

            // Account
            Account account = getAccountDao().load(position.getAccount().getId());

            // Transaction
            Transaction transaction = new TransactionImpl();

            transaction.setNumber(0); // we dont habe a number
            transaction.setDateTime(option.getExpiration());
            transaction.setQuantity(-position.getQuantity());
            transaction.setPrice(new BigDecimal(0));
            transaction.setCommission(new BigDecimal(0));
            transaction.setType(TransactionType.EXPIRATION);
            transaction.setSecurity(option);

            transaction.setAccount(account);
            account.getTransactions().add(transaction);

            // attach the object
            position.setQuantity(0);
            position.setExitValue(new BigDecimal(0));
            position.setMargin(new BigDecimal(0));

            position.getTransactions().add(transaction);
            transaction.setPosition(position);

            getPositionDao().update(position);
            getTransactionDao().create(transaction);
            getAccountDao().update(account);
        }
    }

    protected void handleSetMargin(Position position, BigDecimal settlement, BigDecimal underlaying)
            throws ConvergenceException, FunctionEvaluationException {

        StockOption option = (StockOption) position.getSecurity();

        BigDecimal margin = StockOptionUtil.getMargin(option, settlement, underlaying);

        int quantity = Math.abs(position.getQuantity());
        position.setMargin(margin.multiply(new BigDecimal(quantity)));

        getPositionDao().update(position);
    }

    protected StockOption handleCreateDummyStockOption(Security underlaying, Date expiration, BigDecimal strike, OptionType type) throws Exception {

        // set third Friday of the month
        Calendar cal = new GregorianCalendar();
        cal.setTime(expiration);
        cal.set(Calendar.WEEK_OF_MONTH, 3);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        expiration = cal.getTime();

        // round to 50.-
        double rounded = MathUtils.round(strike.doubleValue()/ 50.0, 0, BigDecimal.ROUND_FLOOR) * 50.0;
        strike = new BigDecimal(rounded).setScale(2, BigDecimal.ROUND_HALF_UP);

        // symbol
        String symbol = "O" +
        underlaying.getSymbol() + " " +
        new SimpleDateFormat("MMM").format(cal.getTime()).toUpperCase() + "/" +
        (cal.get(Calendar.YEAR) + "-").substring(2) +
        type.toString().substring(0, 1) + " " +
        strike.intValue() + " " +
        contractSize;

        StockOption stockOption = new StockOptionImpl();
        stockOption.setIsin(null); // dummys don't have a isin
        stockOption.setSymbol(symbol);
        stockOption.setMarket(market);
        stockOption.setCurrency(currency);
        stockOption.setOnWatchlist(false);
        stockOption.setDummy(true);
        stockOption.setStrike(strike);
        stockOption.setExpiration(expiration);
        stockOption.setType(type);
        stockOption.setContractSize(contractSize);
        stockOption.setUnderlaying(underlaying);

        getStockOptionDao().create(stockOption);

        return stockOption;
    }

    protected StockOption handleFindNearestStockOption(Security underlaying, Date expiration, BigDecimal strike,
            OptionType type) throws Exception {

           StockOptionCriteria criteria = new StockOptionCriteria(underlaying, expiration, strike, type);
           criteria.setMaximumResultSize(new Integer(1));

           return (StockOption)getStockOptionDao().findByCriteria(criteria).get(0);
    }

}
