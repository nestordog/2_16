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

package ch.algotrader.rest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;

import ch.algotrader.UnrecoverableCoreException;
import ch.algotrader.entity.PositionVO;
import ch.algotrader.entity.TransactionVO;
import ch.algotrader.entity.exchange.ExchangeVO;
import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.entity.security.BondFamilyVO;
import ch.algotrader.entity.security.BondVO;
import ch.algotrader.entity.security.CombinationVO;
import ch.algotrader.entity.security.CommodityVO;
import ch.algotrader.entity.security.ForexVO;
import ch.algotrader.entity.security.FutureFamilyVO;
import ch.algotrader.entity.security.FutureVO;
import ch.algotrader.entity.security.GenericFutureFamilyVO;
import ch.algotrader.entity.security.GenericFutureVO;
import ch.algotrader.entity.security.IndexVO;
import ch.algotrader.entity.security.IntrestRateVO;
import ch.algotrader.entity.security.OptionFamilyVO;
import ch.algotrader.entity.security.OptionVO;
import ch.algotrader.entity.security.SecurityFamilyVO;
import ch.algotrader.entity.security.SecurityVO;
import ch.algotrader.entity.security.StockVO;
import ch.algotrader.entity.strategy.StrategyVO;
import ch.algotrader.entity.trade.LimitOrderVO;
import ch.algotrader.entity.trade.OrderStatusVO;
import ch.algotrader.entity.trade.OrderVO;
import ch.algotrader.entity.trade.StopLimitOrderVO;
import ch.algotrader.entity.trade.StopOrderVO;
import ch.algotrader.vo.PositionValuationVO;

@RestController
@RequestMapping(path = "/rest/meta")
public class MetaDataRestController extends RestControllerBase {

    private static class ClassIgnoringIntrospector extends JacksonAnnotationIntrospector {

        private static final long serialVersionUID = -7345861056075400255L;

        private final Class clz;

        public ClassIgnoringIntrospector(Class clz) {
            this.clz = clz;
        }

        @Override
        public boolean hasIgnoreMarker(final AnnotatedMember m) {
            return m.getDeclaringClass() == clz || super.hasIgnoreMarker(m);
        }
    }

    private final List<JsonNode> meta;

    public MetaDataRestController(){
        List<SchemaConfig> configs = Arrays.asList(
                new SchemaConfig(TickVO.class, "Tick"),
                new SchemaConfig(PositionVO.class, "Position"),
                new SchemaConfig(OrderStatusVO.class,"OrderStatus"),
                new SchemaConfig(TransactionVO.class, "Transaction"),
                new SchemaConfig(StrategyVO.class, "Strategy"),
                new SchemaConfig(ExchangeVO.class, "Exchange"),
                new SchemaConfig(PositionValuationVO.class, "PositionValuation"));

        meta = configs
            .stream()
            .map(clz -> buildSchemaFor(clz, Optional.empty())).collect(Collectors.toList());

        meta.add(buildGroupedSchema(
                new SchemaConfig(SecurityFamilyVO.class,"SecurityFamily"),
                Arrays.asList(
                        new SchemaConfig(FutureFamilyVO.class, "FutureFamily"),
                        new SchemaConfig(GenericFutureFamilyVO.class, "GenericFutureFamily"),
                        new SchemaConfig(OptionFamilyVO.class, "OptionFamily"),
                        new SchemaConfig(BondFamilyVO.class, "BondFamily"))));

        meta.add(buildGroupedSchema(
                new SchemaConfig(SecurityVO.class,"Security"),
                Arrays.asList(
                        new SchemaConfig(IndexVO.class, "Index"),
                        new SchemaConfig(ForexVO.class, "Forex"),
                        new SchemaConfig(StockVO.class, "Stock"),
                        new SchemaConfig(OptionVO.class, "Option"),
                        new SchemaConfig(StockVO.class, "Stock"),
                        new SchemaConfig(CommodityVO.class, "Commodity"),
                        new SchemaConfig(BondVO.class, "Bond"),
                        new SchemaConfig(GenericFutureVO.class, "GenericFuture"),
                        new SchemaConfig(IntrestRateVO.class, "IntrestRate"),
                        new SchemaConfig(OptionVO.class, "Option"),
                        new SchemaConfig(CombinationVO.class, "Combination"),
                        new SchemaConfig(FutureVO.class, "Future"))));

        meta.add(buildGroupedSchema(
                new SchemaConfig(OrderVO.class,"Order"),
                Arrays.asList(
                        new SchemaConfig(LimitOrderVO.class, "LimitOrder"),
                        new SchemaConfig(StopOrderVO.class, "StopOrder"),
                        new SchemaConfig(StopLimitOrderVO.class, "StopLimitOrder"))));
    }

    @CrossOrigin
    @RequestMapping(path = "", method = RequestMethod.GET)
    public List<JsonNode> getMeta(){
       return meta;
    }

    private ObjectNode buildSchemaFor(final SchemaConfig config, final Optional<JacksonAnnotationIntrospector> introspector) {
        try {
            ObjectMapper m = new ObjectMapper();
            introspector.ifPresent(m::setAnnotationIntrospector);
            SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
            m.acceptJsonFormatVisitor(m.constructType(config.clazz), visitor);
            JsonSchema schema = visitor.finalSchema();
            schema.setId(config.name);
            return m.valueToTree(schema);
        } catch (JsonMappingException ex) {
            throw new UnrecoverableCoreException(ex);
        }
    }

    private ObjectNode buildGroupedSchema(final SchemaConfig config, final List<SchemaConfig> subClasses) {
        ObjectNode base = buildSchemaFor(config, Optional.empty());
        List<ObjectNode> subSchemas = subClasses.stream()
                .map(clz -> buildSchemaFor(clz, Optional.of(new ClassIgnoringIntrospector(config.clazz))))
                .collect(Collectors.toList());
        base.putPOJO("schemas", subSchemas);
        return base;
    }

    private static class SchemaConfig{
        public final Class clazz;
        public final String name;

        public SchemaConfig(Class clazz, String name) {
            this.clazz = clazz;
            this.name = name;
        }
    }

}
