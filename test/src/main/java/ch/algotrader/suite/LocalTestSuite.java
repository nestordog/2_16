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
package ch.algotrader.suite;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ch.algotrader.service.TransactionServiceTest;
import ch.algotrader.service.CombinationServiceTest;
import ch.algotrader.service.PortfolioPersistenceServiceTest;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TransactionServiceTest.class,
    CombinationServiceTest.class,
    PortfolioPersistenceServiceTest.class
 })
public class LocalTestSuite {

    @BeforeClass 
    public static void setUpClass() {    
        System.setProperty("spring.profiles.active", "singleDataSource");
    }
}
