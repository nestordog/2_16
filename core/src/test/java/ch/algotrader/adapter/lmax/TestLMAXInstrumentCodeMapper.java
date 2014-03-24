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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class TestLMAXInstrumentCodeMapper {

    @Test
    public void testSymbolToIdMapping() throws Exception {
        LMAXInstrumentCodeMapper mapper = LMAXInstrumentCodeMapper.load();

        Assert.assertEquals("4008", mapper.mapToCode("AUD/JPY"));
        Assert.assertEquals("4007", mapper.mapToCode("AUD/USD"));
        Assert.assertEquals("100765", mapper.mapToCode("CLN3"));
        Assert.assertEquals(null, mapper.mapToCode(null));
        Assert.assertEquals(null, mapper.mapToCode("whatever"));

        Assert.assertEquals("AUD/JPY", mapper.mapToSymbol("4008"));
        Assert.assertEquals("AUD/USD", mapper.mapToSymbol("4007"));
        Assert.assertEquals("CLN3", mapper.mapToSymbol("100765"));
        Assert.assertEquals(null, mapper.mapToSymbol(null));
        Assert.assertEquals(null, mapper.mapToSymbol("whatever"));
    }

}
