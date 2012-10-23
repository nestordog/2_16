package com.algoTrader.service.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.TransactionImpl;
import com.algoTrader.entity.security.Security;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.util.ZipUtil;

public class UIReconciliationServiceImpl extends UIReconciliationServiceBase {

    private static Logger logger = MyLogger.getLogger(UIReconciliationServiceImpl.class.getName());

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private static NumberFormat numberFormat = NumberFormat.getInstance(Locale.GERMANY);

    private @Value("#{T(com.algoTrader.enumeration.Currency).fromString('${misc.portfolioBaseCurrency}')}") Currency portfolioBaseCurrency;

    @Override
    protected void handleReconcile() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void handleReconcile(List<String> fileNames) throws Exception {

        // compose a sorted map of new files with their corresponding date as key
        TreeMap<Date, File> newFiles = new TreeMap<Date, File>();
        for (String fileName : fileNames) {

            // unzip potential zip files
            File file;
            if (fileName.endsWith(".zip")) {

                // check for one and only one entry file
                List<String> entryFileNames = ZipUtil.unzip(fileNames.get(0), true);
                if (entryFileNames.size() == 0 || entryFileNames.size() > 1) {
                    throw new IllegalStateException("expecting 1 file inside the zip file");
                } else if (!entryFileNames.get(0).endsWith(".txt")) {
                    throw new IllegalStateException("expecting txt entry file");
                }

                file = new File(entryFileNames.get(0));

            } else {

                file = new File(fileName);
            }

            String dateString = file.getName().substring(16, 26);
            Date date = dateFormat.parse(dateString);
            newFiles.put(date, file);
        }

        // compose a sorted map of all files in the directory with their corresponding date as key
        TreeMap<Date, File> allFiles = new TreeMap<Date, File>();
        File dir = new File("files/ui");
        for (File file : dir.listFiles()) {

            if (file.getName().startsWith("Bewertungsdaten")) {
                String dateString = file.getName().substring(16, 26);
                Date date = dateFormat.parse(dateString);
                allFiles.put(date, file);
            }
        }

        for (Map.Entry<Date, File> entry : newFiles.entrySet()) {

            // create the cash transaction based on todays and yesterdays fees
            Map.Entry<Date, File> lowerEntry = allFiles.lowerEntry(entry.getKey());
            File lowerValue = lowerEntry != null ? lowerEntry.getValue() : null;
            createCashTransaction(entry.getKey(), entry.getValue(), lowerValue);

            // reconcile positions
            reconcilePositions(entry.getValue());
        }
    }

    private void createCashTransaction(Date date, File currentFile, File lastFile) throws FileNotFoundException, IOException, ParseException {

        double newFees = getFees(currentFile);

        double oldFees = 0.0;
        if (lastFile != null) {
            oldFees = getFees(lastFile);
        }

        Strategy strategy = getStrategyDao().findByName(StrategyImpl.BASE);
        BigDecimal price = RoundUtil.getBigDecimal(newFees - oldFees); // difference in fees
        String description = "UI Fees";
        TransactionType transactionType = TransactionType.FEES;

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
            transaction.setQuantity(1);
            transaction.setPrice(price);
            transaction.setCurrency(this.portfolioBaseCurrency);
            transaction.setType(transactionType);
            transaction.setStrategy(strategy);
            transaction.setDescription(description);

            // persist the transaction
            getTransactionService().persistTransaction(transaction);
        }
    }

    private double getFees(File newFile) throws FileNotFoundException, IOException, ParseException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(newFile), "ISO-8859-1"));

        // add all items of type 090 (Verbindlichkeiten)
        String line;
        double totalAmount = 0;
        while ((line = reader.readLine()) != null) {

            String[] values = line.split("\u00b6");

            if ("090".equals(values[0])) {

                totalAmount += parseDouble(values, 11);
            }
        }
        reader.close();

        return totalAmount;
    }

    private void reconcilePositions(File newFile) throws FileNotFoundException, IOException, ParseException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(newFile), "ISO-8859-1"));

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
                logger.error("position " + dateFormat.format(date) + " " + security + " does not exist");
            } else if (actualyQuantity != quantity) {
                logger.error("position " + dateFormat.format(date) + " " + security + " quantity does not match db: " + actualyQuantity + " broker: " + quantity);
            } else {
                logger.info("position " + dateFormat.format(date) + " " + security + " ok");
            }
        } else {
            logger.error("security does not exist, ric: " + ric);
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
}
