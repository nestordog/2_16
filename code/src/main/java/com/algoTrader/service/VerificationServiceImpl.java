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

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.supercsv.exception.SuperCSVException;

import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.Tick;
import com.algoTrader.entity.Transaction;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.stockOption.StockOptionUtil;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.util.io.CsvTickReader;
import com.espertech.esper.client.time.CurrentTimeEvent;

public class VerificationServiceImpl extends VerificationServiceBase {

    private static final double MILLISECONDS_PER_YEAR = 31536000000l;

    private Map<String, List<Tick>> optionMap = new HashMap<String, List<Tick>>();

    protected void handleVerifyTransactions() throws Exception {

        getRuleService().initServiceProvider(StrategyImpl.BASE);

        Collection<Transaction> transactions = getTransactionDao().findAllTrades();

        for (Transaction transaction : transactions) {

            Date date = transaction.getDateTime();
            getRuleService().sendEvent(StrategyImpl.BASE, new CurrentTimeEvent(date.getTime()));

            if (!(transaction.getType().equals(TransactionType.BUY) || transaction.getType().equals(TransactionType.SELL)))
                continue;

            StockOption stockOption = (StockOption)transaction.getSecurity();

            Tick optionTick = getTickDao().findByDateAndSecurity(date, stockOption.getId());
            Tick underlayingTick = getTickDao().findByDateAndSecurity(date, stockOption.getUnderlaying().getId());
            Tick volaTick = getTickDao().findByDateAndSecurity(date, stockOption.getUnderlaying().getVolatility().getId());

            if (optionTick == null || underlayingTick == null | volaTick == null || underlayingTick.getLast() == null) {
                continue;
            }

            // date / transactionType / stockOptionType / strikeDistance / years
            System.out.print(date + ",");
            System.out.print(transaction.getType().toString() + ",");
            System.out.print(stockOption.getType() + ",");
            System.out.print((stockOption.getStrike().doubleValue() - underlayingTick.getLast().doubleValue()) + ",");
            System.out.print(((stockOption.getExpiration().getTime() - date.getTime()) / MILLISECONDS_PER_YEAR) + ",");

            // real price of option
            System.out.print(optionTick.getCurrentValue() + ",");

            // sabr
            double underlayingValue = underlayingTick.getLast().doubleValue();
            double volaValue = volaTick.getLast().doubleValue() / 100.0;
            double sabrValue = StockOptionUtil.getOptionPriceSabr(stockOption, underlayingValue, volaValue);
            System.out.print(RoundUtil.getBigDecimal(sabrValue) + ",");

            System.out.println();
        }
    }

    protected void handleVerifyTicks() throws Exception {

        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd kk:mm");
        getRuleService().initServiceProvider(StrategyImpl.BASE);

        CsvTickReader underlayingReader = new CsvTickReader("CH0008616382");
        Security underlaying = getSecurityDao().findByIsin("CH0008616382");
        Tick tick;
        List<Tick> underlayingTicks = new ArrayList<Tick>();
        while ((tick = underlayingReader.readTick()) != null) {
            tick.setSecurity(underlaying);
            underlayingTicks.add(tick);
        }

        CsvTickReader volaReader = new CsvTickReader("CH0019900841");
        Security vola = getSecurityDao().findByIsin("CH0019900841");
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
            Security option = getSecurityDao().findByIsin(isin);
            while ((tick = optionReader.readTick()) != null) {
                tick.setSecurity(option);
                optionTicks.add(tick);
            }
        }

        final Date startDate = format.parse("2010.03.30 15:00");
        CollectionUtils.filter(optionTicks, new Predicate<Tick>() {
            public boolean evaluate(Tick tick) {
                return tick.getDateTime().compareTo(startDate) > 0;
            }});

        Collections.sort(optionTicks, new Comparator<Tick>() {
            public int compare(Tick tick1, Tick tick2) {
                return (tick1).getDateTime().compareTo((tick2).getDateTime());
            }});

        for (Tick optionTick : optionTicks) {

            getRuleService().sendEvent(StrategyImpl.BASE, new CurrentTimeEvent(optionTick.getDateTime().getTime()));

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
        CollectionUtils.select(list, new Predicate<Tick>() {
            public boolean evaluate(Tick tick) {

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
}
