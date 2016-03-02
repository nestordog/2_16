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
package ch.algotrader.service.algo;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.entity.marketData.TickI;
import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.OrderStatusVO;
import ch.algotrader.entity.trade.algo.AlgoOrder;
import ch.algotrader.entity.trade.algo.SlicingOrder;
import ch.algotrader.entity.trade.algo.SlicingOrderStateVO;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.service.MarketDataCacheService;
import ch.algotrader.service.OrderExecutionService;
import ch.algotrader.service.SimpleOrderService;
import ch.algotrader.util.collection.Pair;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class SlicingOrderService extends AbstractAlgoOrderExecService<SlicingOrder, SlicingOrderStateVO> {

    private static final Logger LOGGER = LogManager.getLogger(SlicingOrderService.class);
    private static final DecimalFormat twoDigitFormat = new DecimalFormat("#,##0.00");

    private final OrderExecutionService orderExecutionService;
    private final MarketDataCacheService marketDataCacheService;
    private final SimpleOrderService simpleOrderService;

    public SlicingOrderService(
            final OrderExecutionService orderExecutionService,
            final MarketDataCacheService marketDataCacheService,
            final SimpleOrderService simpleOrderService) {

        super(orderExecutionService, simpleOrderService);

        Validate.notNull(marketDataCacheService, "MarketDataCacheService is null");

        this.orderExecutionService = orderExecutionService;
        this.marketDataCacheService = marketDataCacheService;
        this.simpleOrderService = simpleOrderService;
    }

    @Override
    public Class<? extends AlgoOrder> getAlgoOrderType() {
        return SlicingOrder.class;
    }

    @Override
    protected SlicingOrderStateVO createAlgoOrderState(final SlicingOrder algoOrder) {
        return new SlicingOrderStateVO();
    }

    @Override
    public void handleSendOrder(final SlicingOrder algoOrder, final SlicingOrderStateVO slicingOrderState) {

        sendNextOrder(algoOrder, slicingOrderState);
    }

    @Override
    protected void handleModifyOrder(final SlicingOrder algoOrder, final SlicingOrderStateVO algoOrderState) {
        throw new UnsupportedOperationException();
    }

    public void increaseOffsetTicks(SlicingOrder slicingOrder) {

        Validate.notNull(slicingOrder, "slicingOrder missing");

        SlicingOrderStateVO orderState = getAlgoOrderState(slicingOrder);
        if (orderState != null) {
            orderState.setCurrentOffsetTicks(orderState.getCurrentOffsetTicks() + 1);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("increaseOffsetTicks of {} to {}", slicingOrder.getDescription(), orderState.getCurrentOffsetTicks());
            }
        }
    }

    public void decreaseOffsetTicks(SlicingOrder slicingOrder) {

        Validate.notNull(slicingOrder, "slicingOrder missing");

        SlicingOrderStateVO orderState = getAlgoOrderState(slicingOrder);
        if (orderState != null) {
            orderState.setCurrentOffsetTicks(Math.max(orderState.getCurrentOffsetTicks() - 1, 0));
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("decreaseOffsetTicks of {} to {}", slicingOrder.getDescription(), orderState.getCurrentOffsetTicks());
            }
        }
    }

    public void sendNextOrder(SlicingOrder slicingOrder) {

        sendNextOrder(slicingOrder, getAlgoOrderState(slicingOrder));
    }

    private void sendNextOrder(SlicingOrder slicingOrder, SlicingOrderStateVO slicingOrderState) {

        Validate.notNull(slicingOrder, "slicingOrder missing");
        if (slicingOrderState == null) {
            return; // already done
        }

        Security security = slicingOrder.getSecurity();
        SecurityFamily family = security.getSecurityFamily();

        long remainingQuantity;
        OrderStatusVO orderStatus = this.orderExecutionService.getStatusByIntId(slicingOrder.getIntId());
        if (orderStatus != null) {
            remainingQuantity = orderStatus.getRemainingQuantity();
        } else {
            remainingQuantity = slicingOrder.getQuantity();
        }

        TickVO tick = (TickVO) this.marketDataCacheService.getCurrentMarketDataEvent(security.getId());
        if (tick == null) {
            throw new IllegalStateException("no market data subscription for " + security);
        }

        // limit (at least one tick above market but do not exceed the market)
        BigDecimal limit;
        long marketVolume;
        if (Side.BUY.equals(slicingOrder.getSide())) {

            marketVolume = tick.getVolAsk();
            limit = family.adjustPrice(null, tick.getAsk(), -slicingOrderState.getCurrentOffsetTicks());

            if (limit.compareTo(tick.getBid()) <= 0.0) {
                limit = family.adjustPrice(null, tick.getBid(), 1);
                slicingOrderState.setCurrentOffsetTicks(family.getSpreadTicks(null, tick.getBid(), tick.getAsk()) - 1);
            }

            if (limit.compareTo(tick.getAsk()) > 0.0) {
                limit = tick.getAsk();
                slicingOrderState.setCurrentOffsetTicks(0);
            }

        } else {

            marketVolume = tick.getVolBid();
            limit = family.adjustPrice(null, tick.getBid(), slicingOrderState.getCurrentOffsetTicks());

            if (limit.compareTo(tick.getAsk()) >= 0.0) {
                limit = family.adjustPrice(null, tick.getAsk(), -1);
                slicingOrderState.setCurrentOffsetTicks(family.getSpreadTicks(null, tick.getBid(), tick.getAsk()) - 1);
            }

            if (limit.compareTo(tick.getBid()) < 0.0) {
                limit = tick.getBid();
                slicingOrderState.setCurrentOffsetTicks(0);
            }
        }

        // ignore maxVolPct / maxQuantity if they are zero
        double maxVolPct = slicingOrder.getMaxVolPct() == 0.0 ? Double.MAX_VALUE : slicingOrder.getMaxVolPct();
        long maxQuantity = slicingOrder.getMaxQuantity() == 0 ? Long.MAX_VALUE : slicingOrder.getMaxQuantity();

        // evaluate the order minimum and maximum qty
        long orderMinQty = Math.max(Math.round(marketVolume * slicingOrder.getMinVolPct()), slicingOrder.getMinQuantity());
        long orderMaxQty = Math.min(Math.round(marketVolume * maxVolPct), maxQuantity);

        // orderMinQty cannot be greater than orderMaxQty
        if (orderMinQty > orderMaxQty) {
            orderMinQty = orderMaxQty;
        }

        // randomize the quantity between orderMinQty and orderMaxQty
        long quantity = Math.round(orderMinQty + Math.random() * (orderMaxQty - orderMinQty));

        // make sure that the remainingQty after the next slice is greater than minQuantity
        long remainingQuantityAfterSlice = remainingQuantity - quantity;
        if (slicingOrder.getMinQuantity() > 0 && slicingOrder.getMaxQuantity() > 0 && remainingQuantityAfterSlice > 0 && remainingQuantityAfterSlice < slicingOrder.getMinQuantity()) {

            // if quantity is below half between minQuantity and maxQuantity
            if (quantity < (slicingOrder.getMinQuantity() + slicingOrder.getMaxQuantity()) / 2.0) {

                // take full remaining quantity but not more than orderMaxQty
                quantity = Math.min(remainingQuantity, orderMaxQty);
            } else {

                // make sure remaining after slice quantity is greater than minQuantity
                quantity = remainingQuantity - slicingOrder.getMinQuantity();
            }
        }

        // qty should be at least one
        quantity = Math.max(quantity, 1);

        // qty should be maximum remainingQuantity
        quantity = Math.min(quantity, remainingQuantity);

        // create the limit order
        LimitOrder order = LimitOrder.Factory.newInstance();
        order.setSecurity(security);
        order.setStrategy(slicingOrder.getStrategy());
        order.setSide(slicingOrder.getSide());
        order.setQuantity(quantity);
        order.setLimit(limit);
        order.setAccount(slicingOrder.getAccount());

        // associate the childOrder with the parentOrder(this)
        order.setParentOrder(slicingOrder);

        // store the current order and tick
        slicingOrderState.addPair(new Pair<>(order, tick));

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("next slice for {},currentOffsetTicks={},qty={},vol={},limit={},bid={},ask={}", slicingOrder.getDescription(), slicingOrderState.getCurrentOffsetTicks(), order.getQuantity(),
                    (Side.BUY.equals(order.getSide()) ? tick.getVolAsk() : tick.getVolBid()), limit, tick.getBid(), tick.getAsk());
        }

        this.simpleOrderService.sendOrder(order);
    }

    @Override
    public void handleChildFill(SlicingOrder algoOrder, SlicingOrderStateVO orderState, Fill fill) {
        orderState.storeFill(fill);
    }

    @Override
    public void handleOrderStatus(SlicingOrder algoOrder, SlicingOrderStateVO algoOrderState, OrderStatus orderStatus) {

        if (!EnumSet.of(Status.EXECUTED, Status.CANCELED).contains(orderStatus.getStatus())) {
            return;
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(algoOrder.getDescription() + getResults(algoOrder, algoOrderState));
        }
    }

    Map<String, Object> getResults(SlicingOrder slicingOrder, SlicingOrderStateVO algoOrderState) {
        List<Pair<LimitOrder, TickI>> pairs = algoOrderState.getPairs();
        List<Fill> fills = algoOrderState.getFills();
        String broker = slicingOrder.getAccount().getBroker();
        SecurityFamily securityFamily = slicingOrder.getSecurity().getSecurityFamily();

        long startTime = (pairs.get(0).getFirst()).getDateTime().getTime();
        Fill lastFill = fills.get(fills.size() - 1);
        long totalTime = lastFill.getDateTime().getTime() - startTime;

        long totalTimeToFill = 0;
        int orderCount = pairs.size();
        int fillCount = 0;
        int filledOrderCount = 0;
        double sumOrderQtyToMarketVol = 0;
        double sumFilledQtyToMarketVol = 0;
        double sumFilledQtyToOrderQty = 0;
        double sumOffsetOrderToMarket = 0;
        double sumOffsetFillToOrder = 0;

        for (Pair<LimitOrder, TickI> pair : pairs) {

            TickI tick = pair.getSecond();
            LimitOrder order = pair.getFirst();

            double marketVol = order.getSide().equals(Side.BUY) ? tick.getVolAsk() : tick.getVolBid();
            double orderQty = order.getQuantity();

            BigDecimal marketPrice = order.getSide().equals(Side.BUY) ? tick.getAsk() : tick.getBid();
            BigDecimal orderPrice = order.getLimit();

            sumOrderQtyToMarketVol += order.getQuantity() / marketVol;
            filledOrderCount = fills.size() > 0 ? ++filledOrderCount : filledOrderCount;

            List<Fill> fillsByIntOrderId = algoOrderState.getFillsByIntOrderId(order.getIntId());
            if (fillsByIntOrderId != null) {
                for (Fill fill : fillsByIntOrderId) {

                    double filledQty = fill.getQuantity();
                    BigDecimal fillPrice = fill.getPrice();

                    fillCount++;

                    totalTimeToFill += fill.getDateTime().getTime() - order.getDateTime().getTime();

                    sumOrderQtyToMarketVol += orderQty / marketVol;
                    sumFilledQtyToOrderQty += filledQty / orderQty;
                    sumFilledQtyToMarketVol += filledQty / marketVol;

                    sumOffsetOrderToMarket += Math.abs(securityFamily.getSpreadTicks(broker, orderPrice, marketPrice));
                    sumOffsetFillToOrder += Math.abs(securityFamily.getSpreadTicks(broker, fillPrice, orderPrice));
                }
            }

        }

        Map<String, Object> results = new HashMap<>();
        results.put("totalTime(msec)", totalTime);
        results.put("avgTimeToFill(msec)", (int) (totalTimeToFill / fillCount));
        results.put("orderCount", orderCount);
        results.put("filledOrderCount", filledOrderCount);
        results.put("filledOrder%", twoDigitFormat.format(filledOrderCount / orderCount * 100.0));
        results.put("avgOrderQtyToMarketVol%", twoDigitFormat.format(sumOrderQtyToMarketVol / fillCount * 100.0));
        results.put("avgFilledQtyToOrderQty%", twoDigitFormat.format(sumFilledQtyToOrderQty / fillCount * 100.0));
        results.put("avgFilledQtyToMarketVol%", twoDigitFormat.format(sumFilledQtyToMarketVol / fillCount * 100.0));
        results.put("avgOffsetOrderToMarket", twoDigitFormat.format(sumOffsetOrderToMarket / fillCount));
        results.put("avgOffsetFillToOrder", twoDigitFormat.format(sumOffsetFillToOrder / fillCount));
        return results;
    }

}
