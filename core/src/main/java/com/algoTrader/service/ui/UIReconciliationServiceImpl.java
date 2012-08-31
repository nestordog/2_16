package com.algoTrader.service.ui;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.TransactionImpl;
import com.algoTrader.entity.security.Security;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.service.ib.IBAccountServiceException;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.util.ZipUtil;

public class UIReconciliationServiceImpl extends UIReconciliationServiceBase {

    private static Logger logger = MyLogger.getLogger(UIReconciliationServiceImpl.class.getName());

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    NumberFormat numberFormat = NumberFormat.getInstance(Locale.GERMANY);
    private static SimpleDateFormat cashDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd, kk:mm:ss");
    private static SimpleDateFormat cashDateFormat = new SimpleDateFormat("yyyy-MM-dd");

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

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(entryFileNames.get(0))));

        String line;
        while ((line = reader.readLine()) != null) {

            String[] values = line.split(String.valueOf((char) 182));

            // Fondsschlüsseldaten
            if ("010".equals(values[0])) {

                String fondsNummer = values[1];
                Date bewertungsDatum = parseDate(values, 2);
                Date lieferDatum = parseDate(values, 3);
                String lieferNummer = values[4];
                String wkn = values[5];
                String isin = values[6];
                String fondsname = values[7];
                Currency fondswaehrung = Currency.valueOf(values[8]);

                // Vermögensdaten
            } else if ("020".equals(values[0])) {

                //                BigDecimal fondsVermoegen = parseBigDecimal(values, 11, 2);
                //                BigDecimal ausgabePreis = parseBigDecimal(values, 12, 2);
                //                BigDecimal ruecknahmePreis = parseBigDecimal(values, 13, 2);
                //                long anteileImUmlauf = parseLong(values, 14);

                // Kontensalden / Bankkonten
            } else if ("030".equals(values[0])) {

                //                String kontonummer = values[7];
                //                String kontoBezeichnung = values[8];
                //                Currency kontoWaehrung = Currency.valueOf(values[10]);
                //                BigDecimal saldoInFondswaehrung = parseBigDecimal(values, 11, 2);
                //                BigDecimal waehrungsBetrag = parseBigDecimal(values, 12, 2);
                //                BigDecimal devisenkurs = parseBigDecimal(values, 28, 4);

                // Futurebestände
            } else if ("050".equals(values[0])) {

                Date bewertungsDatum = parseDate(values, 2);
                //                String wkn = values[5];
                //                String isinBasis = values[6];
                //                String geschaeftsBezeichnung = values[7];
                //                String kurzbezeichnung = values[8];
                //                String futureArt = values[9];
                //                Currency waehrung = Currency.valueOf(values[10]);
                //                BigDecimal kurswertInFondswaehrung = parseBigDecimal(values, 11, 2);
                //                BigDecimal tageskurs = parseBigDecimal(values, 14, 2);
                //                int kontraktgroesse = parseInt(values, 15);
                //                String isin = values[18];
                //                BigDecimal einstandpreis = parseBigDecimal(values, 22, 6);
                long anzahlKontrakte = parseLong(values, 27);
                //                BigDecimal devisenkurs = parseBigDecimal(values, 28, 4);
                //                Date verfalldatum = parseDate(values, 36);
                //                String bloombergUID = values[40];
                //                Side kaufVerkauf = "K".equals(values[41]) ? Side.BUY : Side.SELL;
                String reutersRIC = values[43];
                //                String bloombergKuerzel = values[44];

                // reconcile position
                reconcilePosition(reutersRIC, anzahlKontrakte, bewertungsDatum);

                // Optionsbestände
            } else if ("060".equals(values[0])) {

                Date bewertungsDatum = parseDate(values, 2);
                //                String wkn = values[5];
                //                String isinBasis = values[6];
                //                String geschaeftsBezeichnung = values[7];
                //                OptionType putCall = OptionType.valueOf(values[8]);
                //                String geschaeftsArt = values[9];
                //                Currency waehrung = Currency.valueOf(values[10]);
                //                BigDecimal kurswertInFondswaehrung = parseBigDecimal(values, 11, 2);
                //                BigDecimal tageskurs = parseBigDecimal(values, 14, 2);
                //                int kontraktgroesse = parseInt(values, 15);
                //                String isin = values[18];
                //                double delta = parseDouble(values, 20);
                //                BigDecimal basisPreis = parseBigDecimal(values, 21, 2);
                //                BigDecimal einstandPreis = parseBigDecimal(values, 22, 6);
                //                BigDecimal einstandsWert = parseBigDecimal(values, 23, 5);
                //                BigDecimal vorlaeufigesErgebnis = parseBigDecimal(values, 24, 6);
                long anzahlKontrakte = parseLong(values, 27);
                //                BigDecimal devisenkurs = parseBigDecimal(values, 28, 4);
                //                Date verfalldatum = parseDate(values, 36);
                //                String bloombergUID = values[40];
                //                Side kaufVerkauf = "K".equals(values[41]) ? Side.BUY : Side.SELL;
                String reutersRIC = values[43];
                //                String bloombergKuerzel = values[44];

                // reconcile position
                reconcilePosition(reutersRIC, anzahlKontrakte, bewertungsDatum);

                // Verbindlichkeiten
            } else if ("090".equals(values[0])) {

                String kontoNummer = values[7];
                String bezeichnung = values[8];
                Currency waehrung = Currency.valueOf(values[10]);
                BigDecimal verbindlichkeitInFondswaehrung = parseBigDecimal(values, 11, 2);
                BigDecimal waehrungsbetrag = parseBigDecimal(values, 14, 2);
                BigDecimal devisenkurs = parseBigDecimal(values, 28, 4);
            }
        }
    }

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
            if (typeString.equals("Other Fees")) {
                if (amountDouble < 0) {
                    transactionType = TransactionType.FEES;
                } else {
                    transactionType = TransactionType.REFUND;
                }
            } else if (typeString.equals("Broker Interest Paid")) {
                transactionType = TransactionType.INTREST_PAID;
            } else if (typeString.equals("Broker Interest Received")) {
                transactionType = TransactionType.INTREST_RECEIVED;
            } else if (typeString.equals("Deposits & Withdrawals")) {
                if (amountDouble > 0) {
                    transactionType = TransactionType.CREDIT;
                } else {
                    transactionType = TransactionType.DEBIT;
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
                transaction.setQuantity(1);
                transaction.setPrice(price);
                transaction.setCommission(new BigDecimal(0));
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

    private void reconcilePosition(String ric, long quantity, Date date) {

        Security security = getSecurityDao().findByRic(ric);
        if (security == null) {

            logger.error("security: " + ric + " does not exist");
        } else {

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
            return this.numberFormat.parse(values[idx].trim()).doubleValue();
        }
    }

    private long parseLong(String[] values, int idx) throws ParseException {

        if ("".equals(values[idx])) {
            return 0;
        } else {
            return this.numberFormat.parse(values[idx].trim()).longValue();
        }
    }

    private BigDecimal parseBigDecimal(String[] values, int idx, int scale) throws ParseException {

        return RoundUtil.getBigDecimal(parseDouble(values, idx), scale);
    }

    private Date parseDate(String[] values, int idx) throws ParseException {

        return this.dateFormat.parse(values[idx]);
    }
}
