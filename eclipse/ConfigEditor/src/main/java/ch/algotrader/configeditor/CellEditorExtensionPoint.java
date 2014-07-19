package ch.algotrader.configeditor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Composite;

public class CellEditorExtensionPoint {

    public static CellEditorFactory createCellEditorFactory(String dataType, Composite parent) throws InvalidRegistryObjectException, CoreException {
        IExtensionRegistry reg = Platform.getExtensionRegistry();
        IConfigurationElement[] extensions = reg.getConfigurationElementsFor("ch.algotrader.ConfigEditor.CellEditor");
        for (int i = 0; i < extensions.length; i++) {
            IConfigurationElement element = extensions[i];
            if (element.getAttribute("dataType").equals(dataType)) {
                return (CellEditorFactory) element.createExecutableExtension("factory");
            }
        }
        return null;
    }
}
