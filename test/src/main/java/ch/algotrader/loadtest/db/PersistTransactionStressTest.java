/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.loadtest.db;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.TransactionType;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.TransactionService;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class PersistTransactionStressTest {

    public static void main(String... args) throws Exception {
        final ServiceLocator serviceLocator = ServiceLocator.instance();
        serviceLocator.init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);
        final TransactionService transactionService = serviceLocator.getService("transactionService", TransactionService.class);
        final LookupService lookupService = serviceLocator.getService("lookupService", LookupService.class);

        final Currency[] currencies = new Currency[] {Currency.CHF, Currency.USD, Currency.EUR, Currency.GBP, Currency.JPY};
        final List<Forex> forexList = new ArrayList<>();

        for (Currency base: currencies) {
            for (Currency transact: currencies) {
                if (!base.equals(transact)) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("baseCurrency", base);
                    params.put("transactionCurrency", transact);
                    List<?> list = lookupService.get(
                            "from ForexImpl f join f.securityFamily sf where f.baseCurrency = :baseCurrency and sf.currency = :transactionCurrency", params);
                    if (!list.isEmpty()) {
                        Object[] objs = (Object[]) list.get(0);
                        Forex forex = (Forex) objs[0];
                        forexList.add(forex);
                    }
                }
            }
        }

        System.out.println(forexList);

        final AtomicLong count = new AtomicLong();

        int n = 10;

        final ExecutorService executorService = Executors.newFixedThreadPool(n);
        final Callable<Boolean> task = () -> {
            for (int i = 0; i < 10; i++) {
                for (Forex forex: forexList) {

                    transactionService.createTransaction(
                            forex.getId(),
                            "LOAD_TEST",
                            Long.toString(System.currentTimeMillis()) + "-" + Long.toString(count.incrementAndGet()),
                            new Date(),
                            1, BigDecimal.ONE, null, null, null, forex.getTransactionCurrency(),
                            TransactionType.BUY,
                            "LOAD_TEST",
                            "Load testing");
                    if (Thread.currentThread().isInterrupted()) {
                        System.out.println("exiting");
                        return Boolean.FALSE;
                    }
                }
                Thread.sleep(25);
            }
            return Boolean.TRUE;
        };

        try {
            Queue<Future<?>> queue = new LinkedList<>();
            for (int i = 0; i < n; i++) {
                Future<?> future = executorService.submit(task);
                queue.add(future);
            }

            while (!queue.isEmpty()) {
                Future<?> future = queue.remove();
                future.get();
            }
        } finally {
            executorService.shutdown();
            try {
                executorService.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException ignore) {
            }
        }
    }

}
