package ch.algotrader.configeditor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

public class TextCellEditorFactory implements CellEditorFactory {

    @Override
    public CellEditor createCellEditor(Composite parent) {
        return new TextCellEditor(parent);
    }
}
