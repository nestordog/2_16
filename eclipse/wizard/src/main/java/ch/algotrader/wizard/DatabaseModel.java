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

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public final class DatabaseModel implements IDatabaseModel {

    private static final String ALGOTRADER_CORE = "algotrader-core";
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";

    @Override
    public Collection<String> getDataSets(MarketDataType marketDataType) throws CoreException {

        IProject coreProject = ResourcesPlugin.getWorkspace().getRoot().getProject(ALGOTRADER_CORE);
        if (coreProject == null || !coreProject.exists()) {
            throw new IllegalStateException("algotrader-core project is not available");
        }

        IFolder folder = coreProject.getFolder(File.separator + "files" + File.separator + marketDataType.toString().toLowerCase() + "data");

        List<String> list = new LinkedList<String>();
        for (IResource resource : folder.members()) {
            if (!resource.getName().startsWith(".")) {
                list.add(resource.getName());
            }
        }
        return list;
    }

    @Override
    public Collection<String> getDatabases() throws ClassNotFoundException, SQLException {

        Connection con = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            rs = con.getMetaData().getCatalogs();

            List<String> list = new LinkedList<String>();
            while (rs.next()) {
                list.add(rs.getString("TABLE_CAT"));
            }

            return list;

        } finally {
            if (rs != null) {
                rs.close();
            }
            if (con != null) {
                con.close();
            }
        }
    }

    @Override
    public String getInstruments(MarketDataType marketDataType, String dataSet) throws ClassNotFoundException, SQLException, CoreException {

        IProject coreProject = ResourcesPlugin.getWorkspace().getRoot().getProject(ALGOTRADER_CORE);
        if (coreProject == null) {
            throw new IllegalStateException("algotrader-core project is not available");
        }

        IFolder folder = coreProject.getFolder(File.separator + "files" + File.separator + marketDataType.toString().toLowerCase() + "data" + File.separator + dataSet);

        if (folder.members().length > 0) {
            StringBuffer buffer = new StringBuffer();
            for (IResource resource : folder.members()) {
                if (!resource.getName().startsWith(".")) {
                    buffer.append("'");
                    buffer.append(resource.getName().subSequence(0, resource.getName().lastIndexOf(".")));
                    buffer.append("'");
                    buffer.append(",");
                }
            }
            buffer.deleteCharAt(buffer.length() - 1);
            return buffer.toString();
        } else {
            return ",";
        }
    }
}
