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
package ch.algotrader.adapter.ib;

import java.util.concurrent.LinkedBlockingDeque;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ch.algotrader.adapter.fix.fix42.FixTestUtils;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.MarketOrderImpl;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Status;
import ch.algotrader.service.OrderExecutionService;
import quickfix.fix42.OrderCancelReject;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class TestIBFixOrderMessageHandler {

    @Mock
    private OrderExecutionService orderExecutionService;

    private SecurityFamily family;
    private Forex forex;

    private IBFixOrderMessageHandler impl;

    @Before
    public void setup() throws Exception {

        MockitoAnnotations.initMocks(this);

        this.family = new SecurityFamilyImpl();
        this.family.setCurrency(Currency.USD);
        this.family.setScale(3);

        this.forex = new ForexImpl();
        this.forex.setSymbol("EUR.USD");
        this.forex.setBaseCurrency(Currency.EUR);
        this.forex.setSecurityFamily(family);


        this.impl = new IBFixOrderMessageHandler(this.orderExecutionService, new LinkedBlockingDeque<>());
    }

    @Test
    public void testOrderCancelRejectedAlreadyFilled() throws Exception {

        String s = "8=FIX.4.2|9=000151|35=9|34=012603|43=N|52=20160226-15:35:00|49=IB|56=algotr1|37=0|11=ibft354.1|41=ibft354.0|39=2|102=0|" +
                "58=Cannot cancel the filled order|1=U1659362|434=1|10=054|";
        OrderCancelReject cancelReject = FixTestUtils.parseFix42Message(s, OrderCancelReject.class);
        Assert.assertNotNull(cancelReject);

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(this.forex);
        Mockito.when(this.orderExecutionService.getOpenOrderByIntId("ibft354.0")).thenReturn(order);

        this.impl.onMessage(cancelReject, FixTestUtils.fakeFix42Session());

        Mockito.verify(this.orderExecutionService, Mockito.never()).handleOrderStatus(Mockito.any());
    }

    @Test
    public void testOrderCancelRejectedUnknownOrder() throws Exception {

        String s = "8=FIX.4.2|9=000134|35=9|34=011969|43=N|52=20160304-07:00:00|49=IB|56=algotr1|37=0|11=ibft639.1|41=ibft639.0|39=8|102=1|" +
                "58=No such order|1=U1659362|434=1|10=004|";
        OrderCancelReject cancelReject = FixTestUtils.parseFix42Message(s, OrderCancelReject.class);
        Assert.assertNotNull(cancelReject);

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);
        family.setScale(3);

        Forex forex = new ForexImpl();
        forex.setSymbol("EUR.USD");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(forex);
        Mockito.when(this.orderExecutionService.getOpenOrderByIntId("ibft639.0")).thenReturn(order);

        this.impl.onMessage(cancelReject, FixTestUtils.fakeFix42Session());

        ArgumentCaptor<OrderStatus> argumentCaptor = ArgumentCaptor.forClass(OrderStatus.class);
        Mockito.verify(this.orderExecutionService, Mockito.times(1)).handleOrderStatus(argumentCaptor.capture());

        OrderStatus orderStatus1 = argumentCaptor.getValue();
        Assert.assertEquals("ibft639.0", orderStatus1.getIntId());
        Assert.assertEquals(Status.REJECTED, orderStatus1.getStatus());
    }

}
