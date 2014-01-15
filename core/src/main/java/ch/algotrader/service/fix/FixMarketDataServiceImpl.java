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
package ch.algotrader.service.fix;

import ch.algotrader.entity.security.Security;
import ch.algotrader.service.InitializingServiceI;

/**
 * Generic FIXmarket data service
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class FixMarketDataServiceImpl extends FixMarketDataServiceBase implements InitializingServiceI {

    private static final long serialVersionUID = 4880040246465806082L;

    @Override
    protected void handleInit() throws Exception {

    }

    @Override
    protected void handleInitSubscriptions() {

    }

    @Override
    protected void handleExternalSubscribe(Security security) throws Exception {

    }

    @Override
    protected void handleExternalUnsubscribe(Security security) throws Exception {

    }
}

