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
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

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
                files = getFiles((IProject) newInput);
            } catch (IOException e) {
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

    List<File> getFiles(IProject project) throws IOException {
        File file = project.getFile("src/main/resources/META-INF/" + project.getName() + ".hierarchy").getLocation().toFile();
        if (!file.exists()) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(), "hierarchy file missing", "Config Editor was not able to locate src\\main\\resources\\META-INF\\" + project.getName()
                    + ".hierarchy");
            return null;
        }
        String[] fileNames = null;
        BufferedReader br = new BufferedReader(new FileReader(file));
        try {
            fileNames = br.readLine().split(":");
        } catch (Exception e) {
            e.printStackTrace();
            MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "hierarchy file is empty", "this projects hierarchy file is epmty");
            return Collections.emptyList();
        } finally {
            br.close();
        }
        List<File> files = new ArrayList<File>();
        for (String fname : fileNames) {
            File f = project.getFile("src/main/resources/META-INF/" + fname + ".properties").getLocation().toFile();
            if (f.exists())
                files.add(f);
            else {
                MessageDialog.openError(Display.getCurrent().getActiveShell(), "hierarchy file broken", "Config Editor has encountered problems, while reading src\\main\\resources\\META-INF\\"
                        + project.getName() + ".hierarchy");
                return Collections.emptyList();
            }
            StructuredProperties structProps = new StructuredProperties();
            this.editorPropertyPage.propMap.put(f, structProps);
            try {
                structProps.load(f);
            } catch (Exception e) {
                e.printStackTrace();
                MessageDialog.openError(Display.getCurrent().getActiveShell(), "error while reading property file", "Error in:\n" + f.getPath() + "\n Caused by: \n" + e.getMessage());
                return Collections.emptyList();
            }

            List<Object[]> elementsList = new ArrayList<Object[]>();
            for (String key : structProps.getKeys())
                elementsList.add(new Object[] { key, structProps.getValue(key) });
            this.editorPropertyPage.editorData.put(f, elementsList.toArray());
        }

        if (files.isEmpty()) {
            MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "hierarchy file is empty", "this projects hierarchy file is epmty");
            return Collections.emptyList();
        }

        return files;
    }
}
