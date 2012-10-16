package com.algoTrader.service.rbs;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.HashBag;
import org.apache.commons.collections15.keyvalue.MultiKey;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.supercsv.exception.SuperCSVReflectionException;

import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.SecurityFamily;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;

public class RBSReconciliationServiceImpl extends RBSReconciliationServiceBase {

    private static Logger logger = MyLogger.getLogger(RBSReconciliationServiceImpl.class.getName());

    private @Value("${misc.portfolioDigits}") int portfolioDigits;

    @Override
    protected void handleReconcile() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void handleReconcile(List<String> fileNames) throws Exception {

        for (String fileName : fileNames) {
            if (fileName.contains("Positions")) {
                reconcilePositions(fileName);
            } else if (fileName.contains("Trades")) {
                reconcileTrades(fileName);
            }
        }
    }

    private void reconcilePositions(String fileName) throws IOException {

        List<Map<String, ? super Object>> positions = CsvRBSPositionReader.readPositions(fileName);
        for (Map<String, ? super Object> position : positions) {

            // parse parameters
            String securityCode = (String) position.get("Security Code");
            String tradeType = (String) position.get("Trade Type");
            Date exerciseDate = DateUtils.addHours((Date) position.get("Exercise Date"), 13); // expiration is at 13:00:00
            BigDecimal strikePrice = (BigDecimal) position.get("Strike Price");
            Date statementDate = (Date) position.get("Statement Date");
            Long quantity = (Long) position.get("Quantity");

            // find the securities by ricRoot, expiration, strike and type
            SecurityFamily family = getSecurityFamilyDao().findByRicRoot(securityCode);
            if (family == null) {
                logger.error("unknown securityFamily for ric root " + securityCode);
                continue;
            }

            Security security;
            if ("F".equals(tradeType)) {
                security = getFutureDao().findByExpiration(family.getId(), exerciseDate);
            } else if ("C".equals(tradeType) || "P".equals(tradeType)) {
                security = getStockOptionDao().findByExpirationStrikeAndType(family.getId(), exerciseDate, strikePrice, OptionType.fromValue(tradeType));
            } else {
                throw new IllegalArgumentException("unkown tradeType: " + tradeType);
            }

            if (security != null) {

                // get the actual quantity of the position as of the specified date
                Long actualyQuantity = getTransactionDao().findQuantityBySecurityAndDate(security.getId(), statementDate);

                if (actualyQuantity == null) {
                    logger.error("position(s) on security: " + security + " does not exist");
                } else if (actualyQuantity.longValue() != quantity.longValue()) {
                    logger.error("position(s) on security: " + security + " quantity does not match db: " + actualyQuantity + " broker: " + quantity);
                } else {
                    logger.info("position(s) on security: " + security + " ok");
                }
            } else {
                logger.error("security does not exist, product: " + securityCode + " expiration: " + exerciseDate + " strike: " + strikePrice + " tradeType: " + tradeType);
            }
        }
    }

    private void reconcileTrades(String fileName) throws SuperCSVReflectionException, IOException {

        // read the file
        List<Map<String, ? super Object>> trades = CsvRBSTradeReader.readPositions(fileName);

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
        Collection<Transaction> transactions = getTransactionDao().findTransactionsByMinDateAndMaxDate(minDate, DateUtils.addDays(maxDate, 1));

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
                logger.error("unknown securityFamily for ric root " + securityCode);
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
            BigDecimal commissionPerContract = RoundUtil.getBigDecimal(commission.doubleValue() / absQuantity, this.portfolioDigits);
            if (!family.getClearingCommission().setScale(this.portfolioDigits).equals(commissionPerContract)) {
                logger.error("transaction from " + tradeDate + " " + transactionType + " " + absQuantity + " " + security + " price: " + tradePrice + " clearing commission is "
                        + commissionPerContract + " where it should be " + family.getClearingCommission());
            }

            // lookup by date, transactionType, security and price
            MultiKey<Object> key = new MultiKey<Object>(tradeDate, transactionType, security, tradePrice.doubleValue());
            if (!transactionBag.contains(key)) {

                logger.error("transactions from " + tradeDate + " " + transactionType + " " + security + " price: " + tradePrice + " quantity: " + absQuantity + " does not exist");
            } else if (absQuantity > transactionBag.getCount(key)) {

                logger.error("transactions from " + tradeDate + " " + transactionType + " " + security + " price: " + tradePrice + " unmatched file quantity " + absQuantity);
            } else {

                // remove that corresponding quantity
                transactionBag.remove(key, absQuantity.intValue());

                logger.info("transaction from " + tradeDate + " " + transactionType + " " + absQuantity + " " + security + " price: " + tradePrice + " is ok");
            }
        }

        for (MultiKey<Object> key : transactionBag.uniqueSet()) {
            logger.error("transactions from " + key.getKey(0) + " " + key.getKey(1) + " " + key.getKey(2) + " price: " + key.getKey(3) + " unmatched db quantitiy " + transactionBag.getCount(key));
        }
    }
}
