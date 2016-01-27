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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServer;

import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.jmx.ManagementContext;
import org.apache.activemq.broker.region.policy.LastImageSubscriptionRecoveryPolicy;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.filter.DestinationMapEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.broker.eviction.TopicEvictionPlugin;
import ch.algotrader.broker.marketdata.ConsumerEventThrottler;
import ch.algotrader.broker.marketdata.ThrottlingDispatchPolicy;
import ch.algotrader.broker.marketdata.TickThrottlingPlugin;
import ch.algotrader.enumeration.InitializingServiceType;
import ch.algotrader.service.InitializationPriority;
import ch.algotrader.service.InitializingServiceI;

@InitializationPriority(value = InitializingServiceType.CONNECTOR, priority = 1)
public class EmbeddedActiveMQBroker implements InitializingServiceI {

    private final Logger LOGGER = LogManager.getLogger(EmbeddedActiveMQBroker.class);

    private final BrokerService broker;
    private final int port;
    private final int wsPort;

    public EmbeddedActiveMQBroker(
            final int port, final int wsPort,
            final double memUsagePc,
            final double maxRatePerConnection, final double minRatePerConsumer,
            final MBeanServer mbeanServer) {
        this.port = port;
        this.wsPort = wsPort;
        this.broker = new BrokerService();

        if (mbeanServer != null) {
            ManagementContext managementContext = new ManagementContext(mbeanServer);
            managementContext.setCreateMBeanServer(false);
            managementContext.setCreateConnector(false);
            this.broker.setManagementContext(managementContext);
            this.broker.setUseJmx(true);
        } else {
            this.broker.setUseJmx(false);
        }
        this.broker.setPersistent(false);
        this.broker.setAdvisorySupport(true);

        long jvmLimit = Runtime.getRuntime().maxMemory();
        long activeMQMemLimit = (long) (jvmLimit * (memUsagePc > 0 && memUsagePc < 90 ? (memUsagePc / 100) : 0.7));
        this.broker.getSystemUsage().getMemoryUsage().setLimit(activeMQMemLimit);

        ConsumerEventThrottler tickThrottler = new ConsumerEventThrottler(maxRatePerConnection, minRatePerConsumer);

        PolicyMap policyMap = new PolicyMap();
        LastImageSubscriptionRecoveryPolicy policy = new LastImageSubscriptionRecoveryPolicy();
        List<DestinationMapEntry> entryList = new ArrayList<>();
        for (SubscriptionTopic subscriptionTopic: SubscriptionTopic.values()) {
            if (subscriptionTopic.getPolicy() == SubscriptionTopic.Policy.LAST_IMAGE) {
                PolicyEntry policyEntry = new PolicyEntry();
                policyEntry.setSubscriptionRecoveryPolicy(policy);
                policyEntry.setDestination(new ActiveMQTopic(subscriptionTopic.getBaseTopic() +  ".>"));
                if (subscriptionTopic == SubscriptionTopic.TICK) {
                    policyEntry.setDispatchPolicy(new ThrottlingDispatchPolicy(tickThrottler));
                }
                entryList.add(policyEntry);
            }
        }
        policyMap.setPolicyEntries(entryList);
        this.broker.setDestinationPolicy(policyMap);
        this.broker.setPlugins(new BrokerPlugin[]{new TopicEvictionPlugin(), new TickThrottlingPlugin(tickThrottler)});
    }

    public void start() throws Exception {

        this.broker.start();
    }

    public void stop() throws Exception {

        this.broker.stop();
    }

    @Override
    public void init() throws Exception {

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("JMS connector on port {}", this.port);
        }
        this.broker.addConnector(new URI("tcp", null, "0.0.0.0", this.port, null, null, null));
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Websocket connector on port {}", this.wsPort);
        }
        this.broker.addConnector(new URI("ws", null, "0.0.0.0", this.wsPort, null, null, null));

        this.broker.startAllConnectors();

    }

}
