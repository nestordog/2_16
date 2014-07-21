package ch.algotrader.configeditor.editingsupport;

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;

import ch.algotrader.configeditor.IPropertySerializer;

public class PropertyDefExtensionPoint {

    public static CellEditorFactory createCellEditorFactory(String propertyId) throws InvalidRegistryObjectException, CoreException {
        IConfigurationElement config = getConfig(propertyId);
        if (config != null) {
            CellEditorFactory factory = (CellEditorFactory) config.createExecutableExtension("cellEditorFactory");
            if (factory instanceof ISetDataType)
                ((ISetDataType) factory).setDataType(getDataType(propertyId));
            return factory;
        }
        return null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Object deserialize(String propertyId, String stringValue) {
        if(stringValue == null)
            return null;
        try {
            Class<?> dataClass = Class.forName(PropertyDefExtensionPoint.getDataType(propertyId));
            if(IPropertySerializer.class.isAssignableFrom(dataClass))
                return ((IPropertySerializer)dataClass.newInstance()).deserialize(stringValue);
            if (dataClass.isEnum())
                return Enum.valueOf((Class<? extends Enum>) dataClass, stringValue);
            return dataClass.getDeclaredConstructor(String.class).newInstance(stringValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static IConfigurationElement getConfig(String propertyId) {
        IExtensionRegistry reg = Platform.getExtensionRegistry();
        IConfigurationElement[] extensions = reg.getConfigurationElementsFor("ch.algotrader.ConfigEditor.PropertyDef");
        for (int i = 0; i < extensions.length; i++) {
            IConfigurationElement element = extensions[i];
            if (element.getAttribute("id").equals(propertyId)) {
                return element;
            }
        }
        return null;
    }

    public static String getRegex(String propertyId) {
        IConfigurationElement config = getConfig(propertyId);
        if (config != null)
            return config.getAttribute("regex");
        return null;
    }

    public static String getRegexErrorMessage(String propertyId, String value) {
        String regexErrorMessage;
        IConfigurationElement config = getConfig(propertyId);
        assert config != null;
        regexErrorMessage = config.getAttribute("regexErrorMessage");
        if (regexErrorMessage == null)
            regexErrorMessage = "User input ''{0}'' does not satisfy pattern {1}";
        return MessageFormat.format(regexErrorMessage, value, config.getAttribute("regex"));
    }

    public static String getDataType(String propertyId) {
        IConfigurationElement config = getConfig(propertyId);
        if (config != null)
            return config.getAttribute("dataType");
        return null;
    }

    public static String serialize(String propertyId, Object value) {
        if(value == null)
            return "null";
        try {
            Class<?> dataClass = Class.forName(getDataType(propertyId));
            if(IPropertySerializer.class.isAssignableFrom(dataClass))
                return ((IPropertySerializer)dataClass.newInstance()).serialize(value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return value.toString();
    }
}
