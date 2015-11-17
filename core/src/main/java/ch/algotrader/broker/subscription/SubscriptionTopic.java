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

/**
 * @author <a href="mailto:vgolding@algotrader.ch">Vince Golding</a>
 */
public enum SubscriptionTopic {

    ORDER("order"),
    ORDER_STATUS("order-status"),
    TRANSACTION("transaction"),
    TICK("tick"),
    POSITION("position"),
    MARKET_DATA_SUBSCRIPTION("market-data-subscription"),
    ;

    final String baseTopic;

    SubscriptionTopic(String baseTopic) {
        this.baseTopic = baseTopic;
    }

    public String getBaseTopic() {
        return baseTopic;
    }

}
