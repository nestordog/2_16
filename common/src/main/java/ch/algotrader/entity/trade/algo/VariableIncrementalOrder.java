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

import ch.algotrader.entity.trade.OrderValidationException;
import ch.algotrader.util.RoundUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class VariableIncrementalOrder extends IncrementalOrder {

    private static final long serialVersionUID = 6631564632498034454L;

    private double startOffsetPct;

    private double endOffsetPct;

    private double increment;

    /**
     * The percentage above/below the market the first {@link LimitOrder} will be placed.
     * <ul>
     * <li>BUY: % of the spread above bid</li>
     * <li>SELL: % of the spread below ask</li>
     * </ul>
     * @return this.startOffsetPct double
     */
    public double getStartOffsetPct() {
        return this.startOffsetPct;
    }

    /**
     * The percentage above/below the market the first {@link LimitOrder} will be placed.
     * <ul>
     * <li>BUY: % of the spread above bid</li>
     * <li>SELL: % of the spread below ask</li>
     * </ul>
     * @param startOffsetPctIn double
     */
    public void setStartOffsetPct(double startOffsetPctIn) {
        this.startOffsetPct = startOffsetPctIn;
    }

    /**
     * The percentage above/below the market the last {@link LimitOrder} will be placed.
     * <ul>
     * <li>BUY: % of the spread above ask</li>
     * <li>SELL: % of the spread below bid</li>
     * </ul>
     * @return this.endOffsetPct double
     */
    public double getEndOffsetPct() {
        return this.endOffsetPct;
    }

    /**
     * The percentage above/below the market the last {@link LimitOrder} will be placed.
     * <ul>
     * <li>BUY: % of the spread above ask</li>
     * <li>SELL: % of the spread below bid</li>
     * </ul>
     * @param endOffsetPctIn double
     */
    public void setEndOffsetPct(double endOffsetPctIn) {
        this.endOffsetPct = endOffsetPctIn;
    }

    /**
     * The percentage of the spread the price will be incremented by each step.
     * @return this.increment double
     */
    public double getIncrement() {
        return this.increment;
    }

    /**
     * The percentage of the spread the price will be incremented by each step.
     * @param incrementIn double
     */
    public void setIncrement(double incrementIn) {
        this.increment = incrementIn;
    }

    @Override
    public String getExtDescription() {

        //@formatter:off
            return "startOffsetPct=" + getStartOffsetPct() +
            ",endOffsetPct=" + getEndOffsetPct() +
            ",spreadPositionIncrement=" + getIncrement() +
            ",increment=" + RoundUtil.getBigDecimal(this.increment, getSecurity().getSecurityFamily().getScale(null) + 1) +
            " " + getOrderProperties();
        //@formatter:on
    }

    @Override
    public void validate() throws OrderValidationException {

        if (getIncrement() == 0.0) {
            throw new OrderValidationException("increment cannot be 0 for " + this);
        }
    }

}
