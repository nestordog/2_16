package com.algoTrader.entity.trade;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.security.SecurityFamily;
import com.algoTrader.enumeration.Side;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.Pair;

public class SlicingOrderImpl extends SlicingOrder {

    private static final long serialVersionUID = -9017761050542085585L;

    private static final SimpleDateFormat format = new SimpleDateFormat("yyy-MM-dd kk:mm:ss,SSS");
    private static Logger logger = MyLogger.getLogger(SlicingOrderImpl.class.getName());

    private int currentOffsetTicks = 1;
    private List<Pair<LimitOrder, Tick>> pairs = new ArrayList<Pair<LimitOrder, Tick>>();

    @Override
    public String getDescription() {

        //@formatter:off
            return "vol: " + getMinVolPct() + " - " + getMaxVolPct() + " max " + getMaxQuantity() +
            " duration: " + getMinDuration() + " - " + getMaxDuration() +
            " delay: " + getMinDelay() + " - " + getMaxDelay() +
            " currentOffsetTicks: " + this.currentOffsetTicks;
        //@formatter:on
    }

    @Override
    public void increaseOffsetTicks() {
        this.currentOffsetTicks = this.currentOffsetTicks + 1;
        logger.debug("increaseOffsetTicks of " + toString() + " to " + this.currentOffsetTicks);
    }

    @Override
    public void decreaseOffsetTicks() {
        this.currentOffsetTicks = Math.max(this.currentOffsetTicks - 1, 0);
        logger.debug("decreaseOffsetTicks " + toString() + " to " + this.currentOffsetTicks);
    }

    @Override
    public LimitOrder nextOrder(long remainingQuantity) {

        SecurityFamily family = getSecurity().getSecurityFamily();

        Tick tick = getSecurity().getLastTick();

        if (tick == null) {
            throw new IllegalStateException("no last tick available to initialize SlicingOrder");
        }

        // vol between minVol and maxVol of the market
        double vol = getMinVolPct() + Math.random() * (getMaxVolPct() - getMinVolPct());

        // limit (at least one tick above market but do not exceed the market)
        BigDecimal limit;
        long quantity;
        if (Side.BUY.equals(getSide())) {

            quantity = Math.round(vol * tick.getVolAsk());
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

            quantity = Math.round(vol * tick.getVolBid());
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

        // at least one but
        quantity = Math.max(quantity, 1);

        // maximium remainingQuantity
        quantity = Math.min(quantity, remainingQuantity);

        // maximium maxQuantitity
        quantity = Math.min(quantity, getMaxQuantity());

        // create the limit order
        LimitOrder order = LimitOrder.Factory.newInstance();
        order.setSecurity(this.getSecurity());
        order.setStrategy(this.getStrategy());
        order.setSide(this.getSide());
        order.setQuantity(quantity);
        order.setLimit(limit);

        // associate the childOrder with the parentOrder(this)
        order.setParentOrder(this);

        // store the current order and tick
        this.pairs.add(new Pair<LimitOrder, Tick>(order, tick));

        //@formatter:off
        MyLogger.getLogger(SlicingOrderImpl.class.getName()).info(
                "next slice for " + toString() +
                " qty: " + order.getQuantity() +
                " vol: "+ (Side.BUY.equals(order.getSide()) ? tick.getVolAsk() : tick.getVolBid()) +
                " limit: " + limit +
                " bid: " + tick.getBid() +
                " ask: " + tick.getAsk());
        //@formatter:off

        return order;
    }

    @Override
    public void done() {

        int orderCount = this.pairs.size();
        int filledOrderCount = 0;

        // totalTime
        long startTime = this.pairs.get(0).getFirst().getDateTime().getTime();
        Order lastOrder = this.pairs.get(this.pairs.size() - 1).getFirst();
        long totalTime;
        if (lastOrder.getFills().size() > 0) {
            Fill lastFill = ((List<Fill>) lastOrder.getFills()).get(lastOrder.getFills().size() - 1);
            totalTime = lastFill.getDateTime().getTime() - startTime;
        } else {
            totalTime = lastOrder.getDateTime().getTime() - startTime;
        }

        for (Pair<LimitOrder, Tick> pair : this.pairs) {

            Tick tick = pair.getSecond();
            LimitOrder order = pair.getFirst();

            if (order.getFills().size() > 0) {
                filledOrderCount++;
            }

            //@formatter:off
            logger.debug("order: " + format.format(order.getDateTime()) +
                " qty: " + order.getQuantity() +
                " vol: "+ (Side.BUY.equals(order.getSide()) ? tick.getVolAsk() : tick.getVolBid()) +
                " limit: " + order.getLimit() +
                " bid: " + tick.getBid() +
                " ask: " + tick.getAsk());

            for (Fill fill : order.getFills()) {
                logger.debug("fill: " + format.format(fill.getDateTime()) +
                " qty: " + fill.getQuantity() +
                " price: " + fill.getPrice());
            }
            //@formatter:on
        }

        logger.info("orderCount: " + orderCount);
        logger.info("filledOrderCount: " + filledOrderCount + " (" + (filledOrderCount * 100.0 / orderCount) + "%)");
        logger.info("totalTime: " + (totalTime / 1000.0) + " sec");
    }
}
