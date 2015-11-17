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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.apache.commons.lang.Validate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;

import ch.algotrader.event.EventTypeCache;

public class JMSTopicPublisher implements TopicPublisher<Object> {

    private final EventTypeCache eventTypeCache;
    private final ConcurrentMap<Class<?>, TopicCreator<?>> topicCreators;
    private final JmsTemplate template;

    public JMSTopicPublisher(final ConnectionFactory connectionFactory, final MessageConverter converter) {
        this.eventTypeCache = new EventTypeCache();
        this.topicCreators = new ConcurrentHashMap<>();
        this.template = new JmsTemplate(connectionFactory);
        this.template.setMessageConverter(converter);
    }

    public <T> void register(final Class<T> eventClass, final TopicCreator<T> topicCreator) {

        this.topicCreators.put(eventClass, topicCreator);
    }

    public void publish(final Object event) {

        publish(event, Optional.<String>empty());
    }

    public void publish(final Object event, final Optional<String> strategyName) {

        Validate.notNull(event, "Event is null");

        Class<?>[] allTypes = this.eventTypeCache.getTypeHierarchy(event.getClass());
        for (Class<?> type: allTypes) {

            @SuppressWarnings("unchecked")
            TopicCreator<Object> topicCreator = (TopicCreator<Object>) this.topicCreators.get(type);
            if (topicCreator != null) {
                Destination destination = topicCreator.create(event, strategyName);
                this.template.convertAndSend(destination, event);
                return;
            }
        }
    }

}
