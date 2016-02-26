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
package ch.algotrader.entity.trade.algo;

import java.math.BigDecimal;

import ch.algotrader.entity.trade.LimitOrder;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class IncrementalOrderStateVO extends AlgoOrderStateVO {

    private static final long serialVersionUID = 1L;

    private final BigDecimal startLimit;

    private final BigDecimal endLimit;

    private BigDecimal currentLimit;

    private LimitOrder limitOrder;

    public IncrementalOrderStateVO(BigDecimal startLimit, BigDecimal endLimit, BigDecimal currentLimit) {
        this.startLimit = startLimit;
        this.endLimit = endLimit;
        this.currentLimit = currentLimit;
    }

    public BigDecimal getStartLimit() {
        return this.startLimit;
    }

    public BigDecimal getEndLimit() {
        return this.endLimit;
    }

    public BigDecimal getCurrentLimit() {
        return this.currentLimit;
    }

    public void setCurrentLimit(BigDecimal currentLimit) {
        this.currentLimit = currentLimit;
    }

    public LimitOrder getLimitOrder() {
        return this.limitOrder;
    }

    public void setLimitOrder(LimitOrder limitOrder) {
        this.limitOrder = limitOrder;
    }
}
