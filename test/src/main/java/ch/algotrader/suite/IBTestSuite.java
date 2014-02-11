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
package ch.algotrader.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ch.algotrader.cache.CacheTest;
import ch.algotrader.service.HistoricalDataServiceTest;
import ch.algotrader.service.OrderServiceTest;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    CacheTest.class,
    HistoricalDataServiceTest.class,
    OrderServiceTest.class
 })
public class IBTestSuite {

}
