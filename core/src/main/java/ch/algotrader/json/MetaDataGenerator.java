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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;

public class MetaDataGenerator {

    private static class ClassIgnoringIntrospector extends JacksonAnnotationIntrospector {

        private static final long serialVersionUID = -7345861056075400255L;

        private final Class clz;

        public ClassIgnoringIntrospector(Class clz) {
            this.clz = clz;
        }

        @Override
        public boolean hasIgnoreMarker(final AnnotatedMember m) {

            if (m.getDeclaringClass() == clz) {
                return true;
            }
            if ("getObjectType".equals(m.getName())) {
                return true;
            }
            return super.hasIgnoreMarker(m);
        }
    }

    private ObjectNode buildSchemaFor(final SchemaConfig config, final JacksonAnnotationIntrospector introspector) throws JsonMappingException {
        ObjectMapper m = new ObjectMapper();
        if (introspector != null) {
            m.setAnnotationIntrospector(introspector);
        }
        SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
        m.acceptJsonFormatVisitor(m.constructType(config.getType()), visitor);
        JsonSchema schema = visitor.finalSchema();
        schema.setId(config.getName());
        return m.valueToTree(schema);
    }

    public ObjectNode buildSchema(final SchemaConfig config) throws JsonMappingException {
        return buildSchemaFor(config, null);
    }

    public ObjectNode buildGroupedSchema(final SchemaConfig config, final SchemaConfig... subClasses) throws JsonMappingException {
        ObjectNode base = buildSchemaFor(config, null);
        List<ObjectNode> subs = new ArrayList<>();
        for (SchemaConfig subClass: subClasses) {
            ObjectNode sub = buildSchemaFor(subClass, new ClassIgnoringIntrospector(config.getType()));
            subs.add(sub);
        }
        base.putPOJO("schemas", subs);
        return base;
    }

}
