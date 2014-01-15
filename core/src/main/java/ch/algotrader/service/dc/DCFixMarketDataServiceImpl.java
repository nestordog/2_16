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
package ch.algotrader.service.dc;

import quickfix.fix44.MarketDataRequest;
import ch.algotrader.entity.security.Security;

/**
 * DukasCopy market data service implementation.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class DCFixMarketDataServiceImpl extends DCFixMarketDataServiceBase {

    private static final long serialVersionUID = 7765025849172510539L;

    @Override
    protected void handleSendSubscribeRequest(Security security, MarketDataRequest request) throws Exception {
    }

    @Override
    protected void handleSendUnsubscribeRequest(Security security, MarketDataRequest request) throws Exception {

    }
}
