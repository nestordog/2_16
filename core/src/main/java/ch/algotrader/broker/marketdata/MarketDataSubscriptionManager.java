/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/

package ch.algotrader.broker.marketdata;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.broker.SubscriptionTopic;
import ch.algotrader.broker.subscription.SubscriptionEventVO;

public final class MarketDataSubscriptionManager {

    private final Logger LOGGER = LogManager.getLogger(MarketDataSubscriptionManager.class);

    public void onSubscriptionEvent(final SubscriptionEventVO event) {

        if (event.getBaseTopic().equalsIgnoreCase(SubscriptionTopic.TICK.getBaseTopic())) {

            if (LOGGER.isDebugEnabled()) {
                if (event.isSubscribe()) {
                    LOGGER.debug("Market data subscription requested for security {}", event.getSubscriptionKey());
                } else {
                    LOGGER.debug("Market data subscription terminated for security {}", event.getSubscriptionKey());
                }
            }
        }
    }

}
