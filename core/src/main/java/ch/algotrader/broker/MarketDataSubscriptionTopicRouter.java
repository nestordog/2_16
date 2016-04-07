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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.activemq.command.ActiveMQTopic;
import org.apache.commons.lang.Validate;

import ch.algotrader.vo.marketData.MarketDataSubscriptionVO;

public class MarketDataSubscriptionTopicRouter<T extends MarketDataSubscriptionVO> implements TopicRouter<T> {

    private final String baseTopic;

    public MarketDataSubscriptionTopicRouter(final String baseTopic) {

        Validate.notEmpty(baseTopic, "Base topic is empty");

        this.baseTopic = baseTopic;
    }

    public Destination create(final T vo, final Optional<String> strategyName) {
        StringBuilder buf = new StringBuilder();
        buf.append(this.baseTopic);
        buf.append(SEPARATOR);
        buf.append(vo.getStrategyName());
        buf.append(SEPARATOR);
        buf.append(vo.getSecurityId());
        if (vo.getFeedType() != null) {
            buf.append(SEPARATOR);
            buf.append(vo.getFeedType());
        }
        return new ActiveMQTopic(buf.toString());
    }

    @Override
    public void postProcess(final T event, final Message message) throws JMSException {
    }

}
