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
package ch.algotrader.service.rbs;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.HashBag;
import org.apache.commons.collections15.keyvalue.MultiKey;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.supercsv.exception.SuperCSVReflectionException;

import ch.algotrader.adapter.rbs.CsvRBSPositionReader;
import ch.algotrader.adapter.rbs.CsvRBSTradeReader;
import ch.algotrader.util.MyLogger;
import ch.algotrader.util.RoundUtil;
import ch.algotrader.util.collection.CollectionUtil;
import ch.algotrader.util.collection.LongMap;

import com.algoTrader.entity.Position;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.SecurityFamily;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.service.rbs.RBSReconciliationServiceBase;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class RBSReconciliationServiceImpl extends RBSReconciliationServiceBase {

    private static Logger logger = MyLogger.getLogger(RBSReconciliationServiceImpl.class.getName());
    private static Logger notificationLogger = MyLogger.getLogger("ch.algorader.service.NOTIFICATION");


    private static SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

    private @Value("${misc.portfolioDigits}") int portfolioDigits;

    @Override
    protected void handleReconcile(String fileName, byte[] data) throws Exception {

        if (fileName.contains("Positions")) {
            reconcilePositions(data);
        } else if (fileName.contains("Trades")) {
            reconcileTrades(data);
        }
    }

    private void reconcilePositions(byte[] data) throws IOException {

        // get open positions by statement date
        Map<String, ? super Object> firstPosition = CollectionUtil.getFirstElementOrNull(CsvRBSPositionReader.readPositions(data));
        if (firstPosition == null) {
            return;
        }

        Date date = (Date) firstPosition.get("Statement Date");
        Collection<Position> positions = getPositionDao().findOpenPositionsByMaxDateAggregated(date);

        // group position quantities per security
        LongMap<Security> quantitiesPerSecurity = new LongMap<Security>();
        for (Position position : positions) {
            quantitiesPerSecurity.put(position.getSecurity(), position.getQuantity());
        }

        for (Map<String, ? super Object> position : CsvRBSPositionReader.readPositions(data)) {

            // parse parameters
            String securityCode = (String) position.get("Security Code");
            String tradeType = (String) position.get("Trade Type");
            Date exerciseDate = DateUtils.addHours((Date) position.get("Exercise Date"), 13); // expiration is at 13:00:00
            BigDecimal strikePrice = (BigDecimal) position.get("Strike Price");
            Long quantity = (Long) position.get("Quantity");

            // find the securities by ricRoot, expiration, strike and type
            SecurityFamily family = getSecurityFamilyDao().findByRicRoot(securityCode);
            if (family == null) {
                notificationLogger.warn("unknown securityFamily for ric root " + securityCode);
                continue;
            }

            Security security;
            if ("F".equals(tradeType)) {
                security = getFutureDao().findByExpirationMonth(family.getId(), exerciseDate);
            } else if ("C".equals(tradeType) || "P".equals(tradeType)) {
                security = getStockOptionDao().findByExpirationStrikeAndType(family.getId(), exerciseDate, strikePrice, OptionType.fromValue(tradeType));
            } else {
                throw new IllegalArgumentException("unkown tradeType: " + tradeType);
            }

            if (security == null) {
                notificationLogger.warn("security does not exist, product: " + securityCode + " expiration: " + format.format(exerciseDate) + " strike: " + strikePrice + " tradeType: " + tradeType);
                continue;
            }

            // compare quantities
            Long actualyQuantity = quantitiesPerSecurity.getLong(security);
            if (actualyQuantity == null) {
                notificationLogger.warn("position " + format.format(date) + " " + quantity + " " + security + " does not exist");
            } else if (actualyQuantity.longValue() != quantity.longValue()) {
                notificationLogger.warn("position " + format.format(date) + " " + security + " quantity does not match db: " + actualyQuantity + " file: " + quantity);
            } else {
                quantitiesPerSecurity.remove(security);
                logger.info("position " + format.format(date) + " " + quantity + " " + security + " ok");
            }
        }

        for (Map.Entry<Security, AtomicLong> entry : quantitiesPerSecurity.entrySet()) {
            notificationLogger.warn("position " + format.format(date) + " " + entry.getKey() + " unmatched db quantitiy " + entry.getValue());
        }
    }

    private void reconcileTrades(byte[] data) throws SuperCSVReflectionException, IOException {

        // read the file
        List<Map<String, ? super Object>> trades = CsvRBSTradeReader.readPositions(data);

        // get minDate and maxDate
        Date minDate = new Date(Long.MAX_VALUE);
        Date maxDate = new Date(0);
        for (Map<String, ? super Object> trade : trades) {
            Date date = (Date) trade.get("Trade Date");
            if (date.getTime() < minDate.getTime()) {
                minDate = date;
            }
            if (date.getTime() > maxDate.getTime()) {
                maxDate = date;
            }
        }

        // get all transactions for the date range
        Collection<Transaction> transactions = getTransactionDao().findTradesByMinDateAndMaxDate(minDate, DateUtils.addDays(maxDate, 1));

        // group transactions by date, transactionType, security and price
        Bag<MultiKey<Object>> transactionBag = new HashBag<MultiKey<Object>>();
        for (Transaction transaction : transactions) {

            Date date = DateUtils.truncate(transaction.getDateTime(), Calendar.DATE);
            MultiKey<Object> key = new MultiKey<Object>(date, transaction.getType(), transaction.getSecurity(), transaction.getPrice().doubleValue());
            transactionBag.add(key, (int)Math.abs(transaction.getQuantity()));
        }

        for (Map<String, ? super Object> trade : trades) {

            // parse parameters
            final Date tradeDate = (Date) trade.get("Trade Date");
            String securityCode = (String) trade.get("Security Code");
            String tradeType = (String) trade.get("Trade Type");
            BigDecimal strikePrice = (BigDecimal) trade.get("Strike Price");
            Date exerciseDate = (Date) trade.get("Exercise Date");
            Long absQuantity = (Long) trade.get("Quantity");
            final BigDecimal tradePrice = (BigDecimal) trade.get("Trade Price");
            BigDecimal commission = ((BigDecimal) trade.get("Commission")).abs();
            TransactionType transactionType = TransactionType.fromValue((String) trade.get("Buy/Sell Indicator"));

            // find the securityFamily and securitiy by ricRoot, expiration, strike and type
            SecurityFamily family = getSecurityFamilyDao().findByRicRoot(securityCode);
            if (family == null) {
                notificationLogger.warn("unknown securityFamily for ric root " + securityCode);
                continue;
            }

            final Security security;
            if ("F".equals(tradeType)) {
                security = getFutureDao().findByExpirationMonth(family.getId(), exerciseDate); // futures exerciseDate is the last day of the month
            } else if ("C".equals(tradeType) || "P".equals(tradeType)) {
                Date expiration = DateUtils.addHours(exerciseDate, 13); // option expiration in db is at 13:00:00
                security = getStockOptionDao().findByExpirationStrikeAndType(family.getId(), expiration, strikePrice, OptionType.fromValue(tradeType));
            } else {
                throw new IllegalArgumentException("unkown tradeType: " + tradeType);
            }

            // check clearing commission
            if (family.getClearingCommission() != null) {
                BigDecimal commissionPerContract = RoundUtil.getBigDecimal(commission.doubleValue() / absQuantity, this.portfolioDigits);
                if (!family.getClearingCommission().setScale(this.portfolioDigits).equals(commissionPerContract)) {
                    notificationLogger.warn("transaction " + format.format(tradeDate) + " " + transactionType + " " + absQuantity + " " + security + " price: " + tradePrice
                            + " clearing commission is " + commissionPerContract + " where it should be " + family.getClearingCommission());
                }
            } else {
                throw new IllegalArgumentException("no clearing commission defined for security family " + family);
            }

            // lookup by date, transactionType, security and price
            MultiKey<Object> key = new MultiKey<Object>(tradeDate, transactionType, security, tradePrice.doubleValue());
            if (!transactionBag.contains(key)) {

                notificationLogger.warn("transactions " + format.format(tradeDate) + " " + transactionType + " " + security + " price: " + tradePrice + " quantity: " + absQuantity
                        + " does not exist in db");
            } else if (absQuantity > transactionBag.getCount(key)) {

                notificationLogger.warn("transactions " + format.format(tradeDate) + " " + transactionType + " " + security + " price: " + tradePrice + " unmatched file quantity " + absQuantity);
            } else {

                // remove that corresponding quantity
                transactionBag.remove(key, absQuantity.intValue());

                logger.info("transaction " + format.format(tradeDate) + " " + transactionType + " " + absQuantity + " " + security + " price: " + tradePrice + " is ok");
            }
        }

        for (MultiKey<Object> key : transactionBag.uniqueSet()) {
            notificationLogger.warn("transactions " + format.format(key.getKey(0)) + " " + key.getKey(1) + " " + key.getKey(2) + " price: " + key.getKey(3) + " unmatched db quantitiy "
                    + transactionBag.getCount(key));
        }
    }
}
