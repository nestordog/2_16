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
import java.util.List;
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
    private List<String> trailingComments = null;

    public StructuredProperties(EditorPropertyPage propertyPage) {
        properties = new LinkedHashMap<String, ValueStruct>();
        this.propertyPage = propertyPage;
    }

    public Iterable<String> getComments(String key) {
        ValueStruct temp = properties.get(key);
        if (temp != null && temp.comments != null)
            return properties.get(key).comments;
        return Collections.emptyList();
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

    public void load(File f, Collection<String> errorMessages) throws Exception {
        ValueStruct struct = new ValueStruct();
        BufferedReader reader = new BufferedReader(new FileReader(f));
        try {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.isEmpty())
                    struct.comments.add("");
                else if (line.charAt(0) == '#') {
                    struct.comments.add(line);
                } else {
                    try {
                        parseKeyValueLine(struct, line);
                    } catch (Exception e) {
                        if (errorMessages == null)
                            throw e;
                        String errMessage = MessageFormat.format("Error reading file ''{0}'': {1}", f.getName(), (e.getMessage() == null ? e.getClass().getName() : e.getMessage()));
                        errorMessages.add(errMessage);
                    }
                    struct = new ValueStruct();
                }
            }
        } finally {
            reader.close();
        }
        if (!struct.comments.isEmpty())
            trailingComments = struct.comments;
    }

    private void parseKeyValueLine(ValueStruct struct, String line) throws Exception {
        boolean escape = false;
        boolean isKey = true;
        StringBuilder keyBuffer = new StringBuilder();
        StringBuilder valueBuffer = new StringBuilder();
        String inlineComment = null;
        for (int i = 0; i < line.length(); i++) {

            if (escape) {
                escape = false;
                if (isKey)
                    keyBuffer.append(line.charAt(i));
                else
                    valueBuffer.append(line.charAt(i));
            } else if (line.charAt(i) == '\\') {
                escape = true;
            } else if (line.charAt(i) == '=') {
                isKey = false;
            } else if (line.charAt(i) == '#') {
                inlineComment = line.substring(i + 1);
                break;
            } else {
                if (isKey)
                    keyBuffer.append(line.charAt(i));
                else
                    valueBuffer.append(line.charAt(i));
            }
        }
        String key = keyBuffer.toString();
        String value = valueBuffer.toString();

        int firstNonSpaceIndex = key.length();
        for (int i = 0; i < key.length(); i++)
            if (!Character.isWhitespace(key.charAt(i))) {
                firstNonSpaceIndex = i;
                break;
            }
        struct.keyLeadingSpaces = key.substring(0, firstNonSpaceIndex);
        if (firstNonSpaceIndex != key.length()) {
            int lastNonSpaceIndex = -1;
            for (int i = key.length() - 1; i >= 0; i--)
                if (!Character.isWhitespace(key.charAt(i))) {
                    lastNonSpaceIndex = i;
                    break;
                }
            struct.keyTrailingSpaces = key.substring(lastNonSpaceIndex + 1, key.length());
        } else
            struct.keyTrailingSpaces = "";

        firstNonSpaceIndex = value.length();
        for (int i = 0; i < value.length(); i++)
            if (!Character.isWhitespace(value.charAt(i))) {
                firstNonSpaceIndex = i;
                break;
            }
        struct.valueLeadingSpaces = value.substring(0, firstNonSpaceIndex);
        if (firstNonSpaceIndex != value.length()) {
            int lastNonSpaceIndex = -1;
            for (int i = value.length() - 1; i >= 0; i--)
                if (!Character.isWhitespace(value.charAt(i))) {
                    lastNonSpaceIndex = i;
                    break;
                }
            struct.valueTrailingSpaces = value.substring(lastNonSpaceIndex + 1, value.length());
        } else
            struct.valueTrailingSpaces = "";

        String propertyId = new PropertyModel(struct).getPropertyId();
        struct.value = propertyPage.projectProperties.deserialize(propertyId, value.trim());
        if (inlineComment != null)
            struct.inlineComment = inlineComment.trim();
        properties.put(key.trim(), struct);
    }

    public void save(File f) throws IOException {
        PrintWriter out = new PrintWriter(f);
        try {
            for (String key : properties.keySet()) {
                ValueStruct struct = properties.get(key);
                writeComments(out, struct.comments);
                out.print(struct.keyLeadingSpaces);
                out.print(key);
                out.print(struct.keyTrailingSpaces);
                out.print("=");
                out.print(struct.valueLeadingSpaces);
                out.print(struct.getSaveReadyValue(propertyPage.projectProperties));
                out.print(struct.valueTrailingSpaces);
                if (struct.inlineComment != null) {
                    out.print("#");
                    out.print(struct.inlineComment);
                }
                out.println();
            }
            if (trailingComments != null)
                writeComments(out, trailingComments);
        } finally {
            out.close();
        }
    }

    public void setValue(String key, Object value) {
        ValueStruct temp = properties.get(key);
        if (temp != null)
            properties.get(key).value = value;
        else
            properties.put(key, new ValueStruct(value));
    }

    private void writeComments(PrintWriter out, List<String> comments) {
        for (int i = 0; i < comments.size(); i++)
            out.println(comments.get(i));
    }
}
