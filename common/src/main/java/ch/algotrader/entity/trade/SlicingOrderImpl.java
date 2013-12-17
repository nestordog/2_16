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
import java.text.DecimalFormat;
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
    private static DecimalFormat twoDigitFormat = new DecimalFormat("#,##0.00");

    private static Logger logger = MyLogger.getLogger(SlicingOrderImpl.class.getName());

    private int currentOffsetTicks = 1;
    private List<Pair<LimitOrder, Tick>> pairs = new ArrayList<Pair<LimitOrder, Tick>>();

    @Override
    public String getExtDescription() {

        //@formatter:off
            return "vol=" + getMinVolPct() + "-" + getMaxVolPct() +
            ",max=" + getMaxQuantity() +
            ",duration=" + getMinDuration() + "-" + getMaxDuration() +
            ",delay=" + getMinDelay() + "-" + getMaxDelay() +
            ",currentOffsetTicks=" + this.currentOffsetTicks;
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

    @Override
    public void done() {

        long totalTime;
        long totalTimeToFill = 0;

        double orderCount = this.pairs.size();
        double fillCount = 0;
        double filledOrderCount = 0;

        double sumOrderQtyToMarketVol = 0;
        double sumFilledQtyToMarketVol = 0;
        double sumFilledQtyToOrderQty = 0;

        double sumOffsetOrderToMarket = 0;
        double sumOffsetFillToOrder = 0;

        // totalTime
        long startTime = ((Order) this.pairs.get(0).getFirst()).getDateTime().getTime();
        Order lastOrder = this.pairs.get(this.pairs.size() - 1).getFirst();
        if (lastOrder.getFills().size() > 0) {
            Fill lastFill = ((List<Fill>) lastOrder.getFills()).get(lastOrder.getFills().size() - 1);
            totalTime = lastFill.getDateTime().getTime() - startTime;
        } else {
            totalTime = lastOrder.getDateTime().getTime() - startTime;
        }

        for (Pair<LimitOrder, Tick> pair : this.pairs) {

            Tick tick = pair.getSecond();
            LimitOrder order = pair.getFirst();
            SecurityFamily securityFamily = order.getSecurity().getSecurityFamily();

            double marketVol = order.getSide().equals(Side.BUY) ? tick.getVolAsk() : tick.getVolBid();
            double orderQty = order.getQuantity();

            BigDecimal marketPrice = order.getSide().equals(Side.BUY) ? tick.getAsk() : tick.getBid();
            BigDecimal orderPrice = order.getLimit();

            sumOrderQtyToMarketVol += order.getQuantity() / marketVol;
            filledOrderCount = order.getFills().size() > 0 ? ++filledOrderCount : filledOrderCount;

            for (Fill fill : order.getFills()) {

                double filledQty = fill.getQuantity();
                BigDecimal fillPrice = fill.getPrice();

                fillCount++;

                totalTimeToFill += fill.getDateTime().getTime() - order.getDateTime().getTime();

                sumOrderQtyToMarketVol += orderQty / marketVol;
                sumFilledQtyToOrderQty += filledQty / orderQty;
                sumFilledQtyToMarketVol += filledQty / marketVol;

                sumOffsetOrderToMarket += Math.abs(securityFamily.getSpreadTicks(orderPrice, marketPrice));
                sumOffsetFillToOrder += Math.abs(securityFamily.getSpreadTicks(fillPrice, orderPrice));
            }
        }

        //@formatter:off
        logger.info(getDescription() +
            " totalTime: " + totalTime + " msec" +
            " avgTimeToFill: " + (int)(totalTimeToFill / fillCount) + " msec" +

            " orderCount: " + orderCount +
            " filledOrderCount: " + filledOrderCount + " " + twoDigitFormat.format(filledOrderCount / orderCount * 100.0) + "%" +

            " avgOrderQtyToMarketVol: " + twoDigitFormat.format(sumOrderQtyToMarketVol / fillCount * 100.0) + "%" +
            " avgFilledQtyToOrderQty: " + twoDigitFormat.format(sumFilledQtyToOrderQty / fillCount * 100.0) + "%" +
            " avgFilledQtyToMarketVol: " + twoDigitFormat.format(sumFilledQtyToMarketVol / fillCount * 100.0) + "%" +

            " avgOffsetOrderToMarket: " + twoDigitFormat.format(sumOffsetOrderToMarket / fillCount) +
            " avgOffsetFillToOrder: " + twoDigitFormat.format(sumOffsetFillToOrder / fillCount));
        //@formatter:on
    }
}
