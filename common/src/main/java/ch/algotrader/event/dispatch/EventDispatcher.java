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
package ch.algotrader.event.dispatch;

import java.util.Set;

import ch.algotrader.entity.marketData.MarketDataEventVO;

/**
 * Platform wide communication interface capable of submitting events to multiple
 * {@link ch.algotrader.esper.Engine}s and {@link ch.algotrader.event.EventListener}s
 * both inside and outside of the current JRE.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public interface EventDispatcher {

    /**
     * Sends event related to the given strategy to local or remote recipients.
     */
    void sendEvent(String strategyName, Object event);

    /**
     * Re-sends past event related to the given strategy to internal recipients such as UI
     * primarily to restore strategy state upon startup.
     */
    void resendPastEvent(String strategyName, Object event);

    /**
     * Registers market data subscription subscription
     */
    void registerMarketDataSubscription(String strategyName, long securityId);

    /**
     * Un-registers market data subscription subscription
     */
    void unregisterMarketDataSubscription(String strategyName, long securityId);

    /**
     * Sends market data event all subscribed strategies running either locally or remotely.
     */
    void sendMarketDataEvent(MarketDataEventVO marketDataEvent);

    /**
     * broadcasts a generic event to recipients.
     */
    void broadcast(Object event, Set<EventRecipient> recipients);

}
