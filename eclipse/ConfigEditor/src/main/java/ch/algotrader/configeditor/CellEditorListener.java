package ch.algotrader.configeditor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.TextCellEditor;

class CellEditorListener implements ICellEditorListener {

    private final EditorPropertyPage propertyPage;
    private final CellEditor cellEditor;

    CellEditorListener(EditorPropertyPage propertyPage, CellEditor cellEditor) {
        this.propertyPage = propertyPage;
        this.cellEditor = cellEditor;
    }

    @Override
    public void applyEditorValue() {
        propertyPage.setErrorMessage(null);
    }

    @Override
    public void cancelEditor() {
        propertyPage.setErrorMessage(null);
    }

    @Override
    public void editorValueChanged(boolean oldValidState, boolean newValidState) {
        if (newValidState)
            propertyPage.setErrorMessage(null);
        else if (cellEditor instanceof TextCellEditor)
            propertyPage.setErrorMessage(((TextCellEditor) cellEditor).getErrorMessage());
    }
}
