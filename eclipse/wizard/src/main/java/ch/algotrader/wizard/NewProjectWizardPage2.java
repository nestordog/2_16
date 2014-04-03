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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.m2e.core.ui.internal.wizards.AbstractMavenWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Second wizard page, provides UI for maven-specific properties.
 *
 * @author <a href="mailto:akhikhl@gmail.com">Andrey Hihlovskiy</a>
 *
 * @version $Revision$ $Date$
 */
@SuppressWarnings("restriction")
final class NewProjectWizardPage2 extends AbstractMavenWizardPage {

    private final WizardConfig config;
    private Composite container;
    private ComboViewer cboGroupId;
    private ComboViewer cboArtifactId;
    private ComboViewer cboVersion;
    private ComboViewer cboPackage;
    private ComboViewer cboService;
    private boolean modifyingGroupId = false;
    private boolean modifyingArtifactId = false;
    private boolean customPackage = false;
    private boolean customService = false;

    protected NewProjectWizardPage2(WizardConfig config) {
        super(NewProjectWizardPage2.class.getName()); // make refactoring happy
        this.config = config;
        this.setTitle("AlgoTrader - Trading Strategy");
        this.setMessage("Specify archetype parameters");
        this.setImageDescriptor(Activator.getInstance().getImage("NewProjectLogo", "images/new_project_logo.png"));
    }

    @Override
    public void createControl(Composite parent) {
        try {
            this.container = new Composite(parent, SWT.FILL);
            this.container.setLayout(new GridLayout(2, false));

            Label label = new Label(this.container, SWT.NONE);
            label.setText("Group Id:");

            this.cboGroupId = new ComboViewer(this.container, SWT.DROP_DOWN);
            this.cboGroupId.getControl().setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
            this.cboGroupId.getCombo().addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    String newValue = NewProjectWizardPage2.this.cboGroupId.getCombo().getText();
                    NewProjectWizardPage2.this.modifyingGroupId = true;
                    try {
                        if (NewProjectWizardPage2.this.customPackage) {
                            if (newValue.equals(NewProjectWizardPage2.this.cboPackage.getCombo().getText())) {
                                NewProjectWizardPage2.this.customPackage = false;
                            }
                        }
                        if (!NewProjectWizardPage2.this.customPackage) {
                            NewProjectWizardPage2.this.cboPackage.getCombo().setText(newValue);
                        }
                    } finally {
                        NewProjectWizardPage2.this.modifyingGroupId = false;
                    }
                    NewProjectWizardPage2.this.config.groupId = newValue;
                    updateCompleteness();
                }
            });
            addFieldWithHistory("groupId", this.cboGroupId.getCombo());

            label = new Label(this.container, SWT.NONE);
            label.setText("Artifact Id:");

            this.cboArtifactId = new ComboViewer(this.container, SWT.DROP_DOWN);
            this.cboArtifactId.getControl().setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
            this.cboArtifactId.getCombo().addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    String newValue = NewProjectWizardPage2.this.cboArtifactId.getCombo().getText();
                    NewProjectWizardPage2.this.modifyingArtifactId = true;
                    try {
                        String newService = newValue.isEmpty() ? newValue : (newValue.substring(0, 1).toUpperCase() + newValue.substring(1).toLowerCase());
                        if (NewProjectWizardPage2.this.customService) {
                            if (newService.equals(NewProjectWizardPage2.this.cboService.getCombo().getText())) {
                                NewProjectWizardPage2.this.customService = false;
                            }
                        }
                        if (!NewProjectWizardPage2.this.customService) {
                            NewProjectWizardPage2.this.cboService.getCombo().setText(newService);
                        }
                    } finally {
                        NewProjectWizardPage2.this.modifyingArtifactId = false;
                    }
                    NewProjectWizardPage2.this.config.artifactId = newValue;
                    updateCompleteness();
                }
            });
            addFieldWithHistory("artifactId", this.cboArtifactId.getCombo());

            label = new Label(this.container, SWT.NONE);
            label.setText("Version:");

            this.cboVersion = new ComboViewer(this.container, SWT.DROP_DOWN);
            this.cboVersion.getControl().setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
            this.cboVersion.getCombo().addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    NewProjectWizardPage2.this.config.version = NewProjectWizardPage2.this.cboVersion.getCombo().getText();
                    updateCompleteness();
                }
            });
            this.cboVersion.getCombo().setText("1.0.0-SNAPSHOT");
            addFieldWithHistory("version", this.cboVersion.getCombo());

            label = new Label(this.container, SWT.NONE);
            label.setText("Package:");

            this.cboPackage = new ComboViewer(this.container, SWT.DROP_DOWN);
            this.cboPackage.getControl().setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
            this.cboPackage.getCombo().addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    if (!NewProjectWizardPage2.this.modifyingGroupId) {
                        NewProjectWizardPage2.this.customPackage = true;
                    }
                    NewProjectWizardPage2.this.config.packageName = NewProjectWizardPage2.this.cboPackage.getCombo().getText();
                    updateCompleteness();
                }
            });
            addFieldWithHistory("packageName", this.cboPackage.getCombo());

            label = new Label(this.container, SWT.NONE);
            label.setText("Service:");

            this.cboService = new ComboViewer(this.container, SWT.DROP_DOWN);
            this.cboService.getControl().setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
            this.cboService.getCombo().addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    if (!NewProjectWizardPage2.this.modifyingArtifactId) {
                        NewProjectWizardPage2.this.customService = true;
                    }
                    NewProjectWizardPage2.this.config.serviceName = NewProjectWizardPage2.this.cboService.getCombo().getText();
                    updateCompleteness();
                }
            });
            addFieldWithHistory("serviceName", this.cboService.getCombo());

            setControl(this.container);
            updateCompleteness();

        } catch (Exception e) {
            e.printStackTrace();
            MessageDialog.openError(getShell(), "Error", e.getMessage());
        }
    }

    private void updateCompleteness() {
        setPageComplete(!this.cboGroupId.getCombo().getText().isEmpty() && !this.cboArtifactId.getCombo().getText().isEmpty() && !this.cboVersion.getCombo().getText().isEmpty()
                && !this.cboPackage.getCombo().getText().isEmpty() && !this.cboService.getCombo().getText().isEmpty());
    }
}
