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

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.commons.io.Charsets;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import ch.algotrader.UnrecoverableCoreException;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class TopicEventDumper {

    public static void main(String... args) throws Exception {

        SingleConnectionFactory jmsActiveMQFactory = new SingleConnectionFactory(new ActiveMQConnectionFactory("tcp://localhost:61616"));
        jmsActiveMQFactory.afterPropertiesSet();
        jmsActiveMQFactory.resetConnection();

        ActiveMQTopic topic = new ActiveMQTopic(">");

        DefaultMessageListenerContainer messageListenerContainer = new DefaultMessageListenerContainer();
        messageListenerContainer.setDestination(topic);
        messageListenerContainer.setConnectionFactory(jmsActiveMQFactory);
        messageListenerContainer.setSubscriptionShared(false);
        messageListenerContainer.setCacheLevel(DefaultMessageListenerContainer.CACHE_CONSUMER);

        messageListenerContainer.setMessageListener((MessageListener) message -> {
            try {
                Destination destination = message.getJMSDestination();
                if (message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    System.out.println(destination + " -> " + textMessage.getText());
                } else if (message instanceof BytesMessage) {
                    BytesMessage bytesMessage = (BytesMessage) message;
                    byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
                    bytesMessage.readBytes(bytes);
                    System.out.println(destination + " -> " + new String(bytes, Charsets.UTF_8));
                }
            } catch (JMSException ex) {
                throw new UnrecoverableCoreException(ex);
            }
        });

        messageListenerContainer.initialize();
        messageListenerContainer.start();

        System.out.println("Dumping messages from all topics");

        Runtime.getRuntime().addShutdownHook(new Thread(messageListenerContainer::stop));

        Thread.sleep(Long.MAX_VALUE);
    }

}
