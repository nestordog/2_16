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
package com.algoTrader.service.ui;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.TransactionImpl;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.strategy.Strategy;
import com.algoTrader.entity.strategy.StrategyImpl;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.CollectionUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.util.ZipUtil;
import com.algoTrader.util.collection.Pair;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class UIReconciliationServiceImpl extends UIReconciliationServiceBase {

    private static Logger logger = MyLogger.getLogger(UIReconciliationServiceImpl.class.getName());
    private static Logger notificationLogger = MyLogger.getLogger("com.algoTrader.service.NOTIFICATION");

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private static NumberFormat numberFormat = NumberFormat.getInstance(Locale.GERMANY);

    private @Value("#{T(com.algoTrader.enumeration.Currency).fromString('${misc.portfolioBaseCurrency}')}") Currency portfolioBaseCurrency;

    @Override
    protected void handleReconcile(String fileName, byte[] data) throws Exception {

        // unzip potential zip files
        if (fileName.endsWith(".zip")) {

            // check for one and only one entry file
            List<Pair<String, byte[]>> entries = ZipUtil.unzip(data);
            if (entries.size() == 0 || entries.size() > 1) {
                throw new IllegalStateException("expecting 1 file inside the zip file");
            } else if (!entries.get(0).getFirst().endsWith(".txt")) {
                throw new IllegalStateException("expecting txt entry file");
            }

            fileName = entries.get(0).getFirst();
            data = entries.get(0).getSecond();
        }

        // create the cash transaction based on todays and yesterdays fees
        createCashTransaction(fileName, data);

        // reconcile positions
        reconcilePositions(fileName, data);
    }

    private void createCashTransaction(String fileName, byte[] data) throws FileNotFoundException, IOException, ParseException {

        String dateString = fileName.substring(16, 26);
        Date date = dateFormat.parse(dateString);

        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data), "ISO-8859-1"));

        // calculate the fee total by adding all items of type 090 (Verbindlichkeiten)
        String line;
        double newFees = 0;
        while ((line = reader.readLine()) != null) {

            String[] values = line.split("\u00b6");
            if ("090".equals(values[0])) {
                newFees += parseDouble(values, 11);
            }
        }
        reader.close();

        // get yesterdays fees from the database
        double oldFees = 0.0;
        String description = "UI Fees";
        Collection<Transaction> transactions = getTransactionDao().findByDescriptionAndMaxDate(1, 1, description, date);
        if (!transactions.isEmpty()) {
            oldFees = -CollectionUtil.getFirstElement(transactions).getPrice().doubleValue();
        }

        Strategy strategy = getStrategyDao().findByName(StrategyImpl.BASE);
        double totalFees = newFees - oldFees;
        BigDecimal price = RoundUtil.getBigDecimal(totalFees).abs(); // price is always positive
        int quantity = totalFees < 0 ? -1 : 1;
        TransactionType transactionType = totalFees < 0 ? TransactionType.FEES : TransactionType.REFUND;

        if (getTransactionDao().findByDateTimePriceTypeAndDescription(date, price, transactionType, description) != null) {

            // @formatter:off
            logger.warn("cash transaction already exists" +
                    " dateTime: " + dateFormat.format(date) +
                    " price: " + price +
                    " type: " + transactionType +
                    " description: " + description);
            // @formatter:on

        } else {

            // create the transaction
            Transaction transaction = new TransactionImpl();
            transaction.setDateTime(date);
            transaction.setQuantity(quantity);
            transaction.setPrice(price);
            transaction.setCurrency(this.portfolioBaseCurrency);
            transaction.setType(transactionType);
            transaction.setStrategy(strategy);
            transaction.setDescription(description);

            // persist the transaction
            getTransactionService().persistTransaction(transaction);
        }
    }

    private void reconcilePositions(String fileName, byte[] data) throws FileNotFoundException, IOException, ParseException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data), "ISO-8859-1"));

        // reoncile all futures and option positions
        String line;
        while ((line = reader.readLine()) != null) {

            String[] values = line.split("\u00b6");

            // Futures
            if ("050".equals(values[0])) {

                Date date = parseDate(values, 2);
                long quantity = parseLong(values, 27);

                String ric = values[43];
                if (!ric.endsWith(":VE")) {
                    ric = ric + ":VE";
                }

                if (ric.startsWith("URO")) {
                    ric = ric.replace("URO", "EC");
                }

                // reconcile position
                reconcilePosition(ric, quantity, date);

                // Options
            } else if ("060".equals(values[0])) {

                Date date = parseDate(values, 2);
                long quantity = parseLong(values, 27);
                String ric = values[43];

                // reconcile position
                reconcilePosition(ric, quantity, date);
            }
        }
        reader.close();
    }

    private void reconcilePosition(String ric, long quantity, Date date) {

        Security security = getSecurityDao().findByRic(ric);
        if (security != null) {

            // get the actual quantity of the position as of the specified date
            Long actualyQuantity = getTransactionDao().findQuantityBySecurityAndDate(security.getId(), DateUtils.addDays(date, 1));

            if (actualyQuantity == null) {
                notificationLogger.warn("position " + dateFormat.format(date) + " " + security + " does not exist");
            } else if (actualyQuantity != quantity) {
                notificationLogger.warn("position " + dateFormat.format(date) + " " + security + " quantity does not match db: " + actualyQuantity + " broker: " + quantity);
            } else {
                logger.info("position " + dateFormat.format(date) + " " + security + " ok");
            }
        } else {
            notificationLogger.warn("security does not exist, ric: " + ric);
        }
    }

    private double parseDouble(String[] values, int idx) throws ParseException {

        if ("".equals(values[idx])) {
            return 0.0;
        } else {
            return numberFormat.parse(values[idx].trim()).doubleValue();
        }
    }

    private long parseLong(String[] values, int idx) throws ParseException {

        if ("".equals(values[idx])) {
            return 0;
        } else {
            return numberFormat.parse(values[idx].trim()).longValue();
        }
    }

    private Date parseDate(String[] values, int idx) throws ParseException {

        return dateFormat.parse(values[idx]);
    }

    @Override
    protected void handleReconcileNAV(Date date, boolean ignoreExecutionCommission, Collection<Integer> ignoredTransactions, Collection<Date> ignoredOptionCommisionDates, Collection<Date> ignoredFutureCommisionDates) {

    }
}
