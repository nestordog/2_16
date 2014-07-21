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
    Calendar c = Calendar.getInstance();
    if ((this.getStyle() & SWT.DATE) == 0) {
      c.set(0, 0, 0, widget.getHours(), widget.getMinutes(), widget.getSeconds());
    } else {
      c.set(widget.getYear(), widget.getMonth(), widget.getDay(), 0, 0, 0);
    }
    return c.getTime();
  }

  @Override
  protected void doSetFocus() {
    widget.setFocus();
  }

  @Override
  protected void doSetValue(Object value) {
    System.out.println("doSetValue Input: " + value);
    Date date = (Date) value;
    System.out.println("doSetValue Date: " + date);
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    System.out.println("doSetValue calendar: " + c);
    if ((this.getStyle() & SWT.DATE) == 0) {
      widget.setTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
    } else {
      System.out.println("calendar output: yyyy=" + c.get(Calendar.YEAR) + ", month=" + c.get(Calendar.MONTH) + ", day=" + c.get(Calendar.DAY_OF_MONTH));
      widget.setDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
    }
  }

}
