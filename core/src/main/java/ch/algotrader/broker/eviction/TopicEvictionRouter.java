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

import java.util.Optional;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.activemq.command.ActiveMQTopic;

import ch.algotrader.broker.TopicRouter;

public class TopicEvictionRouter implements TopicRouter<TopicEvictionVO> {

    public static final String TOPIC = "evict";
    public static final String ATTRIBUTE_NAME   = "at.topic-to-evict";

    public TopicEvictionRouter() {
    }

    public Destination create(final TopicEvictionVO vo, final Optional<String> strategyName) {
        return new ActiveMQTopic(TOPIC);
    }

    @Override
    public void postProcess(final TopicEvictionVO event, final Message message) throws JMSException {

        message.setStringProperty(ATTRIBUTE_NAME, event.getDestination());
    }

}
