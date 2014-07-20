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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import ch.algotrader.configeditor.editingSupport.CellEditorExtensionPoint;
import ch.algotrader.configeditor.editingSupport.CellEditorFactory;

public class EditorPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {

    private class ListContentProvider implements IStructuredContentProvider {
        private List<File> files;

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            if (newInput == null)
                files = null;
            else
                try {
                    files = getFiles((IProject) newInput);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
        }

        @Override
        public Object[] getElements(Object inputElement) {
            return files.toArray();
        }

        private List<File> getFiles(IProject project) throws IOException {
            File file = project.getFile("META-INF/" + project.getName() + ".hierarchy").getLocation().toFile();
            String[] fileNames = null;
            BufferedReader br = new BufferedReader(new FileReader(file));
            try {
                fileNames = br.readLine().split(":");
            } finally {
                br.close();
            }
            List<File> files = new ArrayList<File>();
            for (String fname : fileNames)
                files.add(project.getFile("META-INF/" + fname + ".properties").getLocation().toFile());
            return files;
        }
    }

    private class TableContentProvider implements IStructuredContentProvider {
        private Object[] elements;

        @Override
        public void dispose() {
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            if (newInput == null)
                elements = null;
            else {
                if (editorData.containsKey(newInput)) {
                    elements = editorData.get(newInput);
                } else {
                    StructuredProperties structProps = new StructuredProperties();
                    propMap.put((File) newInput, structProps);
                    try {
                        structProps.load((File) newInput);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    List elementsList = new ArrayList();
                    for (String key : structProps.getKeys()) {
                        Object v = structProps.getValue(key);
                        System.out.println("ContentProvider inputChanged, key=" + key + ", value-id=" + System.identityHashCode(v) + ", value=" + v);
                        elementsList.add(new Object[] { key, v });
                    }
                    elements = elementsList.toArray();
                    editorData.put((File) newInput, elements);
                }
            }
        }

        @Override
        public Object[] getElements(Object inputElement) {
            return elements;
        }

        @SuppressWarnings("unused")
        public Properties getData() {
            return null;

        }
    }

    class ValueEditor extends EditingSupport {

        private final CellEditor defaultEditor;

        public ValueEditor(TableViewer viewer) {
            super(viewer);
            this.defaultEditor = new TextCellEditor(viewer.getTable());
        }

        @Override
        protected void setValue(Object element, Object value) {
            Object[] row = (Object[]) element;
            System.out.println("ValueEditor.setValue, value-id: " + System.identityHashCode(value) + ", value: " + value.toString());
            row[1] = value;
            tableViewer.update(element, null);
        }

        @Override
        protected Object getValue(Object element) {
            Object[] row = (Object[]) element;
            System.out.println("getting value: " + row[1].toString());
            return row[1];
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            Object[] row = (Object[]) element;
            FieldModel model = new FieldModel((String) row[0], propMap.get(getSelectedFile()).getValueStruct((String) row[0]));
            System.out.println(model.getDatatype() + " editor has been created");
            try {
                CellEditorFactory factory = CellEditorExtensionPoint.createCellEditorFactory(model.getDatatype());
                if (factory == null)
                    return defaultEditor;
                System.out.println("and returned");
                return factory.createCellEditor(tableViewer.getTable());

            } catch (InvalidRegistryObjectException | CoreException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }
    }

    private TableViewer tableViewer;
    private ListViewer listViewer;
    private Map<File, Object[]> editorData = new HashMap<File, Object[]>();
    private Map<File, StructuredProperties> propMap = new HashMap<File, StructuredProperties>();

    public EditorPropertyPage() {
    }

    @Override
    protected Control createContents(Composite parent) {

        SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);

        listViewer = new ListViewer(sashForm, SWT.BORDER);

        listViewer.setContentProvider(new ListContentProvider());
        listViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return ((File) element).getName().split("\\.")[0];
            }
        });

        listViewer.setInput(getProject());

        listViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                tableViewer.setInput(getSelectedFile());

            }
        });

        tableViewer = new TableViewer(sashForm, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

        sashForm.setWeights(new int[] { 1, 2 });

        Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableViewerColumn colKey = new TableViewerColumn(tableViewer, SWT.NONE);
        colKey.getColumn().setWidth(200);
        colKey.getColumn().setText("Key");
        colKey.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                Object[] row = (Object[]) element;
                return (new FieldModel((String) row[0], propMap.get(getSelectedFile()).getValueStruct((String) row[0]))).getLabel();
            }
        });

        TableViewerColumn colValue = new TableViewerColumn(tableViewer, SWT.NONE);
        colValue.getColumn().setWidth(200);
        colValue.getColumn().setText("Value");
        colValue.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                Object[] row = (Object[]) element;
                String dataType = (new FieldModel((String) row[0], propMap.get(getSelectedFile()).getValueStruct((String) row[0]))).getDatatype();
                switch (dataType) {
                    case "Date": {
                        Date d = (Date) row[1];
                        DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                        return formatter.format(d);
                    }
                    case "Time": {
                        Date d = (Date) row[1];
                        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                        return formatter.format(d);
                    }
                    default: {
                        return row[1].toString();
                    }
                }
            }
        });
        colValue.setEditingSupport(new ValueEditor(tableViewer));

        tableViewer.setContentProvider(new TableContentProvider());
        return sashForm;
    }

    private File getSelectedFile() {
        return (File) ((StructuredSelection) listViewer.getSelection()).getFirstElement();
    }

    public void save() throws IOException {
        for (File file : editorData.keySet()) {
            Object[] elements = editorData.get(file);
            StructuredProperties structuredProps = propMap.get(file);
            for (int i = 0; i < elements.length; i++) {
                Object[] row = (Object[]) elements[i];
                // TODO: take care of date formats!
                structuredProps.setValue((String) row[0], row[1].toString());
            }
            structuredProps.save(file);
        }
    }

    public LinkedHashMap<String, Object> getHashMap() {
        LinkedHashMap<String, Object> m = new LinkedHashMap<String, Object>();
        Object[] elements = (Object[]) tableViewer.getInput();
        for (int i = 0; i < elements.length; i++) {
            Object[] row = (Object[]) elements[i];
            m.put((String) row[0], row[1]);
        }
        return m;
    }

    public Iterable<java.io.File> getFiles() {
        ListContentProvider t = new ListContentProvider();
        try {
            return t.getFiles(getProject());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public Properties getInMemoryData(File f) {
        Object[] elements = editorData.get(f);
        Properties p = new Properties();
        for (int i = 0; i < elements.length; i++) {
            Object[] row = (Object[]) elements[i];
            p.put(row[0], row[1]);
        }
        return p;
    }

    public IProject getProject() {
        if (this.getElement() instanceof IProject)
            return (IProject) this.getElement();
        return ((IProjectNature) this.getElement()).getProject();
    }

    @Override
    protected void performApply() {
        try {
            save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        super.performApply();
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
    }

    @Override
    public boolean performOk() {
        try {
            save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return super.performOk();
    }
}
