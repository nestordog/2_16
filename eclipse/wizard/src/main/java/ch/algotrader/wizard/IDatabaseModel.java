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

import java.sql.SQLException;
import java.util.Collection;

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
     * gets the securities that correspond to the files inside the dataset directory
     */
    String getInstruments(MarketDataType marketDataType, String dataSet) throws ClassNotFoundException, SQLException, CoreException;
}
