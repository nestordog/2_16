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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents mapping from property names to structured values (values + comments).
 *
 * @author <a href="mailto:ahihlovskiy@algotrader.ch">Andrey Hihlovskiy</a>
 *
 * @version $Revision$ $Date$
 */
public class StructuredProperties {

    private final Map<String, ValueStruct> properties;
    private final EditorPropertyPage propertyPage;

    public StructuredProperties(EditorPropertyPage propertyPage) {
        properties = new LinkedHashMap<String, ValueStruct>();
        this.propertyPage = propertyPage;
    }

    public void load(File f, Collection<String> errorMessages) throws Exception {
        ValueStruct n = new ValueStruct();
        BufferedReader reader = new BufferedReader(new FileReader(f));
        try {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.isEmpty())
                    continue;
                if (line.charAt(0) == '#') {
                    n.comments.add(line.substring(1));
                } else {
                    try {
                        parseKeyValueLine(n, line);
                    } catch (Exception e) {
                        if (errorMessages == null)
                            throw e;
                        String errMessage = MessageFormat.format("Error reading file ''{0}'': {1}", f.getName(), (e.getMessage() == null ? e.getClass().getName() : e.getMessage()));
                        errorMessages.add(errMessage);
                    }
                    n = new ValueStruct();
                }
            }
        } finally {
            reader.close();
        }
    }

    private void parseKeyValueLine(ValueStruct n, String line) throws Exception {
        boolean escape = false;
        boolean isKey = true;
        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();
        String inlineComment = null;
        for (int i = 0; i < line.length(); i++) {

            if (escape) {
                escape = false;
                if (isKey)
                    key.append(line.charAt(i));
                else
                    value.append(line.charAt(i));
            } else if (line.charAt(i) == '\\') {
                escape = true;
            } else if (line.charAt(i) == '=') {
                isKey = false;
            } else if (line.charAt(i) == '#') {
                inlineComment = line.substring(i + 1);
                break;
            } else {
                if (isKey)
                    key.append(line.charAt(i));
                else
                    value.append(line.charAt(i));
            }
        }
        String stringValue = value.toString().trim();
        String propertyId = new PropertyModel(n).getPropertyId();
        n.value = propertyPage.projectProperties.deserialize(propertyId, stringValue);
        if (inlineComment != null)
            n.inlineComment = inlineComment.trim();
        properties.put(key.toString().trim(), n);
    }

    public void save(File f) throws IOException {
        PrintWriter out = new PrintWriter(f);
        try {
            for (String key : properties.keySet()) {
                for (int i = 0; i < properties.get(key).comments.size(); i++) {
                    out.println("#" + properties.get(key).comments.get(i));
                }
                out.print(key + "=" + properties.get(key).getSaveReadyValue(propertyPage.projectProperties));
                if (properties.get(key).inlineComment != null)
                    out.print(" #" + properties.get(key).inlineComment);
                out.println();
            }
        } finally {
            out.close();
        }
    }

    public Iterable<String> getKeys() {
        return properties.keySet();
    }

    public Object getValue(String key) {
        ValueStruct temp = properties.get(key);
        if (temp != null)
            return temp.value;
        return null;
    }

    public ValueStruct getValueStruct(String key) {
        return properties.get(key);
    }

    public void setValue(String key, Object value) {
        ValueStruct temp = properties.get(key);
        if (temp != null)
            properties.get(key).value = value;
        else
            properties.put(key, new ValueStruct(value));
    }

    public Iterable<String> getComments(String key) {
        ValueStruct temp = properties.get(key);
        if (temp != null && temp.comments != null)
            return properties.get(key).comments;
        return Collections.emptyList();
    }
}
