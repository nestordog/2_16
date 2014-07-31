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
package ch.algotrader.configeditor.editingsupport;

import java.util.Date;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class DateCellEditor extends CellEditor {

    private CDateTime widget;

    DateCellEditor(Composite parent, int flag) {
        super(parent, flag);
    }

    @Override
    protected Control createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        FillLayout l = new FillLayout();
        l.marginHeight = 2;
        composite.setLayout(l);
        if ((this.getStyle() & SWT.DATE) == 0) {
            widget = new CDateTime(composite, CDT.NONE);
            widget.setPattern("HH:mm:ss");
        } else {
            widget = new CDateTime(composite, CDT.DROP_DOWN);
            widget.setPattern("yyyy-MM-dd");
        }
        widget.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        return composite;
    }

    @Override
    protected Object doGetValue() {
        return widget.getSelection();
    }

    @Override
    protected void doSetFocus() {
        widget.setFocus();
    }

    @Override
    protected void doSetValue(Object value) {
        widget.setSelection((Date) value);
    }

}
