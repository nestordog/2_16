package ch.algotrader.configeditor;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;

public class DateCellEditor extends CellEditor {

    private DateTime widget;

    DateCellEditor(Composite parent, int flag) {
        super(parent, flag);
    }

    @Override
    protected Control createControl(Composite parent) {
        widget = new DateTime(parent, this.getStyle());
        return widget;
    }

    @Override
    protected Object doGetValue() {
        String str = null;
        DateFormat formatter = null;
        if ((this.getStyle() & SWT.DATE) == 0) {
            str = String.format("%d.%d.%d", widget.getDay(), widget.getMonth(), widget.getYear());
            formatter = new SimpleDateFormat("dd.MM.yyyy");
        } else {
            str = String.format("%d:%d:%d", widget.getHours(), widget.getMinutes(), widget.getSeconds());
            formatter = new SimpleDateFormat("HH:mm:ss");
        }
        try {
            return (Date) formatter.parse(str);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doSetFocus() {
        widget.setFocus();
    }

    @Override
    protected void doSetValue(Object value) {
        Date date = (Date) value;
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        if ((this.getStyle() & SWT.DATE) == 0) {
            widget.setDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        } else {
            widget.setTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
        }
    }

}
