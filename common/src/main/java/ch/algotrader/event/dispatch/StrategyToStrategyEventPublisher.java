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

import org.apache.activemq.command.ActiveMQTopic;
import org.apache.commons.lang.Validate;
import org.springframework.jms.core.JmsTemplate;

import ch.algotrader.entity.marketData.MarketDataEventVO;
import ch.algotrader.event.EventPublisher;

/**
* Strategy to strategy event publisher.
*
* @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
*/
public class StrategyToStrategyEventPublisher implements EventPublisher {

    private final JmsTemplate template;
    private final ActiveMQTopic genericTopic;

    public StrategyToStrategyEventPublisher(final JmsTemplate template, final ActiveMQTopic genericTopic) {

        Validate.notNull(template, "JmsTemplate is null");
        Validate.notNull(genericTopic, "ActiveMQTopic is null");

        this.genericTopic = genericTopic;
        this.template = template;
    }

    @Override
    public void publishStrategyEvent(final Object event, final String strategyName) {

        throw new EventDispatchException("Strategy event publishing not supported");
    }

    @Override
    public void publishGenericEvent(final Object event) {

        this.template.convertAndSend(this.genericTopic, event, message -> {

            // add class Property
            message.setStringProperty("clazz", event.getClass().getName());
            return message;
        });
    }

    @Override
    public void publishMarketDataEvent(final MarketDataEventVO event) {

        throw new EventDispatchException("Market event publishing not supported");
    }

}
