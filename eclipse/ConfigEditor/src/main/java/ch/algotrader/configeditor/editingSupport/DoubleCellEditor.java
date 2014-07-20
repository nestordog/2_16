package ch.algotrader.configeditor.editingSupport;

import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

public class DoubleCellEditor extends TextCellEditor {

    DoubleCellEditor(Composite parent) {
        super(parent);
    }

    @Override
    protected Object doGetValue() {
        return Double.valueOf((String) super.doGetValue());
    }

    @Override
    protected void doSetValue(Object value) {
        super.doSetValue(value.toString());
    }
}
