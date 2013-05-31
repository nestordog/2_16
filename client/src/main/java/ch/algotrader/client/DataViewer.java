/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.client;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang.ArrayUtils;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class DataViewer extends JPanel {

    private static final long serialVersionUID = 2014300386194071692L;

    public DataViewer(Object object) {

        super(new GridLayout(1, 0));

        JTable table = new JTable(new TableModel(object));
        table.setPreferredScrollableViewportSize(new Dimension(500, 4 + Math.max(table.getModel().getRowCount(), 2) * 16));
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setDefaultRenderer(Date.class, new DateRenderer());
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        TableColumnAdjuster tableColumnAdjuster = new TableColumnAdjuster(table);
        tableColumnAdjuster.adjustColumns();

        // Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);

        // Add the scroll pane to this panel.
        add(scrollPane);
    }

    private static class TableModel extends AbstractTableModel {

        private static final long serialVersionUID = -1330727223337493071L;

        private String[] columnNames;
        private Class<?>[] columnClasses;
        private Object[][] cells;

        public TableModel(Object value) {

            if (value instanceof Collection) {
                parseCollection((Collection<?>) value);
            } else if (value instanceof Map) {
                parseMap((Map<?, ?>) value);
            } else if (value instanceof Object[]) {
                parseArray((Object[]) value);
            }
        }

        private void parseCollection(Collection<?> col) {

            List<Object> list = new ArrayList<Object>(col);
            Class<?> cl = list.get(0).getClass();

            if (cl.getPackage().getName().startsWith("java.lang")) {

                this.columnNames = new String[] { "" };
                this.columnClasses = new Class<?>[] { cl };
                this.cells = new Object[list.size()][1];

                for (int i = 0; i < list.size(); i++) {
                    this.cells[i][0] = list.get(i);
                }

            } else {

                Field[] allFields = new Field[0];
                do {
                    allFields = (Field[]) ArrayUtils.addAll(cl.getDeclaredFields(), allFields);
                    cl = cl.getSuperclass();
                } while (cl != null && cl != Object.class);

                AccessibleObject.setAccessible(allFields, true);
                List<Field> fields = new ArrayList<Field>();
                for (Field field : allFields) {
                    if (field.getName().startsWith("set")) {
                        continue;
                    } else if (Modifier.isTransient(field.getModifiers())) {
                        continue;
                    } else if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    fields.add(field);
                }
                this.columnNames = new String[fields.size()];
                this.columnClasses = new Class<?>[fields.size()];
                this.cells = new Object[list.size()][fields.size()];
                for (int i = 0; i < fields.size(); i++) {
                    this.columnNames[i] = fields.get(i).getName();
                    this.columnClasses[i] = convertClassIfNecessary(fields.get(i).getType());
                }
                for (int i = 0; i < list.size(); i++) {
                    Object obj = list.get(i);
                    for (int j = 0; j < fields.size(); j++) {
                        Field field = fields.get(j);
                        try {
                            this.cells[i][j] = field.get(obj);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }

        private void parseMap(Map<?, ?> map) {

            this.columnNames = new String[] { "Key", "Value" };
            this.columnClasses = new Class<?>[] { Object.class, Object.class };

            this.cells = new Object[map.keySet().size()][2];
            int i = 0;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                this.cells[i][0] = entry.getKey();
                this.cells[i][1] = entry.getValue();
                i++;
            }
        }

        private void parseArray(Object[] array) {

            // handle empty arrays
            if (array.length == 0) {
                this.columnNames = new String[0];
                this.columnClasses = new Class<?>[0];
                this.cells = new Object[0][0];

                // handle special arrays returned by "fireAndForgetQueryType1"
            } else if ((array.length == 2) && (array[0] instanceof String[][]) && array[1] instanceof Object[][]) {

                String[][] headers = (String[][]) array[0];
                Object[][] rows = (Object[][]) array[1];
                this.columnNames = new String[headers.length];
                this.columnClasses = new Class<?>[headers.length];
                for (int i = 0; i < headers.length; i++) {
                    String[] header = headers[i];
                    this.columnNames[i] = header[0];
                    this.columnClasses[i] = convertToWrapper(header[1]);
                }
                this.cells = rows;

                // handle special arrays returned by "StmtStreamNotifierType1MBean"
            } else if ((array.length == 2)
                    && (((array[0] instanceof Object[]) && ((Object[]) array[0])[0] instanceof Object[]) || array[0] == null)
                    && (((array[1] instanceof Object[]) && ((Object[]) array[1])[0] instanceof Object[]) || array[1] == null)) {

                Object[] insertStream = (Object[]) array[0];
                Object[] removeStream = (Object[]) array[1];

                Object[] object1 = (Object[]) (insertStream != null ? insertStream[0] : removeStream[0]);
                this.columnNames = new String[object1.length + 1];
                this.columnClasses = new Class<?>[object1.length + 1];
                this.columnNames[0] = "In/Out";
                this.columnClasses[0] = String.class;
                for (int i = 0; i < object1.length; i++) {
                    if (object1[i] != null) {
                        this.columnClasses[i + 1] = object1[i].getClass();
                    } else {
                        this.columnClasses[i + 1] = Object.class;
                    }
                }

                this.cells = new Object[(insertStream != null ? insertStream.length : 0) + (removeStream != null ? removeStream.length : 0)][object1.length + 1];
                int i = 0;
                if (insertStream != null) {
                    for (Object obj : insertStream) {
                        Object[] events = (Object[]) obj;
                        this.cells[i][0] = "In";
                        System.arraycopy(events, 0, this.cells[i], 1, events.length);
                        i++;
                    }
                }
                if (removeStream != null) {
                    for (Object obj : removeStream) {
                        Object[] events = (Object[]) obj;
                        this.cells[i][0] = "Out";
                        System.arraycopy(events, 0, this.cells[i], 1, events.length);
                        i++;
                    }
                }

                // handle array[][]
            } else if (array[0] instanceof Object[]) {

                Object[] cells = (Object[]) array[0];
                this.columnNames = new String[cells.length];
                this.columnClasses = new Class<?>[cells.length];
                for (int i = 0; i < cells.length; i++) {
                    if (cells[i] != null) {
                        this.columnClasses[i] = cells[i].getClass();
                    } else {
                        this.columnClasses[i] = Object.class;
                    }
                }
                this.cells = (Object[][]) array;

                // handle arrays of simple types
            } else if (isSimpleClass(array[0].getClass())) {

                this.columnNames = new String[] { "Entry" };
                this.columnClasses = new Class<?>[] { array[0].getClass() };
                this.cells = new Object[array.length][1];
                for (int i = 0; i < array.length; i++) {
                    this.cells[i][0] = array[i];
                }
            }
        }

        private Class<?> convertClassIfNecessary(Class<?> cl) {

            if (cl.isPrimitive()) {
                if (cl.equals(byte.class))
                    return Byte.class;
                if (cl.equals(short.class))
                    return Short.class;
                if (cl.equals(int.class))
                    return Integer.class;
                if (cl.equals(long.class))
                    return Long.class;
                if (cl.equals(float.class))
                    return Float.class;
                if (cl.equals(double.class))
                    return Double.class;
                if (cl.equals(boolean.class))
                    return Boolean.class;
            }
            return cl;
        }

        private boolean isSimpleClass(Class<?> cl) {

            if (cl.equals(String.class)
                    || cl.equals(Class.class)
                    || cl.equals(Boolean.class)
                    || cl.equals(Byte.class)
                    || cl.equals(Character.class)
                    || cl.equals(Short.class)
                    || cl.equals(Integer.class)
                    || cl.equals(Long.class)
                    || cl.equals(Float.class)
                    || cl.equals(Double.class)) {
                return true;
            } else {
                return false;
            }
        }

        private Class<?> convertToWrapper(String className) {

            if (className.equals("boolean"))
                return Boolean.class;
            if (className.equals("byte"))
                return Byte.class;
            if (className.equals("char"))
                return Character.class;
            if (className.equals("short"))
                return Short.class;
            if (className.equals("int"))
                return Integer.class;
            if (className.equals("long"))
                return Long.class;
            if (className.equals("float"))
                return Float.class;
            if (className.equals("double"))
                return Double.class;
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public int getColumnCount() {

            return this.columnNames.length;
        }

        @Override
        public int getRowCount() {

            return this.cells.length;
        }

        @Override
        public String getColumnName(int col) {

            return this.columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {

            return this.cells[row][col];
        }

        @Override
        public Class<?> getColumnClass(int col) {

            return this.columnClasses[col];
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return true;
        }
    }
}
