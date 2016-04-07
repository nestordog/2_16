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

package ch.algotrader.broker.eviction;

import org.apache.activemq.broker.BrokerPluginSupport;
import org.apache.activemq.broker.ProducerBrokerExchange;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.command.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TopicEvictionPlugin extends BrokerPluginSupport {

    private final Logger LOGGER = LogManager.getLogger(TopicEvictionPlugin.class);

    @Override
    public void send(final ProducerBrokerExchange producerExchange, final Message message) throws Exception {

        super.send(producerExchange, message);

        ActiveMQDestination destination = message.getDestination();
        if (TopicEvictionRouter.TOPIC.equals(destination.getPhysicalName())) {

            String topicToEvict = (String) message.getProperty(TopicEvictionRouter.ATTRIBUTE_NAME);
            if (topicToEvict != null) {
                try {
                    getRoot().removeDestination(producerExchange.getConnectionContext(), new ActiveMQTopic(topicToEvict), 1);
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        }
    }

}
