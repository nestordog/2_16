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
import java.util.Collections;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.enumeration.Side;
import ch.algotrader.util.RoundUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class VariableIncrementalOrder extends IncrementalOrder {

    private static final long serialVersionUID = 6631564632498034454L;

    private BigDecimal startLimit;

    private BigDecimal endLimit;

    private BigDecimal currentLimit;

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
            ",startLimit=" + this.startLimit +
            ",endLimit=" + this.endLimit +
            ",currentLimit=" + this.currentLimit +
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

    @Override
    public List<SimpleOrder> getInitialOrders(Tick tick) {

        SecurityFamily family = getSecurity().getSecurityFamily();

        double bidDouble = tick.getBid().doubleValue();
        double askDouble = tick.getAsk().doubleValue();
        double spread = askDouble - bidDouble;
        this.increment = getIncrement() * spread;

        double limit;
        double maxLimit;
        if (Side.BUY.equals(getSide())) {
            double limitRaw = bidDouble + getStartOffsetPct() * spread;
            double maxLimitRaw = bidDouble + getEndOffsetPct() * spread;
            limit = RoundUtil.roundToNextN(limitRaw, family.getTickSize(null, limitRaw, true), BigDecimal.ROUND_FLOOR);
            maxLimit = RoundUtil.roundToNextN(maxLimitRaw, family.getTickSize(null, maxLimitRaw, true), BigDecimal.ROUND_CEILING);
        } else {
            double limitRaw = askDouble - getStartOffsetPct() * spread;
            double maxLimitRaw = askDouble - getEndOffsetPct() * spread;
            limit = RoundUtil.roundToNextN(limitRaw, family.getTickSize(null, limitRaw, true), BigDecimal.ROUND_CEILING);
            maxLimit = RoundUtil.roundToNextN(maxLimitRaw, family.getTickSize(null, maxLimitRaw, true), BigDecimal.ROUND_FLOOR);
        }

        // limit and maxLimit are correctly rounded according to tickSizePattern
        this.startLimit = RoundUtil.getBigDecimal(limit, family.getScale(null));
        this.endLimit = RoundUtil.getBigDecimal(maxLimit, family.getScale(null));
        this.currentLimit = this.startLimit;

        LimitOrder limitOrder = LimitOrder.Factory.newInstance();
        limitOrder.setSecurity(this.getSecurity());
        limitOrder.setStrategy(this.getStrategy());
        limitOrder.setSide(this.getSide());
        limitOrder.setQuantity(this.getQuantity());
        limitOrder.setLimit(this.startLimit);
        limitOrder.setAccount(this.getAccount());

        return Collections.singletonList((SimpleOrder) limitOrder);
    }

    @Override
    public SimpleOrder modifyOrder(Tick tick) {

        SecurityFamily family = getSecurity().getSecurityFamily();

        BigDecimal newLimit;
        if (getSide().equals(Side.BUY)) {

            double tickSize = family.getTickSize(null, this.currentLimit.doubleValue(), true);
            double increment = RoundUtil.roundToNextN(this.increment, tickSize, BigDecimal.ROUND_CEILING);
            BigDecimal roundedIncrement = RoundUtil.getBigDecimal(increment, family.getScale(null));
            newLimit = this.currentLimit.add(roundedIncrement);
        } else {

            double tickSize = family.getTickSize(null, this.currentLimit.doubleValue(), false);
            double increment = RoundUtil.roundToNextN(this.increment, tickSize, BigDecimal.ROUND_CEILING);
            BigDecimal roundedIncrement = RoundUtil.getBigDecimal(increment, family.getScale(null));
            newLimit = this.currentLimit.subtract(roundedIncrement);
        }

        try {
            LimitOrder newOrder = (LimitOrder) BeanUtils.cloneBean(this);
            newOrder.setLimit(newLimit);
            return newOrder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean checkLimit() {

        SecurityFamily family = getSecurity().getSecurityFamily();

        if (getSide().equals(Side.BUY)) {

            double tickSize = family.getTickSize(null, this.currentLimit.doubleValue(), true);
            double increment = RoundUtil.roundToNextN(this.increment, tickSize, BigDecimal.ROUND_CEILING);
            BigDecimal roundedIncrement = RoundUtil.getBigDecimal(increment, family.getScale(null));

            return this.currentLimit.add(roundedIncrement).compareTo(this.endLimit) <= 0;
        } else {
            double tickSize = family.getTickSize(null, this.currentLimit.doubleValue(), false);
            double increment = RoundUtil.roundToNextN(this.increment, tickSize, BigDecimal.ROUND_CEILING);
            BigDecimal roundedIncrement = RoundUtil.getBigDecimal(increment, family.getScale(null));

            return this.currentLimit.subtract(roundedIncrement).compareTo(this.endLimit) >= 0;
        }
    }
}
