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
package ch.algotrader.adapter.tt;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ch.algotrader.adapter.fix.DefaultFixApplication;
import ch.algotrader.adapter.fix.DefaultFixSessionStateHolder;
import ch.algotrader.adapter.fix.FixApplicationTestBase;
import ch.algotrader.adapter.fix.FixConfigUtils;
import ch.algotrader.concurrent.PromiseImpl;
import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.security.FutureFamily;
import ch.algotrader.entity.security.OptionFamily;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.event.dispatch.EventDispatcher;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.field.SecurityReqID;
import quickfix.field.SecurityType;
import quickfix.fix42.SecurityDefinitionRequest;

public class TTRequestDefinitionRequestTest extends FixApplicationTestBase {

    private TTSecurityDefinitionRequestFactory requestFactory;
    private TTPendingRequests pendingRequests;
    private EventDispatcher eventDispatcher;

    @Before
    public void setup() throws Exception {

        this.requestFactory = new TTSecurityDefinitionRequestFactory();
        this.eventDispatcher = Mockito.mock(EventDispatcher.class);

        SessionSettings settings = FixConfigUtils.loadSettings();
        SessionID sessionId = FixConfigUtils.getSessionID(settings, "TTRD");

        this.pendingRequests = new TTPendingRequests();

        DefaultFixApplication fixApplication = new DefaultFixApplication(sessionId,
                new TTFixSecurityDefinitionMessageHandler(this.pendingRequests),
                new TTLogonMessageHandler(settings),
                new DefaultFixSessionStateHolder("TTRD", this.eventDispatcher));

        setupSession(settings, sessionId, fixApplication);
    }

    @Test
    public void testRequestCrudeOilFutureDefinitions() throws Exception {

        Exchange exchange = Exchange.Factory.newInstance();
        exchange.setName("CME");
        exchange.setCode("CME");
        exchange.setTimeZone("US/Central");

        FutureFamily futureFamily = FutureFamily.Factory.newInstance();
        futureFamily.setSymbolRoot("CL");
        futureFamily.setCurrency(Currency.USD);
        futureFamily.setExchange(exchange);

        SecurityDefinitionRequest request = this.requestFactory.create("test-1", futureFamily, SecurityType.FUTURE);

        String requestId = request.getSecurityReqID().getValue();
        Assert.assertNotNull(requestId);

        PromiseImpl<List<TTSecurityDefVO>> promise = new PromiseImpl<>(null);
        this.pendingRequests.addSecurityDefinitionRequest(requestId, promise);

        this.session.send(request);

        List<TTSecurityDefVO> securityDefs = promise.get(10, TimeUnit.SECONDS);
        Assert.assertNotNull(securityDefs);
        securityDefs.forEach(System.out::println);
    }

    @Test
    public void testRequestSPOptDefinitions() throws Exception {

        Exchange exchange = Exchange.Factory.newInstance();
        exchange.setName("CME");
        exchange.setCode("CME");
        exchange.setTimeZone("US/Central");

        OptionFamily optionFamily = OptionFamily.Factory.newInstance();
        optionFamily.setSymbolRoot("SP");
        optionFamily.setCurrency(Currency.USD);
        optionFamily.setExchange(exchange);

        SecurityDefinitionRequest request = this.requestFactory.create("test-2", optionFamily, SecurityType.OPTION);

        String requestId = request.getSecurityReqID().getValue();
        Assert.assertNotNull(requestId);

        PromiseImpl<List<TTSecurityDefVO>> promise = new PromiseImpl<>(null);
        this.pendingRequests.addSecurityDefinitionRequest(requestId, promise);

        this.session.send(request);

        List<TTSecurityDefVO> securityDefs = promise.get(10, TimeUnit.SECONDS);
        Assert.assertNotNull(securityDefs);
        securityDefs.forEach(System.out::println);
    }

    @Test
    public void testRequestForexDefinitions() throws Exception {

        Exchange exchange = Exchange.Factory.newInstance();
        exchange.setName("IDEALPRO");
        exchange.setCode("IDEALPRO");
        exchange.setTimeZone("US/Central");

        SecurityFamily securityFamily = SecurityFamily.Factory.newInstance();
        securityFamily.setCurrency(Currency.USD);
        securityFamily.setExchange(exchange);

        SecurityDefinitionRequest request = this.requestFactory.create("test-3", securityFamily, SecurityType.FOREIGN_EXCHANGE_CONTRACT);

        String requestId = request.getSecurityReqID().getValue();
        Assert.assertNotNull(requestId);

        PromiseImpl<List<TTSecurityDefVO>> promise = new PromiseImpl<>(null);
        this.pendingRequests.addSecurityDefinitionRequest(requestId, promise);

        this.session.send(request);

        List<TTSecurityDefVO> securityDefs = promise.get(10, TimeUnit.SECONDS);
        Assert.assertNotNull(securityDefs);
        securityDefs.forEach(System.out::println);
    }

    @Test
    public void testRequestAllDefinitions() throws Exception {

        SecurityDefinitionRequest request = new SecurityDefinitionRequest();
        request.set(new SecurityReqID("all-stuff"));

        String requestId = request.getSecurityReqID().getValue();
        Assert.assertNotNull(requestId);

        PromiseImpl<List<TTSecurityDefVO>> promise = new PromiseImpl<>(null);
        this.pendingRequests.addSecurityDefinitionRequest(requestId, promise);

        this.session.send(request);

        List<TTSecurityDefVO> securityDefs = promise.get(10, TimeUnit.MINUTES);
        Assert.assertNotNull(securityDefs);
        securityDefs.forEach(System.out::println);
    }

}
