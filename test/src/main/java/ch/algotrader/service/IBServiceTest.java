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
package ch.algotrader.service;

import org.junit.BeforeClass;

import ch.algotrader.ServiceLocator;
import ch.algotrader.esper.Engine;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBServiceTest {

    private static boolean initialized = false;

    @BeforeClass
    public static void setupClass() {

        ServiceLocator.instance().init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);

        if (!initialized) {

            Engine engine = ServiceLocator.instance().getEngineManager().getServerEngine();
            engine.setInternalClock(true);
            engine.deployAllModules();

            ServiceLocator.instance().getLifecycleManager().runServices();

            initialized = true;
        }
    }
}
