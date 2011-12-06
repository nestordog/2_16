package com.algoTrader.client;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.table.DefaultTableCellRenderer;

public class DateRenderer extends DefaultTableCellRenderer.UIResource {

    private static final long serialVersionUID = -1703296861294766348L;
    private static DateFormat formatter = new SimpleDateFormat("EEE dd.MM.yyyy HH:mm:ss");

    public DateRenderer() {
        super();
    }

    public void setValue(Object value) {
        setText((value == null) ? "" : formatter.format(value));
    }
}
