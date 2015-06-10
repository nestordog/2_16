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
package ch.algotrader.adapter.ftx;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.adapter.fix.FixApplicationException;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.security.Stock;
import ch.algotrader.entity.security.StockImpl;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Currency;
import quickfix.field.SecurityType;
import quickfix.field.Symbol;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

public class TestFTXSymbologyResolver {

    private Forex forex;
    private FTXSymbologyResolver symbologyResolver;

    @Before
    public void setup() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        this.forex = new ForexImpl();
        this.forex.setSymbol("EUR.USD");
        this.forex.setLmaxid("4001");
        this.forex.setBaseCurrency(Currency.EUR);
        this.forex.setSecurityFamily(family);

        this.symbologyResolver = new FTXSymbologyResolver();
    }

    @Test
    public void testNewOrderForex() throws Exception {

        NewOrderSingle message = new NewOrderSingle();

        this.symbologyResolver.resolve(message, this.forex, Broker.FTX);

        Assert.assertEquals(new Symbol("EUR/USD"), message.getSymbol());
        Assert.assertEquals(new SecurityType(SecurityType.FOREIGN_EXCHANGE_CONTRACT), message.getSecurityType());
    }

    @Test(expected = FixApplicationException.class)
    public void testNewOrderStock() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Stock stock = new StockImpl();
        stock.setSecurityFamily(family);
        stock.setSymbol("APPL");
        NewOrderSingle message = new NewOrderSingle();

        this.symbologyResolver.resolve(message, stock, Broker.FTX);
    }

    @Test
    public void testModifyOrderForex() throws Exception {

        OrderCancelReplaceRequest message = new OrderCancelReplaceRequest();

        this.symbologyResolver.resolve(message, this.forex, Broker.FTX);

        Assert.assertEquals(new Symbol("EUR/USD"), message.getSymbol());
        Assert.assertEquals(new SecurityType(SecurityType.FOREIGN_EXCHANGE_CONTRACT), message.getSecurityType());
    }

    @Test
    public void testCancelOrderForex() throws Exception {

        OrderCancelRequest message = new OrderCancelRequest();

        this.symbologyResolver.resolve(message, this.forex, Broker.FTX);

        Assert.assertEquals(new Symbol("EUR/USD"), message.getSymbol());
        Assert.assertEquals(new SecurityType(SecurityType.FOREIGN_EXCHANGE_CONTRACT), message.getSecurityType());
    }

}
