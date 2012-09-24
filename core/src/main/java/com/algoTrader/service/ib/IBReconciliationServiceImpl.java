package com.algoTrader.service.ib;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

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
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.MarketChannel;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.HttpClientUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.util.XmlUtil;

public class IBReconciliationServiceImpl extends IBReconciliationServiceBase {

    private static Logger logger = MyLogger.getLogger(IBReconciliationServiceImpl.class.getName());

    private @Value("${simulation}") boolean simulation;
    private @Value("${misc.portfolioDigits}") int portfolioDigits;
    private @Value("${ib.faEnabled}") boolean faEnabled;

    private @Value("${ib.masterAccount}") String masterAccount;
    private @Value("${ib.flexToken}") String flexToken;
    private @Value("${ib.flexQueryId}") String flexQueryId;
    private @Value("${ib.timeDifferenceHours}") int timeDifferenceHours;
    private @Value("${ib.recreateTransactions}") boolean recreateTransactions;

    private static final String requestUrl = "https://www.interactivebrokers.com/Universal/servlet/FlexStatementService.SendRequest";
    private static final String statementUrl = "https://www.interactivebrokers.com/Universal/servlet/FlexStatementService.GetStatement";

    private static SimpleDateFormat fileFormat = new SimpleDateFormat("yyyyMMdd_kkmmss");
    private static SimpleDateFormat cashDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd, kk:mm:ss");
    private static SimpleDateFormat cashDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat tradeDateTimeFormat = new SimpleDateFormat("yyyyMMdd kkmmss");

    @Override
    protected void handleReconcile() throws Exception {

        if (this.simulation) {
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

            XmlUtil.saveDocumentToFile(document, fileFormat.format(new Date()) + "_flexReferenceCode.xml", "flex");

        } finally {
            get.releaseConnection();
        }

        String code = XPathAPI.selectSingleNode(document, "//code/text()").getNodeValue();

        if (!NumberUtils.isDigits(code)) {
            throw new IBAccountServiceException(code);
        }

        // get the statement
        url = statementUrl + "?t=" + this.flexToken + "&q=" + code + "&v=2";

        // repeat until the statement is generated
        while (true) {

            get = new GetMethod(url);

            try {
                int status = standardClient.executeMethod(get);

                if (status != HttpStatus.SC_OK) {
                    throw new HttpException("invalid flex statement request with url:" + url);
                }

                // get the xml-document
                document = builder.parse(new InputSource(get.getResponseBodyAsStream()));

                XmlUtil.saveDocumentToFile(document, fileFormat.format(new Date()) + "_flexStatement.xml", "flex");

            } finally {
                get.releaseConnection();
            }

            Node errorNode = XPathAPI.selectSingleNode(document, "/FlexStatementResponse/code/text()");
            if (errorNode == null) {
                break; // statement was generated sucessfully
            } else {
                if ("Statement generation in progress. Please try again shortly.".equals(errorNode.getNodeValue())) {
                    Thread.sleep(10000); // wait 10 sec
                } else {
                    throw new IBAccountServiceException(errorNode.getNodeValue());
                }
            }
        }

        // do the actual reconciliation
        processCashTransactions(document);
        reconcilePositions(document);
        reconcileTrades(document);
    }

    @Override
    protected void handleReconcile(List<String> fileNames) throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        for (String fileName : fileNames) {

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(fileName);

            // do the actual reconciliation
            reconcileUnbookedTrades(document);
        }
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
            long quantity;
            if (typeString.equals("Other Fees")) {
                if (amountDouble < 0) {
                    transactionType = TransactionType.FEES;
                    quantity = -1;
                } else {
                    transactionType = TransactionType.REFUND;
                    quantity = 1;
                }
            } else if (typeString.equals("Broker Interest Paid")) {
                transactionType = TransactionType.INTREST_PAID;
                quantity = -1;
            } else if (typeString.equals("Broker Interest Received")) {
                transactionType = TransactionType.INTREST_RECEIVED;
                quantity = 1;
            } else if (typeString.equals("Deposits & Withdrawals")) {
                if (amountDouble > 0) {
                    transactionType = TransactionType.CREDIT;
                    quantity = 1;
                } else {
                    transactionType = TransactionType.DEBIT;
                    quantity = -1;
                }
            } else {
                throw new IBAccountServiceException("unknown cast transaction type " + typeString);
            }

            BigDecimal price = RoundUtil.getBigDecimal(Math.abs(amountDouble));

            if (getTransactionDao().findByDateTimePriceTypeAndDescription(dateTime, price, transactionType, description) != null) {

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
                transaction.setQuantity(quantity);
                transaction.setPrice(price);
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

            // persist the transaction
            getTransactionService().persistTransaction(transaction);
        }

        // rebalance portfolio if necessary
        if (transactions.size() > 0) {
            getAccountService().rebalancePortfolio();
        }
    }

    @Override
    protected void handleReconcilePositions(Document document) throws Exception {

        NodeIterator iterator = XPathAPI.selectNodeIterator(document, "//OpenPosition");

        Node node;
        while ((node = iterator.nextNode()) != null) {

            String conid = XPathAPI.selectSingleNode(node, "@conid").getNodeValue();

            Security security = getSecurityDao().findByConid(conid);
            if (security == null) {

                logger.error("security for conid: " + conid + " does not exist");
            } else {

                long totalQuantity = 0;
                for (Position position : security.getPositions()) {
                    totalQuantity += position.getQuantity();
                }

                String quantityString = XPathAPI.selectSingleNode(node, "@position").getNodeValue();
                long quantity = Long.parseLong(quantityString);

                if (totalQuantity != quantity) {
                    logger.error("position(s) on security: " + conid + " totalQuantity does not match db: " + totalQuantity + " broker: " + quantity);
                } else {
                    logger.info("position(s) on security: " + conid + " ok");
                }
            }
        }
    }

    @Override
    protected void handleReconcileTrades(Document document) throws Exception {

        NodeIterator iterator;
        if (this.faEnabled) {
            iterator = XPathAPI.selectNodeIterator(document, "//Trade[@accountId='" + this.masterAccount + "' and @transactionType='ExchTrade']");
        } else {
            iterator = XPathAPI.selectNodeIterator(document, "//Trade[@transactionType='ExchTrade']");
        }

        Node node;
        while ((node = iterator.nextNode()) != null) {

            String extId = XPathAPI.selectSingleNode(node, "@ibExecID").getNodeValue();
            String conid = XPathAPI.selectSingleNode(node, "@conid").getNodeValue();
            String dateString = XPathAPI.selectSingleNode(node, "@tradeDate").getNodeValue();
            String timeString = XPathAPI.selectSingleNode(node, "@tradeTime").getNodeValue();
            String quantityString = XPathAPI.selectSingleNode(node, "@quantity").getNodeValue();
            String priceString = XPathAPI.selectSingleNode(node, "@tradePrice").getNodeValue();
            String commissionString = XPathAPI.selectSingleNode(node, "@ibCommission").getNodeValue();
            String currencyString = XPathAPI.selectSingleNode(node, "@currency").getNodeValue();
            String typeString = XPathAPI.selectSingleNode(node, "@buySell").getNodeValue();

            Date dateTime = DateUtils.addHours(tradeDateTimeFormat.parse(dateString + " " + timeString), this.timeDifferenceHours);
            long quantity = Long.parseLong(quantityString);
            double priceDouble = Double.parseDouble(priceString);
            double commissionDouble = Math.abs(Double.parseDouble(commissionString));
            Currency currency = Currency.fromString(currencyString);
            TransactionType transactionType = TransactionType.valueOf(typeString);

            internalReconcileTrade(extId, conid, dateTime, quantity, priceDouble, commissionDouble, currency, transactionType);
        }
    }

    @Override
    protected void handleReconcileUnbookedTrades(Document document) throws Exception {

        NodeIterator iterator = XPathAPI.selectNodeIterator(document, "//UnbookedTrade");

        Node node;
        while ((node = iterator.nextNode()) != null) {

            String extId = XPathAPI.selectSingleNode(node, "@ibExecID").getNodeValue();
            String conid = XPathAPI.selectSingleNode(node, "@conid").getNodeValue();
            String dateTimeString = XPathAPI.selectSingleNode(node, "@dateTime").getNodeValue();
            String quantityString = XPathAPI.selectSingleNode(node, "@quantity").getNodeValue();
            String priceString = XPathAPI.selectSingleNode(node, "@tradePrice").getNodeValue();
            String commissionString = XPathAPI.selectSingleNode(node, "@commission").getNodeValue();
            String currencyString = XPathAPI.selectSingleNode(node, "@currency").getNodeValue();

            Date dateTime = DateUtils.addHours(cashDateTimeFormat.parse(dateTimeString), this.timeDifferenceHours);
            long quantity = Long.parseLong(quantityString);
            double priceDouble = Double.parseDouble(priceString);
            double commissionDouble = Math.abs(Double.parseDouble(commissionString));
            Currency currency = Currency.fromString(currencyString);
            TransactionType transactionType = quantity > 0 ? TransactionType.BUY : TransactionType.SELL;

            internalReconcileTrade(extId, conid, dateTime, quantity, priceDouble, commissionDouble, currency, transactionType);
        }
    }

    private void internalReconcileTrade(String extId, String conid, Date dateTime, long quantity, double priceDouble, double commissionDouble, Currency currency, TransactionType transactionType) {

        Transaction transaction = getTransactionDao().findByExtId(extId);

        if (!this.recreateTransactions && transaction == null) {

            throw new IllegalStateException("transaction: " + extId + " does not exist");

        } else if (transaction == null) {

            Strategy strategy = getStrategyDao().findByName(StrategyImpl.BASE);
            Security security = getSecurityDao().findByConid(conid);

            if (security == null) {
                throw new IllegalStateException("unknown security for conid " + conid);
            }

            BigDecimal price = RoundUtil.getBigDecimal(priceDouble, security.getSecurityFamily().getScale());
            BigDecimal commission = RoundUtil.getBigDecimal(commissionDouble, this.portfolioDigits);

            transaction = new TransactionImpl();
            transaction.setSecurity(security);
            transaction.setStrategy(strategy);
            transaction.setDateTime(dateTime);
            transaction.setExtId(extId);
            transaction.setQuantity(quantity);
            transaction.setPrice(price);
            transaction.setType(transactionType);
            transaction.setCurrency(currency);
            transaction.setExecutionCommission(commission);
            transaction.setMarketChannel(MarketChannel.IB);

            getTransactionService().persistTransaction(transaction);

        } else {


            boolean success = true;
            if (!(new Date(transaction.getDateTime().getTime())).equals(dateTime)) {
                logger.warn("transaction: " + extId + " dateTime does not match db: " + transaction.getDateTime() + " broker: " + dateTime);
                success = false;
            }

            if (transaction.getQuantity() != quantity) {
                logger.error("transaction: " + extId + " quantity does not match db: " + transaction.getQuantity() + " broker: " + quantity);
                success = false;
            }

            if (transaction.getPrice().doubleValue() != priceDouble) {
                logger.error("transaction: " + extId + " price does not match db: " + transaction.getPrice() + " broker: " + priceDouble);
                success = false;
            }

            if (!transaction.getCurrency().equals(currency)) {
                logger.error("transaction: " + extId + " currency does not match db: " + transaction.getCurrency() + " broker: " + currency);
                success = false;
            }

            if (!transaction.getType().equals(transactionType)) {
                logger.error("transaction: " + extId + " type does not match db: " + transaction.getType() + " broker: " + transactionType);
                success = false;
            }

            BigDecimal commission = RoundUtil.getBigDecimal(Math.abs(commissionDouble), this.portfolioDigits);
            BigDecimal existingCommission = transaction.getExecutionCommission();

            if (!existingCommission.equals(commission)) {

                // update the transaction
                transaction.setExecutionCommission(commission);

                // process the difference in commission
                BigDecimal commissionDiff = RoundUtil.getBigDecimal(existingCommission.doubleValue() - commission.doubleValue(), this.portfolioDigits);

                getCashBalanceService().processAmount(transaction.getStrategy().getName(), transaction.getCurrency(), commissionDiff);

                logger.info("transaction: " + extId + " adjusted commission from: " + existingCommission + " to: " + commission);
                success = false;
            }

            if (success) {
                logger.info("transaction: " + extId + " ok");
            }
        }
    }

}
