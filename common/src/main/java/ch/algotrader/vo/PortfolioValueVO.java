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
package ch.algotrader.vo;

import java.math.BigDecimal;
import java.util.Date;

/**
 * A ValueObject representing a {@link ch.algotrader.entity.strategy.PortfolioValue PortfolioValue}.
 */
public class PortfolioValueVO extends ch.algotrader.entity.strategy.PortfolioValueVO {

    private static final long serialVersionUID = 7104071653742687817L;

    public PortfolioValueVO(long id, Date dateTime, BigDecimal netLiqValue, BigDecimal marketValue, BigDecimal realizedPL, BigDecimal unrealizedPL, BigDecimal cashBalance, int openPositions, double leverage, BigDecimal cashFlow, long strategyId) {
        super(id, dateTime, netLiqValue, marketValue, realizedPL, unrealizedPL, cashBalance, openPositions, leverage, cashFlow, strategyId);
    }

    public PortfolioValueVO(long id, Date dateTime, BigDecimal netLiqValue, BigDecimal marketValue, BigDecimal realizedPL, BigDecimal unrealizedPL, BigDecimal cashBalance, int openPositions, double leverage, long strategyId) {
        super(id, dateTime, netLiqValue, marketValue, realizedPL, unrealizedPL, cashBalance, openPositions, leverage, strategyId);
    }

    /**
     * The Performance at the specified {@code dateTime} since the beginning of the evaluation time series.
     */
    private double performance;

    public double getPerformance() {
        return this.performance;
    }

    public void setPerformance(double performance) {
        this.performance = performance;
    }

    @Override
    public String toString() {
        return super.toString() + ",performance=" + this.performance;
    }


}
