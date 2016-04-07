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

package ch.algotrader.wiring.server;

import javax.jms.ConnectionFactory;
import javax.management.MBeanServer;
import javax.net.ssl.SSLContext;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.algotrader.broker.CoreToUIEventPublisher;
import ch.algotrader.broker.EmbeddedActiveMQBroker;
import ch.algotrader.broker.JMSTopicPublisher;
import ch.algotrader.broker.MarketDataSubscriptionTopicRouter;
import ch.algotrader.broker.SimpleTopicRouter;
import ch.algotrader.broker.StrategyTopicRouter;
import ch.algotrader.broker.SubscriptionTopic;
import ch.algotrader.broker.TopicPublisher;
import ch.algotrader.broker.eviction.TopicEvictionRouter;
import ch.algotrader.broker.eviction.TopicEvictionVO;
import ch.algotrader.broker.subscription.TopicSubscriptionHandler;
import ch.algotrader.config.ConfigParams;
import ch.algotrader.entity.PositionVO;
import ch.algotrader.entity.TransactionVO;
import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.entity.strategy.CashBalanceVO;
import ch.algotrader.entity.trade.OrderStatusVO;
import ch.algotrader.entity.trade.OrderVO;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.json.ObjectMapperFactory;
import ch.algotrader.vo.LogEventVO;
import ch.algotrader.vo.marketData.MarketDataSubscriptionVO;

@Configuration
public class BrokerWiring {

    private static final Logger LOGGER = LogManager.getLogger(BrokerWiring.class);

    @Bean(name = "objectMapper")
    public ObjectMapper createObjectMapper() {

        return new ObjectMapperFactory().create();
    }

    @Profile(value = "!embeddedBroker")
    @Bean(name = "jmsActiveMQFactory")
    public ConnectionFactory createRemoteBrokerConnectionFactory(
            @Value("${activeMQ.host}") final String activeMQHost,
            @Value("${activeMQ.port}") final int activeMQPort) {

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Using standalone message broker at {}", activeMQHost);
        }
        // consider using CachingConnectionFactory instead of SingleConnectionFactory
        // in order to re-use sessions as well as the underlying connection
        return new SingleConnectionFactory(new ActiveMQConnectionFactory("failover:tcp://" + activeMQHost + ":" + activeMQPort));
    }

    @Profile(value = "embeddedBroker")
    @Bean(name="jmsActiveMQFactory")
    @DependsOn("internalBroker")
    public ConnectionFactory createInternalBrokerConnectionFactory() throws Exception {

        LOGGER.info("Using embedded message broker");

        // consider using CachingConnectionFactory instead of SingleConnectionFactory
        // in order to re-use sessions as well as the underlying connection
        return new SingleConnectionFactory(new ActiveMQConnectionFactory("vm://localhost?create=false"));
    }

    @Profile(value = "embeddedBroker")
    @Bean(name = "internalBroker", initMethod = "start", destroyMethod = "stop")
    public EmbeddedActiveMQBroker createEmbeddedActiveMQBroker(
            final ConfigParams configParams,
            final SSLContext serverSSLContext,
            final MBeanServer mbeanServer) {

        boolean sslEnabled = configParams.getBoolean("security.ssl");
        int activeMQPort = configParams.getInteger("activeMQ.port", 61616);
        int activeMQWSPort = configParams.getInteger("activeMQ.ws.port", 61614);
        int activeMQWSSPort = configParams.getInteger("activeMQ.wss.port", 61613);
        double maxRatePerConnection = configParams.getDouble("activeMQ.maxRatePerConnection", 50.0d);
        double minRatePerConsumer = configParams.getDouble("activeMQ.minRatePerConsumer", 0.1d);
        double memUsagePc = configParams.getDouble("activeMQ.memoryUsage", 50.0d);

        return new EmbeddedActiveMQBroker(
                activeMQPort,
                sslEnabled ? activeMQWSSPort : activeMQWSPort,
                memUsagePc, maxRatePerConnection, minRatePerConsumer,
                serverSSLContext,
                mbeanServer);
    }

    @Bean(name = "tickActiveMQTopic")
    public ActiveMQTopic createTickActiveMQTopic() {
        return new ActiveMQTopic(SubscriptionTopic.TICK.getBaseTopic() + ".>");
    }

    @Bean(name = "tickDataListenerContainer")
    public MessageListenerContainer createTickDataListenerContainer(
            final ActiveMQTopic tickActiveMQTopic,
            final ConnectionFactory jmsActiveMQFactory,
            final EventDispatcher eventDispatcher) {

        DefaultMessageListenerContainer messageListenerContainer = new DefaultMessageListenerContainer();
        messageListenerContainer.setDestination(AdvisorySupport.getConsumerAdvisoryTopic(tickActiveMQTopic));
        messageListenerContainer.setConnectionFactory(jmsActiveMQFactory);
        messageListenerContainer.setMessageListener(new TopicSubscriptionHandler(eventDispatcher));
        return messageListenerContainer;
    }

    @Bean(name="internalTopicPublisher")
    public TopicPublisher<Object> createInternalTopicPublisher(
            final ConnectionFactory jmsActiveMQFactory,
            final ObjectMapper objectMapper) {

        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        messageConverter.setObjectMapper(objectMapper);

        JMSTopicPublisher publisher = new JMSTopicPublisher(jmsActiveMQFactory, messageConverter);

        publisher.register(TickVO.class,
                new SimpleTopicRouter<>(SubscriptionTopic.TICK.getBaseTopic(), tick -> String.valueOf(tick.getSecurityId())));
        publisher.register(OrderVO.class,
                new StrategyTopicRouter<>(SubscriptionTopic.ORDER.getBaseTopic(), OrderVO::getIntId));
        publisher.register(OrderStatusVO.class,
                new StrategyTopicRouter<>(SubscriptionTopic.ORDER_STATUS.getBaseTopic(), OrderStatusVO::getIntId));
        publisher.register(TransactionVO.class,
                new StrategyTopicRouter<>(SubscriptionTopic.TRANSACTION.getBaseTopic(), TransactionVO::getUuid));
        publisher.register(PositionVO.class,
                new StrategyTopicRouter<>(SubscriptionTopic.POSITION.getBaseTopic(), position -> String.valueOf(position.getId())));
        publisher.register(CashBalanceVO.class,
                new StrategyTopicRouter<>(SubscriptionTopic.CASH_BALANCE.getBaseTopic(), cashBalance -> String.valueOf(cashBalance.getId())));
        publisher.register(MarketDataSubscriptionVO.class,
                new MarketDataSubscriptionTopicRouter<>(SubscriptionTopic.MARKET_DATA_SUBSCRIPTION.getBaseTopic()));
        publisher.register(LogEventVO.class,
                new SimpleTopicRouter<>(SubscriptionTopic.LOG_EVENT.getBaseTopic(), LogEventVO::getPriority));

        publisher.register(TopicEvictionVO.class, new TopicEvictionRouter());

        return publisher;
    }

    @Bean(name="internalEventPropagator")
    public CoreToUIEventPublisher createInternalEventPropagator(final TopicPublisher<Object> internalTopicPublisher) {

        return new CoreToUIEventPublisher(internalTopicPublisher);
    }

//    @Bean(name="marketDataSubscriptionManager")
//    public MarketDataSubscriptionManager createMarketDataSubscriptionManager(
//            final EventListenerRegistry eventListenerRegistry) {
//
//        MarketDataSubscriptionManager marketDataSubscriptionManager = new MarketDataSubscriptionManager();
//        eventListenerRegistry.register(marketDataSubscriptionManager::onSubscriptionEvent, SubscriptionEventVO.class);
//        return marketDataSubscriptionManager;
//    }
//
}
