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
package ch.algotrader.service.fix;

import ch.algotrader.entity.security.Security;
import ch.algotrader.service.ExternalMarketDataService;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface FixMarketDataService extends ExternalMarketDataService {

    /**
     * Called before sending the subscribe request so that Broker specific Tags can be set.
     */
    public void sendSubscribeRequest(Security security);

    /**
     * Called before sending the unsubscribe request so that Broker specific Tags can be set.
     */
    public void sendUnsubscribeRequest(Security security);

    /**
     * Converts a Security into a market data feed specific tickerId
     */
    public String getTickerId(Security security);

}
