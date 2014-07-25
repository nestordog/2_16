/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH. The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation, disassembly or reproduction of this material is strictly forbidden unless
 * prior written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH Badenerstrasse 16 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.configeditor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

import ch.algotrader.configeditor.editingsupport.CellEditorFactory;
import ch.algotrader.configeditor.editingsupport.PropertyDefExtensionPoint;

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
        FieldModel model = propertyPage.getFieldModel(propertyPage.getSelectedFile(), key);
        CellEditor editor;
        System.out.println("Creating celleditorfactory for a " + model.getPropertyId());
        CellEditorFactory factory = PropertyDefExtensionPoint.createCellEditorFactory(model.getPropertyId());
        if (factory == null)
            editor = new TextCellEditor(getViewer().getTable());
        else {
            System.out.println("Creating celleditor for a " + model.getPropertyId());
            editor = factory.createCellEditor(getViewer().getTable());
        }
        editor.setValidator(new CellEditorValidator(propertyPage, model.getPropertyId(), key));
        editor.addListener(new CellEditorListener(propertyPage, editor));
        return editor;
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
