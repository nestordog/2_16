package com.algoTrader.service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.supercsv.exception.SuperCSVException;

import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.Tick;
import com.algoTrader.entity.Transaction;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.stockOption.StockOptionUtil;
import com.algoTrader.util.EsperService;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.util.csv.CsvTickReader;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.time.CurrentTimeEvent;

public class TestServiceImpl extends com.algoTrader.service.TestServiceBase {

    private Map<String, List<Tick>> optionMap = new HashMap<String, List<Tick>>();

    @SuppressWarnings("unchecked")
    protected void handleVerifyTransactions() throws Exception {

        EPServiceProvider cep = EsperService.getEPServiceInstance();

        Collection<Transaction> transactions = getTransactionDao().findAllTrades();

        CsvTickReader underlayingReader = new CsvTickReader("CH0008616382");
        Tick tick;
        List<Tick> underlayings = new ArrayList<Tick>();
        while ((tick = underlayingReader.readTick()) != null) {
            underlayings.add(tick);
        }

        CsvTickReader volaReader = new CsvTickReader("CH0019900841");
        List<Tick> volatilities = new ArrayList<Tick>();
        while ((tick = volaReader.readTick()) != null) {
            volatilities.add(tick);
        }

        for (Transaction transaction : transactions) {

            Date date = transaction.getDateTime();
            cep.getEPRuntime().sendEvent(new CurrentTimeEvent(date.getTime()));

            if (transaction.getType().equals(TransactionType.CREDIT) || transaction.getType().equals(TransactionType.EXPIRATION)) continue;

            StockOption stockOption = (StockOption)transaction.getSecurity();

            Tick underlayingTick = selectTickByDate(underlayings, date);
            Tick volaTick = selectTickByDate(volatilities, date);
            Tick optionTick = selectTickByDate(getOptionTicks(transaction.getSecurity().getIsin()), date);

            if (volaTick == null) continue;


            // actual price
            System.out.print(date + ",");
            System.out.print(transaction.getType().toString() + ",");
            System.out.print(RoundUtil.getBigDecimal(transaction.getPrice().doubleValue() / 10.0) + ",");

            // real bid/ask
            if (TransactionType.SELL.equals(transaction.getType())) {
                System.out.print(optionTick.getBid() + ",");
            } else if (TransactionType.BUY.equals(transaction.getType())) {
                System.out.print(optionTick.getAsk() + ",");
            }

            // simulated bid/ask
            double meanValue = (optionTick.getAsk().doubleValue() + optionTick.getBid().doubleValue()) / 2.0;
            if (TransactionType.SELL.equals(transaction.getType())) {
                System.out.print(RoundUtil.getBigDecimal(StockOptionUtil.getDummyBid(meanValue)) + ",");
            } else if (TransactionType.BUY.equals(transaction.getType())) {
                System.out.print(RoundUtil.getBigDecimal(StockOptionUtil.getDummyAsk(meanValue)) + ",");
            }

            // sabr
            double underlayingValue = underlayingTick.getLast().doubleValue();
            double volaValue = volaTick.getLast().doubleValue() / 100.0;
            double sabrValue = StockOptionUtil.getOptionPriceSabr(stockOption, underlayingValue, volaValue);

            if (TransactionType.SELL.equals(transaction.getType())) {
                System.out.print(RoundUtil.getBigDecimal(StockOptionUtil.getDummyBid(sabrValue)) + ",");
            } else if (TransactionType.BUY.equals(transaction.getType())) {
                System.out.print(RoundUtil.getBigDecimal(StockOptionUtil.getDummyAsk(sabrValue)) + ",");
            }

            // bs
            double bsValue = StockOptionUtil.getOptionPriceBS(stockOption, underlayingValue, volaValue);

            if (TransactionType.SELL.equals(transaction.getType())) {
                System.out.print(RoundUtil.getBigDecimal(StockOptionUtil.getDummyBid(bsValue)));
            } else if (TransactionType.BUY.equals(transaction.getType())) {
                System.out.print(RoundUtil.getBigDecimal(StockOptionUtil.getDummyAsk(bsValue)));
            }

            if (Math.abs(underlayingTick.getDateTime().getTime() - transaction.getDateTime().getTime()) > 300000) System.out.print(",underlaying invalid!");
            if (Math.abs(volaTick.getDateTime().getTime() - transaction.getDateTime().getTime()) > 300000) System.out.print(",vola invalid!");
            if (Math.abs(optionTick.getDateTime().getTime() - transaction.getDateTime().getTime()) > 300000) System.out.print(",option invalid!");

            System.out.println();

        }
    }

    @SuppressWarnings("unchecked")
    protected void handleVerifyTicks() throws Exception {

        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd kk:mm");
        EPServiceProvider cep = EsperService.getEPServiceInstance();

        CsvTickReader underlayingReader = new CsvTickReader("CH0008616382");
        Security underlaying = getSecurityDao().findByISIN("CH0008616382");
        Tick tick;
        List<Tick> underlayingTicks = new ArrayList<Tick>();
        while ((tick = underlayingReader.readTick()) != null) {
            tick.setSecurity(underlaying);
            underlayingTicks.add(tick);
        }

        CsvTickReader volaReader = new CsvTickReader("CH0019900841");
        Security vola = getSecurityDao().findByISIN("CH0019900841");
        List<Tick> volaTicks = new ArrayList<Tick>();
        while ((tick = volaReader.readTick()) != null) {
            tick.setSecurity(vola);
            volaTicks.add(tick);
        }

        File directory = new File("results/tickdata/current/");
        File[] files = directory.listFiles();
        List<Tick> optionTicks = new ArrayList<Tick>();
        for (File file : files) {

            if (file.getName().startsWith("CH")) continue;

            String isin = file.getName().split("\\.")[0];
            CsvTickReader optionReader = new CsvTickReader(isin);
            Security option = getSecurityDao().findByISIN(isin);
            while ((tick = optionReader.readTick()) != null) {
                tick.setSecurity(option);
                optionTicks.add(tick);
            }
        }

        final Date startDate = format.parse("2010.03.30 15:00");
        CollectionUtils.filter(optionTicks, new Predicate() {
            public boolean evaluate(Object obj) {
                return ((Tick)obj).getDateTime().compareTo(startDate) > 0;
            }});

        Collections.sort(optionTicks, new Comparator() {
            public int compare(Object arg0, Object arg1) {
                return ((Tick)arg0).getDateTime().compareTo(((Tick)arg1).getDateTime());
            }});

        for (Tick optionTick : optionTicks) {

            cep.getEPRuntime().sendEvent(new CurrentTimeEvent(optionTick.getDateTime().getTime()));

            StockOption stockOption = (StockOption)optionTick.getSecurity();

            Date testDate = optionTick.getDateTime();

            Tick underlayingTick = selectTickByDate(underlayingTicks, testDate);
            Tick volaTick = selectTickByDate(volaTicks, testDate);

            if (volaTick == null) continue;

            double sabrEst = StockOptionUtil.getOptionPriceSabr(stockOption, underlayingTick.getLast().doubleValue(), volaTick.getLast().doubleValue() / 100.0);
            double bsEst = StockOptionUtil.getOptionPriceBS(stockOption, underlayingTick.getLast().doubleValue(), volaTick.getLast().doubleValue() / 100.0);
            double currentValue = (optionTick.getAsk().doubleValue() + optionTick.getBid().doubleValue()) / 2.0;


            System.out.print(format.format(optionTick.getDateTime()) + ",");
            System.out.print(stockOption.getIsin() + ",");
            System.out.print(",strike=" + stockOption.getStrike());
            System.out.print(",exp=" + format.format(stockOption.getExpiration()));
            System.out.print(",ul=" + underlayingTick.getCurrentValue());
            System.out.print(",vola=" + volaTick.getCurrentValue());
            System.out.print(",cv=" + RoundUtil.getBigDecimal(currentValue));
            System.out.print(",sabr=" + RoundUtil.getBigDecimal(sabrEst));
            System.out.print(",bs=" + RoundUtil.getBigDecimal(bsEst));

            System.out.print(",abs=" + RoundUtil.getBigDecimal(currentValue - sabrEst));
            System.out.println(",rel=" + RoundUtil.getBigDecimal((currentValue / sabrEst - 1) * 100) + "%");


        }
    }

    private Tick selectTickByDate(final List<Tick> list, final Date date) {

        List<Tick> truncatedList = new ArrayList<Tick>();
        CollectionUtils.select(list, new Predicate() {
            public boolean evaluate(Object obj) {

                Tick tick = (Tick)obj;
                return tick.getDateTime().compareTo(date) < 0 ;
            }}, truncatedList);

        if (truncatedList.size() == 0) return null;

        return truncatedList.get(truncatedList.size() - 1);
    }

    private List<Tick> getOptionTicks(String isin) throws SuperCSVException, IOException {

        List<Tick> optionTicks = this.optionMap.get(isin);

        if (optionTicks == null) {

            CsvTickReader optionReader = new CsvTickReader(isin);
            optionTicks = new ArrayList<Tick>();
            Tick tick;
            while ((tick = optionReader.readTick()) != null) {
                optionTicks.add(tick);
            }
            this.optionMap.put(isin, optionTicks);
        }

        return optionTicks;
    }

    protected void handleCacheTest() {

        Transaction transaction = getTransactionDao().load(10136);
        transaction.setQuantity(10);
        getTransactionDao().update(transaction);
    }
}
