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
package ch.algotrader.json;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.Month;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import ch.algotrader.entity.trade.MarketOrderVO;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.TIF;
import ch.algotrader.util.DateTimeLegacy;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class TestJacksonObjectMapper {

    private ObjectMapper objectMapper;

    @Before
    public void setup() {

        this.objectMapper = new ObjectMapperFactory().create();
    }

    @Test
    public void testMarketOrderSerialization() throws Exception {

        ObjectWriter objectWriter = this.objectMapper.writerFor(MarketOrderVO.class);
        MarketOrderVO order = new MarketOrderVO(0L, null, null, Side.BUY, 1000L, null, 10L, 101L, 1L);
        StringWriter writer = new StringWriter();
        objectWriter.writeValue(writer, order);

        String s = writer.toString();
        Assert.assertEquals("{\"id\":0,\"intId\":null,\"extId\":null,\"dateTime\":null,\"side\":\"BUY\",\"quantity\":1000," +
                "\"tif\":null,\"tifDateTime\":null,\"exchangeId\":0,\"securityId\":10,\"accountId\":101,\"strategyId\":1," +
                "\"objectType\":\"MarketOrder\"}", s);
    }

    @Test
    public void testMarketOrderSerializationOptional() throws Exception {

        ObjectWriter objectWriter = this.objectMapper.writerFor(MarketOrderVO.class);
        MarketOrderVO order = new MarketOrderVO(0L, "this", "that", null, Side.BUY, 1000L,
                TIF.GTD, DateTimeLegacy.toGMTDateTime(LocalDateTime.of(2015, Month.NOVEMBER, 10, 20, 0, 0)), 0L, 10L, 101L, 1L);
        StringWriter writer = new StringWriter();
        objectWriter.writeValue(writer, order);

        String s = writer.toString();
        Assert.assertEquals("{\"id\":0,\"intId\":\"this\",\"extId\":\"that\",\"dateTime\":null,\"side\":\"BUY\",\"quantity\":1000," +
                "\"tif\":\"GTD\",\"tifDateTime\":1447185600000,\"exchangeId\":0,\"securityId\":10,\"accountId\":101,\"strategyId\":1," +
                "\"objectType\":\"MarketOrder\"}", s);
    }

    @Test
    public void testMarketOrderDeserialization() throws Exception {

        String s = "{\"side\":\"BUY\",\"quantity\":1000,\"securityId\":10,\"accountId\":101,\"strategyId\":1}";
        ObjectReader objectReader = this.objectMapper.readerFor(MarketOrderVO.class);
        MarketOrderVO order = objectReader.readValue(s);

        Assert.assertNotNull(order);
        Assert.assertEquals(0L, order.getId());
        Assert.assertEquals(null, order.getIntId());
        Assert.assertEquals(null, order.getExtId());
        Assert.assertEquals(null, order.getDateTime());
        Assert.assertEquals(Side.BUY, order.getSide());
        Assert.assertEquals(1000L, order.getQuantity());
        Assert.assertEquals(null, order.getTif());
        Assert.assertEquals(null, order.getTifDateTime());
        Assert.assertEquals(10L, order.getSecurityId());
        Assert.assertEquals(101L, order.getAccountId());
        Assert.assertEquals(1L, order.getStrategyId());
    }

    @Test
    public void testMarketOrderDeserializationWithMillEpoch() throws Exception {

        String s = "{\"side\":\"BUY\",\"quantity\":1000,\"dateTime\":1447185600000,\"securityId\":10,\"accountId\":101,\"strategyId\":1}";
        ObjectReader objectReader = this.objectMapper.readerFor(MarketOrderVO.class);
        MarketOrderVO order = objectReader.readValue(s);

        Assert.assertNotNull(order);
        Assert.assertEquals(0L, order.getId());
        Assert.assertEquals(null, order.getIntId());
        Assert.assertEquals(null, order.getExtId());
        Assert.assertEquals(DateTimeLegacy.toGMTDateTime(LocalDateTime.of(2015, Month.NOVEMBER, 10, 20, 0, 0)), order.getDateTime());
        Assert.assertEquals(Side.BUY, order.getSide());
        Assert.assertEquals(1000L, order.getQuantity());
        Assert.assertEquals(null, order.getTif());
        Assert.assertEquals(null, order.getTifDateTime());
        Assert.assertEquals(10L, order.getSecurityId());
        Assert.assertEquals(101L, order.getAccountId());
        Assert.assertEquals(1L, order.getStrategyId());
    }

    @Test
    public void testMarketOrderDeserializationOptional() throws Exception {

        String s = "{\"id\":1,\"intId\":\"this\",\"extId\":\"that\",\"dateTime\":null,\"side\":\"BUY\",\"quantity\":1000," +
                "\"tif\":\"DAY\",\"tifDateTime\":\"2015-11-10 20:00:00\",\"exchangeId\":2,\"securityId\":10,\"accountId\":101," +
                "\"strategyId\":1}";
        ObjectReader objectReader = this.objectMapper.readerFor(MarketOrderVO.class);
        MarketOrderVO order = objectReader.readValue(s);

        Assert.assertNotNull(order);
        Assert.assertEquals(1L, order.getId());
        Assert.assertEquals("this", order.getIntId());
        Assert.assertEquals("that", order.getExtId());
        Assert.assertEquals(null, order.getDateTime());
        Assert.assertEquals(Side.BUY, order.getSide());
        Assert.assertEquals(1000L, order.getQuantity());
        Assert.assertEquals(TIF.DAY, order.getTif());
        Assert.assertEquals(DateTimeLegacy.toGMTDateTime(LocalDateTime.of(2015, Month.NOVEMBER, 10, 20, 0, 0)), order.getTifDateTime());
        Assert.assertEquals(2L, order.getExchangeId());
        Assert.assertEquals(10L, order.getSecurityId());
        Assert.assertEquals(101L, order.getAccountId());
        Assert.assertEquals(1L, order.getStrategyId());
    }

}
