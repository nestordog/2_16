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
package ch.algotrader.configeditor.editingsupport;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;

/**
 * Cell editor for date and time values.
 *
 * @author <a href="mailto:ahihlovskiy@algotrader.ch">Andrey Hihlovskiy</a>
 *
 * @version $Revision$ $Date$
 */
public class DateTimeCellEditor extends CellEditor {

    private DateTime dateWidget;
    private DateTime timeWidget;

    DateTimeCellEditor(Composite parent) {
        super(parent);
    }

    @Override
    protected Control createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        GridLayout layout = new GridLayout(2, true);
        layout.horizontalSpacing = layout.verticalSpacing = layout.marginWidth = layout.marginHeight = 0;
        composite.setLayout(layout);
        dateWidget = new DateTime(composite, SWT.DATE | SWT.DROP_DOWN);
        dateWidget.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
        timeWidget = new DateTime(composite, SWT.TIME);
        timeWidget.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
        return composite;
    }

    @Override
    protected Object doGetValue() {
        Calendar c = Calendar.getInstance();
        c.set(dateWidget.getYear(), dateWidget.getMonth(), dateWidget.getDay(), timeWidget.getHours(), timeWidget.getMinutes(), timeWidget.getSeconds());
        return c.getTime();
    }

    @Override
    protected void doSetFocus() {
        dateWidget.setFocus();
    }

    @Override
    protected void doSetValue(Object value) {
        Date date = (Date) value;
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        dateWidget.setDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        timeWidget.setTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
    }
}
