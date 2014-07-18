package configeditor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StructuredProperties {
    private class ValueStruct {
        private String value;
        private List<String> comments;
        @SuppressWarnings("unused") private String inlineComment;

        public ValueStruct() {
            comments = new ArrayList<String>();
        }

        public ValueStruct(String pValue) {
            comments = new ArrayList<String>();
            value = pValue;
        }
    }

    private Map<String, ValueStruct> properties;

    public StructuredProperties() {
        properties = new HashMap<String, ValueStruct>();
    }

    public void load(File f) {
        ValueStruct n = new ValueStruct();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(f));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.charAt(0) == '#') {
                    n.comments.add(line.substring(1));
                } else {
                    parseKeyValueLine(n, line);
                    n = new ValueStruct();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void parseKeyValueLine(ValueStruct n, String line) {
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
        n.value = value.toString().trim();
        if (inlineComment != null)
            n.inlineComment = inlineComment.trim();
        properties.put(key.toString().trim(), n);
    }

    public void save(File f) {
    }

    public Iterable<String> getKeys() {
        return properties.keySet();
    }

    public String getValue(String key) {
        ValueStruct temp = properties.get(key);
        if (temp != null)
            return temp.value;
        return null;
    }

    public void setValue(String key, String value) {
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
