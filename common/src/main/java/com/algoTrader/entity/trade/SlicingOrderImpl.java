package com.algoTrader.entity.trade;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.algoTrader.entity.marketData.MarketDataEvent;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.security.SecurityFamily;
import com.algoTrader.enumeration.Side;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.collection.Pair;

public class SlicingOrderImpl extends SlicingOrder {

    private static final long serialVersionUID = -9017761050542085585L;
    private static DecimalFormat twoDigitFormat = new DecimalFormat("#,##0.00");

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
    public void validate() throws OrderValidationException {

        if (getMaxVolPct() == 0.0) {
            throw new OrderValidationException("maxVolPct cannot be 0 for " + this);
        } else if (getMaxQuantity() == 0) {
            throw new OrderValidationException("maxQuantity cannot be 0 for " + this);
        } else if (getMaxDuration() == 0.0) {
            throw new OrderValidationException("maxDuration cannot be 0 for " + this);
        } else if (getMinDelay() == 0.0) {
            throw new OrderValidationException("minDelay cannot be 0 for " + this);
        } else if (getMaxDelay() == 0.0) {
            throw new OrderValidationException("maxDelay cannot be 0 for " + this);
        }
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

        MarketDataEvent marketDataEvent = getSecurity().getCurrentMarketDataEvent();

        if (marketDataEvent == null) {
            throw new IllegalStateException("no marketDataEvent available to initialize SlicingOrder");
        } else if (!(marketDataEvent instanceof Tick)) {
            throw new IllegalStateException("only ticks are supported, " + marketDataEvent.getClass() + " are not supported");
        }

        Tick tick = (Tick) marketDataEvent;

        // limit (at least one tick above market but do not exceed the market)
        BigDecimal limit;
        long marketQuantity;
        if (Side.BUY.equals(getSide())) {

            marketQuantity = tick.getVolAsk();
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

            marketQuantity = tick.getVolBid();
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

        // reduce the marketQuantity to a maximum of maxQuantity
        long reducedMarketQuantity = Math.min(marketQuantity, getMaxQuantity());

        // volPct between minVolPct and maxVolPct of the market
        double volPct = getMinVolPct() + Math.random() * (getMaxVolPct() - getMinVolPct());

        // multiply the reducedMarketQuantity by volPct
        long quantity = Math.round(volPct * reducedMarketQuantity);

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
        order.setMarketChannel(this.getMarketChannel());

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
        logger.info(
            "totalTime: " + totalTime + " msec" +
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
