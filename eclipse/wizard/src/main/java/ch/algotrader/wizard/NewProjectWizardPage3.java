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
package ch.algotrader.wizard;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.m2e.core.ui.internal.wizards.AbstractMavenWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * Third wizard page, provides UI for database-specific properties.
 *
 * @author <a href="mailto:akhikhl@gmail.com">Andrey Hihlovskiy</a>
 *
 * @version $Revision$ $Date$
 */
@SuppressWarnings("restriction")
final class NewProjectWizardPage3 extends AbstractMavenWizardPage {

    private final WizardConfig config;
    private Composite container;
    private ComboViewer cboDataSet;

    protected NewProjectWizardPage3(WizardConfig config) {
        super(NewProjectWizardPage3.class.getName()); // make refactoring happy
        this.config = config;
        this.setTitle("AlgoTrader - Trading Strategy");
        this.setMessage("Specify database parameters");
        this.setImageDescriptor(Activator.getInstance().getImage("NewProjectLogo", "images/new_project_logo.png"));
    }

    @Override
    public void createControl(Composite parent) {
        try {
            this.container = new Composite(parent, SWT.FILL);
            this.container.setLayout(new GridLayout(2, false));

            final Group grpDatasetType = new Group(this.container, SWT.NONE);
            grpDatasetType.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
            grpDatasetType.setText("DataSet Type:");
            grpDatasetType.setLayout(new GridLayout(2, true));

            final Button rdoTick = new Button(grpDatasetType, SWT.RADIO);
            rdoTick.setText("tick");
            rdoTick.setSelection(true);
            this.config.dataSetType = MarketDataType.TICK;
            rdoTick.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    NewProjectWizardPage3.this.config.dataSetType = MarketDataType.TICK;
                    populateDataSetCombo();
                }
            });

            final Button rdoBar = new Button(grpDatasetType, SWT.RADIO);
            rdoBar.setText("bar");
            rdoBar.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    NewProjectWizardPage3.this.config.dataSetType = MarketDataType.BAR;
                    populateDataSetCombo();
                }
            });

            Label label = new Label(this.container, SWT.NONE);
            label.setText("DataSet:");

            this.cboDataSet = new ComboViewer(this.container, SWT.DROP_DOWN);
            this.cboDataSet.getControl().setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
            this.cboDataSet.getCombo().addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    NewProjectWizardPage3.this.config.dataSet = NewProjectWizardPage3.this.cboDataSet.getCombo().getText();
                }
            });
            this.cboDataSet.setContentProvider(ArrayContentProvider.getInstance());
            this.cboDataSet.setLabelProvider(new LabelProvider() {

                @Override
                public String getText(Object element) {
                    return element.toString();
                }

            });
            populateDataSetCombo();

            label = new Label(this.container, SWT.NONE);
            label.setText("Database:");

            final ComboViewer cboDatabase = new ComboViewer(this.container, SWT.DROP_DOWN);
            cboDatabase.getControl().setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
            cboDatabase.getCombo().addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    NewProjectWizardPage3.this.config.databaseName = cboDatabase.getCombo().getText();
                }
            });
            cboDatabase.setContentProvider(ArrayContentProvider.getInstance());
            cboDatabase.setLabelProvider(new LabelProvider() {

                @Override
                public String getText(Object element) {
                    return element.toString();
                }

            });
            cboDatabase.setInput(this.config.databaseModel.getDatabases());

            final Button chkUpdateDatabase = new Button(this.container, SWT.CHECK);
            chkUpdateDatabase.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
            chkUpdateDatabase.setText("Update Database");
            chkUpdateDatabase.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    NewProjectWizardPage3.this.config.updateDatabase = chkUpdateDatabase.getSelection();
                }
            });

            setControl(this.container);
        } catch (Exception e) {
            e.printStackTrace();
            MessageDialog.openError(getShell(), "Error", e.getMessage());
        }
    }

    private void populateDataSetCombo() {
        try {
            this.cboDataSet.setInput(this.config.databaseModel.getDataSets(this.config.dataSetType));
        } catch (CoreException e1) {
            e1.printStackTrace();
            throw new RuntimeException(e1);
        }
    }
}
