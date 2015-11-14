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
package ch.algotrader.vo.marketData;

import java.io.Serializable;

public class MarketDataSubscriptionVO implements Serializable {

    private static final long serialVersionUID = -3662517250923293233L;

    private final String strategyName;
    private final long securityId;
    private final String feedType;
    private final boolean subscribe;

    public MarketDataSubscriptionVO(final String strategyName, final long securityId, final String feedType, final boolean subscribe) {
        this.strategyName = strategyName;
        this.securityId = securityId;
        this.feedType = feedType;
        this.subscribe = subscribe;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public long getSecurityId() {
        return securityId;
    }

    public String getFeedType() {
        return feedType;
    }

    public boolean isSubscribe() {
        return subscribe;
    }

    @Override
    public String toString() {
        return "{" +
                "strategyName='" + strategyName + '\'' +
                ", securityId=" + securityId +
                ", feedType='" + feedType + '\'' +
                ", subscribe=" + subscribe +
                '}';
    }

}
