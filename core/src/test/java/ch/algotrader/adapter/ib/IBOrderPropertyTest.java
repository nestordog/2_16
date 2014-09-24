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
package ch.algotrader.adapter.ib;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.config.IBConfig;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.AccountImpl;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.MarketOrderImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.OrderPropertyType;
import ch.algotrader.enumeration.Side;

import com.ib.client.Contract;
import com.ib.client.TagValue;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBOrderPropertyTest {

    private IBOrderMessageFactory iBOrderMessageFactory;

    @Before
    public void setup() {

        this.iBOrderMessageFactory = new DefaultIBOrderMessageFactory(new IBConfig("", ""));
    }

    @Test
    public void test() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        Account account = new AccountImpl();
        account.setName("TEST");

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(forex);
        order.setSide(Side.BUY);
        order.setQuantity(2000);
        order.setAccount(account);
        order.setIntId("123");

        // IB custom properties
        order.addProperty("transmit", "false", OrderPropertyType.IB);
        order.addProperty("algoStrategy", "AD", OrderPropertyType.IB);

        // IB algo properties
        order.addProperty("componentSize", "100", OrderPropertyType.IB);
        order.addProperty("timeBetweenOrders", "60", OrderPropertyType.IB);

        Contract contract = IBUtil.getContract(order.getSecurityInitialized());
        com.ib.client.Order ibOrder = this.iBOrderMessageFactory.createOrderMessage(order, contract);

        Assert.assertEquals("AD", ibOrder.m_algoStrategy);
        Assert.assertFalse(ibOrder.m_transmit);

        Assert.assertEquals(2, ibOrder.m_algoParams.size());
        for (TagValue tagValue : ibOrder.m_algoParams) {
            if ("componentSize".equals(tagValue.m_tag) && "100".equals(tagValue.m_value)) {
                // ok
            } else if ("timeBetweenOrders".equals(tagValue.m_tag) && "60".equals(tagValue.m_value)) {
                // ok
            } else {
                Assert.fail("unexpected tagValue " + tagValue.m_tag + " " + tagValue.m_value);
            }
        }
    }
}
