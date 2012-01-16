package com.algoTrader.service.ib;

import java.io.StringReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;

import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.TransactionImpl;
import com.algoTrader.enumeration.ConnectionState;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.service.sq.HttpClientUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.util.XmlUtil;

public class IBAccountServiceImpl extends IBAccountServiceBase implements DisposableBean {

    private static final long serialVersionUID = -9010045320078819079L;

    private static Logger logger = MyLogger.getLogger(IBAccountServiceImpl.class.getName());

    private @Value("${simulation}") boolean simulation;
    private @Value("#{'${marketChannel}' == 'IB'}") boolean ibEnabled;
    private @Value("${ib.faEnabled}") boolean faEnabled;
    private @Value("${ib.accountServiceEnabled}") boolean accountServiceEnabled;

    private @Value("${ib.retrievalTimeout}") int retrievalTimeout;
    private @Value("${ib.faMasterAccount}") String faMasterAccount;
    private @Value("${ib.flexToken}") String flexToken;
    private @Value("${ib.flexQueryId}") String flexQueryId;

    private IBClient client;
    private IBDefaultAdapter wrapper;

    private Lock lock = new ReentrantLock();
    private Condition condition = this.lock.newCondition();

    private Map<String, Map<String, String>> allAccountValues;
    private String fa;

    private static int clientId = 1;

    private static final String requestUrl = "https://www.interactivebrokers.com/Universal/servlet/FlexStatementService.SendRequest";
    private static final String statementUrl = "https://www.interactivebrokers.com/Universal/servlet/FlexStatementService.GetStatement";

    private static SimpleDateFormat fileFormat = new SimpleDateFormat("yyyyMMdd_kkmmss");
    private static SimpleDateFormat transactionDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd, kk:mm:ss");
    private static SimpleDateFormat transactionDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void handleInit() throws java.lang.Exception {

        if (!this.ibEnabled || this.simulation || !this.accountServiceEnabled) {
            return;
        }

        this.wrapper = new IBDefaultAdapter(clientId) {

            @Override
            public void updateAccountValue(String key, String value, String currency, String accountName) {

                IBAccountServiceImpl.this.lock.lock();

                try {

                    Map<String, String> values = IBAccountServiceImpl.this.allAccountValues.get(accountName);
                    values.put(key, value);

                    IBAccountServiceImpl.this.condition.signalAll();

                } finally {
                    IBAccountServiceImpl.this.lock.unlock();
                }
            }

            @Override
            public void receiveFA(int faDataType, String xml) {

                IBAccountServiceImpl.this.lock.lock();

                try {

                    IBAccountServiceImpl.this.fa = xml;

                    IBAccountServiceImpl.this.condition.signalAll();

                } finally {
                    IBAccountServiceImpl.this.lock.unlock();
                }
            }

            @Override
            public void connectionClosed() {

                super.connectionClosed();

                connect();
            }
        };

        this.client = new IBClient(clientId, this.wrapper);

        connect();
    }

    @Override
    protected void handleConnect() {

        if (!this.ibEnabled || this.simulation || !this.accountServiceEnabled) {
            return;
        }

        this.allAccountValues = new HashMap<String, Map<String, String>>();

        this.client.connect();
    }

    @Override
    protected ConnectionState handleGetConnectionState() {

        if (this.wrapper == null) {
            return ConnectionState.DISCONNECTED;
        } else {
            return this.wrapper.getState();
        }
    }

    private String retrieveAccountValue(String accountName, String currency, String key) throws InterruptedException {

        IBAccountServiceImpl.this.lock.lock();

        try {

            IBAccountServiceImpl.this.allAccountValues.put(accountName, new HashMap<String, String>());

            this.client.reqAccountUpdates(true, accountName);

            while (this.allAccountValues.get(accountName) == null || this.allAccountValues.get(accountName).get(key) == null) {

                if (!this.condition.await(this.retrievalTimeout, TimeUnit.SECONDS)) {
                    throw new IBAccountServiceException("could not get EquityWithLoanValue for account: " + accountName);
                }
            }
        } finally {
            IBAccountServiceImpl.this.lock.unlock();
        }
        return this.allAccountValues.get(accountName).get(key);
    }

    private Set<String> getAccounts() throws Exception {

        IBAccountServiceImpl.this.lock.lock();

        try {

            IBAccountServiceImpl.this.fa = null;

            this.client.requestFA(1);

            while (this.fa == null) {

                if (!this.condition.await(this.retrievalTimeout, TimeUnit.SECONDS)) {
                    throw new IBAccountServiceException("could not get FA ");
                }
            }
        } finally {
            IBAccountServiceImpl.this.lock.unlock();
        }

        // get the xml-document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(this.fa)));

        // parse the document using XPath
        NodeIterator iterator = XPathAPI.selectNodeIterator(document, "//Group[name='AllClients']/ListOfAccts/String");
        Node node;
        Set<String> accounts = new HashSet<String>();
        while ((node = iterator.nextNode()) != null) {
            accounts.add(node.getFirstChild().getNodeValue());
        }

        return accounts;
    }

    @Override
    protected long handleGetNumberOfContractsByMargin(String strategyName, double initialMarginPerContractInBase) throws Exception {

        if (this.faEnabled) {

            // if financial advisor is enabled, we have to get the number of Contracts per account
            // in order to avoid fractions
            long numberOfContractsByMargin = 0;
            for (String account : getAccounts()) {
                double availableAmount = Double.parseDouble(retrieveAccountValue(account, "CHF", "AvailableFunds"));
                long numberOfContracts = (long) (availableAmount / initialMarginPerContractInBase);
                numberOfContractsByMargin += numberOfContracts;

                logger.debug("assign " + numberOfContracts + " to account " + account);
            }
            return numberOfContractsByMargin;

        } else {

            Strategy strategy = getStrategyDao().findByName(strategyName);
            return (long) (strategy.getAvailableFundsDouble() / initialMarginPerContractInBase);
        }
    }

    @Override
    protected void handleProcessCashTransactions() throws Exception {

        if (!this.ibEnabled || this.simulation) {
            return;
        }

        if (("").equals(this.flexQueryId) || ("").equals(this.flexToken)) {
            throw new IBAccountServiceException("flexQueryId and flexToken have to be defined");
        }

        String url = requestUrl + "?t=" + this.flexToken + "&q=" + this.flexQueryId;

        // get the flex reference code
        GetMethod get = new GetMethod(url);
        HttpClient standardClient = HttpClientUtil.getStandardClient();

        Document document;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        try {
            int status = standardClient.executeMethod(get);

            if (status != HttpStatus.SC_OK) {
                throw new HttpException("invalid flex reference code request with url:" + url);
            }

            // get the xml-document
            document = builder.parse(new InputSource(get.getResponseBodyAsStream()));

            XmlUtil.saveDocumentToFile(document, fileFormat.format(new Date()) + "_flexReferenceCode.xml", "results/flex/");

        } finally {
            get.releaseConnection();
        }

        String code = XPathAPI.selectSingleNode(document, "//code/text()").getNodeValue();

        if (!NumberUtils.isDigits(code)) {
            throw new IBAccountServiceException(code);
        }

        // get the statement
        url = statementUrl + "?t=" + this.flexToken + "&q=" + code + "&v=2";

        get = new GetMethod(url);
        standardClient = HttpClientUtil.getStandardClient();

        try {
            int status = standardClient.executeMethod(get);

            if (status != HttpStatus.SC_OK) {
                throw new HttpException("invalid flex statement request with url:" + url);
            }

            // get the xml-document
            document = builder.parse(new InputSource(get.getResponseBodyAsStream()));

            XmlUtil.saveDocumentToFile(document, fileFormat.format(new Date()) + "_flexStatement.xml", "results/flex/");

        } finally {
            get.releaseConnection();
        }

        Node errorNode = XPathAPI.selectSingleNode(document, "/FlexStatementResponse/code/text()");
        if (errorNode != null) {
            throw new IBAccountServiceException(errorNode.getNodeValue());
        }

        NodeIterator iterator = XPathAPI.selectNodeIterator(document, "//CashTransaction");

        Node node;
        Strategy strategy = getStrategyDao().findByName(StrategyImpl.BASE);
        List<Transaction> transactions = new ArrayList<Transaction>();
        while ((node = iterator.nextNode()) != null) {

            String accountId = XPathAPI.selectSingleNode(node, "@accountId").getNodeValue();

            if (accountId.equals(this.faMasterAccount)) {
                continue;
            }

            String desc = XPathAPI.selectSingleNode(node, "@description").getNodeValue();
            String dateTimeString = XPathAPI.selectSingleNode(node, "@dateTime").getNodeValue();
            String amountString = XPathAPI.selectSingleNode(node, "@amount").getNodeValue();
            String currencyString = XPathAPI.selectSingleNode(node, "@currency").getNodeValue();
            String typeString = XPathAPI.selectSingleNode(node, "@type").getNodeValue();

            Date dateTime = null;
            try {
                dateTime = transactionDateTimeFormat.parse(dateTimeString);
            } catch (ParseException e) {
                dateTime = transactionDateFormat.parse(dateTimeString);
            }

            double amountDouble = Double.parseDouble(amountString);
            Currency currency = Currency.fromString(currencyString);
            String description = accountId + " " + desc;

            TransactionType transactionType;
            if (typeString.equals("Other Fees")) {
                if (amountDouble < 0) {
                    transactionType = TransactionType.FEES;
                } else {
                    transactionType = TransactionType.REFUND;
                }
            } else if (typeString.equals("Broker Interest Paid")) {
                transactionType = TransactionType.INTREST_PAID;
            } else if (typeString.equals("Broker Interest Received")) {
                transactionType = TransactionType.INTREST_RECEIVED;
            } else if (typeString.equals("Deposits & Withdrawals")) {
                if (amountDouble > 0) {
                    transactionType = TransactionType.CREDIT;
                } else {
                    transactionType = TransactionType.DEBIT;
                }
            } else {
                throw new IBAccountServiceException("unknown cast transaction type " + typeString);
            }

            BigDecimal amount = RoundUtil.getBigDecimal(Math.abs(amountDouble));

            Transaction transaction = new TransactionImpl();
            transaction.setDateTime(dateTime);
            transaction.setQuantity(1);
            transaction.setPrice(amount);
            transaction.setCommission(new BigDecimal(0));
            transaction.setCurrency(currency);
            transaction.setType(transactionType);
            transaction.setDescription(description);
            transaction.setStrategy(strategy);

            transactions.add(transaction);
        }

        // sort the transactions according to their dateTime
        Collections.sort(transactions, new Comparator<Transaction>() {
            @Override
            public int compare(Transaction t1, Transaction t2) {
                return t1.getDateTime().compareTo(t2.getDateTime());
            }
        });

        for (Transaction transaction : transactions) {

            getTransactionDao().create(transaction);

            // add the amount to the balance
            getCashBalanceService().processTransaction(transaction);

            // @formatter:off
            logger.info("executed cash transaction" +
                    " dateTime: " + transactionDateTimeFormat.format(transaction.getDateTime()) +
                    " price: " + transaction.getPrice() +
                    " type: " + transaction.getType() +
                    " description: " + transaction.getDescription());
            // @formatter:on

            getRuleService().sendEvent(StrategyImpl.BASE, transaction);
        }

        // rebalance portfolio if necessary
        if (transactions.size() > 0) {
            rebalancePortfolio();
        }
    }

    @Override
    public void destroy() throws Exception {

        if (this.client != null) {
            this.client.disconnect();
        }
    }
}
