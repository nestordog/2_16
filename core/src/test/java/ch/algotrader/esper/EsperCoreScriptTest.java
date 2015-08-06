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
package ch.algotrader.esper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;

public class EsperCoreScriptTest extends EsperTestBase {

    private EPServiceProvider epService;

    @Before
    public void setupEsper() {
        Configuration config = new Configuration();
        config.configure("/META-INF/esper-common.cfg.xml");
        config.configure("/META-INF/esper-core.cfg.xml");
        epService = EPServiceProviderManager.getDefaultProvider(config);

    }

    @After
    public void cleanUpEsper() {
        if (epService != null) {
            epService.destroy();
        }
    }

    @Test
    public void testMarketData() throws Exception {
        ensureCompilable(epService, getClass().getResource("/module-market-data.epl"));
    }

    @Test
    public void testIB() throws Exception {
        ensureCompilable(epService, getClass().getResource("/module-market-data.epl"));
        ensureCompilable(epService, getClass().getResource("/module-ib.epl"));
    }

    @Test
    public void testPersistMarketData() throws Exception {
        ensureCompilable(epService, getClass().getResource("/module-persist-market-data.epl"));
    }

    @Test
    public void testPerformance() throws Exception {
        ensureCompilable(epService, getClass().getResource("/module-performance.epl"));
    }

    @Test
    public void testPortfolio() throws Exception {
        ensureCompilable(epService, getClass().getResource("/module-current-values.epl"));
        ensureCompilable(epService, getClass().getResource("/module-portfolio.epl"));
    }

    @Test
    public void testTrades() throws Exception {
        ensureCompilable(epService, getClass().getResource("/module-current-values.epl"));
        ensureCompilable(epService, getClass().getResource("/module-trades.epl"));
    }

    @Test
    public void testCombination() throws Exception {
        ensureCompilable(epService, getClass().getResource("/module-combination.epl"));
    }

    @Test
    public void testAlgoIncremental() throws Exception {
        ensureCompilable(epService, getClass().getResource("/module-current-values.epl"));
        ensureCompilable(epService, getClass().getResource("/module-algo-incremental.epl"));
    }

    @Test
    public void testAlgoSlicing() throws Exception {
        ensureCompilable(epService, getClass().getResource("/module-current-values.epl"));
        ensureCompilable(epService, getClass().getResource("/module-trades.epl"));
        ensureCompilable(epService, getClass().getResource("/module-algo-slicing.epl"));
    }

}
