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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
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

    private List<File> files;

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput == null)
            files = null;
        else
            try {
                files = getFiles((IJavaProject) newInput);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
    }

    @Override
    public Object[] getElements(Object inputElement) {
        if (files != null)
            return files.toArray();
        else
            return new Object[0];
    }

    List<File> getFiles(IJavaProject javaProject) throws Exception {
        List<File> files;
        File hierarchyFile = getHierarchyFileFromClasspath(javaProject);
        if (hierarchyFile == null) {
            files = getPropertiesFilesFromClasspath(javaProject);
        } else {
            files = getPropertiesFilesFromHierarchyFile(hierarchyFile, javaProject);
        }
        for (File f : files) {
            StructuredProperties structProps = new StructuredProperties();
            this.editorPropertyPage.propMap.put(f, structProps);
            List<String> errorMessages = new ArrayList<String>();
            try {
                structProps.load(f, errorMessages);
            } catch (Exception e) {
                e.printStackTrace();
                String errMessage = MessageFormat.format("Error reading file ''{0}'': {1}", f.getName(), (e.getMessage() == null ? e.getClass().getName() : e.getMessage()));
                errorMessages.add(errMessage);
            }
            if (!errorMessages.isEmpty()) {
                String errMessage = StringUtils.join(errorMessages, "\n");
                this.editorPropertyPage.setErrorMessage(errMessage);
            }
            List<Object[]> elementsList = new ArrayList<Object[]>();
            for (String key : structProps.getKeys())
                elementsList.add(new Object[] { key, structProps.getValue(key) });
            this.editorPropertyPage.editorData.put(f, elementsList.toArray());
        }
        return files;
    }

    File getHierarchyFileFromClasspath(IJavaProject javaProject) throws Exception {
        IPath workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation();
        IProject project = javaProject.getProject();
        String projectName = project.getName();
        javaProject.open(null);
        IClasspathEntry[] classPath = javaProject.getResolvedClasspath(false);
        for (int i = 0; i < classPath.length; i++) {
            IClasspathEntry entry = classPath[i];
            if (entry.getPath().toString().startsWith("/" + projectName)) {
                File dir = new File(new File(workspaceLocation.toString(), entry.getPath().toString()), "META-INF");
                File f = new File(dir, project.getName() + ".hierarchy");
                if (f.exists())
                    return f;
            }
        }
        for (int i = 0; i < classPath.length; i++) {
            IClasspathEntry entry = classPath[i];
            if (entry.getPath().toString().startsWith("/" + projectName)) {
                File dir = new File(new File(workspaceLocation.toString(), entry.getPath().toString()), "META-INF");
                File[] files = dir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.getName().endsWith(".hierarchy");
                    }
                });
                if (files != null && files.length != 0)
                    return files[0];
            }
        }
        return null;
    }

    List<String> getPropertiesFileNamesFromHierarchyFile(File hierarchyFile) throws Exception {
        List<String> result = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new FileReader(hierarchyFile));
        try {
            for (String s : br.readLine().split(":")) {
                if (!s.endsWith(".properties"))
                    s += ".properties";
                result.add(s);
            }
        } finally {
            br.close();
        }
        return result;
    }

    List<File> getPropertiesFilesFromHierarchyFile(File hierarchyFile, IJavaProject javaProject) throws Exception {
        return resolvePropertiesFileNamesAgainstClasspath(getPropertiesFileNamesFromHierarchyFile(hierarchyFile), javaProject);
    }

    List<File> getPropertiesFilesFromClasspath(IJavaProject javaProject) throws Exception {
        List<File> result = new ArrayList<File>();
        // IPath workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation();
        IClasspathEntry[] classPath = javaProject.getResolvedClasspath(false);
        for (int i = 0; i < classPath.length; i++) {
            IClasspathEntry entry = classPath[i];
            if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
                String projectName = entry.getPath().segment(0);
                IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
                if (proj != null) {
                    IJavaProject javaProj;
                    if (proj instanceof IJavaProject)
                        javaProj = (IJavaProject) proj;
                    else
                        javaProj = JavaCore.create(proj);
                    javaProj.open(null);
                    result.addAll(getPropertiesFilesFromClasspath(javaProj));
                }
            } else if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(entry.getPath());
                if (folder != null) {
                    File entryFile = folder.getLocation().toFile().getAbsoluteFile();
                    File dir = new File(entryFile, "META-INF");
                    if (dir.exists()) {
                        for (File f : dir.listFiles(new FileFilter() {
                            @Override
                            public boolean accept(File f) {
                                return f.getName().endsWith(".properties");
                            }
                        })) {
                            result.add(f);
                        }
                    }
                }
            }
        }
        return result;
    }

    File resolvePropertiesFileNameAgainstClasspath(String fileName, IJavaProject javaProject) throws Exception {
        IPath workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation();
        IClasspathEntry[] classPath = javaProject.getResolvedClasspath(false);
        for (int i = 0; i < classPath.length; i++) {
            IClasspathEntry entry = classPath[i];
            String entryPath = entry.getPath().toString();
            if (entryPath.startsWith("/")) {
                String projectName = entryPath.substring(1);
                if (new Path(projectName).segmentCount() == 1) {
                    IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
                    if (proj.exists()) {
                        IJavaProject javaProj;
                        if (proj instanceof IJavaProject)
                            javaProj = (IJavaProject) proj;
                        else
                            javaProj = JavaCore.create(proj);
                        javaProj.open(null);
                        File f = resolvePropertiesFileNameAgainstClasspath(fileName, javaProj);
                        if (f != null)
                            return f;
                    }
                }
            }
            File entryFile = new File(entryPath);
            if (!entryFile.exists())
                entryFile = new File(workspaceLocation.toOSString(), entryPath);
            if (entryFile.isDirectory()) {
                File f = new File(entryFile, "META-INF/" + fileName);
                if (f.exists())
                    return f;
            }
        }
        return null;
    }

    List<File> resolvePropertiesFileNamesAgainstClasspath(List<String> fileNames, IJavaProject javaProject) throws Exception {
        List<File> result = new ArrayList<File>();
        for (String fileName : fileNames) {
            File f = resolvePropertiesFileNameAgainstClasspath(fileName, javaProject);
            if (f == null)
                this.editorPropertyPage.setErrorMessage(MessageFormat.format("File ''{0}'' does not exist.", fileName));
            else
                result.add(f);
        }
        return result;
    }
}
