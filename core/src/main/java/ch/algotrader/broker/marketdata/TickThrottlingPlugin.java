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

import java.net.URI;

import org.apache.activemq.broker.BrokerPluginSupport;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.Connector;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.broker.region.Subscription;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ConnectionInfo;
import org.apache.activemq.command.ConsumerInfo;

import ch.algotrader.broker.SubscriptionTopic;

public class TickThrottlingPlugin extends BrokerPluginSupport {

    private final ConsumerEventThrottler throttler;

    public TickThrottlingPlugin(final ConsumerEventThrottler throttler) {
        this.throttler = throttler;
    }

    private boolean isMarketDataEvent(final ActiveMQDestination destination) {

        String[] destinationPaths = destination.getDestinationPaths();
        return destinationPaths.length > 1 && SubscriptionTopic.TICK.getBaseTopic().equals(destinationPaths[0]);
    }

    private boolean isThrottlingRequired(final ConnectionContext context, final ConsumerInfo info) {

        Connector connector = context.getConnector();
        if (connector instanceof TransportConnector) {
            URI uri = ((TransportConnector) connector).getUri();
            return "ws".equalsIgnoreCase(uri.getScheme());
        }
        return false;
    }

    @Override
    public Subscription addConsumer(final ConnectionContext context, final ConsumerInfo info) throws Exception {

        if (isMarketDataEvent(info.getDestination()) && isThrottlingRequired(context, info)) {
            this.throttler.addConsumer(info.getConsumerId());
        }
        return super.addConsumer(context, info);
    }

    @Override
    public void removeConsumer(final ConnectionContext context, final ConsumerInfo info) throws Exception {

        if (isMarketDataEvent(info.getDestination()) && isThrottlingRequired(context, info)) {
            this.throttler.removeConsumer(info.getConsumerId());
        }
        super.removeConsumer(context, info);
    }

    @Override
    public void removeConnection(final ConnectionContext context, final ConnectionInfo info, final Throwable error) throws Exception {

        this.throttler.removeConnection(info.getConnectionId());
        super.removeConnection(context, info, error);
    }

}
