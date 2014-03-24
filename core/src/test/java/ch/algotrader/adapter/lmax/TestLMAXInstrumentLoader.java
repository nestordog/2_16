/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.adapter.lmax;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class TestLMAXInstrumentLoader {

    private final static String TEST_INPUT =
            "Instrument Name,LMAX ID,LMAX symbol ,Contract Multiplier,Tick Size,Tick Value,Effective Date,Expiry Date,Quoted CCY,\n" +
            "AUD/JPY,4008,AUD/JPY,10000,0.001,10,09/07/2010,,JPY,\n" +
            "AUD/USD,4007,AUD/USD,10000,0.00001,0.1,09/07/2010,,USD,\n" +
            "US Crude (Jul13),100765,CLN3,100,0.01,1,17/04/2013,17/06/2013 19:30,USD,\n";

    @Test
    public void testLoad() throws Exception {
        LMAXInstrumentLoader loader = new LMAXInstrumentLoader();
        List<LMAXInstrumentDef> list = loader.load(new StringReader(TEST_INPUT));
        Assert.assertNotNull(list);
        Assert.assertEquals(3, list.size());

        LMAXInstrumentDef p1 = list.get(0);
        Assert.assertEquals("AUD/JPY", p1.getName());
        Assert.assertEquals("4008", p1.getId());
        Assert.assertEquals("AUD/JPY", p1.getSymbol());
        Assert.assertEquals(new BigDecimal("10000"), p1.getContractMultiplier());
        Assert.assertEquals(new BigDecimal("0.001"), p1.getTickSize());
        Assert.assertEquals(new BigDecimal("10"), p1.getTickValue());

        LMAXInstrumentDef p2 = list.get(1);
        Assert.assertEquals("AUD/USD", p2.getName());
        Assert.assertEquals("4007", p2.getId());
        Assert.assertEquals("AUD/USD", p2.getSymbol());
        Assert.assertEquals(new BigDecimal("10000"), p2.getContractMultiplier());
        Assert.assertEquals(new BigDecimal("0.00001"), p2.getTickSize());
        Assert.assertEquals(new BigDecimal("0.1"), p2.getTickValue());

        LMAXInstrumentDef p3 = list.get(2);
        Assert.assertEquals("US Crude (Jul13)", p3.getName());
        Assert.assertEquals("100765", p3.getId());
        Assert.assertEquals("CLN3", p3.getSymbol());
        Assert.assertEquals(new BigDecimal("100"), p3.getContractMultiplier());
        Assert.assertEquals(new BigDecimal("0.01"), p3.getTickSize());
        Assert.assertEquals(new BigDecimal("1"), p3.getTickValue());
    }

}
