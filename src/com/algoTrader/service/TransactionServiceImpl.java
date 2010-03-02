package com.algoTrader.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

import com.algoTrader.entity.Account;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.PositionImpl;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.TransactionImpl;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.stockOption.StockOptionUtil;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.EsperService;
import com.algoTrader.util.HttpClientUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.PropertiesUtil;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.util.SwissquoteUtil;
import com.algoTrader.util.TidyUtil;
import com.algoTrader.util.XmlUtil;

public class TransactionServiceImpl extends com.algoTrader.service.TransactionServiceBase {

    private static Logger logger = MyLogger.getLogger(TransactionServiceImpl.class.getName());

    private static boolean swissquoteTransactions = new Boolean(PropertiesUtil.getProperty("swissquoteTransactions")).booleanValue();
    private static boolean simulation = new Boolean(PropertiesUtil.getProperty("simulation")).booleanValue();
    private static int confirmationTimeout = Integer.parseInt(PropertiesUtil.getProperty("confirmationTimeout"));
    private static int confirmationRetries = Integer.parseInt(PropertiesUtil.getProperty("confirmationRetries"));

    private static String dispatchUrl = "https://trade.swissquote.ch/sqb_core/DispatchCtrl";
    private static String tradeUrl = "https://trade.swissquote.ch/sqb_core/TradeCtrl";
    private static String accountUrl = "https://trade.swissquote.ch/sqb_core/AccountCtrl?commandName=myOrders&client=" + PropertiesUtil.getProperty("swissquote.trade.clientNumber");
    private static String transactionsUrl = "https://trade.swissquote.ch/sqb_core/TransactionsCtrl?commandName=viewTransactions";

    private static String[] regex = new String[] {"<script(.*?)</script>", "<noscript(.*?)</noscript>", "<style(.*?)</style>", "<!--(.*?)-->", "<!(.*?)>", "<\\?(.*?)\\?>"};
    private static String dailyTransactionsMatch = "//table[@class='trading']/tbody/tr[1]/td[count(//table[@class='trading']/thead/tr/td[.='%1$s']/preceding-sibling::td)+1]";
    private static String executedTransactionsMatch = "//tr[td/a='%1$s']/td[count(//table[@class='trading']/thead/tr/td[.='%2$s']/preceding-sibling::td)+1]";

    private static SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_kkmmss");

    protected Transaction handleExecuteTransaction(int quantity, Security security, BigDecimal current, TransactionType transactionType)
            throws Exception {

        if (quantity == 0) throw new TransactionServiceException("quantity = 0 not allowed");
        if (quantity < 0) throw new TransactionServiceException("negative quantity not allowed");

        // execute the transaction at swissquote
        Transaction transaction = executeSwissquoteTransaction(quantity, security, current, transactionType);

        // TODO handle Expiration

        // if swissquoteTransactions are disabled create a fake transaction
        if (transaction == null) {
            transaction = new TransactionImpl();
            transaction.setDateTime(DateUtil.getCurrentEPTime());
            transaction.setPrice(current.abs());
            transaction.setCommission(getCommission(security, quantity, transactionType));
            transaction.setNumber(0);
        }

        int signedQuantity = TransactionType.SELL.equals(transactionType) ? -Math.abs(quantity) : Math.abs(quantity);

        transaction.setQuantity(signedQuantity);
        transaction.setType(transactionType);
        transaction.setSecurity(security);

        // Account
        Account account = getAccountDao().findByCurrency(security.getCurrency());
        transaction.setAccount(account);
        account.getTransactions().add(transaction);

        // Position
        Position position = security.getPosition();
        if (position == null) {

            position = new PositionImpl();
            position.setQuantity(signedQuantity);

            position.setExitValue(new BigDecimal(0));
            position.setMargin(new BigDecimal(0));

            position.setSecurity(security);
            security.setPosition(position);

            position.getTransactions().add(transaction);
            transaction.setPosition(position);

            position.setAccount(account);
            account.getPositions().add(position);

            getPositionDao().create(position);

        } else {

            // attach the object
            position.setQuantity(position.getQuantity() + signedQuantity);

            position.setExitValue(new BigDecimal(0));
            position.setMargin(new BigDecimal(0));

            position.getTransactions().add(transaction);
            transaction.setPosition(position);

            getPositionDao().update(position);
        }

        getTransactionDao().create(transaction);
        getAccountDao().update(account);
        getSecurityDao().update(security);

        logger.info("executed transaction type: " + transactionType + " quantity: " + transaction.getQuantity() + " of " + security.getSymbol() + " price: " + transaction.getPrice() + " commission: " + transaction.getCommission() + " balance: " + account.getBalance());

        EsperService.getEPServiceInstance().getEPRuntime().sendEvent(transaction);

        return getSwissquoteTransactionConfirmation(security);
    }

    private static Transaction executeSwissquoteTransaction(int quantity, Security security, BigDecimal current, TransactionType transactionType) throws Exception {

        if (simulation || !swissquoteTransactions) return null;

        HttpClient client = HttpClientUtil.getSwissquoteTradeClient();

        // get the transaction screen
        NameValuePair[] params = {
                new NameValuePair("commandName", "trade"),
                new NameValuePair("isin", security.getIsin()),
                new NameValuePair("currency", security.getCurrency().getValue()),
                new NameValuePair("stockExchange", security.getMarket().getValue())
            };

        GetMethod get = new GetMethod(dispatchUrl);
        get.setQueryString(params);
        int status = client.executeMethod(get);

        if (status != HttpStatus.SC_OK) {
            throw new TransactionServiceException("could not get transaction screen: " + security.getIsin() + ", status: " + get.getStatusLine());
        }

        Document document = TidyUtil.parseWithRegex(get.getResponseBodyAsStream(), regex);
        get.releaseConnection();
        XmlUtil.saveDocumentToFile(document, format.format(new Date()) + "_" + security.getIsin() + "_transaction.xml", "results/trade/");

        // TODO checks (volume, Bid/Ask spread, difference to current)

        // process the hidden fields
        NodeIterator nodeIterator = XPathAPI.selectNodeIterator(document, "//input[@type='hidden']");
        Node node;
        Set paramSet = new HashSet();
        while ((node = nodeIterator.nextNode()) != null) {
            String name = XPathAPI.selectSingleNode(node, "@name").getNodeValue();
            String value = XPathAPI.selectSingleNode(node, "@value").getNodeValue();

            if (name.equals("phase")) value = "confirm";

            paramSet.add(new NameValuePair(name, value));
        }

        // transactionType
        String transactionTypeString = getTransactionTypeString(security, transactionType);
        String orderTransactionValue = XPathAPI.selectSingleNode(document, "//tr[td/font/strong='" + transactionTypeString + "']/td/input/@value").getNodeValue();
        paramSet.add(new NameValuePair("order.transaction", orderTransactionValue));

         // quantity
        paramSet.add(new NameValuePair("order.quantity", String.valueOf(quantity)));

        // price
        // TODO select the limit
        paramSet.add(new NameValuePair("order.price", current.toString()));

        // stockExchange
        String stockExchangeValue = XPathAPI.selectSingleNode(document, "//select[@name='stockExchange']/option[@selected='selected']/@value").getNodeValue();
        paramSet.add(new NameValuePair("stockExchange", stockExchangeValue));

        // orderType
        String orderTypeValue = XPathAPI.selectSingleNode(document, "//select[@name='order.orderType']/option[.='Limit']/@value").getNodeValue();
        paramSet.add(new NameValuePair("order.orderType", orderTypeValue));

        // expiration
        String expirationValue  = XPathAPI.selectSingleNode(document, "//select[@name='order.str_expiration']/option[1]/@value").getNodeValue();
        paramSet.add(new NameValuePair("order.str_expiration", expirationValue));

        // get the transaction screen
        params = (NameValuePair[])paramSet.toArray(new NameValuePair[0]);

        get = new GetMethod(tradeUrl);
        get.setQueryString(params);
        status = client.executeMethod(get);

        if (status != HttpStatus.SC_OK) {
            throw new TransactionServiceException("could not get confirmation screen: " + security.getIsin() + ", status: " + get.getStatusLine());
        }

        document = TidyUtil.parseWithRegex(get.getResponseBodyAsStream(), regex);
        get.releaseConnection();
        XmlUtil.saveDocumentToFile(document, format.format(new Date()) + "_" + security.getIsin() + "_confirmation.xml", "results/trade/");

        // process the hidden fields
        nodeIterator = XPathAPI.selectNodeIterator(document, "//input[@type='hidden']");
        paramSet = new HashSet();
        while ((node = nodeIterator.nextNode()) != null) {
            String name = XPathAPI.selectSingleNode(node, "@name").getNodeValue();
            String value = XPathAPI.selectSingleNode(node, "@value").getNodeValue();

            if (name.equals("phase")) value = "ack";

            paramSet.add(new NameValuePair(name, value));
        }

        // get the ack screen
        params = (NameValuePair[])paramSet.toArray(new NameValuePair[0]);

        get = new GetMethod(tradeUrl);
        get.setQueryString(params);
        status = client.executeMethod(get);

        if (status != HttpStatus.SC_OK) {
            throw new TransactionServiceException("could not get ack screen: " + security.getIsin() + ", status: " + get.getStatusLine());
        }

        document = TidyUtil.parseWithRegex(get.getResponseBodyAsStream(), regex);
        get.releaseConnection();
        XmlUtil.saveDocumentToFile(document, format.format(new Date()) + "_" + security.getIsin() + "_ack.xml", "results/trade/");

        return getSwissquoteTransactionConfirmation(security);
    }

    private static Transaction getSwissquoteTransactionConfirmation(Security security) throws Exception {

        HttpClient client = HttpClientUtil.getSwissquoteTradeClient();

        // get the open/daily transactions screen
        GetMethod get = new GetMethod(accountUrl);

        Document document = null;
        int status = 0;
        boolean executed = false;
        for (int i = 0; i < confirmationRetries ; i++) {

            status = client.executeMethod(get);

            if (status != HttpStatus.SC_OK) {
                throw new TransactionServiceException("could not get open / daily transaction screen, status: " + get.getStatusLine());
            }

            document = TidyUtil.parseWithRegex(get.getResponseBodyAsStream(), regex);
            get.releaseConnection();
            XmlUtil.saveDocumentToFile(document, format.format(new Date()) + "_open_daily_transactions.xml", "results/trade/");

            Node node = XPathAPI.selectSingleNode(document, "//table[@class='trading maskMe']/tbody/tr[td='Offen']");
            executed = (node == null) ? true : false;
            if (executed) break;

            Thread.sleep(confirmationTimeout);
        }

        if (!executed) {

            String tradeId = SwissquoteUtil.getValue(document, "//table[@class='trading maskMe']/tbody/tr/td[count(//table[@class='trading maskMe']/thead/tr/td[.='Auftrag']/preceding-sibling::td)+1]");

            // get the delete screen
            NameValuePair[] params = {
                    new NameValuePair("commandName", "delete"),
                    new NameValuePair("tradeId", tradeId)
                };

            get = new GetMethod(dispatchUrl);
            get.setQueryString(params);
            status = client.executeMethod(get);

            document = TidyUtil.parseWithRegex(get.getResponseBodyAsStream(), regex);
            get.releaseConnection();
            XmlUtil.saveDocumentToFile(document, format.format(new Date()) + "_delete_transaction_" + tradeId + ".xml", "results/trade/");

            Node node = XPathAPI.selectSingleNode(document, "//strong[.='Löschauftrag']");

            if (status != HttpStatus.SC_INTERNAL_SERVER_ERROR || node == null) {
                throw new TransactionServiceException("could not delete transaction after reaching timelimit: " + security.getIsin() + ", status: " + get.getStatusLine());
            } else {
                throw new TransactionServiceException("transaction did not execute within timelimit: " + security.getSymbol());
            }
        }

        // parse the daily transaction
        String dateValue = SwissquoteUtil.getValue(document, String.format(dailyTransactionsMatch, "Datum"));
        String timeValue = SwissquoteUtil.getValue(document, String.format(dailyTransactionsMatch, "Zeit"));
        Date dateTime = SwissquoteUtil.getDate(dateValue + " " + timeValue);

        String numberValue = SwissquoteUtil.getValue(document, String.format(dailyTransactionsMatch, "Auftrag"));
        int number = Integer.parseInt(numberValue);

        // get the executed transactions screen
        get = new GetMethod(transactionsUrl);
        status = client.executeMethod(get);

        if (status != HttpStatus.SC_OK) {
            throw new TransactionServiceException("could not get executed transaction screen, status: " + get.getStatusLine());
        }

        document = TidyUtil.parseWithRegex(get.getResponseBodyAsStream(), regex);
        get.releaseConnection();
        XmlUtil.saveDocumentToFile(document, format.format(new Date()) + "_executed_transactions.xml", "results/trade/");

        String pricePerItemValue = SwissquoteUtil.getValue(document, String.format(executedTransactionsMatch, numberValue, "Stückpreis"));
        double pricePerItem = Double.parseDouble(pricePerItemValue);
        int contractSize = (security instanceof StockOption) ? ((StockOption)security).getContractSize() : 1;
        BigDecimal price = RoundUtil.getBigDecimal(pricePerItem * contractSize);

        String commissionValue = SwissquoteUtil.getValue(document, String.format(executedTransactionsMatch + "/a", numberValue, "Kommission"));
        BigDecimal commission = RoundUtil.getBigDecimal(Double.parseDouble(commissionValue));

        Transaction transaction = new TransactionImpl();
        transaction.setDateTime(dateTime);
        transaction.setPrice(price);
        transaction.setCommission(commission);
        transaction.setNumber(number);

        return transaction;
    }

    private static String getTransactionTypeString(Security security, TransactionType transactionType) {

        Position position = security.getPosition();
        String openClose = (position != null && position.getQuantity() != 0) ? "CLOSE" : "OPEN";

        if (transactionType.equals(TransactionType.SELL)) {
            return "SELL to " + openClose;
        } else if (transactionType.equals(TransactionType.BUY)) {
            return "BUY to " + openClose;
        } else if (transactionType.equals(TransactionType.EXPIRATION)) {
            if (position == null || position.getQuantity() == 0) {
                throw new TransactionServiceException("expiration not allowed. as there is no open position");
            } else if (position.getQuantity() > 0) {
                return "SELL to CLOSE";
            } else {
                return "BUY to CLOSE";
            }
        } else {
            throw new TransactionServiceException("unsupported transactionType");
        }
    }

    private static BigDecimal getCommission(Security security, int quantity, TransactionType transactionType) {

        if (security instanceof StockOption &&
                (TransactionType.SELL.equals(transactionType) || TransactionType.BUY.equals(transactionType))) {
            return StockOptionUtil.getCommission(quantity);
        } else {
            return new BigDecimal(0);
        }
    }
}
