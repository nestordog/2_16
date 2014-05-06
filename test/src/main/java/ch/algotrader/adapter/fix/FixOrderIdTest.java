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
package ch.algotrader.adapter.fix;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Assert;

import quickfix.SessionID;
import ch.algotrader.ServiceLocator;
import ch.algotrader.service.LocalServiceTest;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class FixOrderIdTest extends LocalServiceTest {

    private FixOrderIdGenerator fixOrderIdGenerator;

    @Before
    public void setup() throws Exception {

        System.setProperty("spring.profiles.active", "singleDataSource, server, lMAXFix");

        ServiceLocator serviceLocator = ServiceLocator.instance();
        serviceLocator.init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);

        this.fixOrderIdGenerator = serviceLocator.getService("orderIdGenerator", FixOrderIdGenerator.class);
    }

    @After
    public void cleanup() throws Exception{

        ServiceLocator.instance().shutdown();
    }

    @Test
    public void testGetNextOrderId() throws Exception {

        SessionID sessionId = new SessionID("", "", "", "LMAXT");

        String orderId = this.fixOrderIdGenerator.getNextOrderId(sessionId);

        Assert.notNull(orderId);
        Assert.isTrue(orderId.startsWith("lmaxt"));
    }

}
