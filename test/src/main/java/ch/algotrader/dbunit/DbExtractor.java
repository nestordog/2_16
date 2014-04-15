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
package ch.algotrader.dbunit;

import java.io.FileOutputStream;
import java.sql.Connection;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.ext.mysql.MySqlConnection;
import org.hibernate.SessionFactory;
import org.hibernate.impl.SessionFactoryImpl;

import ch.algotrader.ServiceLocator;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class DbExtractor {

    public static void main(String[] args) throws Exception {

        ServiceLocator.instance().init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);

        SessionFactoryImpl sessionFactory = (SessionFactoryImpl)ServiceLocator.instance().getService("sessionFactory", SessionFactory.class);
        Connection connection = sessionFactory.getSettings().getConnectionProvider().getConnection();
        IDatabaseConnection dbunitConnection = new MySqlConnection(connection, "algotrader");

        IDataSet fullDataSet = dbunitConnection.createDataSet();

        //        ITableFilter filter = new DatabaseSequenceFilter(dbunitConnection);
        //        FilteredDataSet filteredDatSet = new FilteredDataSet(filter, fullDataSet);

        FileOutputStream xmlStream = new FileOutputStream("full-database.xml");
        FlatXmlDataSet.write(fullDataSet, xmlStream);

    }
}
