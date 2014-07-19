package ch.algotrader.configeditor;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FieldModel {
    String key;
    ValueStruct values;

    public FieldModel(String pKey, ValueStruct pValues) {
        key = pKey;
        values = pValues;
    }

    @SuppressWarnings("unchecked")
    public String getDatatype() {
        Map<String, Object> definition = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            if (values.comments == null || values.comments.isEmpty())
                return "String";
            definition = mapper.readValue(values.comments.get(0), Map.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Object dataType = definition.get("data-type");
        return dataType == null ? null : dataType.toString();
    }

}
