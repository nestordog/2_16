package configeditor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
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

public class EditorPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {

    private final class ListContentProvider implements IStructuredContentProvider {
        List<File> files;

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

    private final class TableContentProvider implements IStructuredContentProvider {

        private Object[] elements;

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            if (newInput == null)
                elements = null;
            else {
                PropertiesConfiguration config = null;
                try {
                    config = new PropertiesConfiguration((File) newInput);
                } catch (ConfigurationException e1) {
                    new RuntimeException(e1);
                }

                List elementsList = new ArrayList();
                Iterator<String> it = config.getKeys();
                while (it.hasNext()) {
                    String key = it.next();
                    elementsList.add(new Object[] { key, config.getString(key) });
                }
                elements = elementsList.toArray();
            }
        }

        @Override
        public Object[] getElements(Object inputElement) {
            return elements;
        }

        public void save() {
            if (elements == null)
                return;
            PropertiesConfiguration config = new PropertiesConfiguration();
            for (int i = 0; i < elements.length; i++) {
                Object[] row = (Object[]) elements[i];
                config.setProperty((String) row[0], row[1]);
            }
            try {
                config.save(getSelectedFile());
            } catch (ConfigurationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    class ValueEditor extends EditingSupport {

        private final CellEditor editor;

        public ValueEditor(TableViewer viewer) {
            super(viewer);
            this.editor = new TextCellEditor(viewer.getTable());
        }

        @Override
        protected void setValue(Object element, Object value) {
            Object[] row = (Object[]) element;
            row[1] = value;
            tableViewer.update(element, null);
        }

        @Override
        protected Object getValue(Object element) {
            Object[] row = (Object[]) element;
            return row[1];
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            return editor;
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }
    }

    private TableViewer tableViewer;
    private ListViewer listViewer;

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

        listViewer.setInput(((IProjectNature) this.getElement()).getProject());

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
                return super.getText(row[0]);
            }
        });

        TableViewerColumn colValue = new TableViewerColumn(tableViewer, SWT.NONE);
        colValue.getColumn().setWidth(200);
        colValue.getColumn().setText("Value");
        colValue.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                Object[] row = (Object[]) element;
                return super.getText(row[1]);
            }
        });
        colValue.setEditingSupport(new ValueEditor(tableViewer));

        tableViewer.setContentProvider(new TableContentProvider());
        return sashForm;
    }

    private File getSelectedFile() {
        return (File) ((StructuredSelection) listViewer.getSelection()).getFirstElement();
    }

    public void save() {
        ((TableContentProvider) tableViewer.getContentProvider()).save();
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

    @Override
    protected void performApply() {
        save();
        super.performApply();
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
    }

    @Override
    public boolean performOk() {
        save();
        return super.performOk();
    }

}
