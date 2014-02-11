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

import ch.algotrader.service.CashBalanceServiceTest;
import ch.algotrader.service.CombinationServiceTest;
import ch.algotrader.service.PortfolioPersistenceServiceTest;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    CashBalanceServiceTest.class,
    CombinationServiceTest.class,
    PortfolioPersistenceServiceTest.class
 })
public class LocalTestSuite {

}
