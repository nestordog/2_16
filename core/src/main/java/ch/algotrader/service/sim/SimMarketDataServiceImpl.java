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
package ch.algotrader.service.sim;

import ch.algotrader.entity.security.Security;
import ch.algotrader.service.sim.SimMarketDataServiceBase;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class SimMarketDataServiceImpl extends SimMarketDataServiceBase {

    @Override
    protected void handleExternalSubscribe(Security security) throws Exception {
        throw new UnsupportedOperationException("ExternalSubscribe not allowed in simulation");
    }

    @Override
    protected void handleExternalUnsubscribe(Security security) throws Exception {
        throw new UnsupportedOperationException("ExternalUnsubscribe not allowed in simulation");
    }
}
