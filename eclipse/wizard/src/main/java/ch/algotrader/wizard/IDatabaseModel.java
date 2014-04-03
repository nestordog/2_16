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

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * interface to DatabaseModel, resolved to implementation during Wizard initialization
 *
 * @author <a href="mailto:akhikhl@gmail.com">Andrey Hihlovskiy</a>
 *
 * @version $Revision$ $Date$
 */
public interface IDatabaseModel {

    /**
     * gets a list af all local datasets inside algotrader-core
     */
    Collection<String> getDataSets(MarketDataType marketDataType) throws CoreException;

    /**
     * gets a list of all local mysql databases
     */
    Collection<String> getDatabases() throws ClassNotFoundException, SQLException;

    /**
     * gets the next available id from the mysql strategy table
     */
    String getStrategyId(String databaseName) throws ClassNotFoundException, SQLException;

    /**
     * gets the ids of all securities that correspond to the files inside the dataset directory
     */
    String getSubscriptionIds(String databaseName, MarketDataType marketDataType, String dataSet) throws ClassNotFoundException, SQLException, CoreException;

    /**
     * updates the database by resetting the database and uploading the mysql script file of this strategy
     */
    void updateDatabase(String databaseName, IProject project, String artifactId) throws ClassNotFoundException, SQLException, CoreException, IOException;
}
