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

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface ExternalMarketDataService {

    /**
     * Returns {@code true} if the external service is ready to accept initial
     * subscriptions. Returns {@code false} if the external service is not ready
     * or initial subscriptions have already been activated.
     */
    boolean initSubscriptionReady();

    /**
     * Subscribes the given security with the external market data provider.
     */
    void subscribe(Security security);

    /**
     * Un-subscribes the given security with the external market data provider.
     */
    void unsubscribe(Security security);

    /**
     * returns the feed type for this for this service
     */
    String getFeedType();

    /**
     * returns the session qualifier for this service
     */
    String getSessionQualifier();

}
