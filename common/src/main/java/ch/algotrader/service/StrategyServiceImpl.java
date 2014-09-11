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

import ch.algotrader.config.CommonConfig;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class StrategyServiceImpl extends AbstractStrategyServiceImpl {

    private CommonConfig commonConfig;
    private CalendarService calendarService;
    private CombinationService combinationService;
    private FutureService futureService;
    private HistoricalDataService historicalDataService;
    private LookupService lookupService;
    private MarketDataService marketDataService;
    private MeasurementService measurementService;
    private OptionService optionService;
    private OrderService orderService;
    private PortfolioService portfolioService;
    private PositionService positionService;
    private PropertyService propertyService;
    private SecurityRetrieverService securityRetrieverService;
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
    public SecurityRetrieverService getSecurityRetrieverService() {
        return this.securityRetrieverService;
    }
    public void setSecurityRetrieverService(SecurityRetrieverService securityRetrieverService) {
        this.securityRetrieverService = securityRetrieverService;
    }
    public SubscriptionService getSubscriptionService() {
        return this.subscriptionService;
    }
    public void setSubscriptionService(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

}
