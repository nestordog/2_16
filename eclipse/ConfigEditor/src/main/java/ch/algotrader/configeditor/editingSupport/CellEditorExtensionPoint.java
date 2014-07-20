package ch.algotrader.configeditor.editingSupport;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;

public class CellEditorExtensionPoint {

    public static CellEditorFactory createCellEditorFactory(String dataType) throws InvalidRegistryObjectException, CoreException {
        IConfigurationElement config = getConfig(dataType);
        if (config != null)
            return (CellEditorFactory) config.createExecutableExtension("factory");
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

    public static String getRegex(String dataType) throws InvalidRegistryObjectException, CoreException {
        IConfigurationElement config = getConfig(dataType);
        if (config != null)
            return config.getAttribute("regex");
        return null;
    }
}
