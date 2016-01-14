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
package ch.algotrader.adapter.ftx;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.adapter.BrokerAdapterException;
import ch.algotrader.adapter.fxcm.FXCTickerIdGenerator;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.security.Stock;
import ch.algotrader.entity.security.StockImpl;
import ch.algotrader.enumeration.Currency;
import quickfix.field.QuoteRequestType;
import quickfix.field.Symbol;
import quickfix.fix44.QuoteRequest;

public class TestFTXMarketDataRequestFactory {

    private FTXFixMarketDataRequestFactory requestFactory;

    @Before
    public void setup() throws Exception {

        this.requestFactory = new FTXFixMarketDataRequestFactory(new FXCTickerIdGenerator());
    }

    @Test
    public void testRequestForex() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setSymbol("EUR.USD");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        QuoteRequest quoteRequest = requestFactory.create(forex, 1);

        Assert.assertEquals("EUR/USD", quoteRequest.getString(Symbol.FIELD));
        Assert.assertEquals(1, quoteRequest.getInt(QuoteRequestType.FIELD));
    }

    @Test(expected = BrokerAdapterException.class)
    public void testRequestUnsupportedSecurity() throws Exception {

        Stock stock = new StockImpl();
        stock.setSymbol("MSFT");

        requestFactory.create(stock, 1);
    }

    @Test
    public void testRequestUnsubscribeForex() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setSymbol("EUR.USD");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        QuoteRequest quoteRequest = requestFactory.create(forex, 2);

        Assert.assertEquals("EUR/USD", quoteRequest.getString(Symbol.FIELD));
        Assert.assertEquals(2, quoteRequest.getInt(QuoteRequestType.FIELD));
    }

}
