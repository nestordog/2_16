package ch.algotrader.configeditor.editingSupport;

import org.eclipse.jface.viewers.TextCellEditor;

public class IntegerCellEditor extends TextCellEditor {

    @Override
    protected Object doGetValue() {
        return Integer.valueOf((String) super.doGetValue());
    }

    @Override
    protected void doSetValue(Object value) {
        super.doSetValue(value.toString());
    }
}
