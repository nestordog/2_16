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
public class StrategyServiceImpl extends AbstractStrategyServiceImpl {

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
        return commonConfig;
    }
    public void setCommonConfig(CommonConfig commonConfig) {
        this.commonConfig = commonConfig;
    }
    public CalendarService getCalendarService() {
        return calendarService;
    }
    public void setCalendarService(CalendarService calendarService) {
        this.calendarService = calendarService;
    }
    public CombinationService getCombinationService() {
        return combinationService;
    }
    public void setCombinationService(CombinationService combinationService) {
        this.combinationService = combinationService;
    }
    public FutureService getFutureService() {
        return futureService;
    }
    public void setFutureService(FutureService futureService) {
        this.futureService = futureService;
    }
    public HistoricalDataService getHistoricalDataService() {
        return historicalDataService;
    }
    public void setHistoricalDataService(HistoricalDataService historicalDataService) {
        this.historicalDataService = historicalDataService;
    }
    public LookupService getLookupService() {
        return lookupService;
    }
    public void setLookupService(LookupService lookupService) {
        this.lookupService = lookupService;
    }
    public MarketDataService getMarketDataService() {
        return marketDataService;
    }
    public void setMarketDataService(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }
    public MeasurementService getMeasurementService() {
        return measurementService;
    }
    public void setMeasurementService(MeasurementService measurementService) {
        this.measurementService = measurementService;
    }
    public OptionService getOptionService() {
        return optionService;
    }
    public void setOptionService(OptionService optionService) {
        this.optionService = optionService;
    }
    public OrderService getOrderService() {
        return orderService;
    }
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }
    public PortfolioService getPortfolioService() {
        return portfolioService;
    }
    public void setPortfolioService(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }
    public PositionService getPositionService() {
        return positionService;
    }
    public void setPositionService(PositionService positionService) {
        this.positionService = positionService;
    }
    public PropertyService getPropertyService() {
        return propertyService;
    }
    public void setPropertyService(PropertyService propertyService) {
        this.propertyService = propertyService;
    }
    public SecurityRetrieverService getSecurityRetrieverService() {
        return securityRetrieverService;
    }
    public void setSecurityRetrieverService(SecurityRetrieverService securityRetrieverService) {
        this.securityRetrieverService = securityRetrieverService;
    }
    public SubscriptionService getSubscriptionService() {
        return subscriptionService;
    }
    public void setSubscriptionService(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

}
