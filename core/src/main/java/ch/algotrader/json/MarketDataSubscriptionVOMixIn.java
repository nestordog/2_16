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
package ch.algotrader.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class MarketDataSubscriptionVOMixIn {

    @JsonCreator
    public MarketDataSubscriptionVOMixIn(
            @JsonProperty(value = "strategyName", required = true) final String strategyName,
            @JsonProperty(value = "securityId", required = true) final long securityId,
            @JsonProperty(value = "feedType", required = false) final String feedType,
            @JsonProperty(value = "subscribe", required = true) final boolean subscribe) {
    }

}
