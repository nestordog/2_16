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

package ch.algotrader.broker;

import java.util.Optional;

import ch.algotrader.entity.marketData.MarketDataEventVO;
import ch.algotrader.event.EventPublisher;

public class CoreToUIEventPublisher implements EventPublisher {

    private final TopicPublisher<Object> eventPublisher;

    public CoreToUIEventPublisher(final TopicPublisher<Object> eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publishStrategyEvent(final Object event, final String strategyName) {

        this.eventPublisher.publish(event, Optional.of(strategyName));
    }

    @Override
    public void publishGenericEvent(final Object event) {

        this.eventPublisher.publish(event, Optional.<String>empty());
    }

    @Override
    public void publishMarketDataEvent(final MarketDataEventVO event) {

        this.eventPublisher.publish(event, Optional.<String>empty());
    }

}
