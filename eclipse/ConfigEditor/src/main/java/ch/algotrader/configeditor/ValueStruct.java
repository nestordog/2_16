package ch.algotrader.configeditor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ValueStruct {
    public Object value;
    public List<String> comments;
    public String inlineComment;

    public ValueStruct() {
        comments = new ArrayList<String>();
    }

    public ValueStruct(Object pValue) {
        comments = new ArrayList<String>();
        value = pValue;
    }

    public String getSaveReadyValue() {
        FieldModel f = new FieldModel(this);
        String dataType = f.getDatatype();
        switch (dataType) {
            case "Date": {
                Date d = (Date) value;
                DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                return formatter.format(d);
            }
            case "Time": {
                Date d = (Date) value;
                DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                return formatter.format(d);
            }
            case "DateTime": {
                Date d = (Date) value;
                DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                return formatter.format(d);
            }
            default:
                return value.toString();
        }
    }
}
