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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ch.algotrader.entity.AccountTest;
import ch.algotrader.entity.PositionTest;
import ch.algotrader.entity.SubscriptionTest;
import ch.algotrader.entity.TransactionTest;
import ch.algotrader.entity.marketData.BarTest;
import ch.algotrader.entity.marketData.TickTest;
import ch.algotrader.entity.property.PropertyTest;
import ch.algotrader.entity.security.CombinationTest;
import ch.algotrader.entity.security.ComponentTest;
import ch.algotrader.entity.security.EasyToBorrowTest;
import ch.algotrader.entity.security.ForexTest;
import ch.algotrader.entity.security.FutureFamilyTest;
import ch.algotrader.entity.security.FutureTest;
import ch.algotrader.entity.security.ImpliedVolatilityTest;
import ch.algotrader.entity.security.IntrestRateTest;
import ch.algotrader.entity.security.OptionFamilyTest;
import ch.algotrader.entity.security.OptionTest;
import ch.algotrader.entity.security.SecurityFamilyTest;
import ch.algotrader.entity.security.SecurityTest;
import ch.algotrader.entity.security.StockTest;
import ch.algotrader.entity.strategy.CashBalanceTest;
import ch.algotrader.entity.strategy.DefaultOrderPreferenceTest;
import ch.algotrader.entity.strategy.MeasurementTest;
import ch.algotrader.entity.strategy.OrderPreferenceTest;
import ch.algotrader.entity.strategy.PortfolioValueTest;
import ch.algotrader.entity.strategy.StrategyTest;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    AccountTest.class,
    PositionTest.class,
    SubscriptionTest.class,
    TransactionTest.class,
    BarTest.class,
    TickTest.class,
    PropertyTest.class,
    CombinationTest.class,
    ComponentTest.class,
    EasyToBorrowTest.class,
    ForexTest.class,
    FutureFamilyTest.class,
    FutureTest.class,
    ImpliedVolatilityTest.class,
    IntrestRateTest.class,
    OptionFamilyTest.class,
    OptionTest.class,
    SecurityFamilyTest.class,
    SecurityTest.class,
    StockTest.class,
    CashBalanceTest.class,
    DefaultOrderPreferenceTest.class,
    MeasurementTest.class,
    OrderPreferenceTest.class,
    PortfolioValueTest.class,
    StrategyTest.class,
//    OrderStatusTest.class,
//    OrderTest.class
 })
public class EntityTestSuite {

}
