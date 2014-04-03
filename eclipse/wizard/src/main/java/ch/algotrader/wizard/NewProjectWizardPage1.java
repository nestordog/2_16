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
package ch.algotrader.wizard;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.m2e.core.ui.internal.wizards.AbstractMavenWizardPage;
import org.eclipse.m2e.core.ui.internal.wizards.WorkingSetGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;

/**
 * First wizard page, provides UI for project location and workingSets.
 *
 * @author <a href="mailto:akhikhl@gmail.com">Andrey Hihlovskiy</a>
 *
 * @version $Revision$ $Date$
 */
@SuppressWarnings("restriction")
final class NewProjectWizardPage1 extends AbstractMavenWizardPage {

    private final WizardConfig config;
    private Composite container;
    private ComboViewer cboLocation;
    private Button btnBrowseLocation;
    private WorkingSetGroup workingSetGroup;

    protected NewProjectWizardPage1(WizardConfig config) {
        super(NewProjectWizardPage1.class.getName()); // make refactoring happy
        this.config = config;
        this.setTitle("AlgoTrader - Trading Strategy");
        this.setMessage("Select project location");
        this.setImageDescriptor(Activator.getInstance().getImage("NewProjectLogo", "images/new_project_logo.png"));
    }

    @Override
    public void createControl(Composite parent) {
        try {
            this.container = new Composite(parent, SWT.FILL);
            this.container.setLayout(new GridLayout(3, false));

            final Button chkStandardLocation = new Button(this.container, SWT.CHECK);
            chkStandardLocation.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
            chkStandardLocation.setText("Use default Workspace location");
            chkStandardLocation.setSelection(true);
            this.config.path = ResourcesPlugin.getWorkspace().getRoot().getLocation();
            chkStandardLocation.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (chkStandardLocation.getSelection()) {
                        NewProjectWizardPage1.this.config.path = ResourcesPlugin.getWorkspace().getRoot().getLocation();
                    } else {
                        NewProjectWizardPage1.this.config.path = Path.fromOSString(NewProjectWizardPage1.this.cboLocation.getCombo().getText().trim());
                    }
                    NewProjectWizardPage1.this.cboLocation.getControl().setEnabled(!chkStandardLocation.getSelection());
                    NewProjectWizardPage1.this.btnBrowseLocation.setEnabled(!chkStandardLocation.getSelection());
                }
            });

            Label label = new Label(this.container, SWT.NONE);
            GridData ldata = new GridData();
            ldata.horizontalIndent = 10;
            label.setLayoutData(ldata);
            label.setText("Location:");

            this.cboLocation = new ComboViewer(this.container, SWT.SINGLE | SWT.BORDER);
            this.cboLocation.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            this.cboLocation.getControl().setEnabled(false);
            this.cboLocation.getCombo().setText(ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().getAbsolutePath());
            this.cboLocation.getCombo().addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    if (!chkStandardLocation.getSelection()) {
                        NewProjectWizardPage1.this.config.path = Path.fromOSString(NewProjectWizardPage1.this.cboLocation.getCombo().getText().trim());
                    }
                }
            });
            addFieldWithHistory("location", this.cboLocation.getCombo());

            this.btnBrowseLocation = new Button(this.container, SWT.NONE);
            this.btnBrowseLocation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
            this.btnBrowseLocation.setText("Browse...");
            this.btnBrowseLocation.setEnabled(false);
            this.btnBrowseLocation.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    DirectoryDialog dialog = new DirectoryDialog(NewProjectWizardPage1.this.getShell(), SWT.OPEN);
                    dialog.setMessage("Please select a directory");
                    dialog.setFilterPath(NewProjectWizardPage1.this.cboLocation.getCombo().getText().trim());
                    String result = dialog.open();
                    if (result != null) {
                        NewProjectWizardPage1.this.cboLocation.getCombo().setText(result);
                    }
                }
            });

            this.workingSetGroup = new WorkingSetGroup(this.container, this.config.workingSets, getShell());

            setControl(this.container);
        } catch (Exception e) {
            e.printStackTrace();
            MessageDialog.openError(getShell(), "Error", e.getMessage());
        }
    }

    @Override
    public void dispose() {
        this.workingSetGroup.dispose();
        super.dispose();
    }
}
