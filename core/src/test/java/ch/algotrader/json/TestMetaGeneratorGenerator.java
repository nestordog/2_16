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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;

import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.entity.security.SecurityVO;
import ch.algotrader.entity.security.StockVO;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class TestMetaGeneratorGenerator {

    private MetaDataGenerator metaDataGenerator;

    @Before
    public void setup() {

        this.metaDataGenerator = new MetaDataGenerator();
    }

    @Test
    public void testBuildSchema() throws Exception {

        ObjectNode tickNode = this.metaDataGenerator.buildSchema(new SchemaConfig(TickVO.class, "Tick"));
        Assert.assertNotNull(tickNode);

        Assert.assertTrue(tickNode.has("type"));
        Assert.assertEquals("object", tickNode.get("type").asText());
        Assert.assertTrue(tickNode.has("id"));
        Assert.assertEquals("Tick", tickNode.get("id").asText());
        JsonNode propertiesNode = tickNode.get("properties");
        Assert.assertNotNull(propertiesNode);
        Assert.assertTrue(propertiesNode.has("dateTime"));
    }

    @Test
    public void testBuildGroupedSchema() throws Exception {

        ObjectNode securityNode = this.metaDataGenerator.buildGroupedSchema(
                new SchemaConfig(SecurityVO.class, "Security"), new SchemaConfig(StockVO.class, "Stock"));
        Assert.assertNotNull(securityNode);

        Assert.assertTrue(securityNode.has("type"));
        Assert.assertTrue(securityNode.has("id"));
        JsonNode propertiesNode = securityNode.get("properties");
        Assert.assertNotNull(propertiesNode);
        Assert.assertTrue(propertiesNode.has("symbol"));
        Assert.assertTrue(propertiesNode.has("objectType"));
        JsonNode schemasNode = securityNode.get("schemas");
        Assert.assertNotNull(schemasNode);
        Assert.assertTrue(schemasNode instanceof POJONode);
        Object list = ((POJONode)schemasNode).getPojo();
        Assert.assertNotNull(list);
        Assert.assertTrue(list instanceof List);
        ObjectNode stockNode = (ObjectNode) ((List<?>) list).get(0);
        Assert.assertTrue(stockNode.has("type"));
        Assert.assertTrue(stockNode.has("id"));
        JsonNode propertiesNode2 = stockNode.get("properties");
        Assert.assertNotNull(propertiesNode2);
        Assert.assertTrue(propertiesNode2.has("gics"));
        Assert.assertFalse(propertiesNode2.has("objectType"));
    }

}
