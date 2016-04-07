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

package ch.algotrader.broker.subscription;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.event.dispatch.EventRecipient;

/**
 * @author <a href="mailto:vgolding@algotrader.ch">Vince Golding</a>
 */
public class TopicSubscriptionHandler implements MessageListener {

    private final Logger LOGGER = LogManager.getLogger(TopicSubscriptionHandler.class);

    private final ConcurrentMap<String, Boolean> subscriptions;
    private final EventDispatcher eventDispatcher;

    public TopicSubscriptionHandler(final EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
        this.subscriptions = new ConcurrentHashMap<>();
    }

    @Override
    public void onMessage(Message message) {
        if (message instanceof ActiveMQMessage) {
            ActiveMQMessage mqMessage = (ActiveMQMessage) message;
            try {
                if (mqMessage.propertyExists("consumerCount")) {
                    Integer consumerCount = (Integer) mqMessage.getProperty("consumerCount");

                    SubscriptionEventVO event = createEvent(consumerCount, mqMessage.getDestination());
                    handleSubscriptionEvent(event);
                }
            } catch (Exception ioe) {
                LOGGER.error("unable to retrieve consumerCount property", ioe);
            }
        }
    }

    private void handleSubscriptionEvent(SubscriptionEventVO event) {
        if (event.isSubscribe()) {
            subscriptions.computeIfAbsent(event.getSubscriptionKey(), k -> handleSubscribe(event));
        } else {
            subscriptions.computeIfPresent(event.getSubscriptionKey(), (k, v) -> handleUnSubscribe(event));
        }
    }

    private Boolean handleSubscribe(SubscriptionEventVO event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Subscribe: {}.{}", event.getBaseTopic(), event.getSubscriptionKey());
        }
        this.eventDispatcher.broadcast(event, EventRecipient.ALL_LOCAL_LISTENERS);
        return true;
    }

    private Boolean handleUnSubscribe(SubscriptionEventVO event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Unsubscribe: {}.{}", event.getBaseTopic(), event.getSubscriptionKey());
        }
        this.eventDispatcher.broadcast(event, EventRecipient.ALL_LOCAL_LISTENERS);
        return null;  // return null removes from the subscription map
    }

    public SubscriptionEventVO createEvent(final int consumerCount, final ActiveMQDestination destination) {
        String physicalName = destination.getPhysicalName();
        int from = physicalName.lastIndexOf(AdvisorySupport.TOPIC_CONSUMER_ADVISORY_TOPIC_PREFIX);
        String topic = physicalName.substring(from + AdvisorySupport.TOPIC_CONSUMER_ADVISORY_TOPIC_PREFIX.length());
        String parts[] = topic.split("\\.");
        return new SubscriptionEventVO(parts[0], parts.length > 1 ? parts[1] : "*", consumerCount > 0);
    }

}
