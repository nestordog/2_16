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
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;

import com.algoTrader.entity.Position;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.TransactionImpl;
import com.algoTrader.entity.security.Security;
import com.algoTrader.enumeration.ConnectionState;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.HttpClientUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.util.XmlUtil;

public class IBAccountServiceImpl extends IBAccountServiceBase implements DisposableBean {

    private static final long serialVersionUID = -9010045320078819079L;

    private static Logger logger = MyLogger.getLogger(IBAccountServiceImpl.class.getName());

    private @Value("${simulation}") boolean simulation;
    private @Value("#{'${marketChannel}' == 'IB'}") boolean ibEnabled;
    private @Value("${misc.portfolioDigits}") int portfolioDigits;
    private @Value("${ib.faEnabled}") boolean faEnabled;
    private @Value("${ib.accountServiceEnabled}") boolean accountServiceEnabled;

    private @Value("${ib.retrievalTimeout}") int retrievalTimeout;
    private @Value("${ib.faMasterAccount}") String faMasterAccount;
    private @Value("${ib.flexToken}") String flexToken;
    private @Value("${ib.flexQueryId}") String flexQueryId;
    private @Value("${ib.timeDifferenceHours}") int timeDifferenceHours;

    private IBClient client;
    private IBDefaultAdapter wrapper;

    private Lock lock = new ReentrantLock();
    private Condition condition = this.lock.newCondition();

    private Map<String, Map<String, String>> allAccountValues;
    private Set<String> accounts;
    private Map<String, Map<String, Double>> profiles;

    private static int clientId = 1;

    private static final String requestUrl = "https://www.interactivebrokers.com/Universal/servlet/FlexStatementService.SendRequest";
    private static final String statementUrl = "https://www.interactivebrokers.com/Universal/servlet/FlexStatementService.GetStatement";

    private static SimpleDateFormat fileFormat = new SimpleDateFormat("yyyyMMdd_kkmmss");
    private static SimpleDateFormat cashDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd, kk:mm:ss");
    private static SimpleDateFormat cashDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat tradeDateTimeFormat = new SimpleDateFormat("yyyyMMdd kkmmss");

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

                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document document = builder.parse(new InputSource(new StringReader(xml)));

                    if (faDataType == 1) {

                        // parse the document using XPath
                        NodeIterator iterator = XPathAPI.selectNodeIterator(document, "//Group[name='AllClients']/ListOfAccts/String");

                        // get accounts
                        Node node;
                        IBAccountServiceImpl.this.accounts = new HashSet<String>();
                        while ((node = iterator.nextNode()) != null) {
                            IBAccountServiceImpl.this.accounts.add(node.getFirstChild().getNodeValue());
                        }

                    } else if (faDataType == 2) {

                        // parse the document using XPath
                        NodeIterator profileIterator = XPathAPI.selectNodeIterator(document, "//AllocationProfile");

                        Node profileNode;
                        IBAccountServiceImpl.this.profiles = new HashMap<String, Map<String, Double>>();
                        while ((profileNode = profileIterator.nextNode()) != null) {
                            String name = XPathAPI.selectSingleNode(profileNode, "name/text()").getNodeValue();

                            // get allocations
                            NodeIterator allocationIterator = XPathAPI.selectNodeIterator(profileNode, "ListOfAllocations/Allocation");
                            Map<String, Double> allocations = new HashMap<String, Double>();
                            Node allocationNode;
                            while ((allocationNode = allocationIterator.nextNode()) != null) {
                                String account = XPathAPI.selectSingleNode(allocationNode, "acct/text()").getNodeValue();
                                String amount = XPathAPI.selectSingleNode(allocationNode, "amount/text()").getNodeValue();
                                allocations.put(account, Double.valueOf(amount));
                            }
                            IBAccountServiceImpl.this.profiles.put(name, allocations);
                        }
                    }

                    IBAccountServiceImpl.this.condition.signalAll();

                } catch (Exception e) {
                    IBAccountServiceImpl.logger.error("error parsing fa document", e);
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
        this.accounts = new HashSet<String>();
        this.profiles = new HashMap<String, Map<String, Double>>();

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

        if (this.accounts.size() == 0) {

            IBAccountServiceImpl.this.lock.lock();

            try {

                this.client.requestFA(1);

                while (this.accounts.size() == 0) {

                    if (!this.condition.await(this.retrievalTimeout, TimeUnit.SECONDS)) {
                        throw new IBAccountServiceException("could not get FA ");
                    }
                }
            } finally {
                IBAccountServiceImpl.this.lock.unlock();
            }
        }
        return this.accounts;
    }

    private Map<String, Double> getAllocations(String strategyName) throws Exception {

        if (this.profiles.size() == 0) {

            IBAccountServiceImpl.this.lock.lock();

            try {

                this.client.requestFA(2);

                while (this.profiles.size() == 0) {

                    if (!this.condition.await(this.retrievalTimeout, TimeUnit.SECONDS)) {
                        throw new IBAccountServiceException("could not get FA ");
                    }
                }
            } finally {
                IBAccountServiceImpl.this.lock.unlock();
            }
        }

        return this.profiles.get(strategyName.toUpperCase());
    }

    @Override
    protected long handleGetQuantityByMargin(String strategyName, double initialMarginPerContractInBase) throws Exception {

        if (this.faEnabled) {

            // if financial advisor is enabled, we have to get the number of Contracts per account
            // in order to avoid fractions
            long quantityByMargin = 0;
            StringBuffer buffer = new StringBuffer("quantityByMargin:");
            for (String account : getAccounts()) {
                double availableAmount = Double.parseDouble(retrieveAccountValue(account, "CHF", "AvailableFunds"));
                long quantity = (long) (availableAmount / initialMarginPerContractInBase);
                quantityByMargin += quantity;

                buffer.append(" " + account + "=" + quantity);
            }
            logger.debug(buffer.toString());

            return quantityByMargin;

        } else {

            Strategy strategy = getStrategyDao().findByName(strategyName);
            return (long) (strategy.getAvailableFundsDouble() / initialMarginPerContractInBase);
        }
    }

    @Override
    protected long handleGetQuantityByAllocation(String strategyName, long requestedQuantity) throws Exception {

        if (this.faEnabled) {

            // if financial advisor is enabled, we have to get the number of Contracts per account
            // in order to avoid fractions
            long quantityByAllocation = 0;
            StringBuffer buffer = new StringBuffer("quantityByAllocation:");
            for (Map.Entry<String, Double> entry : getAllocations(strategyName).entrySet()) {
                long quantity = (long) (entry.getValue() / 100.0 * requestedQuantity);
                quantityByAllocation += quantity;

                buffer.append(" " + entry.getKey() + "=" + quantity);
            }
            logger.debug(buffer.toString());

            return quantityByAllocation;

        } else {

            return requestedQuantity;
        }
    }

    @Override
    protected void handleReconcile() throws Exception {

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

        // do the actual reconciliation
        processCashTransactions(document);
        reconcilePositions(document);
        reconcileTrades(document);
    }

    @Override
    protected void handleProcessCashTransactions(Document document) throws Exception {

        NodeIterator iterator = XPathAPI.selectNodeIterator(document, "//CashTransaction");

        Node node;
        Strategy strategy = getStrategyDao().findByName(StrategyImpl.BASE);
        List<Transaction> transactions = new ArrayList<Transaction>();
        while ((node = iterator.nextNode()) != null) {

            String accountId = XPathAPI.selectSingleNode(node, "@accountId").getNodeValue();
            String desc = XPathAPI.selectSingleNode(node, "@description").getNodeValue();
            String dateTimeString = XPathAPI.selectSingleNode(node, "@dateTime").getNodeValue();
            String amountString = XPathAPI.selectSingleNode(node, "@amount").getNodeValue();
            String currencyString = XPathAPI.selectSingleNode(node, "@currency").getNodeValue();
            String typeString = XPathAPI.selectSingleNode(node, "@type").getNodeValue();

            Date dateTime = null;
            try {
                dateTime = cashDateTimeFormat.parse(dateTimeString);
            } catch (ParseException e) {
                dateTime = cashDateFormat.parse(dateTimeString);
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

            BigDecimal price = RoundUtil.getBigDecimal(Math.abs(amountDouble));

            if(getTransactionDao().findByDateTimePriceTypeAndDescription(dateTime, price, transactionType, description) != null) {

                // @formatter:off
                logger.warn("cash transaction already exists" +
                        " dateTime: " + cashDateTimeFormat.format(dateTime) +
                        " price: " + price +
                        " type: " + transactionType +
                        " description: " + description);
                // @formatter:on

            } else {

                Transaction transaction = new TransactionImpl();
                transaction.setDateTime(dateTime);
                transaction.setQuantity(1);
                transaction.setPrice(price);
                transaction.setCommission(new BigDecimal(0));
                transaction.setCurrency(currency);
                transaction.setType(transactionType);
                transaction.setDescription(description);
                transaction.setStrategy(strategy);

                transactions.add(transaction);
            }
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
                    " dateTime: " + cashDateTimeFormat.format(transaction.getDateTime()) +
                    " price: " + transaction.getPrice() +
                    " type: " + transaction.getType() +
                    " description: " + transaction.getDescription());
            // @formatter:on

            if (getRuleService().isInitialized(StrategyImpl.BASE)) {
                getRuleService().sendEvent(StrategyImpl.BASE, transaction);
            }
        }

        // rebalance portfolio if necessary
        if (transactions.size() > 0) {
            rebalancePortfolio();
        }
    }

    @Override
    protected void handleReconcilePositions(Document document) throws Exception {

        NodeIterator iterator = XPathAPI.selectNodeIterator(document, "//OpenPosition");

        Node node;
        while ((node = iterator.nextNode()) != null) {

            String extId = XPathAPI.selectSingleNode(node, "@conid").getNodeValue();

            Security security = getSecurityDao().findByExtId(extId);
            if (security == null) {

                logger.warn("security: " + extId + " does not exist");
            } else {

                long totalQuantity = 0;
                for (Position position : security.getPositions()) {
                    totalQuantity += position.getQuantity();
                }

                String quantityString = XPathAPI.selectSingleNode(node, "@position").getNodeValue();
                long quantity = Long.parseLong(quantityString);

                if (totalQuantity != quantity) {
                    logger.warn("position(s) on security: " + extId + " totalQuantity does not match db: " + totalQuantity + " broker: " + quantity);
                } else {
                    logger.info("position(s) on security: " + extId + " ok");
                }
            }
        }
    }

    @Override
    protected void handleReconcileTrades(Document document) throws Exception {

        NodeIterator iterator;
        if (this.faEnabled) {
            iterator = XPathAPI.selectNodeIterator(document, "//Trade[@accountId='" + this.faMasterAccount + "' and @transactionType='ExchTrade']");
        } else {
            iterator = XPathAPI.selectNodeIterator(document, "//Trade[@transactionType='ExchTrade']");
        }

        Node node;
        while ((node = iterator.nextNode()) != null) {

            String extId = XPathAPI.selectSingleNode(node, "@ibExecID").getNodeValue();

            Transaction transaction = getTransactionDao().findByExtId(extId);
            if (transaction == null) {

                logger.warn("transaction: " + extId + " does not exist");
            } else {

                String dateString = XPathAPI.selectSingleNode(node, "@tradeDate").getNodeValue();
                String timeString = XPathAPI.selectSingleNode(node, "@tradeTime").getNodeValue();
                String quantityString = XPathAPI.selectSingleNode(node, "@quantity").getNodeValue();
                String priceString = XPathAPI.selectSingleNode(node, "@tradePrice").getNodeValue();
                String commissionString = XPathAPI.selectSingleNode(node, "@ibCommission").getNodeValue();
                String currencyString = XPathAPI.selectSingleNode(node, "@currency").getNodeValue();
                String typeString = XPathAPI.selectSingleNode(node, "@buySell").getNodeValue();

                Date dateTime = DateUtils.addHours(tradeDateTimeFormat.parse(dateString + " " + timeString), this.timeDifferenceHours);
                long quantity = Long.parseLong(quantityString);
                double price = Double.parseDouble(priceString);
                double commissionDouble = Math.abs(Double.parseDouble(commissionString));
                Currency currency = Currency.fromString(currencyString);
                TransactionType type = TransactionType.valueOf(typeString);

                boolean success = true;
                if (!(new Date(transaction.getDateTime().getTime())).equals(dateTime)) {
                    logger.warn("transaction: " + extId + " dateTime does not match db: " + transaction.getDateTime() + " broker: " + dateTime);
                    success = false;
                }

                if (transaction.getQuantity() != quantity) {
                    logger.warn("transaction: " + extId + " quantity does not match db: " + transaction.getQuantity() + " broker: " + quantity);
                    success = false;
                }

                if (transaction.getPrice().doubleValue() != price) {
                    logger.warn("transaction: " + extId + " price does not match db: " + transaction.getPrice() + " broker: " + price);
                    success = false;
                }

                if (!transaction.getCurrency().equals(currency)) {
                    logger.warn("transaction: " + extId + " currency does not match db: " + transaction.getCurrency() + " broker: " + currency);
                    success = false;
                }

                if (!transaction.getType().equals(type)) {
                    logger.warn("transaction: " + extId + " type does not match db: " + transaction.getType() + " broker: " + type);
                    success = false;
                }

                BigDecimal commission = RoundUtil.getBigDecimal(Math.abs(commissionDouble), this.portfolioDigits);
                BigDecimal existingCommission = transaction.getCommission();

                if (!existingCommission.equals(commission)) {

                    // update the transaction
                    transaction.setCommission(commission);
                    getTransactionDao().update(transaction);

                    // process the difference in commission
                    double CommissionDiffDouble = commission.doubleValue() - existingCommission.doubleValue();
                    BigDecimal commissionDiff = RoundUtil.getBigDecimal(Math.abs(CommissionDiffDouble), this.portfolioDigits);

                    getCashBalanceService().processAmount(transaction.getStrategy(), transaction.getCurrency(), commissionDiff);

                    logger.info("transaction: " + extId + " adjusted commission from: " + existingCommission + " to: " + commission);
                    success = false;
                }

                if (success) {
                    logger.info("transaction: " + extId + " ok");
                }
            }
        }
    }

    @Override
    public void destroy() throws Exception {

        if (this.client != null) {
            this.client.disconnect();
        }
    }
}
