package ch.algotrader.configeditor.editingSupport;

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;

public class CellEditorExtensionPoint {

    public static CellEditorFactory createCellEditorFactory(String dataType) throws InvalidRegistryObjectException, CoreException {
        IConfigurationElement config = getConfig(dataType);
        if (config != null) {
            CellEditorFactory factory = (CellEditorFactory) config.createExecutableExtension("factory");
            if (factory instanceof ISetDataType)
                ((ISetDataType) factory).setDataType(dataType);
            return factory;
        }
        return null;
    }

    private static IConfigurationElement getConfig(String dataType) {
        IExtensionRegistry reg = Platform.getExtensionRegistry();
        IConfigurationElement[] extensions = reg.getConfigurationElementsFor("ch.algotrader.ConfigEditor.CellEditor");
        for (int i = 0; i < extensions.length; i++) {
            IConfigurationElement element = extensions[i];
            if (element.getAttribute("dataType").equals(dataType)) {
                return element;
            }
        }
        return null;
    }

    public static String getRegex(String dataType) {
        IConfigurationElement config = getConfig(dataType);
        if (config != null)
            return config.getAttribute("regex");
        return null;
    }

    public static String getRegexErrorMessage(String dataType, String value) {
        String regexErrorMessage;
        IConfigurationElement config = getConfig(dataType);
        assert config != null;
        regexErrorMessage = config.getAttribute("regexErrorMessage");
        if (regexErrorMessage == null)
            regexErrorMessage = "User input ''{0}'' does not satisfy pattern {1}";
        return MessageFormat.format(regexErrorMessage, value, config.getAttribute("regex"));
    }
}
