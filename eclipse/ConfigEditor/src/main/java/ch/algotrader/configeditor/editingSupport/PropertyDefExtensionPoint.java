package ch.algotrader.configeditor.editingSupport;

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;

public class PropertyDefExtensionPoint {

    public static CellEditorFactory createCellEditorFactory(String id) throws InvalidRegistryObjectException, CoreException {
        IConfigurationElement config = getConfig(id);
        if (config != null) {
            CellEditorFactory factory = (CellEditorFactory) config.createExecutableExtension("cellEditorFactory");
            if (factory instanceof ISetDataType)
                ((ISetDataType) factory).setDataType(id);
            return factory;
        }
        return null;
    }

    private static IConfigurationElement getConfig(String id) {
        IExtensionRegistry reg = Platform.getExtensionRegistry();
        IConfigurationElement[] extensions = reg.getConfigurationElementsFor("ch.algotrader.ConfigEditor.PropertyDef");
        for (int i = 0; i < extensions.length; i++) {
            IConfigurationElement element = extensions[i];
            if (element.getAttribute("id").equals(id)) {
                return element;
            }
        }
        return null;
    }

    public static String getRegex(String id) {
        IConfigurationElement config = getConfig(id);
        if (config != null)
            return config.getAttribute("regex");
        return null;
    }

    public static String getRegexErrorMessage(String id, String value) {
        String regexErrorMessage;
        IConfigurationElement config = getConfig(id);
        assert config != null;
        regexErrorMessage = config.getAttribute("regexErrorMessage");
        if (regexErrorMessage == null)
            regexErrorMessage = "User input ''{0}'' does not satisfy pattern {1}";
        return MessageFormat.format(regexErrorMessage, value, config.getAttribute("regex"));
    }

    public static String getType(String id) {
        IConfigurationElement config = getConfig(id);
        if (config != null)
            return config.getAttribute("type");
        return null;
    }
}
