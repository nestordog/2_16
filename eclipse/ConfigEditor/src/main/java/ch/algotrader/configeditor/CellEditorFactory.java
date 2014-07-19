package ch.algotrader.configeditor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;

public interface CellEditorFactory {
    CellEditor createCellEditor(Composite parent);
}
