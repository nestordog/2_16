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
package ch.algotrader.configeditor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.TextCellEditor;

class CellEditorListener implements ICellEditorListener {

  private final EditorPropertyPage propertyPage;
  private final CellEditor cellEditor;

  CellEditorListener(EditorPropertyPage propertyPage, CellEditor cellEditor) {
    this.propertyPage = propertyPage;
    this.cellEditor = cellEditor;
  }

  @Override
  public void applyEditorValue() {
    propertyPage.setErrorMessage(null);
  }

  @Override
  public void cancelEditor() {
    propertyPage.setErrorMessage(null);
  }

  @Override
  public void editorValueChanged(boolean oldValidState, boolean newValidState) {
    if (newValidState)
      propertyPage.setErrorMessage(null);
    else if (cellEditor instanceof TextCellEditor)
      propertyPage.setErrorMessage(((TextCellEditor) cellEditor).getErrorMessage());
  }
}
