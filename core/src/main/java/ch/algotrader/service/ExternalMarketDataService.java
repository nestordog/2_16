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

import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.FeedType;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface ExternalMarketDataService {

    /**
     * Initializes current Subscriptions with the external Market Data Provider.
     */
    public boolean initSubscriptions();

    /**
     * Subscribes a Security with the external Market Data Provider.
     */
    public void subscribe(Security security);

    /**
     * Unsubscribes a Security with the external Market Data Provider.
     */
    public void unsubscribe(Security security);

    /**
     * returns the {@link FeedType} for this ExternalMarketDataService
     */
    public FeedType getFeedType();

}
