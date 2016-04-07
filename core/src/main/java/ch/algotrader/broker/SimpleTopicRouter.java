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
import java.util.function.Function;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.activemq.command.ActiveMQTopic;

public class SimpleTopicRouter<T> implements TopicRouter<T> {

    private final String baseTopic;
    private final Function<T, String> idExtractor;

    public SimpleTopicRouter(final String baseTopic, final Function<T, String> idExtractor) {
        this.baseTopic = baseTopic;
        this.idExtractor = idExtractor;
    }

    public Destination create(final T vo, final Optional<String> strategyName) {
        return new ActiveMQTopic(create(this.baseTopic, this.idExtractor.apply(vo)));
    }

    @Override
    public void postProcess(final T event, final Message message) throws JMSException {
    }

    public static String create(final String baseTopic, final String id) {
        StringBuilder buf = new StringBuilder();
        buf.append(baseTopic);
        buf.append(SEPARATOR);
        buf.append(id);
        return buf.toString();
    }

}
