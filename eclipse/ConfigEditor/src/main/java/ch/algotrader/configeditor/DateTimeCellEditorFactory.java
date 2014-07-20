package ch.algotrader.configeditor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class DateTimeCellEditorFactory implements CellEditorFactory {

    @Override
    public CellEditor createCellEditor(Composite parent) {
        return new DateCellEditor(parent, SWT.DATE | SWT.DROP_DOWN);
    }
}
