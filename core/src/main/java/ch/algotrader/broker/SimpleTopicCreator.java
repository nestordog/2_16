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

import org.apache.activemq.command.ActiveMQTopic;

public class SimpleTopicCreator<T> implements TopicCreator<T> {

    private final String baseTopic;
    private final Function<T, String> idExtractor;

    public SimpleTopicCreator(final String baseTopic, final Function<T, String> idExtractor) {
        this.baseTopic = baseTopic;
        this.idExtractor = idExtractor;
    }

    public Destination create(final T vo, final Optional<String> strategyName) {
        StringBuilder buf = new StringBuilder();
        buf.append(this.baseTopic);
        buf.append(SEPARATOR);
        buf.append(idExtractor.apply(vo));
        return new ActiveMQTopic(buf.toString());
    }

}
