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
package ch.algotrader.vo.client;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.CommonConfigBuilder;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.AccountImpl;
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

/**
* Unit tests for {@link TransactionVOProducer}.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*
* @version $Revision$ $Date$
*/
public class TransactionVOProducerTest {

    private CommonConfig commonConfig;

    private TransactionVOProducer instance;

    @Before
    public void setup() throws Exception {

        this.commonConfig = CommonConfigBuilder.create().build();

        this.instance = new TransactionVOProducer(this.commonConfig);
    }

    @Test
    public void testConvertWithoutSecurity() {

        Strategy strategy = new StrategyImpl();

        strategy.setName("Strategy");

        Transaction transaction = new TransactionImpl();
        transaction.setUuid(UUID.randomUUID().toString());
        transaction.setId(111);
        transaction.setDateTime(new Date());
        transaction.setQuantity(222);
        transaction.setType(TransactionType.CREDIT);
        transaction.setCurrency(Currency.AUD);
        transaction.setPrice(new BigDecimal(333.33));
        transaction.setStrategy(strategy);

        TransactionVO transactionVO = this.instance.convert(transaction);

        Assert.assertNotNull(transactionVO);

        Assert.assertEquals(111, transactionVO.getId());
        Assert.assertEquals(transaction.getDateTime(), transactionVO.getDateTime());
        Assert.assertEquals(222, transactionVO.getQuantity());
        Assert.assertEquals(TransactionType.CREDIT, transactionVO.getType());
        Assert.assertEquals(Currency.AUD, transactionVO.getCurrency());
        Assert.assertEquals(transaction.getPrice().setScale(this.commonConfig.getPortfolioDigits(), BigDecimal.ROUND_HALF_UP), transactionVO.getPrice());
        Assert.assertSame(strategy.toString(), transactionVO.getStrategy());

        Assert.assertNull(transactionVO.getAccount());
    }

    @Test
    public void testConvertWithSecurity() {

        SecurityFamily family = new SecurityFamilyImpl();

        family.setName("Forex1");
        family.setTickSizePattern("0<0.1");
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();

        forex.setSymbol("EUR.USD");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        Strategy strategy = new StrategyImpl();

        strategy.setName("Strategy");

        Transaction transaction = new TransactionImpl();
        transaction.setUuid(UUID.randomUUID().toString());
        transaction.setId(111);
        transaction.setDateTime(new Date());
        transaction.setQuantity(222);
        transaction.setType(TransactionType.BUY);
        transaction.setCurrency(Currency.AUD);
        transaction.setPrice(new BigDecimal(333.33));
        transaction.setStrategy(strategy);
        transaction.setSecurity(forex);

        TransactionVO transactionVO = this.instance.convert(transaction);

        Assert.assertNotNull(transactionVO);

        Assert.assertEquals(111, transactionVO.getId());
        Assert.assertEquals(transaction.getDateTime(), transactionVO.getDateTime());
        Assert.assertEquals(222, transactionVO.getQuantity());
        Assert.assertEquals(TransactionType.BUY, transactionVO.getType());
        Assert.assertEquals(Currency.AUD, transactionVO.getCurrency());
        Assert.assertEquals(transaction.getPrice().setScale(family.getScale(), BigDecimal.ROUND_HALF_UP), transactionVO.getPrice());
        Assert.assertEquals(strategy.toString(), transactionVO.getStrategy());

        Assert.assertNull(transactionVO.getAccount());
    }

    @Test
    public void testConvertWithSecurityAndAccount() {

        SecurityFamily family = new SecurityFamilyImpl();

        family.setName("Forex1");
        family.setTickSizePattern("0<0.1");
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();

        forex.setSymbol("EUR.USD");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        Strategy strategy = new StrategyImpl();

        strategy.setName("Strategy");

        Account account = new AccountImpl();

        account.setName("Account");

        Transaction transaction = new TransactionImpl();
        transaction.setUuid(UUID.randomUUID().toString());
        transaction.setId(111);
        transaction.setDateTime(new Date());
        transaction.setQuantity(222);
        transaction.setType(TransactionType.BUY);
        transaction.setCurrency(Currency.AUD);
        transaction.setPrice(new BigDecimal(333.33));
        transaction.setStrategy(strategy);
        transaction.setSecurity(forex);
        transaction.setAccount(account);

        TransactionVO transactionVO = this.instance.convert(transaction);

        Assert.assertNotNull(transactionVO);

        Assert.assertEquals(111, transactionVO.getId());
        Assert.assertEquals(transaction.getDateTime(), transactionVO.getDateTime());
        Assert.assertEquals(222, transactionVO.getQuantity());
        Assert.assertEquals(TransactionType.BUY, transactionVO.getType());
        Assert.assertEquals(Currency.AUD, transactionVO.getCurrency());
        Assert.assertEquals(transaction.getPrice().setScale(family.getScale(), BigDecimal.ROUND_HALF_UP), transactionVO.getPrice());
        Assert.assertEquals(strategy.toString(), transactionVO.getStrategy());
        Assert.assertEquals(account.toString(), transactionVO.getAccount());
    }

}
