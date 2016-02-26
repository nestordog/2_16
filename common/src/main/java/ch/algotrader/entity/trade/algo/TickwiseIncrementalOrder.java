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

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class TickwiseIncrementalOrder extends IncrementalOrder {

    private static final long serialVersionUID = 6631564632498034454L;

    private int startOffsetTicks;

    private int endOffsetTicks;

    /**
     * The number of ticks above/below the market the first {@link LimitOrder} will be placed.
     * <ul>
     * <li>BUY: number of ticks above bid</li>
     * <li>SELL: number of ticks below ask</li>
     * </ul>
     * @return this.startOffsetTicks int
     */
    public int getStartOffsetTicks() {
        return this.startOffsetTicks;
    }

    /**
     * The number of ticks above/below the market the first {@link LimitOrder} will be placed.
     * <ul>
     * <li>BUY: number of ticks above bid</li>
     * <li>SELL: number of ticks below ask</li>
     * </ul>
     * @param startOffsetTicksIn int
     */
    public void setStartOffsetTicks(int startOffsetTicksIn) {
        this.startOffsetTicks = startOffsetTicksIn;
    }

    /**
     * The number of ticks above/below the market the last {@link LimitOrder} will be placed.
     * <ul>
     * <li>BUY: number of ticks above ask </li>
     * <li>SELL: number of ticks below bid</li>
     * </ul>
     * @return this.endOffsetTicks int
     */
    public int getEndOffsetTicks() {
        return this.endOffsetTicks;
    }

    /**
     * The number of ticks above/below the market the last {@link LimitOrder} will be placed.
     * <ul>
     * <li>BUY: number of ticks above ask </li>
     * <li>SELL: number of ticks below bid</li>
     * </ul>
     * @param endOffsetTicksIn int
     */
    public void setEndOffsetTicks(int endOffsetTicksIn) {
        this.endOffsetTicks = endOffsetTicksIn;
    }

    @Override
    public String getExtDescription() {

        //@formatter:off
        return "startOffsetTicks=" + getStartOffsetTicks() +
            ",endOffsetTicks=" + getEndOffsetTicks() +
            " " + getOrderProperties();
        //@formatter:on
    }

    @Override
    public void validate() throws OrderValidationException {
        // nothing to validate

    }

}
