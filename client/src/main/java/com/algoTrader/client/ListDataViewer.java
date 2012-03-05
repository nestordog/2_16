package com.algoTrader.client;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

public class ListDataViewer extends JPanel {

    private static final long serialVersionUID = 2014300386194071692L;

    public ListDataViewer(List<?> list) {

        super(new GridLayout(1, 0));

        JTable table = new JTable(new MyTableModel(list));
        table.setPreferredScrollableViewportSize(new Dimension(500, 10 + table.getModel().getRowCount() * 16));
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

    class MyTableModel extends AbstractTableModel {

        private static final long serialVersionUID = -1330727223337493071L;

        private List<?> list;
        private List<Field> fields = new ArrayList<Field>();

        public MyTableModel(List<?> list) {

            this.list = list;
            try {
                Class<?> cl = this.list.get(0).getClass();
                Field[] flds = cl.getDeclaredFields();
                AccessibleObject.setAccessible(flds, true);
                for (Field field : flds) {
                    if (field.getName().startsWith("set")) {
                        continue;
                    } else if (Modifier.isTransient(field.getModifiers())) {
                        continue;
                    } else if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    this.fields.add(field);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getColumnCount() {
            return this.fields.size();
        }

        @Override
        public int getRowCount() {
            return this.list.size();
        }

        @Override
        public String getColumnName(int col) {
            return this.fields.get(col).getName();
        }

        @Override
        public Object getValueAt(int row, int col) {

            Field field = this.fields.get(col);
            Object obj = this.list.get(row);
            try {
                return field.get(obj);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public Class<?> getColumnClass(int col) {
            Class<?> cl = this.fields.get(col).getType();
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

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }

    }
}
