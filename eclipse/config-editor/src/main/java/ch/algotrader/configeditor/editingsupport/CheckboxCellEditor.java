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

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * Cell editor for boolean values.
 *
 * @author <a href="mailto:ahihlovskiy@algotrader.ch">Andrey Hihlovskiy</a>
 *
 * @version $Revision$ $Date$
 */
public class CheckboxCellEditor extends CellEditor {

    private Button checkbox;

    public CheckboxCellEditor(Composite parent) {
        super(parent);
    }

    @Override
    protected Control createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        FillLayout l = new FillLayout();
        l.marginHeight = 2;
        l.marginWidth = 5;
        composite.setLayout(l);
        checkbox = new Button(composite, SWT.CHECK);
        checkbox.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        return composite;
    }

    @Override
    protected Object doGetValue() {
        return checkbox.getSelection();
    }

    @Override
    protected void doSetFocus() {
        checkbox.setFocus();
    }

    @Override
    protected void doSetValue(Object value) {
        checkbox.setSelection(Boolean.parseBoolean(value.toString()));
    }
}
