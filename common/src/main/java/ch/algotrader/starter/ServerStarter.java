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
package ch.algotrader.starter;

import ch.algotrader.ServiceLocator;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineLocator;

/**
 * Abstract Base Class for starting the AlgoTrader Server in Live Trading Mode
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class ServerStarter {

    public static void startServer() throws Exception {

        // deploy all SERVER modules
        Engine engine = EngineLocator.instance().initServerEngine();

        engine.setInternalClock(true);
        engine.deployAllModules();

        // initialize services
        ServiceLocator.instance().initInitializingServices();
    }
}
