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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.OrderStatusVO;
import ch.algotrader.entity.trade.algo.AlgoOrder;
import ch.algotrader.entity.trade.algo.VWAPOrder;
import ch.algotrader.entity.trade.algo.VWAPOrderStateVO;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.MarketDataEventType;
import ch.algotrader.enumeration.Status;
import ch.algotrader.enumeration.TimePeriod;
import ch.algotrader.service.CalendarService;
import ch.algotrader.service.HistoricalDataService;
import ch.algotrader.service.OrderExecutionService;
import ch.algotrader.service.SimpleOrderService;
import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.util.RoundUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class VWAPOrderService extends AbstractAlgoOrderExecService<VWAPOrder, VWAPOrderStateVO> {

    private static final Logger LOGGER = LogManager.getLogger(VWAPOrderService.class);

    private final OrderExecutionService orderExecutionService;
    private final HistoricalDataService historicalDataService;
    private final CalendarService calendarService;
    private final SimpleOrderService simpleOrderService;

    public VWAPOrderService(final OrderExecutionService orderExecutionService, final HistoricalDataService historicalDataService, final CalendarService calendarService, final SimpleOrderService simpleOrderService) {

        super(orderExecutionService);

        Validate.notNull(orderExecutionService, "OrderExecutionService is null");
        Validate.notNull(historicalDataService, "HistoricalDataService is null");
        Validate.notNull(calendarService, "CalendarService is null");
        Validate.notNull(simpleOrderService, "SimpleOrderService is null");

        this.historicalDataService = historicalDataService;
        this.calendarService = calendarService;
        this.simpleOrderService = simpleOrderService;
        this.orderExecutionService = orderExecutionService;
    }

    @Override
    public Class<? extends AlgoOrder> getAlgoOrderType() {
        return VWAPOrder.class;
    }

    @Override
    protected VWAPOrderStateVO createAlgoOrderState(final VWAPOrder vwapOrder) {

        Validate.notNull(vwapOrder, "vwapOrder missing");

        Security security = vwapOrder.getSecurity();
        SecurityFamily family = security.getSecurityFamily();
        Exchange exchange = family.getExchange();

        List<Bar> bars = this.historicalDataService.getHistoricalBars(
                security.getId(), //
                DateUtils.truncate(new Date(), Calendar.DATE), //
                vwapOrder.getLookbackDays(), //
                TimePeriod.DAY, //
                vwapOrder.getBucketSize(), //
                MarketDataEventType.TRADES, //
                Collections.emptyMap());

        TreeMap<LocalTime, Long> buckets = new TreeMap<>();
        Set<LocalDate> tradingDays = new HashSet<>();
        for (Bar bar : bars) {
            int vol = bar.getVol();
            LocalTime time = DateTimeLegacy.toLocalTime(bar.getDateTime());
            tradingDays.add(DateTimeLegacy.toLocalDate(bar.getDateTime()));
            if (buckets.containsKey(time)) {
                buckets.put(time, buckets.get(time) + vol);
            } else {
                buckets.put(time, (long) vol);
            }
        }

        // verify start and end time
        if (vwapOrder.getStartTime() == null) {
            if (this.calendarService.isOpen(exchange.getId())) {
                vwapOrder.setStartTime(LocalTime.now());
            } else {
                Date nextOpenTime = this.calendarService.getNextOpenTime(exchange.getId());
                vwapOrder.setStartTime(DateTimeLegacy.toLocalTime(nextOpenTime));
            }
        }

        if (vwapOrder.getEndTime() == null) {
            Date closeTime = this.calendarService.getNextCloseTime(exchange.getId());
            vwapOrder.setEndTime(DateTimeLegacy.toLocalTime(closeTime));
        }

        int historicalVolume = 0;
        LocalTime firstBucketStart = buckets.floorKey(vwapOrder.getStartTime());
        LocalTime lastBucketStart = buckets.floorKey(vwapOrder.getEndTime());

        SortedMap<LocalTime, Long> subBuckets = buckets.subMap(firstBucketStart, true, lastBucketStart, true);
        for (Map.Entry<LocalTime, Long> bucket : subBuckets.entrySet()) {

            long vol = bucket.getValue() / tradingDays.size();
            bucket.setValue(vol);

            if (bucket.getKey().equals(firstBucketStart)) {
                LocalTime firstBucketEnd = firstBucketStart.plus(vwapOrder.getBucketSize().getValue(), ChronoUnit.MILLIS);
                double fraction = (double) ChronoUnit.MILLIS.between(vwapOrder.getStartTime(), firstBucketEnd) / vwapOrder.getBucketSize().getValue();
                historicalVolume += vol * fraction;
            } else if (bucket.getKey().equals(lastBucketStart)) {
                double fraction = (double) ChronoUnit.MILLIS.between(lastBucketStart, vwapOrder.getEndTime()) / vwapOrder.getBucketSize().getValue();
                historicalVolume += vol * fraction;
            } else {
                historicalVolume += vol;
            }
        }

        double participation = vwapOrder.getQuantity() / (double) historicalVolume;
        return new VWAPOrderStateVO(participation, buckets);
    }

    @Override
    public void handleValidateOrder(final VWAPOrder algoOrder, final VWAPOrderStateVO algoOrderState) {
    }

    @Override
    public void handleSendOrder(final VWAPOrder algoOrder, final VWAPOrderStateVO algoOrderState) {
    }

    @Override
    protected void handleModifyOrder(final VWAPOrder order, final VWAPOrderStateVO algoOrderState) {
    }

    @Override
    protected void handleCancelOrder(final VWAPOrder order, final VWAPOrderStateVO algoOrderState) {
    }

    public void sendNextOrder(VWAPOrder algoOrder, Date dateTime) {

        VWAPOrderStateVO orderState = getAlgoOrderState(algoOrder);
        OrderStatusVO orderStatus = this.orderExecutionService.getStatusByIntId(algoOrder.getIntId());

        if (orderState == null || orderStatus == null) {
            return; // VWAP calculation not finished yet
        }

        long bucketVolume = orderState.getBucketVolume(DateTimeLegacy.toLocalTime(dateTime));
        long targetVolume = Math.round(orderState.getParticipation() * bucketVolume);
        int avgIntervalLenth = (int) ((algoOrder.getMaxInterval() + algoOrder.getMinInterval()) / 2.0 * 1000.0);
        int intervalsPerBucket = (int) (algoOrder.getBucketSize().getValue() / avgIntervalLenth);
        double randomFactor = (1 - algoOrder.getQtyRandomFactor() + 2 * algoOrder.getQtyRandomFactor() * Math.random());
        long quantity = Math.round(randomFactor * targetVolume / intervalsPerBucket);
        quantity = Math.max(1, quantity);
        quantity = Math.min(orderStatus.getRemainingQuantity(), quantity);

        // create the limit order
        MarketOrder order = MarketOrder.Factory.newInstance();
        order.setSecurity(algoOrder.getSecurity());
        order.setStrategy(algoOrder.getStrategy());
        order.setSide(algoOrder.getSide());
        order.setQuantity(quantity);
        order.setAccount(algoOrder.getAccount());

        // associate the childOrder with the parentOrder
        order.setParentOrder(algoOrder);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("next childOrder for {}");
        }

        this.simpleOrderService.sendOrder(order);
    }

    public void storeFill(Fill fill) {

        VWAPOrder algoOrder = (VWAPOrder) fill.getOrder().getParentOrder();
        VWAPOrderStateVO orderState = getAlgoOrderState(algoOrder);
        orderState.storeFill(fill);
    }

    @Override
    public void handleOrderStatus(OrderStatus orderStatus, VWAPOrderStateVO algoOrderState) {

        if (!EnumSet.of(Status.EXECUTED, Status.CANCELED).contains(orderStatus.getStatus())) {
            return;
        }

        VWAPOrder algoOrder = (VWAPOrder) orderStatus.getOrder();
        Map<String, Object> results = getResults(algoOrder, algoOrderState);
        LOGGER.info(algoOrder.getDescription() + results);

        super.handleOrderStatus(orderStatus, algoOrderState);
    }

    Map<String, Object> getResults(VWAPOrder algoOrder, VWAPOrderStateVO algoOrderState) {

        SecurityFamily family = algoOrder.getSecurity().getSecurityFamily();
        int minutes = (int) ChronoUnit.MINUTES.between(algoOrder.getStartTime(), LocalTime.now());

        List<Bar> bars = this.historicalDataService.getHistoricalBars(algoOrder.getSecurity().getId(), //
                new Date(), //
                minutes * 60, //
                TimePeriod.SEC, //
                Duration.MIN_1, //
                MarketDataEventType.TRADES, //
                Collections.emptyMap());

        double benchmarkMarketValue = 0.0;
        long benchmarkVolume = 0;
        for (Bar bar : bars) {
            benchmarkMarketValue += bar.getVwap().doubleValue() * bar.getVol();
            benchmarkVolume += bar.getVol();
        }
        BigDecimal benchmarkPrice = RoundUtil.getBigDecimal(benchmarkMarketValue / benchmarkVolume, family.getScale());

        double marketValue = 0.0;
        long volume = 0;
        for (Fill fill : algoOrderState.getFills()) {
            marketValue += fill.getPrice().doubleValue() * fill.getQuantity();
            volume += fill.getQuantity();
        }
        BigDecimal price = RoundUtil.getBigDecimal(marketValue / volume, family.getScale());

        Map<String, Object> results = new HashMap<>();
        results.put("price", price);
        results.put("benchmarkPrice", benchmarkPrice);
        results.put("duration(mins)", minutes);
        return results;
    }

}
