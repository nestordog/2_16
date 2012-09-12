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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

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

        // check for one and only one zip file
        if (fileNames.size() == 0 || fileNames.size() > 1) {
            throw new IllegalStateException("expecting 1 attachment");
        } else if (!fileNames.get(0).endsWith(".zip")) {
            throw new IllegalStateException("expecting zip attachment");
        }

        // check for one and only one entry file
        List<String> entryFileNames = ZipUtil.unzip(fileNames.get(0), true);
        if (entryFileNames.size() == 0 || entryFileNames.size() > 1) {
            throw new IllegalStateException("expecting 1 file inside the zip file");
        } else if (!entryFileNames.get(0).endsWith(".txt")) {
            throw new IllegalStateException("expecting txt entry file");
        }

        // get the newFile
        File file = new File(entryFileNames.get(0));

        // create the cast transaction for todays fees
        createCashTransaction(file);

        // reconcile positions
        reconcilePositions(file);
    }

    private void createCashTransaction(File newFile) throws FileNotFoundException, IOException, ParseException {


        // compose a map of all files with their corresponding date as key
        Map<Date, File> files = new HashMap<Date, File>();
        for (File file : newFile.getParentFile().listFiles()) {

            if (file.getName().startsWith("Bewertungsdaten")) {
                String dateString = file.getName().substring(16, 25);
                Date date = dateFormat.parse(dateString);
                files.put(date, file);
            }
        }

        // create a sorted set of all dates
        TreeSet<Date> dateSet = (new TreeSet<Date>(files.keySet()));
        Date newDate = dateSet.pollLast(); // remove the last file as it is the newFile
        double newFees = getFees(newFile);

        // get the old file (youngest file besides the newFile) if there is one
        double oldFees = 0;
        if (dateSet.size() > 0) {
            Date oldDate = dateSet.last();
            File oldFile = files.get(oldDate);
            oldFees = getFees(oldFile);
        }

        // get the difference in fees
        BigDecimal fees = RoundUtil.getBigDecimal(newFees - oldFees);

        Strategy strategy = getStrategyDao().findByName(StrategyImpl.BASE);

        // create the transaction
        Transaction transaction = new TransactionImpl();
        transaction.setDateTime(newDate);
        transaction.setQuantity(1);
        transaction.setPrice(fees);
        transaction.setCurrency(this.portfolioBaseCurrency);
        transaction.setType(TransactionType.FEES);
        transaction.setStrategy(strategy);
        transaction.setDescription("UI Fees");

        // persist the transaction
        getTransactionService().persistTransaction(transaction);
    }

    private double getFees(File newFile) throws FileNotFoundException, IOException, ParseException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(newFile)));

        // add all items of type 090 (Verbindlichkeiten)
        String line;
        double totalAmount = 0;
        while ((line = reader.readLine()) != null) {

            String[] values = line.split(String.valueOf((char) 182));

            if ("090".equals(values[0])) {

                totalAmount += parseDouble(values, 11);
            }
        }
        reader.close();

        return totalAmount;
    }

    private void reconcilePositions(File newFile) throws FileNotFoundException, IOException, ParseException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(newFile)));

        // reoncile all futures and option positions
        String line;
        while ((line = reader.readLine()) != null) {

            String[] values = line.split(String.valueOf((char) 182));

            // Futures
            if ("050".equals(values[0])) {

                Date date = parseDate(values, 2);
                long quantity = parseLong(values, 27);
                String ric = values[43];

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
        if (security == null) {

            logger.error("security: " + ric + " does not exist");
        } else {

            // get the actual quantity of the position as of the specified date
            Long actualyQuantity = getTransactionDao().findQuantityBySecurityAndDate(security.getId(), date);

            if (actualyQuantity == null) {
                logger.error("position(s) on security: " + ric + " does not exist");
            } else if (actualyQuantity != quantity) {
                logger.error("position(s) on security: " + ric + " quantity does not match db: " + actualyQuantity + " broker: " + quantity + " date: " + date);
            } else {
                logger.info("position(s) on security: " + ric + " ok");
            }
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
