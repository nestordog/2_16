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

import java.util.List;
import java.util.Properties;

import org.apache.maven.archetype.catalog.Archetype;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.m2e.core.internal.MavenPluginActivator;
import org.eclipse.m2e.core.internal.archetype.ArchetypeManager;
import org.eclipse.m2e.core.project.IProjectConfigurationManager;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;

/**
 * Create project job
 *
 * @author <a href="mailto:akhikhl@gmail.com">Andrey Hihlovskiy</a>
 *
 * @version $Revision$ $Date$
 */
@SuppressWarnings("restriction")
public class CreateProjectJob extends Job {

    private final WizardConfig config;

    public CreateProjectJob(WizardConfig config) {
        super(CreateProjectJob.class.getName()); // make refactoring happy
        this.config = config;
    }

    private static void addToWorkingSets(IProject project, List<IWorkingSet> workingSets) {
        if (workingSets != null && workingSets.size() > 0) {
            // IAdaptable[] elements = workingSet.adaptElements(new IAdaptable[] {project});
            // if(elements.length == 1) {
            for (IWorkingSet workingSet : workingSets) {
                if (workingSet != null) {
                    IAdaptable[] oldElements = workingSet.getElements();
                    IAdaptable[] newElements = new IAdaptable[oldElements.length + 1];
                    System.arraycopy(oldElements, 0, newElements, 0, oldElements.length);
                    newElements[oldElements.length] = project;
                    workingSet.setElements(newElements);
                }
            }
        }
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {

        try {
            IProjectConfigurationManager configManager = MavenPluginActivator.getDefault().getProjectConfigurationManager();

            Archetype archetype = new Archetype();
            archetype.setGroupId("algotrader"); //$NON-NLS-1$
            archetype.setArtifactId("algotrader-archetype"); //$NON-NLS-1$
            archetype.setVersion(Messages.ALGOTRADER_VERSION);
            archetype.setRepository("https://repo.algotrader.ch/"); //$NON-NLS-1$
            Properties properties = new Properties();
            properties.put("groupId", this.config.groupId); //$NON-NLS-1$
            properties.put("artifactId", this.config.artifactId); //$NON-NLS-1$
            properties.put("version", this.config.version); //$NON-NLS-1$
            properties.put("package", this.config.packageName); //$NON-NLS-1$
            properties.put("serviceName", this.config.serviceName); //$NON-NLS-1$
            properties.put("dataSetType", this.config.dataSetType.toString()); //$NON-NLS-1$
            properties.put("dataSet", this.config.dataSet); //$NON-NLS-1$
            properties.put("dataBase", this.config.databaseName); //$NON-NLS-1$
            properties.put("instruments", this.config.databaseModel.getInstruments(this.config.dataSetType, this.config.dataSet)); //$NON-NLS-1$

            // akhikhl-20141210: fix for archetype loading problem:
            // eclipse fails to load an existing archetype unless getRequiredProperties is called at least once.
            // Seems to be initialization problem or similar.
            ArchetypeManager archetypeManager = MavenPluginActivator.getDefault().getArchetypeManager();
            archetypeManager.getRequiredProperties(archetype, archetypeManager.getArchetypeRepository(archetype), null);
            // end of fix

            List<IProject> projects = configManager.createArchetypeProjects(this.config.path, archetype, this.config.groupId, this.config.artifactId, this.config.version, this.config.packageName,
                    properties, new ProjectImportConfiguration(), monitor);

            if (projects != null) {
                for (IProject project : projects) {
                    addToWorkingSets(project, this.config.workingSets);
                    configManager.updateProjectConfiguration(project, monitor);
                }
            }

        } catch (Exception e) {

            final String errMessage = e.getMessage();
            monitor.setCanceled(true);
            monitor.subTask(errMessage);
            e.printStackTrace();
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error", errMessage); //$NON-NLS-1$
                }
            });
            return Status.CANCEL_STATUS;
        }

        return Status.OK_STATUS;
    }
}
