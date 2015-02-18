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
package ch.algotrader.entity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.vo.ExpirePositionVO;

/**
* Unit tests for {@link ch.algotrader.entity.ExpirePositionVOProducer}.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*
* @version $Revision$ $Date$
*/
public class ExpirePositionVOProducerTest {

    private ExpirePositionVOProducer instance;

    @Before
    public void setup() throws Exception {

        this.instance = ExpirePositionVOProducer.INSTANCE;
    }

    @Test
    public void testConvert() {

        Forex forex = new ForexImpl();
        forex.setId(666);

        Position position = new PositionImpl();

        position.setId(111);
        position.setQuantity(222);
        position.setSecurity(forex);

        ExpirePositionVO expirePositionVO = this.instance.convert(position);

        Assert.assertNotNull(expirePositionVO);

        Assert.assertEquals(111, expirePositionVO.getId());
        Assert.assertEquals(222, expirePositionVO.getQuantity());
        Assert.assertEquals(666, expirePositionVO.getSecurityId());
    }

}
