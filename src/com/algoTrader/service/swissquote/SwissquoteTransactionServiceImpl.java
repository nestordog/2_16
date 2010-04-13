package com.algoTrader.service.swissquote;

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

import com.algoTrader.entity.Order;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.Tick;
import com.algoTrader.entity.TickImpl;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.TransactionImpl;
import com.algoTrader.enumeration.OrderStatus;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.service.TransactionServiceException;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.PropertiesUtil;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.util.TidyUtil;
import com.algoTrader.util.XmlUtil;

public class SwissquoteTransactionServiceImpl extends SwissquoteTransactionServiceBase {

    private static Logger logger = MyLogger.getLogger(SwissquoteTransactionServiceImpl.class.getName());

    private static int confirmationTimeout = PropertiesUtil.getIntProperty("swissquote.confirmationTimeout");
    private static int confirmationRetries = PropertiesUtil.getIntProperty("swissquote.confirmationRetries");

    private static String dispatchUrl = "https://trade.swissquote.ch/sqb_core/DispatchCtrl";
    private static String tradeUrl = "https://trade.swissquote.ch/sqb_core/TradeCtrl";
    private static String ordersUrl = "https://trade.swissquote.ch/sqb_core/AccountCtrl?commandName=myOrders&client=" + PropertiesUtil.getProperty("swissquote.trade.clientNumber");
    private static String transactionsUrl = "https://trade.swissquote.ch/sqb_core/TransactionsCtrl?commandName=viewTransactions";

    private static String dailyOrdersMatch = "//table[@class='trading']/tbody/tr[1]/td[count(//table[@class='trading']/thead/tr/td[.='%1$s']/preceding-sibling::td)+1]";
    private static String executedTransactionsMatch = "td[count(//table[@class='trading']/thead/tr/td[.='%1$s']/preceding-sibling::td)+1]";
    private static String tickMatch = "//tr[td/font/strong='%1$s']/following-sibling::tr[1]/td[count(//tr/td[font/strong='%1$s']/preceding-sibling::td)+1]/font";

    private static SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_kkmmss");

    @SuppressWarnings("unchecked")
    protected void handleExecuteExternalTransaction(Order order) throws Exception {

        Security security = order.getSecurity();
        TransactionType transactionType = order.getTransactionType();
        long requestedQuantity = order.getRequestedQuantity();

        HttpClient client = HttpClientUtil.getSwissquoteTradeClient();


        // 1. get the order screen
        NameValuePair[] params = new NameValuePair[] {
                new NameValuePair("commandName", "trade"),
                new NameValuePair("isin", security.getIsin()),
                new NameValuePair("currency", security.getCurrency().getValue()),
                new NameValuePair("stockExchange", security.getMarket().getValue())
            };

        GetMethod get = new GetMethod(dispatchUrl);
        get.setQueryString(params);
        int status = client.executeMethod(get);

        if (status != HttpStatus.SC_OK) {
            throw new TransactionServiceException("could not get order screen: " + security.getIsin() + ", status: " + get.getStatusLine());
        }

        Document document = TidyUtil.parseAndFilter(get.getResponseBodyAsStream());
        get.releaseConnection();
        XmlUtil.saveDocumentToFile(document, format.format(new Date()) + "_" + security.getIsin() + "_order.xml", "results/trade/");

        // create a temporary tick
        String volBidValue = SwissquoteUtil.getValue(document, String.format(tickMatch, "Geldkurs-Volumen"));
        int volBid = SwissquoteUtil.getInt(volBidValue);

        String volAskValue = SwissquoteUtil.getValue(document, String.format(tickMatch, "Briefkurs-Volumen"));
        int volAsk = SwissquoteUtil.getInt(volAskValue);

        String bidValue = SwissquoteUtil.getValue(document, String.format(tickMatch, "Geldkurs"));
        BigDecimal bid = RoundUtil.getBigDecimal(SwissquoteUtil.getDouble(bidValue));

        String askValue = SwissquoteUtil.getValue(document, String.format(tickMatch, "Briefkurs"));
        BigDecimal ask = RoundUtil.getBigDecimal(SwissquoteUtil.getDouble(askValue));

        Tick tick = new TickImpl();
        tick.setVolBid(volBid);
        tick.setVolAsk(volAsk);
        tick.setBid(bid);
        tick.setAsk(ask);

        // validity check (volume and bid/ask spread)
        if (!tick.isValid()) {
            throw new TransactionServiceException("tickdata is not valid for order on " + security.getIsin() + ", tick: " + tick);
        }

        // validity check (available volume)
        if (TransactionType.BUY.equals(transactionType) && volAsk < requestedQuantity) {
            logger.warn("available volume (" + volAsk + ") is smaler than requested quantity (" + requestedQuantity + ") for a order on " + security.getIsin());
        } else if (TransactionType.SELL.equals(transactionType) && volBid < requestedQuantity) {
            logger.warn("available volume (" + volBid + ") is smaler than requested quantity (" + requestedQuantity + ") for a order on " + security.getIsin());
        }

        // process the hidden fields
        NodeIterator nodeIterator = XPathAPI.selectNodeIterator(document, "//input[@type='hidden']");
        Node node;
        Set<NameValuePair> paramSet = new HashSet<NameValuePair>();
        while ((node = nodeIterator.nextNode()) != null) {
            String name = SwissquoteUtil.getValue(node, "@name");
            String value = SwissquoteUtil.getValue(node, "@value");

            if (name.equals("phase")) value = "confirm";

            paramSet.add(new NameValuePair(name, value));
        }

        // transactionType
        Position position = security.getPosition();
        String openClose = (position != null && position.getQuantity() != 0) ? "CLOSE" : "OPEN";
        String transactionTypeString = TransactionType.SELL.equals(transactionType) ? "SELL to " + openClose : "BUY to " + openClose;
        String orderTransactionValue = SwissquoteUtil.getValue(document, "//tr[td/font/strong='" + transactionTypeString + "']/td/input/@value");
        paramSet.add(new NameValuePair("order.transaction", orderTransactionValue));

        // quantity
        paramSet.add(new NameValuePair("order.quantity", String.valueOf(requestedQuantity)));

        // price
        if (TransactionType.BUY.equals(transactionType)) {
            paramSet.add(new NameValuePair("order.price", ask.toString()));
        } else if (TransactionType.SELL.equals(transactionType)) {
            paramSet.add(new NameValuePair("order.price", bid.toString()));
        }

        // stockExchange
        String stockExchangeValue = SwissquoteUtil.getValue(document, "//select[@name='stockExchange']/option[@selected='selected']/@value");
        paramSet.add(new NameValuePair("stockExchange", stockExchangeValue));

        // orderType
        String orderTypeValue = SwissquoteUtil.getValue(document, "//select[@name='order.orderType']/option[.='Limit']/@value");
        paramSet.add(new NameValuePair("order.orderType", orderTypeValue));

        // expiration
        String expirationValue  = SwissquoteUtil.getValue(document, "//select[@name='order.str_expiration']/option[1]/@value");
        paramSet.add(new NameValuePair("order.str_expiration", expirationValue));

        params = (NameValuePair[])paramSet.toArray(new NameValuePair[0]);

        // 2. get the confirmation screen
        get = new GetMethod(tradeUrl);
        get.setQueryString(params);
        status = client.executeMethod(get);

        if (status != HttpStatus.SC_OK) {
            throw new TransactionServiceException("could not get confirmation screen: " + security.getIsin() + ", status: " + get.getStatusLine());
        }

        document = TidyUtil.parseAndFilter(get.getResponseBodyAsStream());
        get.releaseConnection();
        XmlUtil.saveDocumentToFile(document, format.format(new Date()) + "_" + security.getIsin() + "_confirmation.xml", "results/trade/");

        // process the hidden fields
        nodeIterator = XPathAPI.selectNodeIterator(document, "//input[@type='hidden']");
        paramSet = new HashSet<NameValuePair>();
        while ((node = nodeIterator.nextNode()) != null) {
            String name = SwissquoteUtil.getValue(node, "@name");
            String value = SwissquoteUtil.getValue(node, "@value");

            if (name.equals("phase")) value = "ack";

            paramSet.add(new NameValuePair(name, value));
        }

        // 3. get the ack screen
        params = (NameValuePair[])paramSet.toArray(new NameValuePair[0]);

        get = new GetMethod(tradeUrl);
        get.setQueryString(params);
        status = client.executeMethod(get);

        if (status != HttpStatus.SC_OK) {
            throw new TransactionServiceException("could not get ack screen: " + security.getIsin() + ", status: " + get.getStatusLine());
        }

        document = TidyUtil.parseAndFilter(get.getResponseBodyAsStream());
        get.releaseConnection();
        XmlUtil.saveDocumentToFile(document, format.format(new Date()) + "_" + security.getIsin() + "_ack.xml", "results/trade/");


        // 4. get the open/daily orders screen
        get = new GetMethod(ordersUrl);

        for (int i = 0; i < confirmationRetries ; i++) {

            status = client.executeMethod(get);

            if (status != HttpStatus.SC_OK) {
                throw new TransactionServiceException("could not get open / daily orders screen, status: " + get.getStatusLine());
            }

            document = TidyUtil.parseAndFilter(get.getResponseBodyAsStream());
            get.releaseConnection();
            XmlUtil.saveDocumentToFile(document, format.format(new Date()) + "_open_daily_orders.xml", "results/trade/");

            Node openNode = XPathAPI.selectSingleNode(document, "//table[@class='trading maskMe']/tbody/tr/td[.='Offen']|//table[@class='trading maskMe']/tbody/tr/td[.='Unreleased']");
            Node partiallyExecutedNode = XPathAPI.selectSingleNode(document, "//table[@class='trading maskMe']/tbody/tr/td[.='Teilweise Ausgeführt']");
            Node executedNode = XPathAPI.selectSingleNode(document, "//table[@class='trading maskMe']/tbody/tr/td[.='Sie haben keine Aufträge']");

            if (openNode != null) {
                order.setStatus(OrderStatus.OPEN);
            } else if (partiallyExecutedNode != null) {
                order.setStatus(OrderStatus.PARTIALLY_EXECUTED);
            } else if (executedNode != null) {
                order.setStatus(OrderStatus.EXECUTED);
                break;
            } else {
                throw new TransactionServiceException("unknown order status for order on: " + security.getSymbol());
            }

            Thread.sleep(confirmationTimeout);
        }

        // if the order did not execute fully, delete it
        if (!OrderStatus.EXECUTED.equals(order.getStatus())) {

            String orderNumber = SwissquoteUtil.getValue(document, "//table[@class='trading maskMe']/tbody/tr/td[count(//table[@class='trading maskMe']/thead/tr/td[.='Auftrag']/preceding-sibling::td)+1]");
            order.setNumber(orderNumber);

            // 4B. get the delete screen
            params = new NameValuePair[] {
                    new NameValuePair("commandName", "delete"),
                    new NameValuePair("tradeId", orderNumber)
                };

            get = new GetMethod(dispatchUrl);
            get.setQueryString(params);
            status = client.executeMethod(get);

            document = TidyUtil.parseAndFilter(get.getResponseBodyAsStream());
            get.releaseConnection();
            XmlUtil.saveDocumentToFile(document, format.format(new Date()) + "_delete_order_" + orderNumber + ".xml", "results/trade/");

            node = XPathAPI.selectSingleNode(document, "//strong[.='Löschauftrag']");

            if (status != HttpStatus.SC_INTERNAL_SERVER_ERROR || node == null) {
                throw new TransactionServiceException("could not delete order after reaching timelimit: " + security.getSymbol() + ", status: " + get.getStatusLine());
            } else if (OrderStatus.OPEN.equals(order.getStatus())) {
                throw new TransactionServiceException("orrer did not execute at all within timelimit: " + security.getSymbol());
            }
        }

        // parse the rest of the open/daily order screen
        String dateValue = SwissquoteUtil.getValue(document, String.format(dailyOrdersMatch, "Datum"));
        String timeValue = SwissquoteUtil.getValue(document, String.format(dailyOrdersMatch, "Zeit"));
        Date dateTime = SwissquoteUtil.getDate(dateValue + " " + timeValue);

        String orderNumber = SwissquoteUtil.getValue(document, String.format(dailyOrdersMatch, "Auftrag"));
        order.setNumber(orderNumber);

        // 5. get the executed transactions screen
        get = new GetMethod(transactionsUrl);
        status = client.executeMethod(get);

        if (status != HttpStatus.SC_OK) {
            throw new TransactionServiceException("could not get executed transaction screen, status: " + get.getStatusLine());
        }

        document = TidyUtil.parseAndFilter(get.getResponseBodyAsStream());
        get.releaseConnection();
        XmlUtil.saveDocumentToFile(document, format.format(new Date()) + "_executed_transactions.xml", "results/trade/");

        nodeIterator = XPathAPI.selectNodeIterator(document, "//tr[td/a='" + orderNumber + "']");
        while ((node = nodeIterator.nextNode()) != null) {

            String transactionNumber = SwissquoteUtil.getValue(node, String.format(executedTransactionsMatch + "/a/@href", "Auftrag #"));
            transactionNumber = transactionNumber.split("contractNumber=")[1];

            String executedQuantityValue = SwissquoteUtil.getValue(node, String.format(executedTransactionsMatch, "Anzahl"));
            int executedQuantity = Math.abs(SwissquoteUtil.getInt(executedQuantityValue));
            executedQuantity = TransactionType.SELL.equals(transactionType) ? -executedQuantity : executedQuantity;

            String pricePerItemValue = SwissquoteUtil.getValue(node, String.format(executedTransactionsMatch, "Stückpreis"));
            double pricePerItem = SwissquoteUtil.getDouble(pricePerItemValue);
            BigDecimal price = RoundUtil.getBigDecimal(pricePerItem * ((StockOption)security).getContractSize());

            String commissionValue = SwissquoteUtil.getValue(node, String.format(executedTransactionsMatch + "/a", "Kommission"));
            BigDecimal commission = RoundUtil.getBigDecimal(SwissquoteUtil.getDouble(commissionValue));

            Transaction transaction = new TransactionImpl();
            transaction.setDateTime(dateTime);
            transaction.setNumber(transactionNumber);
            transaction.setQuantity(executedQuantity);
            transaction.setPrice(price);
            transaction.setCommission(commission);

            order.getTransactions().add(transaction);
        }
    }
}
