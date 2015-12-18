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

/**
 * @author <a href="mailto:vgolding@algotrader.ch">Vince Golding</a>
 */
public enum SubscriptionTopic {

    ORDER("order", Policy.LAST_IMAGE),
    ORDER_STATUS("order-status", Policy.LAST_IMAGE),
    TRANSACTION("transaction", Policy.DEFAULT),
    TICK("tick", Policy.LAST_IMAGE),
    POSITION("position", Policy.LAST_IMAGE),
    CASH_BALANCE("cash-balance", Policy.LAST_IMAGE),
    MARKET_DATA_SUBSCRIPTION("market-data-subscription", Policy.LAST_IMAGE),
    LOG_EVENT("log-event", Policy.DEFAULT),
    ;

    enum Policy { LAST_IMAGE, DEFAULT };

    private final String baseTopic;
    private final Policy policy;

    SubscriptionTopic(final String baseTopic, final Policy policy) {
        this.baseTopic = baseTopic;
        this.policy = policy;
    }

    public String getBaseTopic() {
        return baseTopic;
    }

    public Policy getPolicy() {
        return policy;
    }

}
