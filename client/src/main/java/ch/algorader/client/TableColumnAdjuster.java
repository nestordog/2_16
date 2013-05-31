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
package ch.algorader.client;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/*
 * Class to manage the widths of colunmns in a table.
 *
 * Various properties control how the width of the column is calculated. Another property controls whether column width calculation should be dynamic. Finally,
 * various Actions will be added to the table to allow the user to customize the functionality.
 *
 * This class was designed to be used with tables that use an auto resize mode of AUTO_RESIZE_OFF. With all other modes you are constrained as the width of the
 * columns must fit inside the table. So if you increase one column, one or more of the other columns must decrease. Because of this the resize mode of
 * RESIZE_ALL_COLUMNS will work the best.
 */
/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class TableColumnAdjuster implements PropertyChangeListener, TableModelListener {
    private JTable table;
    private int spacing;
    private boolean isColumnHeaderIncluded;
    private boolean isColumnDataIncluded;
    private boolean isOnlyAdjustLarger;
    private boolean isDynamicAdjustment;
    private Map<TableColumn, Integer> columnSizes = new HashMap<TableColumn, Integer>();

    /*
     *  Specify the table and use default spacing
     */
    public TableColumnAdjuster(JTable table) {
        this(table, 6);
    }

    /*
     *  Specify the table and spacing
     */
    public TableColumnAdjuster(JTable table, int spacing) {
        this.table = table;
        this.spacing = spacing;
        setColumnHeaderIncluded(true);
        setColumnDataIncluded(true);
        setOnlyAdjustLarger(true);
        setDynamicAdjustment(false);
        installActions();
    }

    /*
     *  Adjust the widths of all the columns in the table
     */
    public void adjustColumns() {
        TableColumnModel tcm = this.table.getColumnModel();

        for (int i = 0; i < tcm.getColumnCount(); i++) {
            adjustColumn(i);
        }
    }

    /*
     *  Adjust the width of the specified column in the table
     */
    public void adjustColumn(final int column) {
        TableColumn tableColumn = this.table.getColumnModel().getColumn(column);

        if (!tableColumn.getResizable())
            return;

        int columnHeaderWidth = getColumnHeaderWidth(column);
        int columnDataWidth = getColumnDataWidth(column);
        int preferredWidth = Math.max(columnHeaderWidth, columnDataWidth);

        updateTableColumn(column, preferredWidth);
    }

    /*
     *  Calculated the width based on the column name
     */
    private int getColumnHeaderWidth(int column) {
        if (!this.isColumnHeaderIncluded)
            return 0;

        TableColumn tableColumn = this.table.getColumnModel().getColumn(column);
        Object value = tableColumn.getHeaderValue();
        TableCellRenderer renderer = tableColumn.getHeaderRenderer();

        if (renderer == null) {
            renderer = this.table.getTableHeader().getDefaultRenderer();
        }

        Component c = renderer.getTableCellRendererComponent(this.table, value, false, false, -1, column);
        return c.getPreferredSize().width;
    }

    /*
     *  Calculate the width based on the widest cell renderer for the
     *  given column.
     */
    private int getColumnDataWidth(int column) {
        if (!this.isColumnDataIncluded)
            return 0;

        int preferredWidth = 0;
        int maxWidth = this.table.getColumnModel().getColumn(column).getMaxWidth();

        for (int row = 0; row < this.table.getRowCount(); row++) {
            preferredWidth = Math.max(preferredWidth, getCellDataWidth(row, column));

            //  We've exceeded the maximum width, no need to check other rows

            if (preferredWidth >= maxWidth)
                break;
        }

        return preferredWidth;
    }

    /*
     *  Get the preferred width for the specified cell
     */
    private int getCellDataWidth(int row, int column) {
        //  Inovke the renderer for the cell to calculate the preferred width

        TableCellRenderer cellRenderer = this.table.getCellRenderer(row, column);
        Component c = this.table.prepareRenderer(cellRenderer, row, column);
        int width = c.getPreferredSize().width + this.table.getIntercellSpacing().width;

        return width;
    }

    /*
     *  Update the TableColumn with the newly calculated width
     */
    private void updateTableColumn(int column, int width) {
        final TableColumn tableColumn = this.table.getColumnModel().getColumn(column);

        if (!tableColumn.getResizable())
            return;

        width += this.spacing;

        //  Don't shrink the column width

        if (this.isOnlyAdjustLarger) {
            width = Math.max(width, tableColumn.getPreferredWidth());
        }

        this.columnSizes.put(tableColumn, new Integer(tableColumn.getWidth()));
        this.table.getTableHeader().setResizingColumn(tableColumn);
        tableColumn.setWidth(width);
    }

    /*
     *  Restore the widths of the columns in the table to its previous width
     */
    public void restoreColumns() {
        TableColumnModel tcm = this.table.getColumnModel();

        for (int i = 0; i < tcm.getColumnCount(); i++) {
            restoreColumn(i);
        }
    }

    /*
     *  Restore the width of the specified column to its previous width
     */
    private void restoreColumn(int column) {
        TableColumn tableColumn = this.table.getColumnModel().getColumn(column);
        Integer width = this.columnSizes.get(tableColumn);

        if (width != null) {
            this.table.getTableHeader().setResizingColumn(tableColumn);
            tableColumn.setWidth(width.intValue());
        }
    }

    /*
     *    Indicates whether to include the header in the width calculation
     */
    public void setColumnHeaderIncluded(boolean isColumnHeaderIncluded) {
        this.isColumnHeaderIncluded = isColumnHeaderIncluded;
    }

    /*
     *    Indicates whether to include the model data in the width calculation
     */
    public void setColumnDataIncluded(boolean isColumnDataIncluded) {
        this.isColumnDataIncluded = isColumnDataIncluded;
    }

    /*
     *    Indicates whether columns can only be increased in size
     */
    public void setOnlyAdjustLarger(boolean isOnlyAdjustLarger) {
        this.isOnlyAdjustLarger = isOnlyAdjustLarger;
    }

    /*
     *  Indicate whether changes to the model should cause the width to be
     *  dynamically recalculated.
     */
    public void setDynamicAdjustment(boolean isDynamicAdjustment) {
        //  May need to add or remove the TableModelListener when changed

        if (this.isDynamicAdjustment != isDynamicAdjustment) {
            if (isDynamicAdjustment) {
                this.table.addPropertyChangeListener(this);
                this.table.getModel().addTableModelListener(this);
            } else {
                this.table.removePropertyChangeListener(this);
                this.table.getModel().removeTableModelListener(this);
            }
        }

        this.isDynamicAdjustment = isDynamicAdjustment;
    }

    //
    //  Implement the PropertyChangeListener
    //
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        //  When the TableModel changes we need to update the listeners
        //  and column widths

        if ("model".equals(e.getPropertyName())) {
            TableModel model = (TableModel) e.getOldValue();
            model.removeTableModelListener(this);

            model = (TableModel) e.getNewValue();
            model.addTableModelListener(this);
            adjustColumns();
        }
    }

    //
    //  Implement the TableModelListener
    //
    @Override
    public void tableChanged(TableModelEvent e) {
        if (!this.isColumnDataIncluded)
            return;

        //  A cell has been updated

        if (e.getType() == TableModelEvent.UPDATE) {
            int column = this.table.convertColumnIndexToView(e.getColumn());

            //  Only need to worry about an increase in width for this cell

            if (this.isOnlyAdjustLarger) {
                int row = e.getFirstRow();
                TableColumn tableColumn = this.table.getColumnModel().getColumn(column);

                if (tableColumn.getResizable()) {
                    int width = getCellDataWidth(row, column);
                    updateTableColumn(column, width);
                }
            }

            //    Could be an increase of decrease so check all rows

            else {
                adjustColumn(column);
            }
        }

        //  The update affected more than one column so adjust all columns

        else {
            adjustColumns();
        }
    }

    /*
     *  Install Actions to give user control of certain functionality.
     */
    private void installActions() {
        installColumnAction(true, true, "adjustColumn", "control ADD");
        installColumnAction(false, true, "adjustColumns", "control shift ADD");
        installColumnAction(true, false, "restoreColumn", "control SUBTRACT");
        installColumnAction(false, false, "restoreColumns", "control shift SUBTRACT");

        installToggleAction(true, false, "toggleDynamic", "control MULTIPLY");
        installToggleAction(false, true, "toggleLarger", "control DIVIDE");
    }

    /*
     *  Update the input and action maps with a new ColumnAction
     */
    private void installColumnAction(boolean isSelectedColumn, boolean isAdjust, String key, String keyStroke) {
        Action action = new ColumnAction(isSelectedColumn, isAdjust);
        KeyStroke ks = KeyStroke.getKeyStroke(keyStroke);
        this.table.getInputMap().put(ks, key);
        this.table.getActionMap().put(key, action);
    }

    /*
     *  Update the input and action maps with new ToggleAction
     */
    private void installToggleAction(boolean isToggleDynamic, boolean isToggleLarger, String key, String keyStroke) {
        Action action = new ToggleAction(isToggleDynamic, isToggleLarger);
        KeyStroke ks = KeyStroke.getKeyStroke(keyStroke);
        this.table.getInputMap().put(ks, key);
        this.table.getActionMap().put(key, action);
    }

    /*
     *  Action to adjust or restore the width of a single column or all columns
     */
    class ColumnAction extends AbstractAction {

        private static final long serialVersionUID = -633071698315813603L;

        private boolean isSelectedColumn;
        private boolean isAdjust;

        public ColumnAction(boolean isSelectedColumn, boolean isAdjust) {
            this.isSelectedColumn = isSelectedColumn;
            this.isAdjust = isAdjust;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            //  Handle selected column(s) width change actions

            if (this.isSelectedColumn) {
                int[] columns = TableColumnAdjuster.this.table.getSelectedColumns();

                for (int column : columns) {
                    if (this.isAdjust)
                        adjustColumn(column);
                    else
                        restoreColumn(column);
                }
            } else {
                if (this.isAdjust)
                    adjustColumns();
                else
                    restoreColumns();
            }
        }
    }

    /*
     *  Toggle properties of the TableColumnAdjuster so the user can
     *  customize the functionality to their preferences
     */
    class ToggleAction extends AbstractAction {

        private static final long serialVersionUID = -418068207942992962L;

        private boolean isToggleDynamic;
        private boolean isToggleLarger;

        public ToggleAction(boolean isToggleDynamic, boolean isToggleLarger) {
            this.isToggleDynamic = isToggleDynamic;
            this.isToggleLarger = isToggleLarger;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (this.isToggleDynamic) {
                setDynamicAdjustment(!TableColumnAdjuster.this.isDynamicAdjustment);
                return;
            }

            if (this.isToggleLarger) {
                setOnlyAdjustLarger(!TableColumnAdjuster.this.isOnlyAdjustLarger);
                return;
            }
        }
    }
}

