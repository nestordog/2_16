package ch.algotrader.configeditor.editingSupport;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;

public class DateTimeCellEditorFactory implements CellEditorFactory {

    @Override
    public CellEditor createCellEditor(Composite parent) {
        return new DateTimeCellEditor(parent);
    }
}
