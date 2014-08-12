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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import ch.algotrader.entity.marketData.MarketDataEvent;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.enumeration.Side;
import ch.algotrader.util.MyLogger;
import ch.algotrader.util.collection.Pair;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class SlicingOrderImpl extends SlicingOrder {

    private static final long serialVersionUID = -9017761050542085585L;

    private static Logger logger = MyLogger.getLogger(SlicingOrderImpl.class.getName());

    private int currentOffsetTicks = 1;
    private List<Pair<LimitOrder, Tick>> pairs = new ArrayList<Pair<LimitOrder, Tick>>();

    @Override
    public String getExtDescription() {

        //@formatter:off
            return "vol=" + getMinVolPct() + "-" + getMaxVolPct() +
            ",qty=" + getMinQuantity() + "-" + getMaxQuantity() +
            ",duration=" + getMinDuration() + "-" + getMaxDuration() +
            ",delay=" + getMinDelay() + "-" + getMaxDelay() +
            ",currentOffsetTicks=" + this.currentOffsetTicks +
            " " + getOrderProperties();
        //@formatter:on
    }

    @Override
    public void validate() throws OrderValidationException {

        // check greater than
        if (getMinVolPct() > getMaxVolPct()) {
            throw new OrderValidationException("minVolPct cannot be greater than maxVolPct for " + this);
        } else if (getMinQuantity() > getMaxQuantity()) {
            throw new OrderValidationException("minQuantity cannot be greater than maxQuantity for " + this);
        } else if (getMinDuration() > getMaxDuration()) {
            throw new OrderValidationException("minDuration cannot be greater than maxDuration for " + this);
        } else if (getMinDelay() > getMaxDelay()) {
            throw new OrderValidationException("minDelay cannot be greater than maxDelay for " + this);
        }

        // check zero
        if (getMaxVolPct() == 0 && getMaxQuantity() == 0) {
            throw new OrderValidationException("either maxVolPct or maxQuantity have to be defined for " + this);
        } else if (getMaxDuration() == 0) {
            throw new OrderValidationException("maxDuration cannot be zero for " + this);
        } else if (getMaxDelay() == 0) {
            throw new OrderValidationException("maxDelay cannot be zero for " + this);
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

        return Collections.singletonList((Order) nextOrder(getQuantity()));
    }

    @Override
    public void increaseOffsetTicks() {
        this.currentOffsetTicks = this.currentOffsetTicks + 1;
        logger.debug("increaseOffsetTicks of " + getDescription() + " to " + this.currentOffsetTicks);
    }

    @Override
    public void decreaseOffsetTicks() {
        this.currentOffsetTicks = Math.max(this.currentOffsetTicks - 1, 0);
        logger.debug("decreaseOffsetTicks of " + getDescription() + " to " + this.currentOffsetTicks);
    }

    @Override
    public LimitOrder nextOrder(long remainingQuantity) {

        SecurityFamily family = getSecurity().getSecurityFamily();
        Tick tick = (Tick) getSecurity().getCurrentMarketDataEvent();

        // limit (at least one tick above market but do not exceed the market)
        BigDecimal limit;
        long marketVolume;
        if (Side.BUY.equals(getSide())) {

            marketVolume = tick.getVolAsk();
            limit = family.adjustPrice(tick.getAsk(), -this.currentOffsetTicks);

            if (limit.compareTo(tick.getBid()) <= 0.0) {
                limit = family.adjustPrice(tick.getBid(), 1);
                this.currentOffsetTicks = family.getSpreadTicks(tick.getBid(), tick.getAsk()) - 1;
            }

            if (limit.compareTo(tick.getAsk()) > 0.0) {
                limit = tick.getAsk();
                this.currentOffsetTicks = 0;
            }

        } else {

            marketVolume = tick.getVolBid();
            limit = family.adjustPrice(tick.getBid(), this.currentOffsetTicks);

            if (limit.compareTo(tick.getAsk()) >= 0.0) {
                limit = family.adjustPrice(tick.getAsk(), -1);
                this.currentOffsetTicks = family.getSpreadTicks(tick.getBid(), tick.getAsk()) - 1;
            }

            if (limit.compareTo(tick.getBid()) < 0.0) {
                limit = tick.getBid();
                this.currentOffsetTicks = 0;
            }
        }

        // ignore maxVolPct / maxQuantity if they are zero
        double maxVolPct = getMaxVolPct() == 0.0 ? Double.MAX_VALUE : getMaxVolPct();
        long maxQuantity = getMaxQuantity() == 0 ? Long.MAX_VALUE : getMaxQuantity();

        // evaluate the order minimum and maximum qty
        long orderMinQty = Math.max(Math.round(marketVolume * getMinVolPct()), getMinQuantity());
        long orderMaxQty = Math.min(Math.round(marketVolume * maxVolPct), maxQuantity);

        // orderMinQty cannot be greater than orderMaxQty
        if (orderMinQty > orderMaxQty) {
            orderMinQty = orderMaxQty;
        }

        // randomize the quantity between orderMinQty and orderMaxQty
        long quantity = Math.round(orderMinQty + Math.random() * (orderMaxQty - orderMinQty));

        // qty should be at least one
        quantity = Math.max(quantity, 1);

        // qty should be maximium remainingQuantity
        quantity = Math.min(quantity, remainingQuantity);

        // create the limit order
        LimitOrder order = LimitOrder.Factory.newInstance();
        order.setSecurity(this.getSecurity());
        order.setStrategy(this.getStrategy());
        order.setSide(this.getSide());
        order.setQuantity(quantity);
        order.setLimit(limit);
        order.setAccount(this.getAccount());

        // associate the childOrder with the parentOrder(this)
        order.setParentOrder(this);

        // store the current order and tick
        this.pairs.add(new Pair<LimitOrder, Tick>(order, tick));

        //@formatter:off
        logger.info(
                "next slice for " + getDescription() +
                " currentOffsetTicks: " + this.currentOffsetTicks +
                " qty: " + order.getQuantity() +
                " vol: "+ (Side.BUY.equals(order.getSide()) ? tick.getVolAsk() : tick.getVolBid()) +
                " limit: " + limit +
                " bid: " + tick.getBid() +
                " ask: " + tick.getAsk());
        //@formatter:on

        return order;
    }
}
