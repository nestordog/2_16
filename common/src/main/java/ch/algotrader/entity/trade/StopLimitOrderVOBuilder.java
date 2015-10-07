/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
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
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.entity.trade;

import java.math.BigDecimal;

/**
 * Factory for StopLimitOrderVOs
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class StopLimitOrderVOBuilder extends OrderVOBuilder<StopLimitOrderVO> {

    private BigDecimal stop;
    private BigDecimal limit;

    StopLimitOrderVOBuilder() {
    }

    public static StopLimitOrderVOBuilder create() {
        return new StopLimitOrderVOBuilder();
    }

    protected BigDecimal getStop() {
        return this.stop;
    }

    protected BigDecimal getLimit() {
        return this.limit;
    }

    public StopLimitOrderVOBuilder setStop(BigDecimal stop) {
        this.stop = stop;
        return this;
    }

    public StopLimitOrderVOBuilder setLimit(BigDecimal limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public StopLimitOrderVO build() {
        return new StopLimitOrderVO(0, this.getIntId(), null, null, this.getSide(), this.getQuantity(), this.getTif(), this.getTifDateTime(), this.getExchangeId(), this.getSecurityId(),
                this.getAccountId(), this.getStrategyId(), this.stop, this.limit);
    }

}
