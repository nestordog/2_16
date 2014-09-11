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

import java.io.InputStream;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.mysql.MySqlConnection;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

public abstract class AbstractDaoDbUnitTestCase extends AbstractDaoTestCase {

    protected static MySqlConnection dbunitConnection;

    @BeforeClass
    public static void setupDbUnit() throws Exception {

        dbunitConnection = new MySqlConnection(connection, null);
        dbunitConnection.getConfig().setProperty(DatabaseConfig.PROPERTY_ESCAPE_PATTERN, "`?`");
    }

    @AfterClass
    public static void closeDbUnit() throws Exception {

        if (dbunitConnection != null) {
            dbunitConnection = null;
        }
    }

    public static IDataSet getDataSet(String name) throws Exception {

        InputStream inputStream = AbstractDaoDbUnitTestCase.class.getResourceAsStream(name);
        Assert.assertNotNull("file " + name + " not found in classpath", inputStream);

        FlatXmlDataSet dataset = new FlatXmlDataSetBuilder().build(inputStream);
        return dataset;
    }

    public static IDataSet getReplacedDataSet(String name, long id) throws Exception {

        IDataSet originalDataSet = getDataSet(name);

        ReplacementDataSet replacementDataSet = new ReplacementDataSet(originalDataSet);
        replacementDataSet.addReplacementObject("[NULL]", null);
        return replacementDataSet;
    }
}
