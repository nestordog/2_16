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

package ch.algotrader.broker.marketdata;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.activemq.broker.region.Subscription;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ConnectionId;
import org.apache.activemq.command.ConsumerId;
import org.apache.activemq.command.ConsumerInfo;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ConsumerEventThrottler {

    private final Logger LOGGER = LogManager.getLogger(ConsumerEventThrottler.class);

    private final int maxPeriodPerConnection;
    private final int minPeriodPerConsumer;
    private final ConcurrentMap<ConsumerId, Long> consumerLastEvent;
    private final ConcurrentMap<String, Long> connectionLastEvent;

    public ConsumerEventThrottler(final double maxRatePerConnection, final double minRatePerConsumer) {
        Validate.notNull(maxRatePerConnection > 0, "Max rate per connection must be positive");
        Validate.notNull(minRatePerConsumer > 0, "Min rate per consumer must be positive");

        this.maxPeriodPerConnection = (int) (1000 / maxRatePerConnection);
        this.minPeriodPerConsumer = (int) (1000 / minRatePerConsumer);
        this.consumerLastEvent = new ConcurrentHashMap<>();
        this.connectionLastEvent = new ConcurrentHashMap<>();
    }

    public void addConsumer(final ConsumerId consumerId) {

        consumerLastEvent.putIfAbsent(consumerId, 0L);
    }

    public void removeConsumer(final ConsumerId consumerId) {

        consumerLastEvent.remove(consumerId);
    }

    public void removeConnection(final ConnectionId connectionId) {

        connectionLastEvent.remove(connectionId.getValue());
    }

    public List<Subscription> throttle(final List<Subscription> consumers) {

        List<Subscription> filteredConsumers = new ArrayList<>();
        for (Subscription consumer: consumers) {
            ConsumerInfo info = consumer.getConsumerInfo();
            ActiveMQDestination activeMQDestination = consumer.getActiveMQDestination();
            ConsumerId consumerId = info.getConsumerId();
            String connectionId = consumerId.getConnectionId();

            Long lastEvent = this.consumerLastEvent.get(consumerId);
            if (lastEvent == null) {
                filteredConsumers.add(consumer);
            } else {
                long now = System.currentTimeMillis();
                boolean propagate = true;
                long delta = now - lastEvent;
                if (delta < this.minPeriodPerConsumer) {
                    Long lastConnectionEvent = this.connectionLastEvent.getOrDefault(connectionId, lastEvent);
                    delta = now - lastConnectionEvent;
                    if (delta <= this.maxPeriodPerConnection) {
                        propagate = false;
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("Dropping {} event for {}; last consumer event = {}; last connection event = {}", activeMQDestination, connectionId,
                                    Instant.ofEpochMilli(lastEvent), Instant.ofEpochMilli(lastConnectionEvent));
                        }
                    }
                }

                if (propagate) {
                    filteredConsumers.add(consumer);
                    if (this.consumerLastEvent.replace(consumerId, lastEvent, now)) {
                        this.connectionLastEvent.put(connectionId, now);
                    }
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Dispatching {} event for {}", activeMQDestination, connectionId);
                    }
                }
            }
        }
        return filteredConsumers;
    }

}
