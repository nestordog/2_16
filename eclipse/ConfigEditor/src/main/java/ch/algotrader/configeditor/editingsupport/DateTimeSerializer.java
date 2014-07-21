package ch.algotrader.configeditor.editingsupport;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.algotrader.configeditor.IPropertySerializer;

public class DateTimeSerializer implements IPropertySerializer {

    private final DateFormat format;

    DateTimeSerializer() {
        this.format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    }

    DateTimeSerializer(String format) {
        this.format = new SimpleDateFormat(format);
    }

    @Override
    public Object deserialize(String propValue) {
        try {
            return (Date) format.parse(propValue);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String serialize(Object propObject) {
        return format.format((Date) propObject);
    }
}
