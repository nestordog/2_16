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

public class EsperCommonScriptTest extends EsperTestBase {

    private EPServiceProvider epService;

    @Before
    public void setupEsper() {
        Configuration config = new Configuration();
        config.configure("/META-INF/esper-common.cfg.xml");
        epService = EPServiceProviderManager.getDefaultProvider(config);

    }

    @After
    public void cleanUpEsper() {
        if (epService != null) {
            epService.destroy();
        }
    }

    @Test
    public void testCurrentValues() throws Exception {
        ensureCompilable(epService, getClass().getResource("/module-current-values.epl"));
    }

    @Test
    public void testMetrics() throws Exception {
        ensureCompilable(epService, getClass().getResource("/module-metrics.epl"));
    }

}
