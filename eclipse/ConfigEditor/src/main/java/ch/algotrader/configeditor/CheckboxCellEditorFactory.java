package ch.algotrader.configeditor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.swt.widgets.Composite;

public class CheckboxCellEditorFactory implements CellEditorFactory {

    @Override
    public CellEditor createCellEditor(Composite parent) {
        return new CheckboxCellEditor(parent);
    }

}
