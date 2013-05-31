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
package ch.algotrader.service.ui;

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
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import ch.algotrader.entity.TransactionImpl;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.util.MyLogger;
import ch.algotrader.util.RoundUtil;
import ch.algotrader.util.ZipUtil;
import ch.algotrader.util.collection.LongMap;
import ch.algotrader.util.collection.Pair;

import com.algoTrader.entity.Position;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.strategy.PortfolioValue;
import com.algoTrader.entity.strategy.Strategy;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.service.ui.UIReconciliationServiceBase;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class UIReconciliationServiceImpl extends UIReconciliationServiceBase {

    private static Logger logger = MyLogger.getLogger(UIReconciliationServiceImpl.class.getName());
    private static Logger notificationLogger = MyLogger.getLogger("ch.algorader.service.NOTIFICATION");

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private static NumberFormat numberFormat = NumberFormat.getInstance(Locale.GERMANY);

    private @Value("#{T(ch.algorader.enumeration.Currency).fromString('${misc.portfolioBaseCurrency}')}") Currency portfolioBaseCurrency;
    private @Value("${ib.adjustmentThreshold}") long adjustmentThreshold;

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

        // get the statement date (Bewertungsdatum)
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data), "ISO-8859-1"));
        String[] values = reader.readLine().split("\u00b6");
        Date date = dateFormat.parse(values[2]);
        reader.close();

        /// proces Subscriptions / Redemptions
        processCashTransactions(date, data);

        // adjust NAV if necessary
        adjustNAV(date, data);

        // reconcile positions
        reconcilePositions(date, data);
    }

    private void processCashTransactions(Date date, byte[] data) throws FileNotFoundException, IOException, ParseException {

        Date endOfDay = DateUtils.addDays(date, 1);

        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data), "ISO-8859-1"));

        // get the current nav based on 090 (Vermoegensdaten)
        String line;
        double sharePrice = 0;
        int shares = 0;
        while ((line = reader.readLine()) != null) {

            String[] values = line.split("\u00b6");
            if ("020".equals(values[0])) {
                sharePrice = parseDouble(values, 13);
                shares = (int)parseLong(values, 14);
                break;
            }
        }
        reader.close();

        // store shares and sharePrice as measurement
        getMeasurementService().createMeasurement(StrategyImpl.BASE, "shares", endOfDay, shares);
        getMeasurementService().createMeasurement(StrategyImpl.BASE, "sharePrice", endOfDay, sharePrice);

        // get last shares and sharePrice
        Integer oldShares = (Integer) getLookupService().getMeasurementByMaxDate(StrategyImpl.BASE, "shares", date);
        Double oldSharePrice = (Double) getLookupService().getMeasurementByMaxDate(StrategyImpl.BASE, "sharePrice", date);

        // create a Subscription / Redemption if necessary
        if (oldShares != null && oldSharePrice != null && oldShares.intValue() != shares) {

            int deltaShares = shares - oldShares.intValue();
            int quantity = deltaShares > 0 ? 1 : -1;
            BigDecimal price = RoundUtil.getBigDecimal(deltaShares * oldSharePrice).abs(); // price is always positive
            TransactionType transactionType = deltaShares > 0 ? TransactionType.CREDIT : TransactionType.DEBIT;
            Strategy strategy = getStrategyDao().findBase();
            String description = (deltaShares > 0 ? "Subscription" : "Redemption") + " of " + deltaShares + " shares at " + oldSharePrice;

            // create the transaction
            Transaction transaction = new TransactionImpl();
            transaction.setDateTime(endOfDay);
            transaction.setQuantity(quantity);
            transaction.setPrice(price);
            transaction.setCurrency(this.portfolioBaseCurrency);
            transaction.setType(transactionType);
            transaction.setStrategy(strategy);
            transaction.setDescription(description);

            if (getTransactionDao().findByDateTimePriceTypeAndDescription(endOfDay, price, transactionType, description) != null) {

                // @formatter:off
                logger.warn("cash transaction already exists" +
                        " dateTime: " + dateFormat.format(endOfDay) +
                        " price: " + price +
                        " type: " + transactionType +
                        " description: " + description);
                // @formatter:on

            } else {

                // persist the transaction
                getTransactionService().persistTransaction(transaction);

                notificationLogger.info(dateFormat.format(date) + " subscription/redemption of " + deltaShares + " shares at " + oldSharePrice + " totalAmount " + price);
            }
        }
    }

    private void adjustNAV(Date date, byte[] data) throws FileNotFoundException, IOException, ParseException {

        Date endOfDay = DateUtils.addDays(date, 1);

        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data), "ISO-8859-1"));

        // get the current nav based on 090 (Vermoegensdaten)
        String line;
        double nav = 0;
        while ((line = reader.readLine()) != null) {

            String[] values = line.split("\u00b6");
            if ("020".equals(values[0])) {
                nav = parseDouble(values, 11);
                break;
            }
        }
        reader.close();

        // get the end-of-day portfolioValue based on transactions in the db
        PortfolioValue portfolioValue = getPortfolioService().getPortfolioValue(StrategyImpl.BASE, endOfDay);

        // create the transaction
        double adjustment = nav - portfolioValue.getNetLiqValueDouble();
        BigDecimal price = RoundUtil.getBigDecimal(adjustment).abs(); // price is always positive
        int quantity = adjustment < 0 ? -1 : 1;
        TransactionType transactionType = adjustment < 0 ? TransactionType.FEES : TransactionType.REFUND;
        Strategy strategy = getStrategyDao().findBase();
        String description = "UI NAV Adjustment";

        if (price.doubleValue() != 0.0) {

            // create the transaction
            Transaction transaction = new TransactionImpl();
            transaction.setDateTime(endOfDay);
            transaction.setQuantity(quantity);
            transaction.setPrice(price);
            transaction.setCurrency(this.portfolioBaseCurrency);
            transaction.setType(transactionType);
            transaction.setStrategy(strategy);
            transaction.setDescription(description);

            // persist the transaction
            getTransactionService().persistTransaction(transaction);
        }

        if (price.longValue() > this.adjustmentThreshold) {
            notificationLogger.warn(dateFormat.format(date) + " verify adjustment of " + price);
        }
    }

    private void reconcilePositions(Date date, byte[] data) throws FileNotFoundException, IOException, ParseException {

        Date endOfDay = DateUtils.addDays(date, 1);

        // get open positions by the end of the statement date
        Collection<Position> positions = getPositionDao().findOpenPositionsByMaxDateAggregated(endOfDay);

        // group position quantities per security
        LongMap<Security> quantitiesPerSecurity = new LongMap<Security>();
        for (Position position : positions) {
            quantitiesPerSecurity.put(position.getSecurity(), position.getQuantity());
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data), "ISO-8859-1"));

        // reoncile all futures and option positions
        String line;
        String ric = null;
        long quantity = 0;
        while ((line = reader.readLine()) != null) {

            String[] values = line.split("\u00b6");

            // Futures
            if ("050".equals(values[0])) {

                quantity = parseLong(values, 27);

                ric = values[43];

                if (ric.startsWith("URO")) {
                    ric = ric.replace("URO", "EC");
                }

                if (ric.length() == 5 && ric.startsWith("ECM")) {
                    ric = ric.replace("ECM", "M6E");
                }

                if (!ric.endsWith(":VE")) {
                    ric = ric + ":VE";
                }

                // Options
            } else if ("060".equals(values[0])) {

                quantity = parseLong(values, 27);
                ric = values[43];
            } else {
                continue;
            }

            Security security = getSecurityDao().findByRic(ric);
            if (security == null) {
                notificationLogger.warn("security does not exist, ric: " + ric);
                continue;
            }

            // compare quantities
            Long actualyQuantity = quantitiesPerSecurity.getLong(security);
            if (actualyQuantity == null) {
                notificationLogger.warn("position " + dateFormat.format(date) + " " + security + " does not exist");
            } else if (actualyQuantity != quantity) {
                notificationLogger.warn("position " + dateFormat.format(date) + " " + security + " quantity does not match db: " + actualyQuantity + " broker: " + quantity);
            } else {
                quantitiesPerSecurity.remove(security);
                logger.info("position " + dateFormat.format(date) + " " + security + " ok");
            }
        }
        reader.close();

        for (Map.Entry<Security, AtomicLong> entry : quantitiesPerSecurity.entrySet()) {
            notificationLogger.warn("position " + dateFormat.format(date) + " " + entry.getKey() + " unmatched db quantitiy " + entry.getValue());
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
}
