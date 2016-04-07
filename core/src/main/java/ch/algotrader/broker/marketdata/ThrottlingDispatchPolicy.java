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

import java.util.List;

import org.apache.activemq.broker.region.MessageReference;
import org.apache.activemq.broker.region.Subscription;
import org.apache.activemq.broker.region.policy.SimpleDispatchPolicy;
import org.apache.activemq.filter.MessageEvaluationContext;

public class ThrottlingDispatchPolicy extends SimpleDispatchPolicy {

    private final ConsumerEventThrottler throttler;

    public ThrottlingDispatchPolicy(final ConsumerEventThrottler throttler) {
        this.throttler = throttler;
    }

    @Override
    public boolean dispatch(
            final MessageReference node,
            final MessageEvaluationContext msgContext,
            final List<Subscription> consumers) throws Exception {

        List<Subscription> filteredConsumers;
        synchronized (consumers) {
            filteredConsumers = this.throttler.throttle(consumers);
        }
        return super.dispatch(node, msgContext, filteredConsumers);
    }

}
