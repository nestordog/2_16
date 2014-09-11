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

import java.lang.reflect.Method;

import org.dbunit.Assertion;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

@RunWith(AbstractDaoDbUnitTemplateTestCase.DataSetsTemplateRunner.class)
public class AbstractDaoDbUnitTemplateTestCase extends AbstractDaoDbUnitTestCase {

    protected static long id;

    public static class DataSetsTemplateRunner extends BlockJUnit4ClassRunner {

        public DataSetsTemplateRunner(Class<?> clazz) throws InitializationError {
            super(clazz);
        }

        @Override
        protected Statement methodInvoker(FrameworkMethod method, Object test) {
            return new AssertDataSetStatement(super.methodInvoker(method, test), method);
        }

        private class AssertDataSetStatement extends Statement {

            private final Statement invoker;
            private FrameworkMethod method;

            public AssertDataSetStatement(Statement invoker, FrameworkMethod method) {
                this.invoker = invoker;
                this.method = method;
            }

            @Override
            public void evaluate() throws Throwable {
                setupDataSet(this.method);
                this.invoker.evaluate();
                assertDataSet(this.method);
            }
        }

        private void setupDataSet(FrameworkMethod method) {

            DataSets dataSetAnnotation = getAnnotation(method);
            if (dataSetAnnotation != null) {
                String dataSetName = dataSetAnnotation.setUpDataSet();
                if (!dataSetName.equals("")) {
                    try {
                        IDataSet dataSet = getReplacedDataSet(dataSetName, id);
                        DatabaseOperation.CLEAN_INSERT.execute(dbunitConnection, dataSet);
                    } catch (Exception e) {
                        throw new RuntimeException("exception inserting dataset " + dataSetName, e);
                    }
                }
            }
        }

        private void assertDataSet(FrameworkMethod method) {

            DataSets dataSetAnnotation = getAnnotation(method);
            if (dataSetAnnotation != null) {
                String dataSetName = dataSetAnnotation.assertDataSet();
                if (!"".equals(dataSetName)) {
                    try {
                        IDataSet expectedDataSet = getReplacedDataSet(dataSetName, id);
                        IDataSet actualDataSet = dbunitConnection.createDataSet();
                        String assertTable = dataSetAnnotation.assertTable();
                        if (!"".equals(assertTable)) {
                            Assertion.assertEqualsIgnoreCols(expectedDataSet, actualDataSet, assertTable, new String[] { "id" });
                        } else {
                            Assertion.assertEquals(expectedDataSet, actualDataSet);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("exception inserting dataset " + dataSetName, e);
                    }
                }
            }
        }

        private DataSets getAnnotation(FrameworkMethod method) {

            Method javaMethod = method.getMethod();
            return javaMethod.getAnnotation(DataSets.class);
        }
    }
}
