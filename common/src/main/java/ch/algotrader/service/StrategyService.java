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
package ch.algotrader.service;

import java.util.Collections;
import java.util.Map;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.marketData.BarVO;
import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderCompletion;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.event.listener.BarEventListener;
import ch.algotrader.event.listener.ClosePositionEventListener;
import ch.algotrader.event.listener.ExpirePositionEventListener;
import ch.algotrader.event.listener.FillEventListener;
import ch.algotrader.event.listener.LifecycleEventListener;
import ch.algotrader.event.listener.OpenPositionEventListener;
import ch.algotrader.event.listener.OrderCompletionEventListener;
import ch.algotrader.event.listener.OrderEventListener;
import ch.algotrader.event.listener.OrderStatusEventListener;
import ch.algotrader.event.listener.SessionEventListener;
import ch.algotrader.event.listener.TickEventListener;
import ch.algotrader.event.listener.TransactionEventListener;
import ch.algotrader.simulation.SimulationResultsProducer;
import ch.algotrader.vo.ClosePositionVO;
import ch.algotrader.vo.ExpirePositionVO;
import ch.algotrader.vo.LifecycleEventVO;
import ch.algotrader.vo.OpenPositionVO;
import ch.algotrader.vo.SessionEventVO;

/**
 * Base strategy that implements all event listener interfaces. Events are
 * propagated to the listener methods. Alternatively strategies can implement
 * listener interfaces selectively without needing to extend this class.
 * <p>
 * The framework is made aware of event listener via Spring. As a consequence,
 * all implementors of these interfaces should be managed by the Spring
 * container.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
//@formatter:off
public class StrategyService implements
        LifecycleEventListener,
        BarEventListener, TickEventListener,
        OrderEventListener, OrderStatusEventListener, OrderCompletionEventListener, FillEventListener, TransactionEventListener,
        OpenPositionEventListener, ClosePositionEventListener, ExpirePositionEventListener,
        SessionEventListener,
        SimulationResultsProducer {
//@formatter:on

    private CommonConfig commonConfig;
    private CalendarService calendarService;
    private CombinationService combinationService;
    private FutureService futureService;
    private HistoricalDataService historicalDataService;
    private LookupService lookupService;
    private LocalLookupService localLookupService;
    private MarketDataService marketDataService;
    private MeasurementService measurementService;
    private OptionService optionService;
    private OrderService orderService;
    private PortfolioService portfolioService;
    private PositionService positionService;
    private PropertyService propertyService;
    private ReferenceDataService referenceDataService;
    private SubscriptionService subscriptionService;

    public CommonConfig getCommonConfig() {
        return this.commonConfig;
    }

    public void setCommonConfig(CommonConfig commonConfig) {
        this.commonConfig = commonConfig;
    }

    public CalendarService getCalendarService() {
        return this.calendarService;
    }

    public void setCalendarService(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    public CombinationService getCombinationService() {
        return this.combinationService;
    }

    public void setCombinationService(CombinationService combinationService) {
        this.combinationService = combinationService;
    }

    public FutureService getFutureService() {
        return this.futureService;
    }

    public void setFutureService(FutureService futureService) {
        this.futureService = futureService;
    }

    public HistoricalDataService getHistoricalDataService() {
        return this.historicalDataService;
    }

    public void setHistoricalDataService(HistoricalDataService historicalDataService) {
        this.historicalDataService = historicalDataService;
    }

    public LookupService getLookupService() {
        return this.lookupService;
    }

    public void setLookupService(LookupService lookupService) {
        this.lookupService = lookupService;
    }

    public LocalLookupService getLocalLookupService() {
        return this.localLookupService;
    }

    public void setLocalLookupService(LocalLookupService localLookupService) {
        this.localLookupService = localLookupService;
    }

    public MarketDataService getMarketDataService() {
        return this.marketDataService;
    }

    public void setMarketDataService(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    public MeasurementService getMeasurementService() {
        return this.measurementService;
    }

    public void setMeasurementService(MeasurementService measurementService) {
        this.measurementService = measurementService;
    }

    public OptionService getOptionService() {
        return this.optionService;
    }

    public void setOptionService(OptionService optionService) {
        this.optionService = optionService;
    }

    public OrderService getOrderService() {
        return this.orderService;
    }

    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    public PortfolioService getPortfolioService() {
        return this.portfolioService;
    }

    public void setPortfolioService(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    public PositionService getPositionService() {
        return this.positionService;
    }

    public void setPositionService(PositionService positionService) {
        this.positionService = positionService;
    }

    public PropertyService getPropertyService() {
        return this.propertyService;
    }

    public void setPropertyService(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    public ReferenceDataService getReferenceDataService() {
        return this.referenceDataService;
    }

    public void setReferenceDataService(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    public SubscriptionService getSubscriptionService() {
        return this.subscriptionService;
    }

    public void setSubscriptionService(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Override
    public Map<String, Object> getSimulationResults() {

        return Collections.emptyMap();
    }

    @Override
    public void onChange(final LifecycleEventVO event) {
        switch (event.getPhase()) {
            case INIT:
                onInit(event);
                break;
            case PREFEED:
                onPrefeed(event);
                break;
            case START:
                onStart(event);
                break;
            case EXIT:
                onExit(event);
                break;
            default:
                break;
        }
    }

    protected void onInit(final LifecycleEventVO event) {
    }

    protected void onPrefeed(final LifecycleEventVO event) {
    }

    protected void onStart(final LifecycleEventVO event) {
    }

    protected void onExit(final LifecycleEventVO event) {
    }

    @Override
    public void onTick(final TickVO bar) {
    }

    @Override
    public void onBar(final BarVO bar) {
    }

    @Override
    public void onOrder(final Order order) {
    }

    @Override
    public void onOrderStatus(final OrderStatus orderStatus) {
    }

    @Override
    public void onOrderCompletion(final OrderCompletion orderCompletion) {
    }

    @Override
    public void onFill(final Fill fill) {
    }

    @Override
    public void onTransaction(final Transaction transaction) {
    }

    @Override
    public void onOpenPosition(final OpenPositionVO openPosition) {
    }

    @Override
    public void onClosePosition(final ClosePositionVO closePosition) {
    }

    @Override
    public void onExpirePosition(final ExpirePositionVO expirePosition) {
    }

    @Override
    public void onChange(final SessionEventVO event) {
    }

}
