/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package com.algoTrader.entity.marketData;

import java.math.BigDecimal;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.enumeration.Direction;
import com.algoTrader.util.CustomToStringStyle;

public abstract class MarketDataEventImpl extends MarketDataEvent {

    private static final long serialVersionUID = 8758212212560594623L;

    private static @Value("${simulation}") boolean simulation;

    @Override
    public BigDecimal getSettlement() {

        if (simulation && (super.getSettlement() == null || super.getSettlement().compareTo(new BigDecimal(0)) == 0)) {
            return getCurrentValue();
        } else {
            return super.getSettlement();
        }
    }

    @Override
    public double getCurrentValueDouble() {

        return getCurrentValue().doubleValue();
    }

    @Override
    public double getMarketValueDouble(Direction direction) {

        return getMarketValue(direction).doubleValue();
    }

    @Override
    public String toString() {

        return ToStringBuilder.reflectionToString(this, CustomToStringStyle.getInstance());
    }
}
