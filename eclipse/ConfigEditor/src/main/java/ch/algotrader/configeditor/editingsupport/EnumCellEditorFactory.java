package ch.algotrader.configeditor.editingsupport;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;

public class EnumCellEditorFactory implements CellEditorFactory, ISetDataType {

    String enumClass;

    @Override
    public CellEditor createCellEditor(Composite parent) {
        try {
            return new EnumCellEditor(parent, enumClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setDataType(String dataType) {
        this.enumClass = dataType;
    }
}
