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
package ch.algotrader.service.jpm;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import ch.algotrader.adapter.jpm.CsvJPMTradeReader;
import ch.algotrader.util.DateUtil;
import ch.algotrader.util.MyLogger;

import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.SecurityFamily;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.service.jpm.JPMReconciliationServiceBase;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class JPMReconciliationServiceImpl extends JPMReconciliationServiceBase {

    private static Logger logger = MyLogger.getLogger(JPMReconciliationServiceImpl.class.getName());
    private static Logger notificationLogger = MyLogger.getLogger("ch.algorader.service.NOTIFICATION");
    private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMddkk:mm:ss");
    private static SimpleDateFormat monthFormat = new SimpleDateFormat("MMM-yy", Locale.ENGLISH);

    @Override
    protected void handleReconcile(String fileName, byte[] data) throws Exception {

        // read the file
        List<Map<String, ? super Object>> trades = CsvJPMTradeReader.readTrades(data);

        for (Map<String, ? super Object> trade : trades) {

            // parse parameters
            String executionId = (String) trade.get("jpm execution id");
            String executionDate = (String) trade.get("execution date");
            String executionTime = (String) trade.get("execution time");
            String buySell = (String) trade.get("buy/sell");
            Long quantity = (Long) trade.get("executed quantity");
            String symbol = (String) trade.get("symbol");
            String expiry = (String) trade.get("expiry month and year");
            BigDecimal price = (BigDecimal) trade.get("executed price");
            String cur = (String) trade.get("currency");

            Currency currency = Currency.valueOf(cur);
            Date dateTime = DateUtils.addHours(dateTimeFormat.parse(executionDate + executionTime), 1); // JPM is london time
            TransactionType transactionType = TransactionType.fromString(buySell.toUpperCase());
            Date exerciseDate = DateUtil.getLastDayOfMonth(monthFormat.parse(StringUtils.capitalize(expiry.toLowerCase()))); // from DEC-12

            // find the securityFamily and securitiy by ricRoot, expiration, strike and type
            SecurityFamily family = getSecurityFamilyDao().findByRicRoot(symbol);
            if (family == null) {
                notificationLogger.warn("unknown securityFamily for ric root " + symbol);
                continue;
            }

            // find the security expirationMonth (date needs to be last day of the month)
            Security security = getFutureDao().findByExpirationMonth(family.getId(), exerciseDate);
            if (security == null) {
                notificationLogger.warn("unknown security for ric root " + symbol + " and expirationMonth " + exerciseDate);
                continue;
            }

            // find the transaction
            Transaction transaction = getTransactionDao().findByExtId(executionId);
            if (transaction == null) {
                notificationLogger.warn("transaction: " + executionId + " does not exist");
                continue;
            }

            boolean success = true;

            if (!(transaction.getSecurity().equals(security))) {
                logger.warn("transaction: " + executionId + " security does not match db: " + transaction.getSecurity() + " broker: " + security);
                success = false;
            }

            if (!(new Date(transaction.getDateTime().getTime())).equals(dateTime)) {
                logger.warn("transaction: " + executionId + " dateTime does not match db: " + transaction.getDateTime() + " broker: " + dateTime);
                success = false;
            }

            if (Math.abs(transaction.getQuantity()) != quantity) {
                notificationLogger.warn("transaction: " + executionId + " quantity does not match db: " + Math.abs(transaction.getQuantity()) + " broker: " + quantity);
                success = false;
            }

            if (transaction.getPrice().doubleValue() != price.doubleValue()) {
                notificationLogger.warn("transaction: " + executionId + " price does not match db: " + transaction.getPrice() + " broker: " + price);
                success = false;
            }

            if (!transaction.getCurrency().equals(currency)) {
                notificationLogger.warn("transaction: " + executionId + " currency does not match db: " + transaction.getCurrency() + " broker: " + currency);
                success = false;
            }

            if (!transaction.getType().equals(transactionType)) {
                notificationLogger.warn("transaction: " + executionId + " type does not match db: " + transaction.getType() + " broker: " + transactionType);
                success = false;
            }

            if (success) {
                logger.info("transaction: " + executionId + " ok");
            }
        }
    }
}
