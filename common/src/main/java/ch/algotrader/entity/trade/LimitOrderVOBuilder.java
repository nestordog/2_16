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
package ch.algotrader.entity.trade;

import java.math.BigDecimal;

/**
 * Factory for LimitOrderVOs
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class LimitOrderVOBuilder extends OrderVOBuilder<LimitOrderVO> {

    private BigDecimal limit;

    LimitOrderVOBuilder() {
    }

    public static LimitOrderVOBuilder create() {
        return new LimitOrderVOBuilder();
    }

    protected BigDecimal getLimit() {
        return this.limit;
    }

    public LimitOrderVOBuilder setLimit(BigDecimal limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public LimitOrderVO build() {
        return new LimitOrderVO(0, this.getIntId(), null, null, this.getSide(), this.getQuantity(), this.getTif(), this.getTifDateTime(), this.getExchangeId(), this.getSecurityId(), this.getAccountId(), this.getStrategyId(), this.limit);
    }

}
