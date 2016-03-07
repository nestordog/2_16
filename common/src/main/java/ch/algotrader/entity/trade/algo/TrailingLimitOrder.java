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

import ch.algotrader.entity.trade.OrderValidationException;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class TrailingLimitOrder extends AlgoOrder {

    private static final long serialVersionUID = -9017761050542085585L;

    private BigDecimal trailingAmount;
    private BigDecimal increment;

    public BigDecimal getTrailingAmount() {
        return this.trailingAmount;
    }

    public void setTrailingAmount(BigDecimal trailingAmount) {
        this.trailingAmount = trailingAmount;
    }

    public BigDecimal getIncrement() {
        return this.increment;
    }

    public void setIncrement(BigDecimal increment) {
        this.increment = increment;
    }

    @Override
    public String getExtDescription() {

        return "trailingAmount=" + this.trailingAmount + ",increment=" + this.increment + getOrderProperties();
    }

    @Override
    public void validate() throws OrderValidationException {

        if (this.trailingAmount.doubleValue() <= 0) {
            throw new OrderValidationException("trailingAmount must be positive for " + getDescription());
        } else if (this.increment.doubleValue() <= 0) {
            throw new OrderValidationException("increment must be positive for " + getDescription());
        }
    }

}
