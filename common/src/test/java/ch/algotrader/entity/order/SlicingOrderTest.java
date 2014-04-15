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
package ch.algotrader.entity.order;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;

import ch.algotrader.entity.marketData.TickImpl;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.trade.OrderValidationException;
import ch.algotrader.entity.trade.SlicingOrder;
import ch.algotrader.entity.trade.SlicingOrderImpl;
import ch.algotrader.util.BeanUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class SlicingOrderTest {

    @Test
    public void testPopulate() throws IllegalAccessException, InvocationTargetException {

        SlicingOrder order = SlicingOrder.Factory.newInstance();

        String nameValues = "side=BUY,quantity=2,minVolPct=0.5,maxVolPct=1.5,minQuantity=5,maxQuantity=10,minDuration=1.0,minDuration=2.0,minDelay=1.0,maxDelay=2.0";

        Map<String, String> properties = new HashMap<String, String>();
        for (String nameValue : nameValues.split(",")) {
            properties.put(nameValue.split("=")[0], nameValue.split("=")[1]);
        }

        BeanUtil.populate(order, properties);
    }

    @Test
    public void testValidate() throws OrderValidationException {

        Security security = Mockito.mock(Security.class);
        Mockito.when(security.getCurrentMarketDataEvent()).thenReturn(new TickImpl());

        SlicingOrder order = new SlicingOrderImpl();
        order.setMinVolPct(0);
        order.setMaxVolPct(0);
        order.setMinQuantity(10);
        order.setMaxQuantity(80);
        order.setMaxDuration(1.0);
        order.setMaxDelay(1.0);
        order.setSecurity(security);

        order.validate();
    }

}
