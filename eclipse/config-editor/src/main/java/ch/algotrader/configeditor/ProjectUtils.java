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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * Functions dealing with eclipse projects.
 *
 * @author <a href="mailto:ahihlovskiy@algotrader.ch">Andrey Hihlovskiy</a>
 *
 * @version $Revision$ $Date$
 */
public class ProjectUtils {

    public static ClassLoader getClassLoader(IJavaProject javaProject) throws Exception {
        List<URL> classpathUrls = new ArrayList<URL>();
        for (File f : getBinaryClasspath(javaProject))
            classpathUrls.add(f.toURI().toURL());
        return new URLClassLoader(classpathUrls.toArray(new URL[0]), System.class.getClassLoader());
    }

    public static Collection<File> getBinaryClasspath(IJavaProject javaProject) throws Exception {
        IPath projectLocation = javaProject.getProject().getLocation();
        Set<File> result = new LinkedHashSet<File>();
        if (javaProject.getOutputLocation() != null) {
            File f = javaProject.getOutputLocation().toFile().getAbsoluteFile();
            if (!f.exists())
                f = new File(projectLocation.toOSString(), javaProject.getOutputLocation().removeFirstSegments(1).toString());
            result.add(f);
        }
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
                    result.addAll(getBinaryClasspath(javaProj));
                }
            } else if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                if (entry.getOutputLocation() != null) {
                    File f = entry.getOutputLocation().toFile().getAbsoluteFile();
                    if (!f.exists())
                        f = new File(projectLocation.toOSString(), entry.getOutputLocation().removeFirstSegments(1).toString());
                    result.add(f);
                }
            }
        }
        return result;
    }

    public static File getHierarchyFileFromClasspath(IJavaProject javaProject) throws Exception {
        IPath workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation();
        IProject project = javaProject.getProject();
        String projectName = project.getName();
        javaProject.open(null);
        IClasspathEntry[] classPath = javaProject.getResolvedClasspath(false);
        for (int i = 0; i < classPath.length; i++) {
            IClasspathEntry entry = classPath[i];
            if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(entry.getPath());
                if (folder != null) {
                    File entryFile = folder.getLocation().toFile().getAbsoluteFile();
                    File dir = new File(entryFile, "META-INF");
                    File f = new File(dir, project.getName() + ".hierarchy");
                    if (f.exists())
                        return f;
                }
            }
        }
        for (int i = 0; i < classPath.length; i++) {
            IClasspathEntry entry = classPath[i];
            if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(entry.getPath());
                if (folder != null) {
                    File entryFile = folder.getLocation().toFile().getAbsoluteFile();
                    File dir = new File(entryFile, "META-INF");
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
        }
        return null;
    }

    public static Collection<File> getPropertiesFilesFromClasspath(IJavaProject javaProject) throws Exception {
        Set<File> result = new LinkedHashSet<File>();
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

    public static Collection<String> getPropertiesFileNamesFromHierarchyFile(File hierarchyFile) throws Exception {
        Set<String> result = new LinkedHashSet<String>();
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

    public static Collection<File> getPropertiesFilesFromHierarchyFile(File hierarchyFile, IJavaProject javaProject, List<String> errorMessages) throws Exception {
        return resolvePropertiesFileNamesAgainstClasspath(getPropertiesFileNamesFromHierarchyFile(hierarchyFile), javaProject, errorMessages);
    }

    public static void refreshContainerOfFile(File file) {
        String filePath = file.getAbsolutePath();
        for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
            String projectPath = project.getLocation().toOSString();
            if (filePath.startsWith(projectPath)) {
                try {
                    filePath = filePath.substring(projectPath.length());
                    IContainer container = project.getFile(new Path(filePath)).getParent();
                    container.refreshLocal(IResource.DEPTH_INFINITE, null);
                } catch (CoreException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public static File resolvePropertiesFileNameAgainstClasspath(String fileName, IJavaProject javaProject) throws Exception {
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
                    File f = resolvePropertiesFileNameAgainstClasspath(fileName, javaProj);
                    if (f != null)
                        return f;
                }
            } else if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(entry.getPath());
                if (folder != null) {
                    File entryFile = folder.getLocation().toFile().getAbsoluteFile();
                    File f = new File(new File(entryFile, "META-INF"), fileName);
                    if (f.exists())
                        return f;
                }
            }
        }
        return null;
    }

    public static Collection<File> resolvePropertiesFileNamesAgainstClasspath(Collection<String> fileNames, IJavaProject javaProject, List<String> errorMessages) throws Exception {
        Set<File> result = new LinkedHashSet<File>();
        for (String fileName : fileNames) {
            File f = resolvePropertiesFileNameAgainstClasspath(fileName, javaProject);
            if (f == null) {
                String errorMessage = MessageFormat.format("File ''{0}'' not found", fileName);
                if (errorMessages == null)
                    throw new FileNotFoundException(errorMessage);
                errorMessages.add(errorMessage);
            } else
                result.add(f);
        }
        return result;
    }
}
