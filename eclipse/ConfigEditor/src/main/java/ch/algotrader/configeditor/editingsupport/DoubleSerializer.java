package ch.algotrader.configeditor.editingsupport;

import ch.algotrader.configeditor.IPropertySerializer;

public class DoubleSerializer implements IPropertySerializer {

    @Override
    public Object deserialize(String propValue) {
        if (propValue.contains(","))
            propValue = propValue.replace(',', '.');
        return new Double(propValue);
    }

    @Override
    public String serialize(Object propObject) {
        return propObject.toString();
    }
}
