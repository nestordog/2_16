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
package ch.algotrader.entity.trade;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

import ch.algotrader.util.RoundUtil;

import com.algoTrader.entity.marketData.MarketDataEvent;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.security.SecurityFamily;
import com.algoTrader.entity.trade.LimitOrder;
import com.algoTrader.entity.trade.Order;
import com.algoTrader.entity.trade.OrderValidationException;
import com.algoTrader.entity.trade.VariableIncrementalOrder;
import com.algoTrader.enumeration.Side;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class VariableIncrementalOrderImpl extends VariableIncrementalOrder {

    private static final long serialVersionUID = 6631564632498034454L;

    private BigDecimal startLimit;
    private BigDecimal endLimit;
    private BigDecimal currentLimit;
    private double increment;

    @Override
    public String getExtDescription() {

        //@formatter:off
            return "startOffsetPct: " + getStartOffsetPct() +
            " endOffsetPct: " + getEndOffsetPct() +
            " spreadPositionIncrement: " + getIncrement() +
            " startLimit: " + this.startLimit +
            " endLimit: " + this.endLimit +
            " currentLimit: " + this.currentLimit +
            " increment: " + RoundUtil.getBigDecimal(this.increment, getSecurity().getSecurityFamily().getScale() + 1);
        //@formatter:on
    }

    @Override
    public void validate() throws OrderValidationException {

        if (getIncrement() == 0.0) {
            throw new OrderValidationException("increment cannot be 0 for " + this);
        }

        MarketDataEvent marketDataEvent = getSecurity().getCurrentMarketDataEvent();
        if (marketDataEvent == null) {
            throw new OrderValidationException("no marketDataEvent available to initialize SlicingOrder");
        } else if (!(marketDataEvent instanceof Tick)) {
            throw new OrderValidationException("only ticks are supported, " + marketDataEvent.getClass() + " are not supported");
        }
    }

    @Override
    public List<Order> getInitialOrders() {

        Tick tick = (Tick) getSecurity().getCurrentMarketDataEvent();

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
            limit = RoundUtil.roundToNextN(limitRaw, family.getTickSize(limitRaw, true), BigDecimal.ROUND_FLOOR);
            maxLimit = RoundUtil.roundToNextN(maxLimitRaw, family.getTickSize(maxLimitRaw, true), BigDecimal.ROUND_CEILING);
        } else {
            double limitRaw = askDouble - getStartOffsetPct() * spread;
            double maxLimitRaw = askDouble - getEndOffsetPct() * spread;
            limit = RoundUtil.roundToNextN(limitRaw, family.getTickSize(limitRaw, true), BigDecimal.ROUND_CEILING);
            maxLimit = RoundUtil.roundToNextN(maxLimitRaw, family.getTickSize(maxLimitRaw, true), BigDecimal.ROUND_FLOOR);
        }

        // limit and maxLimit are correctly rounded according to tickSizePattern
        this.startLimit = RoundUtil.getBigDecimal(limit, family.getScale());
        this.endLimit = RoundUtil.getBigDecimal(maxLimit, family.getScale());
        this.currentLimit = this.startLimit;

        LimitOrder limitOrder = LimitOrder.Factory.newInstance();
        limitOrder.setSecurity(this.getSecurity());
        limitOrder.setStrategy(this.getStrategy());
        limitOrder.setSide(this.getSide());
        limitOrder.setQuantity(this.getQuantity());
        limitOrder.setLimit(this.startLimit);
        limitOrder.setAccount(this.getAccount());

        return Collections.singletonList((Order) limitOrder);
    }

    @Override
    public LimitOrder modifyOrder() {

        SecurityFamily family = getSecurity().getSecurityFamily();

        BigDecimal newLimit;
        if (getSide().equals(Side.BUY)) {

            double tickSize = family.getTickSize(this.currentLimit.doubleValue(), true);
            double increment = RoundUtil.roundToNextN(this.increment, tickSize, BigDecimal.ROUND_CEILING);
            BigDecimal roundedIncrement = RoundUtil.getBigDecimal(increment, family.getScale());
            newLimit = this.currentLimit.add(roundedIncrement);
        } else {

            double tickSize = family.getTickSize(this.currentLimit.doubleValue(), false);
            double increment = RoundUtil.roundToNextN(this.increment, tickSize, BigDecimal.ROUND_CEILING);
            BigDecimal roundedIncrement = RoundUtil.getBigDecimal(increment, family.getScale());
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

            double tickSize = family.getTickSize(this.currentLimit.doubleValue(), true);
            double increment = RoundUtil.roundToNextN(this.increment, tickSize, BigDecimal.ROUND_CEILING);
            BigDecimal roundedIncrement = RoundUtil.getBigDecimal(increment, family.getScale());

            return this.currentLimit.add(roundedIncrement).compareTo(this.endLimit) <= 0;
        } else {
            double tickSize = family.getTickSize(this.currentLimit.doubleValue(), false);
            double increment = RoundUtil.roundToNextN(this.increment, tickSize, BigDecimal.ROUND_CEILING);
            BigDecimal roundedIncrement = RoundUtil.getBigDecimal(increment, family.getScale());

            return this.currentLimit.subtract(roundedIncrement).compareTo(this.endLimit) >= 0;
        }
    }
}
