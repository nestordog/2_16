package ch.algotrader.configeditor;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FieldModel {
    ValueStruct values;

    public FieldModel(ValueStruct pValues) {
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

    @SuppressWarnings("unchecked")
    public String getLabel() {
        Map<String, Object> definition = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            if (values.comments == null || values.comments.isEmpty())
                return "No Label";
            definition = mapper.readValue(values.comments.get(0), Map.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Object label = definition.get("label");
        return label == null ? null : label.toString();
    }
}
