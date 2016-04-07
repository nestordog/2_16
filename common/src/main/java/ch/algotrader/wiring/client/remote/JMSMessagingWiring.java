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

package ch.algotrader.wiring.client.remote;

import javax.jms.ConnectionFactory;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.SimpleMessageConverter;

import ch.algotrader.event.EventPublisher;
import ch.algotrader.event.dispatch.StrategyToStrategyEventPublisher;

@Configuration
public class JMSMessagingWiring {

    @Bean(name = "jmsActiveMQFactory")
    public ConnectionFactory createInternalBrokerConnectionFactory(
            @Value("${activeMQ.host}") final String activeMQHost,
            @Value("${activeMQ.port}") final int activeMQPort) {

        return new SingleConnectionFactory(new ActiveMQConnectionFactory("failover:tcp://" + activeMQHost + ":" + activeMQPort));
    }

    @Bean(name = "genericActiveMQTopic")
    public ActiveMQTopic createGenericActiveMQTopic() {

        return new ActiveMQTopic("GENERIC.TOPIC");
    }

    @Bean(name = "marketDataActiveMQTopic")
    public ActiveMQTopic createMarketDataActiveMQTopic() {

        return new ActiveMQTopic("MARKETDATA.TOPIC");
    }

    @Bean(name = "jmsMessageConverter")
    public MessageConverter createSimpleMessageConverter() {

        return new SimpleMessageConverter();
    }

    @Bean(name = "jmsTemplate")
    public JmsTemplate createJmsTemplate(
            final ConnectionFactory jmsActiveMQFactory,
            final MessageConverter jmsMessageConverter) {

        JmsTemplate template = new JmsTemplate(jmsActiveMQFactory);
        template.setTimeToLive(10000);
        template.setDeliveryPersistent(false);
        template.setMessageIdEnabled(false);
        template.setExplicitQosEnabled(true);
        template.setSessionAcknowledgeMode(Session.DUPS_OK_ACKNOWLEDGE);
        template.setMessageConverter(jmsMessageConverter);
        return template;
    }

    @Bean(name = "remoteEventPropagator")
    public EventPublisher createRemoteEventPropagator(
            final JmsTemplate jmsTemplate,
            final ActiveMQTopic genericActiveMQTopic) {

        return new StrategyToStrategyEventPublisher(jmsTemplate, genericActiveMQTopic);
    }

    @Bean(name = "marketDataMessageListenerContainer")
    public MessageListenerContainer createMarketDataMessageListenerContainer(
            final ActiveMQTopic marketDataActiveMQTopic,
            final ConnectionFactory jmsActiveMQFactory,
            final MessageConverter jmsMessageConverter,
            final MessageListener eventDispatcher) {

        DefaultMessageListenerContainer messageListenerContainer = new DefaultMessageListenerContainer();
        messageListenerContainer.setDestination(marketDataActiveMQTopic);
        messageListenerContainer.setConnectionFactory(jmsActiveMQFactory);
        messageListenerContainer.setMessageConverter(jmsMessageConverter);
        messageListenerContainer.setMessageSelector("false");
        messageListenerContainer.setCacheLevel(DefaultMessageListenerContainer.CACHE_CONSUMER);
        messageListenerContainer.setMessageListener(eventDispatcher);
        return messageListenerContainer;
    }

    @Bean(name = "genericMessageListenerContainer")
    public MessageListenerContainer createGenericMessageListenerContainer(
            final ActiveMQTopic genericActiveMQTopic,
            final ConnectionFactory jmsActiveMQFactory,
            final MessageConverter jmsMessageConverter,
            final MessageListener eventDispatcher) {

        DefaultMessageListenerContainer messageListenerContainer = new DefaultMessageListenerContainer();
        messageListenerContainer.setDestination(genericActiveMQTopic);
        messageListenerContainer.setConnectionFactory(jmsActiveMQFactory);
        messageListenerContainer.setMessageConverter(jmsMessageConverter);
        messageListenerContainer.setMessageSelector("false");
        messageListenerContainer.setCacheLevel(DefaultMessageListenerContainer.CACHE_CONSUMER);
        messageListenerContainer.setMessageListener(eventDispatcher);
        return messageListenerContainer;
    }

    @Bean(name = "strategyMessageListenerContainer")
    public MessageListenerContainer createStrategyMessageListenerContainer(
            final ActiveMQQueue strategyQueue,
            final ConnectionFactory jmsActiveMQFactory,
            final MessageConverter jmsMessageConverter,
            final MessageListener eventDispatcher) {

        DefaultMessageListenerContainer messageListenerContainer = new DefaultMessageListenerContainer();
        messageListenerContainer.setDestination(strategyQueue);
        messageListenerContainer.setConnectionFactory(jmsActiveMQFactory);
        messageListenerContainer.setMessageConverter(jmsMessageConverter);
        messageListenerContainer.setMessageListener(eventDispatcher);
        return messageListenerContainer;
    }

}
