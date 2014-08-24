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

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider for file list.
 *
 * @author <a href="mailto:ahihlovskiy@algotrader.ch">Andrey Hihlovskiy</a>
 *
 * @version $Revision$ $Date$
 */
class FileListContentProvider implements IStructuredContentProvider {

    private final EditorPropertyPage editorPropertyPage;

    FileListContentProvider(EditorPropertyPage editorPropertyPage) {
        this.editorPropertyPage = editorPropertyPage;
    }

    private Collection<File> files;

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput == null)
            files = null;
        else {
            List<String> errorMessages = new ArrayList<String>();
            try {
                this.editorPropertyPage.setErrorMessage(null);
                files = getFiles((IJavaProject) newInput, errorMessages);
            } catch (JavaModelException e) {
                files = null; // this is not java project
                this.editorPropertyPage.setErrorMessage(MessageFormat.format("Project ''{0}'' is not java project.", ((IJavaProject) newInput).getProject().getName()));
            } catch (Exception e) {
                e.printStackTrace();
                String errMessage = e.getMessage() == null ? e.getClass().getName() : e.getMessage();
                errorMessages.add(errMessage);
            }
            if (!errorMessages.isEmpty()) {
                String errMessage = StringUtils.join(errorMessages, ", ");
                this.editorPropertyPage.setErrorMessage(errMessage);
            }
        }
    }

    @Override
    public Object[] getElements(Object inputElement) {
        if (files != null)
            return files.toArray();
        else
            return new Object[0];
    }

    Collection<File> getFiles(IJavaProject javaProject, List<String> errorMessages) throws Exception {
        Collection<File> files;
        File hierarchyFile = ProjectUtils.getHierarchyFileFromClasspath(javaProject);
        if (hierarchyFile == null) {
            System.out.println("There's no hierarchy file");
            files = ProjectUtils.getPropertiesFilesFromClasspath(javaProject);
        } else {
            System.out.println("Got hierarchy file: " + hierarchyFile);
            files = ProjectUtils.getPropertiesFilesFromHierarchyFile(hierarchyFile, javaProject, errorMessages);
            System.out.println("Got files from hierarchy file: " + files);
        }
        for (File f : files) {
            StructuredProperties structProps = new StructuredProperties(this.editorPropertyPage);
            this.editorPropertyPage.propMap.put(f, structProps);
            try {
                structProps.load(f, errorMessages);
            } catch (Exception e) {
                if (errorMessages == null)
                    throw e;
                e.printStackTrace();
                String errMessage = MessageFormat.format("Error reading file ''{0}'': {1}", f.getName(), (e.getMessage() == null ? e.getClass().getName() : e.getMessage()));
                errorMessages.add(errMessage);
            }
            List<Object[]> elementsList = new ArrayList<Object[]>();
            for (String key : structProps.getKeys())
                elementsList.add(new Object[] { key, structProps.getValue(key) });
            this.editorPropertyPage.editorData.put(f, elementsList.toArray());
        }
        return files;
    }
}
