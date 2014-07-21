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

import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;

public class EnumCellEditor extends ComboBoxViewerCellEditor {

  public EnumCellEditor(Composite parent, final String enumClassName) throws ClassNotFoundException {
    super(parent);
    this.setContentProvider(new IStructuredContentProvider() {

      Class<?> enumClass;

      @Override
      public void dispose() {
      }

      @Override
      public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        enumClass = (Class<?>) newInput;
      }

      @Override
      public Object[] getElements(Object inputElement) {
        return enumClass.getEnumConstants();
      }
    });
    this.setInput(Class.forName(enumClassName));
  }
}
