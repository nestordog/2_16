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
package ch.algotrader.configeditor;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Property model, represents convenience methods for accessing and manipulating a property.
 *
 * @author <a href="mailto:ahihlovskiy@algotrader.ch">Andrey Hihlovskiy</a>
 *
 * @version $Revision$ $Date$
 */
public class PropertyModel {

    ValueStruct values;

    public PropertyModel(ValueStruct pValues) {
        values = pValues;
    }

    public String getPropertyId() {
        Map<String, Object> definition = getDefinition();
        if (definition == null)
            return "String";
        Object type = definition.get("type");
        return type == null ? "String" : type.toString();
    }

    public String getLabel() {
        Map<String, Object> definition = getDefinition();
        if (definition == null)
            return null;
        Object label = definition.get("label");
        return label == null ? null : label.toString();
    }

    public boolean getRequired() {
        Map<String, Object> definition = getDefinition();
        if (definition == null)
            return true;
        Object required = definition.get("required");
        if (required == null)
            return true;
        return (Boolean) required;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getDefinition() {
        ObjectMapper mapper = new ObjectMapper();
        if (values != null && values.comments != null)
            for (String comment : values.comments) {
                try {
                    return mapper.readValue(comment, Map.class);
                } catch (Exception e) {
                    // keep reading other comments in case of errors
                }
            }
        return null;
    }
}
