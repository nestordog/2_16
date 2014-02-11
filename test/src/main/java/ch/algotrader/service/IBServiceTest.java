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

import org.junit.BeforeClass;

import ch.algotrader.ServiceLocator;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineLocator;

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

            Engine engine = EngineLocator.instance().initBaseEngine();
            engine.setInternalClock(true);
            engine.deployAllModules();

            ServiceLocator.instance().initInitializingServices();

            initialized = true;
        }
    }
}
