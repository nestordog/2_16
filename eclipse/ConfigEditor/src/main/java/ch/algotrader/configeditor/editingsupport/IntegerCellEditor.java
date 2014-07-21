package ch.algotrader.configeditor.editingsupport;

import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

public class IntegerCellEditor extends TextCellEditor {

    IntegerCellEditor(Composite parent) {
        super(parent);
    }

    @Override
    protected Object doGetValue() {
        return Integer.valueOf((String) super.doGetValue());
    }

    @Override
    protected void doSetValue(Object value) {
        super.doSetValue(value.toString());
    }
}
