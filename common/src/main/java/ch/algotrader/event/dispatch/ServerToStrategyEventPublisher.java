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
* Server to strategy event publisher.
*
* @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
*/
public class ServerToStrategyEventPublisher implements EventPublisher {

    private final JmsTemplate template;
    private final ActiveMQTopic marketDataTopic;
    private final ActiveMQTopic genericTopic;

    public ServerToStrategyEventPublisher(final JmsTemplate template, final ActiveMQTopic marketDataTopic, final ActiveMQTopic genericTopic) {

        Validate.notNull(template, "JmsTemplate is null");
        Validate.notNull(marketDataTopic, "ActiveMQTopic is null");
        Validate.notNull(genericTopic, "ActiveMQTopic is null");

        this.marketDataTopic = marketDataTopic;
        this.genericTopic = genericTopic;
        this.template = template;
    }

    @Override
    public void publishStrategyEvent(final Object event, final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        this.template.convertAndSend(strategyName + ".QUEUE", event);
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

        this.template.convertAndSend(this.marketDataTopic, event, message -> {
            // add securityId Property
            message.setLongProperty("securityId", event.getSecurityId());
            return message;
        });
    }

}
