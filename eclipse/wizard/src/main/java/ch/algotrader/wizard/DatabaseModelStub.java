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

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * Stub implementation of IDatabaseModel. Used by Wizard, when system property "DatabaseModelStub=true" is defined.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class DatabaseModelStub implements IDatabaseModel {

    @Override
    public Collection<String> getDataSets(MarketDataType marketDataType) throws CoreException {
        List<String> list = new LinkedList<String>();
        for (int i = 0; i < 4; i++) {
            list.add("DataSet_" + marketDataType + "_" + i);
        }
        return list;
    }

    @Override
    public Collection<String> getDatabases() throws ClassNotFoundException, SQLException {
        List<String> list = new LinkedList<String>();
        for (int i = 0; i < 4; i++) {
            list.add("Database_" + i);
        }
        return list;
    }

    @Override
    public String getStrategyId(String databaseName) throws ClassNotFoundException, SQLException {
        return databaseName + "_strategyId";
    }

    @Override
    public String getSubscriptionIds(String databaseName, MarketDataType marketDataType, String dataSet) throws ClassNotFoundException, SQLException, CoreException {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < 4; i++) {
            buffer.append("SubscriptionId_" + databaseName + "_" + marketDataType + "_" + dataSet + "_" + i);
            buffer.append(",");
        }
        buffer.deleteCharAt(buffer.length() - 1);
        return buffer.toString();
    }

    @Override
    public void updateDatabase(String databaseName, IProject project, String artifactId) throws ClassNotFoundException, SQLException, CoreException, IOException {
        System.out.println("DatabaseModelStub.updateDatabase");
    }
}
