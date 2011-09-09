package com.algoTrader.service.sq;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

import com.algoTrader.entity.Order;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.TransactionImpl;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.marketData.TickImpl;
import com.algoTrader.entity.security.Security;
import com.algoTrader.enumeration.Status;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.service.SyncOrderServiceException;
import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.util.TidyUtil;
import com.algoTrader.util.XmlUtil;

public class SQSyncOrderServiceImpl extends SQSyncOrderServiceBase {

    private static Logger logger = MyLogger.getLogger(SQSyncOrderServiceImpl.class.getName());

    private static int confirmationTimeout = ConfigurationUtil.getBaseConfig().getInt("swissquote.confirmationTimeout");
    private static int confirmationRetries = ConfigurationUtil.getBaseConfig().getInt("swissquote.confirmationRetries");
    private static int maxTransactionAge = ConfigurationUtil.getBaseConfig().getInt("swissquote.maxTransactionAge");
    private static String[] spreadPositions = ConfigurationUtil.getBaseConfig().getStringArray("spreadPositions");

    private static String dispatchUrl = "https://trade.swissquote.ch/sqb_core/DispatchCtrl";
    private static String tradeUrl = "https://trade.swissquote.ch/sqb_core/TradeCtrl";
    private static String ordersUrl = "https://trade.swissquote.ch/sqb_core/AccountCtrl?commandName=myOrders&client="
            + ConfigurationUtil.getBaseConfig().getString("swissquote.trade.clientNumber");
    private static String transactionsUrl = "https://trade.swissquote.ch/sqb_core/TransactionsCtrl?commandName=viewTransactions";

    private static String columnMatch = "td[//table[@class='trading']/thead/tr/td[.='%1$s']][count(//table[@class='trading']/thead/tr/td[.='%1$s']/preceding-sibling::td)+1]";
    private static String tickMatch = "//tr[td/font/strong='%1$s']/following-sibling::tr[1]/td[count(//tr/td[font/strong='%1$s']/preceding-sibling::td)+1]/font";

    private static SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_kkmmss");

    @Override
    protected void handleSendExternalOrder(Order order) throws Exception {

        HttpClient client = HttpClientUtil.getSwissquoteTradeClient();

        double bid = 0.0;
        double ask = Double.POSITIVE_INFINITY;
        for (String spreadPosition : spreadPositions) {

            Document orderScreen = getOrderScreen(order, client);

            if (spreadPosition.equals(spreadPositions[0])) {

                // only validate price and volum the first time, because our orders will show up in the orderbook as well
                validateTick(order, orderScreen);

                // also only get the bid and ask the first time
                bid = SQUtil.getDouble(SQUtil.getValue(orderScreen, String.format(tickMatch, "Geldkurs")));
                ask = SQUtil.getDouble(SQUtil.getValue(orderScreen, String.format(tickMatch, "Briefkurs")));
            }

            Document confirmationScreen = getConfirmationScreen(order, client, orderScreen, Double.parseDouble(spreadPosition), bid, ask);

            getAckScreen(order, client, confirmationScreen);

            Document openAndDailyOrdersScreen = getOpenAndDailyOrdersScreen(order, client);

            // if the order did not execute fully, delete it
            if (!Status.EXECUTED.equals(order.getStatus())) {

                getDeleteScreen(order, client, openAndDailyOrdersScreen);

                if (Status.OPEN.equals(order.getStatus())) {

                    // nothing went through, so continue with the next higher
                    // spreadPosition
                    continue;
                }
            }

            getExecutedTransactionsScreen(order, client, openAndDailyOrdersScreen, spreadPosition);

            if (Status.EXECUTED.equals(order.getStatus())) {

                // we are done!
                break;

            } else if (Status.PARTIALLY_EXECUTED.equals(order.getStatus())) {

                // only part of the order has gone through, so reduce the
                // requested
                // numberOfContracts by this number and keep going
                order.setRequestedQuantity(-Math.abs(order.getExecutedQuantity()));
                continue;
            }
        }
    }

    private Document getOrderScreen(Order order, HttpClient client) throws IOException, HttpException, UnsupportedEncodingException, TransformerException,
            ParseException {

        Security security = order.getSecurity();

        // get the order screen
        NameValuePair[] params = new NameValuePair[] { new NameValuePair("commandName", "trade"), new NameValuePair("isin", security.getIsin()),
                new NameValuePair("currency", security.getSecurityFamily().getCurrency().toString()),
                new NameValuePair("stockExchange", SQMarketConverter.marketToString(security.getSecurityFamily().getMarket())) };

        GetMethod get = new GetMethod(dispatchUrl);
        get.setQueryString(params);

        Document orderScreen;
        try {
            int status = client.executeMethod(get);

            orderScreen = TidyUtil.parseAndFilter(get.getResponseBodyAsStream());

            XmlUtil.saveDocumentToFile(orderScreen, format.format(new Date()) + "_" + security.getIsin() + "_order.xml", "results/trade/");

            if (status != HttpStatus.SC_OK) {
                throw new SyncOrderServiceException("could not get order screen: " + security.getIsin() + ", status: " + get.getStatusLine());
            }

        } finally {
            get.releaseConnection();
        }

        return orderScreen;
    }

    private Document getConfirmationScreen(Order order, HttpClient client, Document orderScreen, double spreadPosition, double bid, double ask)
            throws TransformerException, ParseException, IOException, HttpException, UnsupportedEncodingException {

        Security security = order.getSecurity();
        TransactionType transactionType = order.getTransactionType();
        long requestedQuantity = order.getRequestedQuantity();

        // process the hidden fields
        NodeIterator nodeIterator = XPathAPI.selectNodeIterator(orderScreen, "//input[@type='hidden']");
        Node node;
        Set<NameValuePair> paramSet = new HashSet<NameValuePair>();
        while ((node = nodeIterator.nextNode()) != null) {
            String name = SQUtil.getValue(node, "@name");
            String value = SQUtil.getValue(node, "@value");

            if (name.equals("phase")) {
                value = "confirm";
            }

            paramSet.add(new NameValuePair(name, value));
        }

        // transactionType
        Position position = getPositionDao().findBySecurityAndStrategy(security.getId(), order.getStrategy().getName());
        String openClose = (position != null && position.isOpen()) ? "CLOSE" : "OPEN";
        String transactionTypeString = TransactionType.SELL.equals(transactionType) ? "SELL to " + openClose : "BUY to " + openClose;
        String orderTransactionValue = SQUtil.getValue(orderScreen, "//tr[td/font/strong='" + transactionTypeString + "']/td/input/@value");
        paramSet.add(new NameValuePair("order.transaction", orderTransactionValue));

        // quantity
        paramSet.add(new NameValuePair("order.quantity", String.valueOf(requestedQuantity)));

        // price
        BigDecimal price = RoundUtil.getBigDecimal(getPrice(order, spreadPosition, bid, ask));
        paramSet.add(new NameValuePair("order.price", price.toString()));

        // stockExchange
        String stockExchangeValue = SQUtil.getValue(orderScreen, "//select[@name='stockExchange']/option[@selected='selected']/@value");
        paramSet.add(new NameValuePair("stockExchange", stockExchangeValue));

        // orderType
        String orderTypeValue = SQUtil.getValue(orderScreen, "//select[@name='order.orderType']/option[.='Limit']/@value");
        paramSet.add(new NameValuePair("order.orderType", orderTypeValue));

        // expiration
        String expirationValue = SQUtil.getValue(orderScreen, "//select[@name='order.str_expiration']/option[1]/@value");
        paramSet.add(new NameValuePair("order.str_expiration", expirationValue));

        NameValuePair[] params = paramSet.toArray(new NameValuePair[0]);

        // get the confirmation screen
        GetMethod get = new GetMethod(tradeUrl);
        get.setQueryString(params);

        Document confirmationScreen;
        try {
            int status = client.executeMethod(get);

            confirmationScreen = TidyUtil.parseAndFilter(get.getResponseBodyAsStream());

            XmlUtil.saveDocumentToFile(confirmationScreen, format.format(new Date()) + "_" + security.getIsin() + "_confirmation.xml", "results/trade/");

            if (status != HttpStatus.SC_OK) {
                throw new SyncOrderServiceException("could not get confirmation screen: " + security.getIsin() + ", status: " + get.getStatusLine());
            }

        } finally {
            get.releaseConnection();
        }

        logger.debug("place order at spreadPosition: " + spreadPosition + ", bid: " + bid + ", ask: " + ask + ", price: " + price);

        return confirmationScreen;
    }

    private void getAckScreen(Order order, HttpClient client, Document confirmationScreen) throws TransformerException, IOException, HttpException,
            UnsupportedEncodingException, InterruptedException {

        Security security = order.getSecurity();

        // process the hidden fields
        NodeIterator nodeIterator = XPathAPI.selectNodeIterator(confirmationScreen, "//input[@type='hidden']");
        Set<NameValuePair> paramSet = new HashSet<NameValuePair>();
        Node node;
        while ((node = nodeIterator.nextNode()) != null) {
            String name = SQUtil.getValue(node, "@name");
            String value = SQUtil.getValue(node, "@value");

            if (name.equals("phase")) {
                value = "ack";
            }

            paramSet.add(new NameValuePair(name, value));
        }

        NameValuePair[] params = paramSet.toArray(new NameValuePair[0]);

        // get the ack screen
        GetMethod get = new GetMethod(tradeUrl);
        get.setQueryString(params);

        try {
            int status = client.executeMethod(get);

            Document ackScreen = TidyUtil.parseAndFilter(get.getResponseBodyAsStream());

            XmlUtil.saveDocumentToFile(ackScreen, format.format(new Date()) + "_" + security.getIsin() + "_ack.xml", "results/trade/");

            if (status != HttpStatus.SC_OK) {
                throw new SyncOrderServiceException("could not get ack screen: " + security.getIsin() + ", status: " + get.getStatusLine());
            }

        } finally {
            get.releaseConnection();
        }

        Thread.sleep(confirmationTimeout);
    }

    private Document getOpenAndDailyOrdersScreen(Order order, HttpClient client) throws IOException, HttpException, UnsupportedEncodingException,
            TransformerException, InterruptedException {

        Security security = order.getSecurity();

        Document openAndDailyOrdersScreen = null;
        for (int i = 0; i < confirmationRetries; i++) {

            GetMethod get = new GetMethod(ordersUrl);

            try {
                int status = client.executeMethod(get);

                openAndDailyOrdersScreen = TidyUtil.parseAndFilter(get.getResponseBodyAsStream());

                XmlUtil.saveDocumentToFile(openAndDailyOrdersScreen, format.format(new Date()) + "_" + security.getIsin() + "_open_daily_orders.xml",
                        "results/trade/");

                if (status != HttpStatus.SC_OK) {
                    throw new SyncOrderServiceException("could not get open / daily orders screen, status: " + get.getStatusLine());
                }

            } finally {
                get.releaseConnection();
            }

            Node openNode = XPathAPI.selectSingleNode(openAndDailyOrdersScreen,
                    "//table[@class='trading maskMe']/tbody/tr[td='Offen' and contains(td/a/@href, '" + security.getIsin() + "')]");
            Node unreleasedNode = XPathAPI.selectSingleNode(openAndDailyOrdersScreen,
                    "//table[@class='trading maskMe']/tbody/tr[td='Unreleased' and contains(td/a/@href, '" + security.getIsin() + "')]");
            Node partiallyExecutedNode = XPathAPI.selectSingleNode(openAndDailyOrdersScreen,
                    "//table[@class='trading maskMe']/tbody/tr[td='Teilweise Ausgeführt' and contains(td/a/@href, '" + security.getIsin() + "')]");
            Node executedNode = XPathAPI.selectSingleNode(openAndDailyOrdersScreen,
                    "//table[@class='trading maskMe']/tbody/tr[td='Ausgeführt' and contains(td/a/@href, '" + security.getIsin() + "')]");
            Node unknownStateNode = XPathAPI.selectSingleNode(openAndDailyOrdersScreen, "//table[@class='trading maskMe']/tbody/tr[contains(td/a/@href, '"
                    + security.getIsin() + "')]/td[10]");

            if (openNode != null || unreleasedNode != null) {
                order.setStatus(Status.OPEN);
            } else if (partiallyExecutedNode != null) {
                order.setStatus(Status.PARTIALLY_EXECUTED);
            } else if (executedNode != null) {
                order.setStatus(Status.EXECUTED);
                // keep going, the transaction ist executed but has not showed
                // up under executed transactions yet
            } else if (unknownStateNode != null) {
                throw new SyncOrderServiceException("unknown order status " + unknownStateNode.getFirstChild().getNodeValue() + " for order on: "
                        + security.getSymbol());
            } else {
                // the transaction has executed
                order.setStatus(Status.EXECUTED);
                break;
            }

            Thread.sleep(confirmationTimeout);
        }
        return openAndDailyOrdersScreen;
    }

    private void getDeleteScreen(Order order, HttpClient client, Document openAndDailyOrdersScreen) throws TransformerException, IOException, HttpException,
            UnsupportedEncodingException {

        Security security = order.getSecurity();

        String orderNumber = SQUtil.getValue(openAndDailyOrdersScreen, "//table[@class='trading maskMe']/tbody/tr[contains(td/a/@href, '" + security.getIsin()
                + "')]/td[11]");

        if (orderNumber == null) {
            throw new SyncOrderServiceException("could not retrieve orderNumber to delete order: " + security.getSymbol());
        }

        order.setNumber(Integer.parseInt(orderNumber));

        // get the delete screen
        NameValuePair[] params = new NameValuePair[] { new NameValuePair("commandName", "delete"), new NameValuePair("tradeId", orderNumber) };

        GetMethod get = new GetMethod(dispatchUrl);
        get.setQueryString(params);

        int status;
        Document deleteScreen;
        try {
            status = client.executeMethod(get);

            deleteScreen = TidyUtil.parseAndFilter(get.getResponseBodyAsStream());

            XmlUtil.saveDocumentToFile(deleteScreen, format.format(new Date()) + "_" + security.getIsin() + "_delete_order.xml", "results/trade/");

            Node node = XPathAPI.selectSingleNode(deleteScreen, "//strong[.='Löschauftrag']");

            if (status != HttpStatus.SC_INTERNAL_SERVER_ERROR || node == null) {
                throw new SyncOrderServiceException("could not delete order after reaching timelimit: " + security.getSymbol() + ", status: "
                        + get.getStatusLine());
            }

        } finally {
            get.releaseConnection();
        }
    }

    private void getExecutedTransactionsScreen(Order order, HttpClient client, Document openAndDailyOrdersScreen, String spreadPosition)
            throws TransformerException, ParseException, IOException, HttpException, UnsupportedEncodingException {

        Security security = order.getSecurity();
        TransactionType transactionType = order.getTransactionType();

        // check if transaction shows up under daily-orders
        Node dailyOrderNode = XPathAPI.selectSingleNode(openAndDailyOrdersScreen,
                "//table[@class='trading']/tbody/tr[contains(td/a/@href, '" + security.getIsin() + "') and td='Ausgeführt'][1]");
        if (dailyOrderNode == null) {
            throw new SyncOrderServiceException("transaction on " + security.getSymbol()
                    + " did execute but did not show up under daily-orders within timelimit");
        }

        // parse the rest of the open/daily order screen
        String dateValue = SQUtil.getValue(dailyOrderNode, String.format(columnMatch, "Datum"));
        String timeValue = SQUtil.getValue(dailyOrderNode, String.format(columnMatch, "Zeit"));
        Date dateTime = SQUtil.getDate(dateValue + " " + timeValue);

        if (DateUtil.getCurrentEPTime().getTime() - dateTime.getTime() > maxTransactionAge) {
            throw new SyncOrderServiceException("transaction on " + security.getSymbol()
                    + " did execute, but the selected transaction under daily orders is too old");
        }

        String orderNumber = SQUtil.getValue(dailyOrderNode, String.format(columnMatch, "Auftrag"));
        order.setNumber(Integer.parseInt(orderNumber));

        // get the executed transactions screen
        GetMethod get = new GetMethod(transactionsUrl);

        Document executedTransactionsScreen = null;
        try {
            int status = client.executeMethod(get);

            executedTransactionsScreen = TidyUtil.parseAndFilter(get.getResponseBodyAsStream());

            XmlUtil.saveDocumentToFile(executedTransactionsScreen, format.format(new Date()) + "_" + security.getIsin() + "_executed_transactions.xml",
                    "results/trade/");

            if (status != HttpStatus.SC_OK) {
                throw new SyncOrderServiceException("could not get executed transaction screen, status: " + get.getStatusLine());
            }

        } catch (Exception e) {
            get.releaseConnection();
        }

        NodeIterator nodeIterator = XPathAPI.selectNodeIterator(executedTransactionsScreen, "//tr[td/a='" + orderNumber + "']");
        Node node;
        int totalExecutedQuantity = 0;
        while ((node = nodeIterator.nextNode()) != null) {

            String transactionNumber = SQUtil.getValue(node, String.format(columnMatch + "/a/@href", "Auftrag #"));
            transactionNumber = transactionNumber.split("contractNumber=")[1];

            String executedQuantityValue = SQUtil.getValue(node, String.format(columnMatch, "Anzahl"));
            int executedQuantity = Math.abs(SQUtil.getInt(executedQuantityValue));
            executedQuantity = TransactionType.SELL.equals(transactionType) ? -executedQuantity : executedQuantity;
            totalExecutedQuantity += executedQuantity;

            String pricePerItemValue = SQUtil.getValue(node, String.format(columnMatch, "Stückpreis"));
            double pricePerItem = SQUtil.getDouble(pricePerItemValue);
            BigDecimal price = RoundUtil.getBigDecimal(pricePerItem);

            String commissionValue = SQUtil.getValue(node, String.format(columnMatch + "/a", "Kommission"));
            BigDecimal commission = RoundUtil.getBigDecimal(SQUtil.getDouble(commissionValue));

            Transaction transaction = new TransactionImpl();
            transaction.setDateTime(dateTime);
            transaction.setNumber(transactionNumber);
            transaction.setQuantity(executedQuantity);
            transaction.setPrice(price);
            transaction.setCommission(commission);

            order.getTransactions().add(transaction);
        }
        order.setExecutedQuantity(order.getExecutedQuantity() + totalExecutedQuantity);

        logger.info("executed " + order.getExecutedQuantity() + " transactions at spreadPosition: " + spreadPosition);
    }

    private void validateTick(Order order, Document document) throws TransformerException, ParseException {

        Security security = order.getSecurity();
        TransactionType transactionType = order.getTransactionType();
        long requestedQuantity = order.getRequestedQuantity();

        // create a temporary tick
        String volBidValue = SQUtil.getValue(document, String.format(tickMatch, "Geldkurs-Volumen"));
        int volBid = SQUtil.getInt(volBidValue);

        String volAskValue = SQUtil.getValue(document, String.format(tickMatch, "Briefkurs-Volumen"));
        int volAsk = SQUtil.getInt(volAskValue);

        String bidValue = SQUtil.getValue(document, String.format(tickMatch, "Geldkurs"));
        BigDecimal bid = RoundUtil.getBigDecimal(SQUtil.getDouble(bidValue));

        String askValue = SQUtil.getValue(document, String.format(tickMatch, "Briefkurs"));
        BigDecimal ask = RoundUtil.getBigDecimal(SQUtil.getDouble(askValue));

        Tick tick = new TickImpl();
        tick.setSecurity(security);
        tick.setVolBid(volBid);
        tick.setVolAsk(volAsk);
        tick.setBid(bid);
        tick.setAsk(ask);

        // validity check (volume and bid/ask spread)
        tick.validate();

        // validity check (available volume)
        if (TransactionType.BUY.equals(transactionType) && volAsk < requestedQuantity) {
            logger.warn("available volume (" + volAsk + ") is smaler than requested quantity (" + requestedQuantity + ") for a order on " + security.getIsin());
        } else if (TransactionType.SELL.equals(transactionType) && volBid < requestedQuantity) {
            logger.warn("available volume (" + volBid + ") is smaler than requested quantity (" + requestedQuantity + ") for a order on " + security.getIsin());
        }
    }
}
