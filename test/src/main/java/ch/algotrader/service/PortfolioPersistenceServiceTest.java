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
package ch.algotrader.service;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.strategy.Strategy;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class PortfolioPersistenceServiceTest extends LocalServiceTest {

    @Test
    public void test() {

        Date fromDate = DateUtils.addDays(Calendar.getInstance().getTime(), -5);

        Strategy strategy = ServiceLocator.instance().getLookupService().getStrategyByName("BASE");

        ServiceLocator.instance().getService("portfolioPersistenceService", PortfolioPersistenceService.class).restorePortfolioValues(strategy, fromDate, new Date());
    }
}
