package ch.algotrader.configeditor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

import ch.algotrader.configeditor.editingSupport.CellEditorExtensionPoint;
import ch.algotrader.configeditor.editingSupport.CellEditorFactory;

class TableEditingSupport extends EditingSupport {

    private final EditorPropertyPage propertyPage;

    public TableEditingSupport(EditorPropertyPage propertyPage) {
        super(propertyPage.tableViewer);
        this.propertyPage = propertyPage;
    }

    @Override
    protected boolean canEdit(Object element) {
        return true;
    }

    @Override
    protected CellEditor getCellEditor(Object element) {
        Object[] row = (Object[]) element;
        String key = (String) row[0];
        FieldModel model = new FieldModel(propertyPage.getSelectedProperties().getValueStruct(key));
        try {
            CellEditor editor;
            CellEditorFactory factory = CellEditorExtensionPoint.createCellEditorFactory(model.getDatatype());
            if (factory == null)
                editor = new TextCellEditor(getViewer().getTable());
            else
                editor = factory.createCellEditor(getViewer().getTable());
            editor.setValidator(new CellEditorValidator(propertyPage, model.getDatatype(), key));
            editor.addListener(new CellEditorListener(propertyPage, editor));
            return editor;

        } catch (InvalidRegistryObjectException | CoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Object getValue(Object element) {
        Object[] row = (Object[]) element;
        return row[1];
    }

    // overridden to return proper type
    @Override
    public TableViewer getViewer() {
        return (TableViewer) super.getViewer();
    }

    @Override
    protected void setValue(Object element, Object value) {
        Object[] row = (Object[]) element;
        row[1] = value;
        getViewer().update(element, null);
    }
}
