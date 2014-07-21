package ch.algotrader.configeditor;

public interface IPropertySerializer {

    Object deserialize(String propValue);

    String serialize(Object propObject);
}
