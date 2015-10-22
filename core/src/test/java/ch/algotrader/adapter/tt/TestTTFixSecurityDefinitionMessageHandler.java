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

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.adapter.fix.fix42.FixTestUtils;
import ch.algotrader.concurrent.PromiseImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.OptionType;
import quickfix.ConfigError;
import quickfix.DataDictionary;
import quickfix.fix42.SecurityDefinition;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class TestTTFixSecurityDefinitionMessageHandler {

    private final static DataDictionary DATA_DICTIONARY;

    static {
        try {
            DATA_DICTIONARY = new DataDictionary("tt/FIX42.xml");
        } catch (ConfigError configError) {
            throw new Error(configError);
        }
    }

    private TTPendingRequests pendingRequests;
    private TTFixSecurityDefinitionMessageHandler impl;

    @Before
    public void setup() throws Exception {

        this.pendingRequests = new TTPendingRequests();
        this.impl = new TTFixSecurityDefinitionMessageHandler(this.pendingRequests);
    }

    @Test
    public void testParseSecurityDefinitionFuture() throws Exception {

        String s = "8=FIX.4.2|9=00270|35=d|49=TTDEV14P|56=RATKODTS2|34=2|52=20150925-15:34:08.901|55=CL|48=00A0JP00CLZ|" +
                "10455=CLV5|167=FUT|207=CME|15=USD|320=at-1|322=at-1:0|200=201510|16451=0|393=70|323=4|16452=1|16454=10|" +
                "16552=1|16554=10|16456=0|146=0|18206=2|18203=CME|18203=CME-B|864=1|865=5|866=20150922|10=225|";

        SecurityDefinition securityDefinition = FixTestUtils.parseFix42Message(s, DATA_DICTIONARY, SecurityDefinition.class);
        Assert.assertNotNull(securityDefinition);

        TTSecurityDefVO securityDefVO = impl.parse(securityDefinition);
        Assert.assertNotNull(securityDefVO);
        Assert.assertEquals("00A0JP00CLZ", securityDefVO.getId());
        Assert.assertEquals("CL", securityDefVO.getSymbol());
        Assert.assertEquals("CME", securityDefVO.getExchange());
        Assert.assertEquals("FUT", securityDefVO.getType());
        Assert.assertEquals(null, securityDefVO.getDescription());
        Assert.assertEquals(Currency.USD, securityDefVO.getCurrency());
        Assert.assertEquals(LocalDate.of(2015, Month.OCTOBER, 1), securityDefVO.getMaturityDate());
        Assert.assertEquals(LocalDate.of(2015, Month.SEPTEMBER, 22), securityDefVO.getExpiryDate());
        Assert.assertEquals(null, securityDefVO.getOptionType());
        Assert.assertEquals(null, securityDefVO.getStrikePrice());
    }

    @Test
    public void testParseSecurityDefinitionOption() throws Exception {

        String s = "8=FIX.4.2|9=00356|35=d|49=TTDEV14P|56=RATKODTS2|34=5|52=20150925-15:31:32.733|55=ESX|48=92478902|" +
                "10455=Oct15 P6600.00|167=OPT|207=ICE_IPE|15=GBP|320=at-1|322=at-1:3|107=FTSE 100 - Stnd Euro Index Option|" +
                "18207=IFLL.ESX|200=201510|205=16|16451=43|393=82|201=0|323=4|202=6600|16452=0.1|16454=10|16552=0.5|" +
                "16554=10|16456=0|146=0|18206=1|18203=ICE_IPE-B|864=1|865=5|866=20151016|10=076|";

        SecurityDefinition securityDefinition = FixTestUtils.parseFix42Message(s, DATA_DICTIONARY, SecurityDefinition.class);
        Assert.assertNotNull(securityDefinition);

        TTSecurityDefVO securityDefVO = impl.parse(securityDefinition);
        Assert.assertNotNull(securityDefVO);
        Assert.assertEquals("92478902", securityDefVO.getId());
        Assert.assertEquals("ESX", securityDefVO.getSymbol());
        Assert.assertEquals("ICE_IPE", securityDefVO.getExchange());
        Assert.assertEquals("OPT", securityDefVO.getType());
        Assert.assertEquals("FTSE 100 - Stnd Euro Index Option", securityDefVO.getDescription());
        Assert.assertEquals(Currency.GBP, securityDefVO.getCurrency());
        Assert.assertEquals(LocalDate.of(2015, Month.OCTOBER, 16), securityDefVO.getMaturityDate());
        Assert.assertEquals(LocalDate.of(2015, Month.OCTOBER, 16), securityDefVO.getExpiryDate());
        Assert.assertEquals(OptionType.PUT, securityDefVO.getOptionType());
        Assert.assertEquals(new Double(6600.0d), securityDefVO.getStrikePrice());
    }

    @Test
    public void testSecurityDefinitionPromise() throws Exception {

        String s = "8=FIX.4.2|9=00307|35=d|49=TTDEV14P|56=RATKODTS2|34=2|52=20150928-12:10:43.598|55=SP|48=00B0KP00SP410C908Z|" +
                "10455=SPX5 P2340|167=OPT|207=CME|15=USD|320=at-1|322=at-1:0|107=S&P 500 Index|200=201511|16451=0|393=1|" +
                "201=0|323=4|202=234000|16452=1|16454=2.5|16552=1|16554=2.5|16456=0|146=0|18206=1|18203=CME|864=1|865=5|" +
                "866=20151120|10=238|";

        SecurityDefinition securityDefinition = FixTestUtils.parseFix42Message(s, DATA_DICTIONARY, SecurityDefinition.class);
        Assert.assertNotNull(securityDefinition);

        String requestId = securityDefinition.getSecurityReqID().getValue();

        PromiseImpl<List<TTSecurityDefVO>> promise = new PromiseImpl<>(null);
        pendingRequests.addSecurityDefinitionRequest(requestId, promise);

        Assert.assertFalse(promise.isDone());

        impl.onMessage(securityDefinition, FixTestUtils.fakeFix42Session());

        Assert.assertTrue(promise.isDone());
        List<TTSecurityDefVO> securityDefs = promise.get();
        Assert.assertNotNull(securityDefs);
        Assert.assertEquals(1, securityDefs.size());
    }

}
