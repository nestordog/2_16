package com.algoTrader.service.rbs;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.supercsv.exception.SuperCSVReflectionException;

import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.SecurityFamily;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.MyLogger;

public class RBSReconciliationServiceImpl extends RBSReconciliationServiceBase {

    private static Logger logger = MyLogger.getLogger(RBSReconciliationServiceImpl.class.getName());

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
                reconcileTrades(new SimpleDateFormat("dd.MM.yyyy").parse("07.09.2012"), fileName);
            }
        }
    }

    private void reconcilePositions(String fileName) throws IOException {

        List<Map<String, ? super Object>> positions = CsvRBSPositionReader.readPositions(fileName);
        for (Map<String, ? super Object> position : positions) {

            // parse parameters
            String product = (String) position.get("Product");
            String tradeType = (String) position.get("Trade Type");
            Date expiration = DateUtils.addHours((Date) position.get("Prompt"), 13); // expiration is at 13:00:00
            BigDecimal strike = (BigDecimal) position.get("Strike");
            Date date = (Date) position.get("Run Date");
            Long quantity = (Long) position.get("Contracts");

            // find the securities by ricRoot, expiration, strike and type
            SecurityFamily family = getSecurityFamilyDao().findByRicRoot(product);
            if (family == null) {
                logger.error("unknown securityFamily for ric root " + product);
                continue;
            }

            Security security;
            if ("F".equals(tradeType)) {
                security = getFutureDao().findByExpiration(family.getId(), expiration);
            } else if ("C".equals(tradeType) || "P".equals(tradeType)) {
                security = getStockOptionDao().findByExpirationStrikeAndType(family.getId(), expiration, strike, OptionType.fromValue(tradeType));
            } else {
                throw new IllegalArgumentException("unkown tradeType: " + tradeType);
            }

            if (security != null) {

                // get the actual quantity of the position as of the specified date
                Long actualyQuantity = getTransactionDao().findQuantityBySecurityAndDate(security.getId(), date);

                if (actualyQuantity == null) {
                    logger.error("position(s) on security: " + security + " does not exist");
                } else if (actualyQuantity.longValue() != quantity.longValue()) {
                    logger.error("position(s) on security: " + security + " quantity does not match db: " + actualyQuantity + " broker: " + quantity);
                } else {
                    logger.info("position(s) on security: " + security + " ok");
                }
            } else {
                logger.error("security does not exist, product: " + product + " expiration: " + expiration + " strike: " + strike + " tradeType: " + tradeType);
            }
        }
    }

    private void reconcileTrades(Date date, String fileName) throws SuperCSVReflectionException, IOException {

        // get all transactions for that day
        Collection<Transaction> transactions = getTransactionDao().findTransactionsByMinDateAndMaxDate(date, DateUtils.addDays(date, 1));

        List<Map<String, ? super Object>> trades = CsvRBSTradeReader.readPositions(fileName);
        for (Map<String, ? super Object> trade : trades) {

            // parse parameters
            String product = (String) trade.get("Security Code");
            String tradeType = (String) trade.get("Future/Option Indicator");
            BigDecimal strike = (BigDecimal) trade.get("Strike Price");
            Date exerciseDate = (Date) trade.get("Exercise Date");
            Long absQuantity = (Long) trade.get("Quantity");
            final BigDecimal price = (BigDecimal) trade.get("Trade Price");
            BigDecimal commission = ((BigDecimal) trade.get("Commission")).abs();
            TransactionType transactionType = TransactionType.fromValue((String) trade.get("Buy/Sell Indicator"));

            Date expiration = DateUtils.addHours(exerciseDate, 13); // expiration is at 13:00:00
            final long quantity = TransactionType.BUY.equals(transactionType) ? absQuantity : -absQuantity; // signed quantity

            // find the securities by ricRoot, expiration, strike and type
            SecurityFamily family = getSecurityFamilyDao().findByRicRoot(product);
            if (family == null) {
                logger.error("unknown securityFamily for ric root " + product);
                continue;
            }

            final Security security;
            if ("F".equals(tradeType)) {
                security = getFutureDao().findByExpiration(family.getId(), expiration);
            } else if ("C".equals(tradeType) || "P".equals(tradeType)) {
                security = getStockOptionDao().findByExpirationStrikeAndType(family.getId(), expiration, strike, OptionType.fromValue(tradeType));
            } else {
                throw new IllegalArgumentException("unkown tradeType: " + tradeType);
            }

            // find the first transaction that matches security, quantity and price
            Transaction transaction = CollectionUtils.find(transactions, new Predicate<Transaction>() {
                @Override
                public boolean evaluate(Transaction transaction) {
                    return transaction.getSecurity().equals(security) &&
                        transaction.getQuantity() == quantity &&
                        transaction.getPrice().doubleValue() == price.doubleValue();
                }
            });

            if (transaction != null) {

                // remove that transaction from the list
                transactions.remove(transaction);

                // set the clearing commission
                transaction.setClearingCommission(commission);
                getCashBalanceService().processAmount(transaction.getStrategy().getName(), transaction.getCurrency(), commission);

                logger.info("transaction: " + transaction.getExtId() + " set clearing commission to: " + commission);

            } else {
                logger.error("transaction " + transactionType + " " + quantity + " " + security + " price: " + price + " does not exist");
            }
        }

        for (Transaction transaction : transactions) {
            logger.error("transaction " + transaction + " was not present in file");
        }
    }
}
