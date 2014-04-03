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

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.m2e.core.ui.internal.actions.SelectionUtil;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkingSet;

/**
 * Project Wizard
 *
 * @author <a href="mailto:akhikhl@gmail.com">Andrey Hihlovskiy</a>
 *
 * @version $Revision$ $Date$
 */
@SuppressWarnings("restriction")
public final class NewProjectWizard extends Wizard implements INewWizard {

    private final WizardConfig config;

    public NewProjectWizard() {
        this.config = new WizardConfig();
        if ("true".equals(System.getProperty("DatabaseModelStub"))) {
            System.out.println("Attention: using DatabaseModelStub");
            this.config.databaseModel = new DatabaseModelStub();
        } else {
            this.config.databaseModel = new DatabaseModel();
        }
    }

    @Override
    public void addPages() {
        addPage(new NewProjectWizardPage1(this.config));
        addPage(new NewProjectWizardPage2(this.config));
        addPage(new NewProjectWizardPage3(this.config));
    }

    @Override
    public boolean performFinish() {
        try {
            CreateProjectJob job = new CreateProjectJob(this.config);
            job.setUser(true);
            job.setPriority(Job.SHORT);
            job.schedule();
        } catch (Exception e) {
            e.printStackTrace();
            MessageDialog.openError(getShell(), "Error", e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        IWorkingSet workingSet = SelectionUtil.getSelectedWorkingSet(selection);
        if (workingSet != null) {
            this.config.workingSets.add(workingSet);
        }
    }
}
