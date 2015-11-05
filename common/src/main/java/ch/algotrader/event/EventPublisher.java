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
package ch.algotrader.event;

import ch.algotrader.entity.marketData.MarketDataEventVO;

/**
 * Represents a communication interface to publish various events.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public interface EventPublisher {

    /**
     * Publishes event to the given strategy.
     */
    void publishStrategyEvent(Object event, String strategyName);

    /**
     * Publishes generic event.
     */
    void publishGenericEvent(Object event);

    /**
     * Publishes market data event.
     */
    void publishMarketDataEvent(MarketDataEventVO event);

}
