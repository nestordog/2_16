// AlgoTrader: 251 wrapp tooltip error messages
/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.tools.jconsole.inspector;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Array;
import java.util.EventObject;
import java.util.HashMap;
import java.util.WeakHashMap;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import sun.tools.jconsole.JConsole;
import sun.tools.jconsole.MBeansTab;
import sun.tools.jconsole.ProxyClient.SnapshotMBeanServerConnection;
import sun.tools.jconsole.Resources;

/*IMPORTANT :
  There is a deadlock issue there if we don't synchronize well loadAttributes,
  refresh attributes and empty table methods since a UI thread can call
  loadAttributes and at the same time a JMX notification can raise an
  emptyTable. Since there are synchronization in the JMX world it's
  COMPULSORY to not call the JMX world in synchronized blocks */
@SuppressWarnings("serial")
public class XMBeanAttributes extends XTable {
    private final static String[] columnNames =
    {Resources.getText("Name"),
     Resources.getText("Value")};

    private boolean editable = true;

    private XMBean mbean;
    private MBeanInfo mbeanInfo;
    private MBeanAttributeInfo[] attributesInfo;
    private HashMap<String, Object> attributes;
    private HashMap<String, Object> unavailableAttributes;
    private HashMap<String, Object> viewableAttributes;
    private WeakHashMap<XMBean, HashMap<String, ZoomedCell>> viewersCache =
            new WeakHashMap<XMBean, HashMap<String, ZoomedCell>>();
    private TableModelListener attributesListener;
    private MBeansTab mbeansTab;
    private XTable table;
    private TableCellEditor valueCellEditor = new ValueCellEditor();
    private int rowMinHeight = -1;
    private AttributesMouseListener mouseListener = new AttributesMouseListener();

    private static TableCellEditor editor =
            new Utils.ReadOnlyTableCellEditor(new JTextField());

    public XMBeanAttributes(MBeansTab mbeansTab) {
    super();
    this.mbeansTab = mbeansTab;
    ((DefaultTableModel)getModel()).setColumnIdentifiers(columnNames);
    getModel().addTableModelListener(this.attributesListener =
                     new AttributesListener(this));
    getColumnModel().getColumn(NAME_COLUMN).setPreferredWidth(40);

    addMouseListener(this.mouseListener);
    getTableHeader().setReorderingAllowed(false);
    setColumnEditors();
        addKeyListener(new Utils.CopyKeyAdapter());
    }

    public synchronized Component prepareRenderer(TableCellRenderer renderer,
                          int row, int column) {
    //In case we have a repaint thread that is in the process of
    //repainting an obsolete table, just ignore the call.
    //It can happen when MBean selection is switched at a very quick rate
        if(row >= getRowCount())
            return null;
        else
            return super.prepareRenderer(renderer, row, column);
    }

    void updateRowHeight(Object obj, int row) {
    ZoomedCell cell = null;
    if(obj instanceof ZoomedCell) {
        cell = (ZoomedCell) obj;
        if(cell.isInited())
        setRowHeight(row, cell.getHeight());
        else
        if(this.rowMinHeight != - 1)
            setRowHeight(row, this.rowMinHeight);
    } else
        if(this.rowMinHeight != - 1)
        setRowHeight(row, this.rowMinHeight);
    }

    public synchronized TableCellRenderer getCellRenderer(int row,
            int column) {
        //In case we have a repaint thread that is in the process of
        //repainting an obsolete table, just ignore the call.
        //It can happen when MBean selection is switched at a very quick rate
        if (row >= getRowCount()) {
            return null;
        } else {
            if (column == VALUE_COLUMN) {
                Object obj = getModel().getValueAt(row, column);
                if (obj instanceof ZoomedCell) {
                    ZoomedCell cell = (ZoomedCell) obj;
                    if (cell.isInited()) {
                        DefaultTableCellRenderer renderer =
                                (DefaultTableCellRenderer) cell.getRenderer();
                        renderer.setToolTipText(getToolTip(row,column));
                        return renderer;
                    }
                }
            }
            DefaultTableCellRenderer renderer = (DefaultTableCellRenderer)
                super.getCellRenderer(row, column);
            if (!isCellError(row, column)) {
                if (!(isColumnEditable(column) && isWritable(row) &&
                      Utils.isEditableType(getClassName(row)))) {
                    renderer.setForeground(getDefaultColor());
                }
            }
            return renderer;
        }
    }

    private void setColumnEditors() {
        TableColumnModel tcm = getColumnModel();
        for (int i = 0; i < columnNames.length; i++) {
            TableColumn tc = tcm.getColumn(i);
            if (isColumnEditable(i)) {
                tc.setCellEditor(this.valueCellEditor);
            } else {
                tc.setCellEditor(editor);
            }
        }
    }

    public void cancelCellEditing() {
        TableCellEditor editor = getCellEditor();
        if (editor != null) {
            editor.cancelCellEditing();
        }
    }

    public void stopCellEditing() {
        TableCellEditor editor = getCellEditor();
        if (editor != null) {
            editor.stopCellEditing();
        }
    }

    public final boolean editCellAt(int row, int column, EventObject e) {
        boolean retVal = super.editCellAt(row, column, e);
        if (retVal) {
            TableCellEditor editor =
                    getColumnModel().getColumn(column).getCellEditor();
            if (editor == this.valueCellEditor) {
                ((JComponent) editor).requestFocus();
            }
        }
        return retVal;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        // All the cells in non-editable columns are editable
        if (!isColumnEditable(col)) {
            return true;
        }
        // Maximized zoomed cells are editable
        Object obj = getModel().getValueAt(row, col);
        if (obj instanceof ZoomedCell) {
            ZoomedCell cell = (ZoomedCell) obj;
            return cell.isMaximized();
        }
        return true;
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        if (!isCellError(row, column) && isColumnEditable(column) &&
            isWritable(row) && Utils.isEditableType(getClassName(row))) {
            super.setValueAt(value, row, column);
        }
    }

    //Table methods

    public boolean isTableEditable() {
    return true;
    }

    public void setTableValue(Object value, int row) {
    }

    public boolean isColumnEditable(int column) {
    if (column < getColumnCount()) {
        return getColumnName(column).equals(Resources.getText("Value"));
    }
    else {
        return false;
    }
    }

    public String getClassName(int row) {
    int index = convertRowToIndex(row);
    if (index != -1) {
        return this.attributesInfo[index].getType();
    }
    else {
        return null;
    }
    }


    public String getValueName(int row) {
    int index = convertRowToIndex(row);
    if (index != -1) {
        return this.attributesInfo[index].getName();
    }
    else {
        return null;
    }
    }


    public Object getValue(int row) {
    return ((DefaultTableModel) getModel()).getValueAt(row, VALUE_COLUMN);
    }

    //tool tip only for editable column
    public String getToolTip(int row, int column) {
    if (isCellError(row, column)) {
        String text = (String) this.unavailableAttributes.get(getValueName(row));
        return "<HTML>" + text.replace("-->", "<br>-->") + "</HTML>";
    }
    if (isColumnEditable(column)) {
        Object value = getValue(row);
        String tip = null;
        if (value != null) {
        tip = value.toString();
        if(isAttributeViewable(row, VALUE_COLUMN))
            tip = Resources.getText("Double click to expand/collapse")+
            ". " + tip;
        }

        return tip;
    }

    if(column == NAME_COLUMN) {
        int index = convertRowToIndex(row);
        if (index != -1) {
                return this.attributesInfo[index].getDescription();
        }
    }
    return null;
    }

    public synchronized boolean isWritable(int row) {
    int index = convertRowToIndex(row);
    if (index != -1) {
        return (this.attributesInfo[index].isWritable());
    }
    else {
        return false;
    }
    }

    /**
     * Override JTable method in order to make any call to this method
     * atomic with TableModel elements.
     */
    public synchronized int getRowCount() {
    return super.getRowCount();
    }

    public synchronized boolean isReadable(int row) {
    int index = convertRowToIndex(row);
    if (index != -1) {
        return (this.attributesInfo[index].isReadable());
    }
    else {
        return false;
    }
    }

    public synchronized boolean isCellError(int row, int col) {
    return (isColumnEditable(col) &&
        (this.unavailableAttributes.containsKey(getValueName(row))));
    }

    public synchronized boolean isAttributeViewable(int row, int col) {
    boolean isViewable = false;
    if(col == VALUE_COLUMN) {
        Object obj = getModel().getValueAt(row, col);
        if(obj instanceof ZoomedCell)
        isViewable = true;
    }

    return isViewable;
    }

    public void loadAttributes(final XMBean mbean, MBeanInfo mbeanInfo) {
    // To avoid deadlock with events coming from the JMX side,
    // we retrieve all JMX stuff in a non synchronized block.

        if(mbean == null) return;

    final MBeanAttributeInfo[] attributesInfo = mbeanInfo.getAttributes();
    final HashMap<String, Object> attributes =
        new HashMap<String, Object>(attributesInfo.length);
    final HashMap<String, Object> unavailableAttributes =
        new HashMap<String, Object>(attributesInfo.length);
    final HashMap<String, Object> viewableAttributes =
        new HashMap<String, Object>(attributesInfo.length);
    AttributeList list = null;

        try {
            list = mbean.getAttributes(attributesInfo);
        } catch (Exception e) {
            if (JConsole.isDebug()) {
                System.err.println("Error calling getAttributes() on MBean \"" +
                        mbean.getObjectName() + "\". JConsole will " +
                        "try to get them individually calling " +
                        "getAttribute() instead. Exception:");
                e.printStackTrace(System.err);
            }
        list = new AttributeList();
        //Can't load all attributes, do it one after each other.
        for (MBeanAttributeInfo element : attributesInfo) {
        String name = null;
        try {
            name = element.getName();
                    Object value =
                            mbean.getMBeanServerConnection().getAttribute(mbean.getObjectName(), name);
            list.add(new Attribute(name, value));
        }catch(Exception ex) {
            if(element.isReadable()) {
            unavailableAttributes.put(name,
                          Utils.getActualException(ex).
                          toString());
            }
        }
        }
    }
    try {
        int att_length = list.size();
        for (int i=0;i<att_length;i++) {
        Attribute attribute = (Attribute) list.get(i);
        if(isViewable(attribute)) {
            viewableAttributes.put(attribute.getName(),
                       attribute.getValue());
        }
        else
            attributes.put(attribute.getName(),attribute.getValue());

        }
        // if not all attributes are accessible,
        // check them one after the other.
        if (att_length < attributesInfo.length) {
        for (MBeanAttributeInfo element : attributesInfo) {
            MBeanAttributeInfo attributeInfo = element;
            if (!attributes.containsKey(attributeInfo.getName()) &&
            !viewableAttributes.containsKey(attributeInfo.
                            getName()) &&
            !unavailableAttributes.containsKey(attributeInfo.
                               getName())) {
            if (attributeInfo.isReadable()) {
                // getAttributes didn't help resolving the
                // exception.
                // We must call it again to understand what
                // went wrong.
                try {
                                Object v =
                                        mbean.getMBeanServerConnection().getAttribute(
                                        mbean.getObjectName(), attributeInfo.getName());
                //What happens if now it is ok?
                // Be pragmatic, add it to readable...
                attributes.put(attributeInfo.getName(),
                           v);
                }catch(Exception e) {
                //Put the exception that will be displayed
                // in tooltip
                unavailableAttributes.put(attributeInfo.
                              getName(),
                              Utils.
                              getActualException(e)
                              .toString());
                }
            }
            }
        }
        }
    }
    catch(Exception e) {
        //sets all attributes unavailable except the writable ones
        for (MBeanAttributeInfo attributeInfo : attributesInfo) {
        if (attributeInfo.isReadable()) {
            unavailableAttributes.put(attributeInfo.getName(),
                          Utils.getActualException(e).
                          toString());
        }
        }
    }
    //end of retrieval

    //one update at a time
    synchronized(this) {

        this.mbean = mbean;
        this.mbeanInfo = mbeanInfo;
        this.attributesInfo = attributesInfo;
        this.attributes = attributes;
        this.unavailableAttributes = unavailableAttributes;
        this.viewableAttributes = viewableAttributes;

            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    DefaultTableModel tableModel =
                            (DefaultTableModel) getModel();

                    // don't listen to these events
                    tableModel.removeTableModelListener(XMBeanAttributes.this.attributesListener);

                    // add attribute information
                    emptyTable();

                    addTableData(tableModel,
                                 mbean,
                                 attributesInfo,
                                 attributes,
                                 unavailableAttributes,
                                 viewableAttributes);

                    // update the model with the new data
                    tableModel.newDataAvailable(new TableModelEvent(tableModel));
                    // re-register for change events
                    tableModel.addTableModelListener(XMBeanAttributes.this.attributesListener);
                }
            });
        }
    }

    void collapse(String attributeName, final Component c) {
    final int row = getSelectedRow();
    Object obj = getModel().getValueAt(row, VALUE_COLUMN);
    if(obj instanceof ZoomedCell) {
            cancelCellEditing();
        ZoomedCell cell = (ZoomedCell) obj;
        cell.reset();
        setRowHeight(row,
             cell.getHeight());
        editCellAt(row,
               VALUE_COLUMN);
        invalidate();
        repaint();
    }
    }

    ZoomedCell updateZoomedCell(int row,
                int col) {
    Object obj = getModel().getValueAt(row, VALUE_COLUMN);
    ZoomedCell cell = null;
    if(obj instanceof ZoomedCell) {
        cell = (ZoomedCell) obj;
        if(!cell.isInited()) {
        Object elem = cell.getValue();
        String attributeName =
            (String) getModel().getValueAt(row,
                           NAME_COLUMN);
        Component comp = this.mbeansTab.getDataViewer().
                        createAttributeViewer(elem, this.mbean, attributeName, this);
        if(comp != null){
            if(this.rowMinHeight == -1)
            this.rowMinHeight = getRowHeight(row);

            cell.init(super.getCellRenderer(row, col),
                  comp,
                  this.rowMinHeight);

                    this.mbeansTab.getDataViewer().registerForMouseEvent(
                            comp, this.mouseListener);
        } else
            return cell;
        }

        cell.switchState();
        setRowHeight(row,
             cell.getHeight());

        if(!cell.isMaximized()) {
                cancelCellEditing();
        //Back to simple editor.
        editCellAt(row,
               VALUE_COLUMN);
        }

        invalidate();
        repaint();
    }
    return cell;
    }

    public void refreshAttributes() {
        SnapshotMBeanServerConnection mbsc = this.mbeansTab.getSnapshotMBeanServerConnection();
        mbsc.flush();
        stopCellEditing();
        loadAttributes(this.mbean, this.mbeanInfo);
    }

    public void emptyTable() {
        synchronized(this) {
            ((DefaultTableModel) getModel()).
                    removeTableModelListener(this.attributesListener);
            super.emptyTable();
        }
    }

    private boolean isViewable(Attribute attribute) {
    Object data = attribute.getValue();
    return XDataViewer.isViewableValue(data);

    }

    synchronized void removeAttributes() {
        if (this.attributes != null) {
            this.attributes.clear();
        }
        if (this.unavailableAttributes != null) {
            this.unavailableAttributes.clear();
        }
        if (this.viewableAttributes != null) {
            this.viewableAttributes.clear();
        }
        this.mbean = null;
    }

    private ZoomedCell getZoomedCell(XMBean mbean, String attribute, Object value) {
        synchronized (this.viewersCache) {
            HashMap<String, ZoomedCell> viewers;
            if (this.viewersCache.containsKey(mbean)) {
                viewers = this.viewersCache.get(mbean);
            } else {
                viewers = new HashMap<String, ZoomedCell>();
            }
            ZoomedCell cell;
            if (viewers.containsKey(attribute)) {
                cell = viewers.get(attribute);
                cell.setValue(value);
                if (cell.isMaximized() && cell.getType() != XDataViewer.NUMERIC) {
                    // Plotters are the only viewers with auto update capabilities.
                    // Other viewers need to be updated manually.
                    Component comp =
                        this.mbeansTab.getDataViewer().createAttributeViewer(
                            value, mbean, attribute, XMBeanAttributes.this);
                    cell.init(cell.getMinRenderer(), comp, cell.getMinHeight());
                    this.mbeansTab.getDataViewer().registerForMouseEvent(comp, this.mouseListener);
                }
            } else {
                cell = new ZoomedCell(value);
                viewers.put(attribute, cell);
            }
            this.viewersCache.put(mbean, viewers);
            return cell;
        }
    }

    //will be called in a synchronzed block
    protected void addTableData(DefaultTableModel tableModel,
                                XMBean mbean,
                MBeanAttributeInfo[] attributesInfo,
                HashMap<String, Object> attributes,
                HashMap<String, Object> unavailableAttributes,
                HashMap<String, Object> viewableAttributes) {

    Object rowData[] = new Object[2];
    int col1Width = 0;
    int col2Width = 0;
        for (int i = 0; i < attributesInfo.length; i++) {
            rowData[0] = (attributesInfo[i].getName());
            if (unavailableAttributes.containsKey(rowData[0])) {
                rowData[1] = Resources.getText("Unavailable");
            } else if (viewableAttributes.containsKey(rowData[0])) {
                rowData[1] = viewableAttributes.get(rowData[0]);
                if (!attributesInfo[i].isWritable() ||
                    !Utils.isEditableType(attributesInfo[i].getType())) {
                    rowData[1] = getZoomedCell(mbean, (String) rowData[0], rowData[1]);
                }
            } else {
                rowData[1] = attributes.get(rowData[0]);
            }

            tableModel.addRow(rowData);

        //Update column width
        //
        String str = null;
        if(rowData[0] != null) {
        str = rowData[0].toString();
        if(str.length() > col1Width)
            col1Width = str.length();
        }
        if(rowData[1] != null) {
        str = rowData[1].toString();
        if(str.length() > col2Width)
            col2Width = str.length();
        }
    }
    updateColumnWidth(col1Width, col2Width);
    }

    private void updateColumnWidth(int col1Width, int col2Width) {
    TableColumnModel colModel = getColumnModel();

    //Get the column at index pColumn, and set its preferred width.
    col1Width = col1Width * 7;
    col2Width = col2Width * 7;
    if(col1Width + col2Width <
       (int) getPreferredScrollableViewportSize().getWidth())
        col2Width = (int) getPreferredScrollableViewportSize().getWidth()
        - col1Width;

    colModel.getColumn(NAME_COLUMN).setPreferredWidth(50);
    }

    class AttributesMouseListener extends MouseAdapter {

    public void mousePressed(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1) {
        if(e.getClickCount() >= 2) {

            int row = XMBeanAttributes.this.getSelectedRow();
            int col = XMBeanAttributes.this.getSelectedColumn();
            if(col != VALUE_COLUMN) return;
            if(col == -1 || row == -1) return;

            XMBeanAttributes.this.updateZoomedCell(row, col);
                }
        }
    }
    }

    class ValueCellEditor extends XTextFieldEditor {
    // implements javax.swing.table.TableCellEditor
    public Component getTableCellEditorComponent(JTable table,
                             Object value,
                             boolean isSelected,
                             int row,
                             int column) {
        Object val = value;
        if(column == VALUE_COLUMN) {
        Object obj = getModel().getValueAt(row,
                           column);
        if(obj instanceof ZoomedCell) {
            ZoomedCell cell = (ZoomedCell) obj;
            if(cell.getRenderer() instanceof MaximizedCellRenderer) {
            MaximizedCellRenderer zr =
                (MaximizedCellRenderer) cell.getRenderer();
            return zr.getComponent();
            }
        } else {
                    Component comp = super.getTableCellEditorComponent(
                            table, val, isSelected, row, column);
                    if (isCellError(row, column) ||
                        !isWritable(row) ||
                        !Utils.isEditableType(getClassName(row))) {
                        this.textField.setEditable(false);
                    }
                    return comp;
                }
        }
        return super.getTableCellEditorComponent(table,
                             val,
                             isSelected,
                             row,
                             column);
    }
        @Override
        public boolean stopCellEditing() {
            int editingRow = getEditingRow();
            int editingColumn = getEditingColumn();
            if (editingColumn == VALUE_COLUMN) {
                Object obj = getModel().getValueAt(editingRow, editingColumn);
                if (obj instanceof ZoomedCell) {
                    ZoomedCell cell = (ZoomedCell) obj;
                    if (cell.isMaximized()) {
                        this.cancelCellEditing();
                        return true;
                    }
                }
            }
            return super.stopCellEditing();
        }
    }

    class MaximizedCellRenderer extends  DefaultTableCellRenderer {
    Component comp;
    MaximizedCellRenderer(Component comp) {
        this.comp = comp;
            Dimension d = comp.getPreferredSize();
            if (d.getHeight() > 300) {
                comp.setPreferredSize(new Dimension((int) d.getWidth(), 300));
            }
    }
    public Component getTableCellRendererComponent(JTable table,
                               Object value,
                               boolean isSelected,
                               boolean hasFocus,
                               int row,
                               int column) {
        return this.comp;
    }
    public Component getComponent() {
        return this.comp;
    }
    }

    class ZoomedCell {
    TableCellRenderer minRenderer;
    MaximizedCellRenderer maxRenderer;
    int minHeight;
    boolean minimized = true;
    boolean init = false;
    int type;
    Object value;
    ZoomedCell(Object value) {
        this.type = XDataViewer.getViewerType(value);
        this.value = value;
    }

    boolean isInited() {
        return this.init;
    }

    Object getValue() {
        return this.value;
    }

        void setValue(Object value) {
            this.value = value;
        }

    void init(TableCellRenderer minRenderer,
          Component maxComponent,
          int minHeight) {
        this.minRenderer = minRenderer;
        this.maxRenderer = new MaximizedCellRenderer(maxComponent);

        this.minHeight = minHeight;
        this.init = true;
    }

    int getType() {
        return this.type;
    }

    void reset() {
        this.init = false;
        this.minimized = true;
    }

    void switchState() {
        this.minimized = !this.minimized;
    }
    boolean isMaximized() {
        return !this.minimized;
    }
    void minimize() {
        this.minimized = true;
    }

    void maximize() {
        this.minimized = false;
    }

    int getHeight() {
        if(this.minimized) return this.minHeight;
        else
        return (int) this.maxRenderer.getComponent().
            getPreferredSize().getHeight() ;
    }

        int getMinHeight() {
            return this.minHeight;
        }

        public String toString() {

        if(this.value == null) return null;

        if(this.value.getClass().isArray()) {
        String name =
            Utils.getArrayClassName(this.value.getClass().getName());
        int length = Array.getLength(this.value);
        return name + "[" + length +"]";
        }

        if(this.value instanceof CompositeData ||
           this.value instanceof TabularData)
        return this.value.getClass().getName();

        return this.value.toString();
    }

    TableCellRenderer getRenderer() {
        if(this.minimized) return this.minRenderer;
        else return this.maxRenderer;
    }

        TableCellRenderer getMinRenderer() {
            return this.minRenderer;
        }
    }

    class AttributesListener implements  TableModelListener {

    private Component component;

    public AttributesListener(Component component) {
        this.component = component;
    }

    public void tableChanged(final TableModelEvent e) {
        final TableModel model = (TableModel)e.getSource();
        // only post changes to the draggable column
        if (isColumnEditable(e.getColumn())) {
        XMBeanAttributes.this.mbeansTab.workerAdd(new Runnable() {
            public void run() {
                try {
                Object tableValue =
                    model.getValueAt(e.getFirstRow(),
                             e.getColumn());
                // if it's a String, try construct new value
                // using the defined type.
                if (tableValue instanceof String) {
                    tableValue =
                    Utils.createObjectFromString(getClassName(e.getFirstRow()), // type
                                     (String)tableValue);// value
                }
                String attributeName =
                    getValueName(e.getFirstRow());
                Attribute attribute =
                    new Attribute(attributeName,tableValue);
                XMBeanAttributes.this.mbean.setAttribute(attribute);
                }
                catch (Throwable ex) {
                                if (JConsole.isDebug()) {
                                    ex.printStackTrace();
                                }
                ex = Utils.getActualException(ex);

                String message = (ex.getMessage() != null) ? ex.getMessage() : ex.toString();
                EventQueue.invokeLater(new ThreadDialog(AttributesListener.this.component,
                                    message+"\n",
                                    Resources.getText("Problem setting attribute"),
                                    JOptionPane.ERROR_MESSAGE));
                }
                refreshAttributes();
            }
            });
        }
    }
    }
}
