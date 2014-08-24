/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.configeditor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Configuration editor, implemented as PropertyPage.
 *
 * @author <a href="mailto:ahihlovskiy@algotrader.ch">Andrey Hihlovskiy</a>
 *
 * @version $Revision$ $Date$
 */
public class EditorPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {

    protected ListViewer fileListViewer;
    protected TableViewer propertyTableViewer;
    protected Map<File, Object[]> editorData = new HashMap<File, Object[]>();
    protected Map<File, StructuredProperties> propMap = new HashMap<File, StructuredProperties>();
    protected IJavaProject javaProject;
    protected ProjectProperties projectProperties;

    public EditorPropertyPage() {
    }

    @Override
    protected Control createContents(Composite parent) {

        IProject project;
        if (this.getElement() instanceof IProject)
            project = (IProject) this.getElement();
        else {
            assert this.getElement() instanceof IProjectNature;
            project = ((IProjectNature) this.getElement()).getProject();
        }
        if (project instanceof IJavaProject)
            javaProject = (IJavaProject) project;
        else
            javaProject = JavaCore.create(project);

        projectProperties = new ProjectProperties(javaProject);

        SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);

        final Composite listComposite = new Composite(sashForm, SWT.NONE);

        GridLayout layout = new GridLayout(1, true);
        listComposite.setLayout(layout);

        fileListViewer = new ListViewer(listComposite, SWT.BORDER);
        fileListViewer.getControl().setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

        fileListViewer.setContentProvider(new FileListContentProvider(this));
        fileListViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return ((File) element).getName().split("\\.")[0];
            }
        });

        fileListViewer.setInput(javaProject);

        fileListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                propertyTableViewer.setInput(getSelectedFile());
            }
        });

        Label listLabel = new Label(listComposite, SWT.WRAP | SWT.BORDER);
        listLabel.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
        listLabel.setBackground(getControl().getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        listLabel.setForeground(getControl().getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
        listLabel.setText("Files are ordered by priority.\nProperties defined in the\nupper file override the\n properties in the lower files");
        listLabel.setBounds(0, 0, 200, 0);

        propertyTableViewer = new TableViewer(sashForm, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

        sashForm.setWeights(new int[] { 2, 5 });

        Table table = propertyTableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.addListener(SWT.MeasureItem, new Listener() {
            public void handleEvent(Event event) {
                // tweak table row height
                event.height = 24;
            }
        });

        TableViewerColumn colKey = new TableViewerColumn(propertyTableViewer, SWT.NONE);
        colKey.getColumn().setWidth(200);
        colKey.getColumn().setText("Key");
        colKey.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                Object[] row = (Object[]) element;
                String key = (String) row[0];
                String label = getFieldModel(getSelectedFile(), key).getLabel();
                if (label == null)
                    return row[0].toString();
                return label;
            }
        });

        TableViewerColumn colValue = new TableViewerColumn(propertyTableViewer, SWT.NONE);
        colValue.getColumn().setWidth(200);
        colValue.getColumn().setText("Value");
        colValue.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                Object[] row = (Object[]) element;
                String key = (String) row[0];
                Object value = row[1];
                String propertyId = getFieldModel(getSelectedFile(), key).getPropertyId();
                return projectProperties.serialize(propertyId, value);
            }
        });
        colValue.setEditingSupport(new PropertyTableEditingSupport(this));

        propertyTableViewer.setContentProvider(new PropertyTableContentProvider(this));
        return sashForm;
    }

    PropertyModel getFieldModel(File file, String key) {
        return new PropertyModel(propMap.get(file).getValueStruct(key));
    }

    File getSelectedFile() {
        return (File) ((StructuredSelection) fileListViewer.getSelection()).getFirstElement();
    }

    StructuredProperties getSelectedProperties() {
        return propMap.get(getSelectedFile());
    }

    public boolean save() throws IOException {

        for (File file : editorData.keySet()) {
            Object[] elements = editorData.get(file);
            for (int i = 0; i < elements.length; i++) {
                Object[] row = (Object[]) elements[i];
                String key = (String) row[0];
                PropertyModel model = getFieldModel(file, key);
                CellEditorValidator validator = new CellEditorValidator(key, model);
                String validationMessage = validator.isValid(row[1]);
                if (validationMessage != null) {
                    fileListViewer.setSelection(new StructuredSelection(file));
                    propertyTableViewer.setSelection(new StructuredSelection((Object) row), true);
                    propertyTableViewer.getControl().setFocus();
                    this.setErrorMessage(validationMessage);
                    return false;
                }
            }
        }

        // if we get here, then all properties were validated without errors
        this.setErrorMessage(null);

        for (File file : editorData.keySet()) {
            Object[] elements = editorData.get(file);
            StructuredProperties structuredProps = propMap.get(file);
            for (int i = 0; i < elements.length; i++) {
                Object[] row = (Object[]) elements[i];
                structuredProps.setValue((String) row[0], row[1]);
            }
            structuredProps.save(file);
            ProjectUtils.refreshContainerOfFile(file);
        }
        return true;
    }

    public LinkedHashMap<String, Object> getHashMap() {
        LinkedHashMap<String, Object> m = new LinkedHashMap<String, Object>();
        Object[] elements = (Object[]) propertyTableViewer.getInput();
        for (int i = 0; i < elements.length; i++) {
            Object[] row = (Object[]) elements[i];
            m.put((String) row[0], row[1]);
        }
        return m;
    }

    public Iterable<java.io.File> getFiles() {
        FileListContentProvider t = new FileListContentProvider(this);
        try {
            return t.getFiles(javaProject, null);
        } catch (Exception e) {
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

    @Override
    public boolean performOk() {
        try {
            return save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
