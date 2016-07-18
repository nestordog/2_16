/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.dao;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.TransactionImpl;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.TransactionType;
import ch.algotrader.hibernate.InMemoryDBTest;

/**
* Unit tests for {@link ch.algotrader.entity.Transaction}.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*
* @version $Revision$ $Date$
*/
public class TransactionDaoTest extends InMemoryDBTest {

    private TransactionDao dao;

    private SecurityFamily family1;

    private Forex forex1;

    private Strategy strategy1;

    private SecurityFamily family2;

    private Forex forex2;

    private Strategy strategy2;

    public TransactionDaoTest() throws IOException {

        super();
    }

    @Override
    @Before
    public void setup() throws Exception {

        super.setup();

        this.dao = new TransactionDaoImpl(this.sessionFactory);

        this.family1 = new SecurityFamilyImpl();
        this.family1.setName("Forex1");
        this.family1.setTickSizePattern("0<0.1");
        this.family1.setCurrency(Currency.USD);

        this.forex1 = new ForexImpl();
        this.forex1.setSymbol("EUR.USD");
        this.forex1.setBaseCurrency(Currency.EUR);
        this.forex1.setSecurityFamily(this.family1);

        this.strategy1 = new StrategyImpl();
        this.strategy1.setName("Strategy1");

        this.family2 = new SecurityFamilyImpl();
        this.family2.setName("Forex2");
        this.family2.setTickSizePattern("0<0.1");
        this.family2.setCurrency(Currency.GBP);

        this.forex2 = new ForexImpl();
        this.forex2.setSymbol("EUR.GBP");
        this.forex2.setBaseCurrency(Currency.EUR);
        this.forex2.setSecurityFamily(this.family2);

        this.strategy2 = new StrategyImpl();
        this.strategy2.setName("Strategy2");
    }

    @Test
    public void testFindByStrategy() {

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        Transaction transaction1 = new TransactionImpl();
        transaction1.setUuid(UUID.randomUUID().toString());
        transaction1.setSecurity(this.forex1);
        transaction1.setQuantity(222);
        transaction1.setDateTime(new Date());
        transaction1.setPrice(new BigDecimal(111));
        transaction1.setCurrency(Currency.NZD);
        transaction1.setType(TransactionType.BUY);
        transaction1.setStrategy(this.strategy1);

        this.session.save(transaction1);
        this.session.save(this.family2);
        this.session.save(this.forex2);
        this.session.save(this.strategy2);

        Transaction transaction2 = new TransactionImpl();
        transaction2.setUuid(UUID.randomUUID().toString());
        transaction2.setSecurity(this.forex2);
        transaction2.setQuantity(222);
        transaction2.setDateTime(new Date());
        transaction2.setPrice(new BigDecimal(111));
        transaction2.setCurrency(Currency.NZD);
        transaction2.setType(TransactionType.BUY);
        transaction2.setStrategy(this.strategy1);

        this.session.save(transaction2);
        this.session.flush();

        List<Transaction> transactions1 = this.dao.findByStrategy("Dummy");

        Assert.assertEquals(0, transactions1.size());

        List<Transaction> transactions2 = this.dao.findByStrategy("Strategy1");

        Assert.assertEquals(2, transactions2.size());

        Assert.assertEquals(222, transactions2.get(0).getQuantity());
        Assert.assertEquals(transaction1.getDateTime(), transactions2.get(0).getDateTime());
        Assert.assertEquals(new BigDecimal(111), transactions2.get(0).getPrice());
        Assert.assertEquals(Currency.NZD, transactions2.get(0).getCurrency());
        Assert.assertEquals(TransactionType.BUY, transactions2.get(0).getType());
        Assert.assertSame(this.forex1, transactions2.get(0).getSecurity());
        Assert.assertSame(this.family1, transactions2.get(0).getSecurity().getSecurityFamily());
        Assert.assertSame(this.strategy1, transactions2.get(0).getStrategy());

        Assert.assertEquals(222, transactions2.get(1).getQuantity());
        Assert.assertEquals(transaction2.getDateTime(), transactions2.get(1).getDateTime());
        Assert.assertEquals(new BigDecimal(111), transactions2.get(1).getPrice());
        Assert.assertEquals(Currency.NZD, transactions2.get(1).getCurrency());
        Assert.assertEquals(TransactionType.BUY, transactions2.get(1).getType());
        Assert.assertSame(this.forex2, transactions2.get(1).getSecurity());
        Assert.assertSame(this.family2, transactions2.get(1).getSecurity().getSecurityFamily());
        Assert.assertSame(this.strategy1, transactions2.get(1).getStrategy());
    }

    @Test
    public void testFindByExtId() {

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        Assert.assertNull(this.dao.findByExtId("blah"));

        Transaction t1 = new TransactionImpl();
        t1.setUuid(UUID.randomUUID().toString());
        t1.setSecurity(this.forex1);
        t1.setQuantity(222);
        t1.setDateTime(new Date());
        t1.setPrice(new BigDecimal(111));
        t1.setCurrency(Currency.NZD);
        t1.setType(TransactionType.BUY);
        t1.setStrategy(this.strategy1);
        t1.setExtId("blah");

        this.session.save(t1);
        this.session.flush();

        Transaction t2 = this.dao.findByExtId("blah");
        Assert.assertEquals(t1, t2);
    }

    @Test
    public void testFindDailyTransactionsDesc() {

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        Transaction transaction1 = new TransactionImpl();
        transaction1.setUuid(UUID.randomUUID().toString());
        transaction1.setSecurity(this.forex1);
        transaction1.setQuantity(222);
        transaction1.setDateTime(new Date());
        transaction1.setPrice(new BigDecimal(111));
        transaction1.setCurrency(Currency.INR);
        transaction1.setType(TransactionType.CREDIT);
        transaction1.setStrategy(this.strategy1);

        this.session.save(this.family2);
        this.session.save(this.forex2);
        this.session.save(this.strategy2);

        Transaction transaction2 = new TransactionImpl();
        transaction2.setUuid(UUID.randomUUID().toString());
        transaction2.setSecurity(this.forex2);
        transaction2.setQuantity(222);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        transaction2.setDateTime(calendar.getTime());
        transaction2.setPrice(new BigDecimal(111));
        transaction2.setCurrency(Currency.NZD);
        transaction2.setType(TransactionType.BUY);
        transaction2.setStrategy(this.strategy2);

        List<Transaction> transactionVOs1 = this.dao.findDailyTransactions();

        Assert.assertEquals(0, transactionVOs1.size());

        this.session.save(transaction1);
        this.session.save(transaction2);
        this.session.flush();

        List<Transaction> transactionVOs2 = this.dao.findDailyTransactions();

        Assert.assertEquals(1, transactionVOs2.size());

        Transaction transactionVO1 = transactionVOs2.get(0);
        Assert.assertEquals(222, transactionVO1.getQuantity());
        Assert.assertEquals(new BigDecimal(111), transactionVO1.getPrice());
        Assert.assertEquals(Currency.INR, transactionVO1.getCurrency());
        Assert.assertEquals(TransactionType.CREDIT, transactionVO1.getType());
        Assert.assertEquals(this.strategy1, transactionVO1.getStrategy());

    }

    @Test
    public void testFindDailyTransactionsByStrategyDesc() {

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        Transaction transaction1 = new TransactionImpl();
        transaction1.setUuid(UUID.randomUUID().toString());
        transaction1.setSecurity(this.forex1);
        transaction1.setQuantity(222);
        transaction1.setDateTime(new Date());
        transaction1.setPrice(new BigDecimal(111));
        transaction1.setCurrency(Currency.INR);
        transaction1.setType(TransactionType.CREDIT);
        transaction1.setStrategy(this.strategy1);

        this.session.save(transaction1);
        this.session.save(this.family2);
        this.session.save(this.forex2);
        this.session.save(this.strategy2);

        Transaction transaction2 = new TransactionImpl();
        transaction2.setUuid(UUID.randomUUID().toString());
        transaction2.setSecurity(this.forex2);
        transaction2.setQuantity(222);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        transaction2.setDateTime(calendar.getTime());
        transaction2.setPrice(new BigDecimal(111));
        transaction2.setCurrency(Currency.NZD);
        transaction2.setType(TransactionType.BUY);
        transaction2.setStrategy(this.strategy1);

        this.session.save(transaction2);
        this.session.flush();

        List<Transaction> transactionVOs1 = this.dao.findDailyTransactionsByStrategy("Dummy");

        Assert.assertEquals(0, transactionVOs1.size());

        List<Transaction> transactionVOs2 = this.dao.findDailyTransactionsByStrategy("Strategy1");

        Assert.assertEquals(1, transactionVOs2.size());

        Assert.assertEquals(222, transactionVOs2.get(0).getQuantity());
        Assert.assertEquals(new BigDecimal(111), transactionVOs2.get(0).getPrice());
        Assert.assertEquals(Currency.INR, transactionVOs2.get(0).getCurrency());
        Assert.assertEquals(TransactionType.CREDIT, transactionVOs2.get(0).getType());
        Assert.assertEquals(this.strategy1, transactionVOs2.get(0).getStrategy());
    }

    @Test
    public void testFindAllTradesInclSecurity() {

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        Transaction transaction1 = new TransactionImpl();
        transaction1.setUuid(UUID.randomUUID().toString());
        transaction1.setSecurity(this.forex1);
        transaction1.setQuantity(222);
        transaction1.setDateTime(new Date(1));
        transaction1.setPrice(new BigDecimal(111));
        transaction1.setCurrency(Currency.INR);
        transaction1.setType(TransactionType.SELL);
        transaction1.setStrategy(this.strategy1);

        this.session.save(this.family2);
        this.session.save(this.forex2);
        this.session.save(this.strategy2);

        Transaction transaction2 = new TransactionImpl();
        transaction2.setUuid(UUID.randomUUID().toString());
        transaction2.setSecurity(this.forex2);
        transaction2.setQuantity(222);
        transaction2.setDateTime(new Date(2));
        transaction2.setPrice(new BigDecimal(111));
        transaction2.setCurrency(Currency.NZD);
        transaction2.setType(TransactionType.BUY);
        transaction2.setStrategy(this.strategy2);

        Transaction transaction3 = new TransactionImpl();
        transaction3.setUuid(UUID.randomUUID().toString());
        transaction3.setSecurity(this.forex2);
        transaction3.setQuantity(222);
        transaction3.setDateTime(new Date(3));
        transaction3.setPrice(new BigDecimal(111));
        transaction3.setCurrency(Currency.NZD);
        transaction3.setType(TransactionType.EXPIRATION);
        transaction3.setStrategy(this.strategy1);

        Transaction transaction4 = new TransactionImpl();
        transaction4.setUuid(UUID.randomUUID().toString());
        transaction4.setSecurity(this.forex2);
        transaction4.setQuantity(222);
        transaction4.setDateTime(new Date(4));
        transaction4.setPrice(new BigDecimal(111));
        transaction4.setCurrency(Currency.NZD);
        transaction4.setType(TransactionType.TRANSFER);
        transaction4.setStrategy(this.strategy2);

        List<Transaction> transactions1 = this.dao.findAllTradesInclSecurity();

        Assert.assertEquals(0, transactions1.size());

        this.session.save(transaction1);
        this.session.save(transaction2);
        this.session.save(transaction3);
        this.session.save(transaction4);
        this.session.flush();

        List<Transaction> transactions2 = this.dao.findAllTradesInclSecurity();

        Assert.assertEquals(4, transactions2.size());

        Assert.assertEquals(222, transactions2.get(0).getQuantity());
        Assert.assertEquals(new BigDecimal(111), transactions2.get(0).getPrice());
        Assert.assertEquals(Currency.INR, transactions2.get(0).getCurrency());
        Assert.assertEquals(TransactionType.SELL, transactions2.get(0).getType());
        Assert.assertEquals(this.strategy1, transactions2.get(0).getStrategy());

        Assert.assertEquals(222, transactions2.get(1).getQuantity());
        Assert.assertEquals(new BigDecimal(111), transactions2.get(1).getPrice());
        Assert.assertEquals(Currency.NZD, transactions2.get(1).getCurrency());
        Assert.assertEquals(TransactionType.BUY, transactions2.get(1).getType());
        Assert.assertEquals(this.strategy2, transactions2.get(1).getStrategy());

        Assert.assertEquals(222, transactions2.get(2).getQuantity());
        Assert.assertEquals(new BigDecimal(111), transactions2.get(2).getPrice());
        Assert.assertEquals(Currency.NZD, transactions2.get(2).getCurrency());
        Assert.assertEquals(TransactionType.EXPIRATION, transactions2.get(2).getType());
        Assert.assertEquals(this.strategy1, transactions2.get(2).getStrategy());

        Assert.assertEquals(222, transactions2.get(3).getQuantity());
        Assert.assertEquals(new BigDecimal(111), transactions2.get(3).getPrice());
        Assert.assertEquals(Currency.NZD, transactions2.get(3).getCurrency());
        Assert.assertEquals(TransactionType.TRANSFER, transactions2.get(3).getType());
        Assert.assertEquals(this.strategy2, transactions2.get(3).getStrategy());
    }

    @Test
    public void testFindCashflowsByStrategyAndMinDate() {

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        Transaction transaction1 = new TransactionImpl();
        transaction1.setUuid(UUID.randomUUID().toString());
        transaction1.setSecurity(this.forex1);
        transaction1.setQuantity(222);
        transaction1.setDateTime(new Date());
        transaction1.setPrice(new BigDecimal(111));
        transaction1.setCurrency(Currency.INR);
        transaction1.setType(TransactionType.DEBIT);
        transaction1.setStrategy(this.strategy1);

        this.session.save(this.family2);
        this.session.save(this.forex2);
        this.session.save(this.strategy2);

        Transaction transaction2 = new TransactionImpl();
        transaction2.setUuid(UUID.randomUUID().toString());
        transaction2.setSecurity(this.forex2);
        transaction2.setQuantity(222);
        transaction2.setDateTime(new Date());
        transaction2.setPrice(new BigDecimal(111));
        transaction2.setCurrency(Currency.NZD);
        transaction2.setType(TransactionType.INTREST_RECEIVED);
        transaction2.setStrategy(this.strategy1);

        this.session.save(transaction1);
        this.session.save(transaction2);
        this.session.flush();

        List<Transaction> transactions1 = this.dao.findCashflowsByStrategyAndMinDate("Dummy", new Date());

        Assert.assertEquals(0, transactions1.size());

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        List<Transaction> transactions2 = this.dao.findCashflowsByStrategyAndMinDate("Strategy1", calendar.getTime());

        Assert.assertEquals(2, transactions2.size());

        Assert.assertEquals(222, transactions2.get(0).getQuantity());
        Assert.assertEquals(new BigDecimal(111), transactions2.get(0).getPrice());
        Assert.assertEquals(Currency.INR, transactions2.get(0).getCurrency());
        Assert.assertEquals(TransactionType.DEBIT, transactions2.get(0).getType());
        Assert.assertEquals(this.strategy1, transactions2.get(0).getStrategy());

        Assert.assertEquals(222, transactions2.get(1).getQuantity());
        Assert.assertEquals(new BigDecimal(111), transactions2.get(1).getPrice());
        Assert.assertEquals(Currency.NZD, transactions2.get(1).getCurrency());
        Assert.assertEquals(TransactionType.INTREST_RECEIVED, transactions2.get(1).getType());
        Assert.assertEquals(this.strategy1, transactions2.get(1).getStrategy());
    }

    @Test
    public void testFindByMaxDate() {

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        Transaction transaction1 = new TransactionImpl();
        transaction1.setUuid(UUID.randomUUID().toString());
        transaction1.setSecurity(this.forex1);
        transaction1.setQuantity(222);
        transaction1.setDateTime(new Date());
        transaction1.setPrice(new BigDecimal(111));
        transaction1.setCurrency(Currency.INR);
        transaction1.setType(TransactionType.SELL);
        transaction1.setStrategy(this.strategy1);

        this.session.save(this.family2);
        this.session.save(this.forex2);
        this.session.save(this.strategy2);

        Transaction transaction2 = new TransactionImpl();
        transaction2.setUuid(UUID.randomUUID().toString());
        transaction2.setSecurity(this.forex2);
        transaction2.setQuantity(222);
        transaction2.setDateTime(new Date());
        transaction2.setPrice(new BigDecimal(111));
        transaction2.setCurrency(Currency.NZD);
        transaction2.setType(TransactionType.BUY);
        transaction2.setStrategy(this.strategy1);

        this.session.save(transaction1);
        this.session.save(transaction2);
        this.session.flush();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        List<Transaction> transactions1 = this.dao.findByMaxDate(calendar.getTime());

        Assert.assertEquals(0, transactions1.size());

        List<Transaction> transactions2 = this.dao.findByMaxDate(new Date());

        Assert.assertEquals(2, transactions2.size());

        Transaction transaction3 = transactions2.get(0);
        Assert.assertEquals(222, transaction3.getQuantity());
        Assert.assertEquals(new BigDecimal(111), transaction3.getPrice());
        Assert.assertEquals(Currency.INR, transaction3.getCurrency());
        Assert.assertEquals(TransactionType.SELL, transaction3.getType());
        Assert.assertEquals(this.strategy1, transaction3.getStrategy());

        Transaction transaction4 = transactions2.get(1);
        Assert.assertEquals(222, transaction4.getQuantity());
        Assert.assertEquals(new BigDecimal(111), transaction4.getPrice());
        Assert.assertEquals(Currency.NZD, transaction4.getCurrency());
        Assert.assertEquals(TransactionType.BUY, transaction4.getType());
        Assert.assertEquals(this.strategy1, transaction4.getStrategy());
    }

    @Test
    public void testFindByStrategyAndMaxDate() {

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        Transaction transaction1 = new TransactionImpl();
        transaction1.setUuid(UUID.randomUUID().toString());
        transaction1.setSecurity(this.forex1);
        transaction1.setQuantity(222);
        transaction1.setDateTime(new Date());
        transaction1.setPrice(new BigDecimal(111));
        transaction1.setCurrency(Currency.INR);
        transaction1.setType(TransactionType.SELL);
        transaction1.setStrategy(this.strategy1);

        this.session.save(this.family2);
        this.session.save(this.forex2);
        this.session.save(this.strategy2);

        Transaction transaction2 = new TransactionImpl();
        transaction2.setUuid(UUID.randomUUID().toString());
        transaction2.setSecurity(this.forex2);
        transaction2.setQuantity(222);
        transaction2.setDateTime(new Date());
        transaction2.setPrice(new BigDecimal(111));
        transaction2.setCurrency(Currency.NZD);
        transaction2.setType(TransactionType.BUY);
        transaction2.setStrategy(this.strategy1);

        this.session.save(transaction1);
        this.session.save(transaction2);
        this.session.flush();

        List<Transaction> transactions1 = this.dao.findByStrategyAndMaxDate("Dummy", new Date());

        Assert.assertEquals(0, transactions1.size());

        List<Transaction> transactions2 = this.dao.findByStrategyAndMaxDate("Strategy1", new Date());

        Assert.assertEquals(2, transactions2.size());

        Assert.assertEquals(222, transactions2.get(0).getQuantity());
        Assert.assertEquals(new BigDecimal(111), transactions2.get(0).getPrice());
        Assert.assertEquals(Currency.INR, transactions2.get(0).getCurrency());
        Assert.assertEquals(TransactionType.SELL, transactions2.get(0).getType());
        Assert.assertEquals(this.strategy1, transactions2.get(0).getStrategy());

        Assert.assertEquals(222, transactions2.get(1).getQuantity());
        Assert.assertEquals(new BigDecimal(111), transactions2.get(1).getPrice());
        Assert.assertEquals(Currency.NZD, transactions2.get(1).getCurrency());
        Assert.assertEquals(TransactionType.BUY, transactions2.get(1).getType());
        Assert.assertEquals(this.strategy1, transactions2.get(1).getStrategy());
    }

}
