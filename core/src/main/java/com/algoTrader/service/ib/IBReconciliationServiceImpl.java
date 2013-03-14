/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package com.algoTrader.service.ib;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.springframework.beans.factory.annotation.Value;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

import com.algoTrader.entity.Account;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.TransactionImpl;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.strategy.Strategy;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBReconciliationServiceImpl extends IBReconciliationServiceBase {

    private static Logger logger = MyLogger.getLogger(IBReconciliationServiceImpl.class.getName());
    private static Logger notificationLogger = MyLogger.getLogger("com.algoTrader.service.NOTIFICATION");

    private @Value("${simulation}") boolean simulation;
    private @Value("${misc.portfolioDigits}") int portfolioDigits;

    private @Value("${ib.faEnabled}") boolean faEnabled;
    private @Value("${ib.reconciliationAccount}") String reconciliationAccount;
    private @Value("${ib.masterAccount}") String masterAccount;
    private @Value("${ib.timeDifferenceHours}") int timeDifferenceHours;
    private @Value("${ib.recreateTransactions}") boolean recreateTransactions;

    private static SimpleDateFormat cashDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd, kk:mm:ss");
    private static SimpleDateFormat cashDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat tradeDateTimeFormat = new SimpleDateFormat("yyyyMMdd kkmmss");

    @Override
    protected void handleReconcile(String fileName, byte[] data) throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream is = new ByteArrayInputStream(data);
        Document document = builder.parse(is);

        // do the actual reconciliation
        processCashTransactions(document);
        reconcilePositions(document);
        reconcileTrades(document);
        reconcileUnbookedTrades(document);
    }

    private void processCashTransactions(Document document) throws Exception {

        NodeIterator iterator = XPathAPI.selectNodeIterator(document, "//CashTransaction");

        Node node;
        Strategy strategy = getStrategyDao().findBase();
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
            getPortfolioPersistenceService().rebalancePortfolio();
        }
    }

    private void reconcilePositions(Document document) throws Exception {

        NodeIterator iterator = XPathAPI.selectNodeIterator(document, "//OpenPosition");

        Node node;
        while ((node = iterator.nextNode()) != null) {

            String conid = XPathAPI.selectSingleNode(node, "@conid").getNodeValue();

            Security security = getSecurityDao().findByConid(conid);
            if (security == null) {

                notificationLogger.warn("security for conid: " + conid + " does not exist");
            } else {

                long totalQuantity = 0;
                for (Position position : security.getPositions()) {
                    totalQuantity += position.getQuantity();
                }

                String quantityString = XPathAPI.selectSingleNode(node, "@position").getNodeValue();
                long quantity = Long.parseLong(quantityString);

                if (totalQuantity != quantity) {
                    notificationLogger.warn("position(s) on security: " + conid + " totalQuantity does not match db: " + totalQuantity + " broker: " + quantity);
                } else {
                    logger.info("position(s) on security: " + conid + " ok");
                }
            }
        }
    }

    private void reconcileTrades(Document document) throws Exception {

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

    private void reconcileUnbookedTrades(Document document) throws Exception {

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

            notificationLogger.warn("transaction: " + extId + " does not exist");
            return;

        } else if (transaction == null) {

            Strategy strategy = getStrategyDao().findBase();
            Security security = getSecurityDao().findByConid(conid);
            Account account = getAccountDao().findByName(this.reconciliationAccount);

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
            transaction.setAccount(account);

            getTransactionService().persistTransaction(transaction);

        } else {


            boolean success = true;
            if (!(new Date(transaction.getDateTime().getTime())).equals(dateTime)) {
                logger.warn("transaction: " + extId + " dateTime does not match db: " + transaction.getDateTime() + " broker: " + dateTime);
                success = false;
            }

            if (transaction.getQuantity() != quantity) {
                notificationLogger.warn("transaction: " + extId + " quantity does not match db: " + transaction.getQuantity() + " broker: " + quantity);
                success = false;
            }

            if (transaction.getPrice().doubleValue() != priceDouble) {
                notificationLogger.warn("transaction: " + extId + " price does not match db: " + transaction.getPrice() + " broker: " + priceDouble);
                success = false;
            }

            if (!transaction.getCurrency().equals(currency)) {
                notificationLogger.warn("transaction: " + extId + " currency does not match db: " + transaction.getCurrency() + " broker: " + currency);
                success = false;
            }

            if (!transaction.getType().equals(transactionType)) {
                notificationLogger.warn("transaction: " + extId + " type does not match db: " + transaction.getType() + " broker: " + transactionType);
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
