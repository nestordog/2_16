package ch.algotrader.configeditor.editingSupport;

import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;

public class EnumCellEditor extends ComboBoxViewerCellEditor {

    public EnumCellEditor(Composite parent, final String enumClassName) throws ClassNotFoundException {
        super(parent);
        this.setContentProvider(new IStructuredContentProvider() {

            Class<?> enumClass;

            @Override
            public void dispose() {
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                enumClass = (Class<?>) newInput;
            }

            @Override
            public Object[] getElements(Object inputElement) {
                return enumClass.getEnumConstants();
            }
        });
        this.setInput(Class.forName(enumClassName));
    }
}
