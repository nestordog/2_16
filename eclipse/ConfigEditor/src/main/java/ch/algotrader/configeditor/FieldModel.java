package ch.algotrader.configeditor;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FieldModel {
    ValueStruct values;

    public FieldModel(ValueStruct pValues) {
        values = pValues;
    }

    public String getDatatype() {
        Map<String, Object> definition = getDefinition();
        if (definition == null)
            return "String";
        Object dataType = definition.get("data-type");
        return dataType == null ? null : dataType.toString();
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
        Map<String, Object> definition = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            if (values.comments != null && !values.comments.isEmpty())
                definition = mapper.readValue(values.comments.get(0), Map.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return definition;
    }
}
